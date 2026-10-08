#!/usr/bin/env bash
# Pushes generated screenshot PNGs to the orphan "screenshots" branch and
# writes a markdown comment body that embeds them via raw.githubusercontent.com.
#
# Required env:
#   SRC_DIR       directory that contains the generated PNGs
#   PR_NUMBER     pull request number
#   RUN_ID        workflow run id (makes the destination unique per run)
#   HEAD_SHA      head commit sha of the pull request
#   REPOSITORY    owner/repo
#   GITHUB_TOKEN  token with contents: write
#   RUN_URL       URL of the workflow run
#   BODY_FILE     path to write the comment body to
set -euo pipefail

BRANCH=screenshots
SHORT_SHA=${HEAD_SHA:0:7}
DEST="pr-${PR_NUMBER}/${RUN_ID}"
WORK_DIR=$(mktemp -d)

mapfile -t PNGS < <(cd "$SRC_DIR" && find . -type f -name '*.png' | sed 's|^\./||' | LC_ALL=C sort)
if [ "${#PNGS[@]}" -eq 0 ]; then
  echo "No PNGs found in $SRC_DIR"
  exit 0
fi

# --- Push to the screenshots branch in a separate, sparse clone -------------
git -C "$WORK_DIR" init -q
git -C "$WORK_DIR" remote add origin "https://x-access-token:${GITHUB_TOKEN}@github.com/${REPOSITORY}.git"
git -C "$WORK_DIR" config user.name 'github-actions[bot]'
git -C "$WORK_DIR" config user.email '41898283+github-actions[bot]@users.noreply.github.com'
git -C "$WORK_DIR" sparse-checkout set --no-cone "/${DEST}/"

pushed=false
for attempt in 1 2 3 4 5; do
  if git -C "$WORK_DIR" fetch -q --depth=1 --filter=blob:none origin "$BRANCH" 2>/dev/null; then
    git -C "$WORK_DIR" checkout -q -B "$BRANCH" FETCH_HEAD
  else
    echo "Branch $BRANCH does not exist yet; creating an orphan branch"
    git -C "$WORK_DIR" checkout -q --orphan "orphan-${attempt}"
  fi
  mkdir -p "$WORK_DIR/$DEST"
  (cd "$SRC_DIR" && cp --parents "${PNGS[@]}" "$WORK_DIR/$DEST/")
  git -C "$WORK_DIR" add --sparse "$DEST"
  git -C "$WORK_DIR" commit -q -m "Add screenshots for PR #${PR_NUMBER} (${SHORT_SHA}, run ${RUN_ID})"
  if git -C "$WORK_DIR" push -q origin "HEAD:refs/heads/$BRANCH"; then
    pushed=true
    break
  fi
  echo "Push rejected (attempt $attempt); retrying on top of the latest $BRANCH"
  sleep $((attempt * 2))
done
if [ "$pushed" != true ]; then
  echo "Failed to push screenshots after retries" >&2
  exit 1
fi

# --- Build the comment body ---------------------------------------------------
RAW_BASE="https://raw.githubusercontent.com/${REPOSITORY}/${BRANCH}/${DEST}"
img() { printf '<img src="%s/%s" width="360">' "$RAW_BASE" "${1// /%20}"; }

# Group Light/Dark variants of the same preview: strip the variant token from the file name.
declare -A LIGHT DARK
KEYS=()
for png in "${PNGS[@]}"; do
  name=$(basename "$png")
  key="$(dirname "$png")/$(sed -E 's/_(Light|Dark)_/_/; s/^(Light|Dark)_//; s/_(Light|Dark)\.png$/.png/' <<<"$name")"
  case "$name" in
    *Light*) variant=light ;;
    *Dark*) variant=dark ;;
    *) variant=other ;;
  esac
  if [ "$variant" = other ] || { [ "$variant" = light ] && [ -n "${LIGHT[$key]:-}" ]; } || { [ "$variant" = dark ] && [ -n "${DARK[$key]:-}" ]; }; then
    key="$png"
    variant=light
  fi
  if [ -z "${LIGHT[$key]:-}" ] && [ -z "${DARK[$key]:-}" ]; then KEYS+=("$key"); fi
  if [ "$variant" = dark ]; then DARK[$key]=$png; else LIGHT[$key]=$png; fi
done

{
  echo '<!-- screenshot-comment -->'
  echo '## 📸 Screenshots'
  echo
  echo "Head: \`${SHORT_SHA}\` · [Workflow run](${RUN_URL}) · The zip artifact \`screenshots-feature_article_list\` on that run also contains the HTML report."
  echo
  for key in "${KEYS[@]}"; do
    light=${LIGHT[$key]:-}
    dark=${DARK[$key]:-}
    if [ -n "$light" ] && [ -n "$dark" ]; then
      echo "### \`${key#./}\`"
      echo
      echo "| Light: \`$(basename "$light")\` | Dark: \`$(basename "$dark")\` |"
      echo '| --- | --- |'
      echo "| $(img "$light") | $(img "$dark") |"
    else
      file=${light:-$dark}
      echo "### \`${file#./}\`"
      echo
      img "$file"
      echo
    fi
    echo
  done
} > "$BODY_FILE"
