## MyCuration  ![Github Actions](https://github.com/phicdy/MyCuration/actions/workflows/android.yml/badge.svg) [![codecov](https://codecov.io/gh/phicdy/MyCuration/branch/master/graph/badge.svg)](https://codecov.io/gh/phicdy/MyCuration)

<a href="https://play.google.com/store/apps/details?id=com.phicdy.mycuration&hl=ja"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80px"/></a> 

MyCuration is RSS(Feed) Reader that has curation and filter feature.

## Features

### RSS Reader

* Subscribe RSS(RSS1.0/2.0/ATOM)
* Manage read/unread status
* Manual/Schedule update
* Sort articles by the published date
* Search articles by keyword

### Curation

Collect articles from subscribed RSS by user settings

#### Settings

* Keywords in the article title

### Filter

Set status to read automatically by user settings

#### Settings

* Keyword in the article title
* URL in the article URL
* Target RSS
* Enable/Disable

## Mutli-Module

<img src="https://raw.githubusercontent.com/phicdy/MyCuration/develop/modules.png" alt="modules" title="modules">


## Credits

* [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines)
* [Dagger Hilt](https://dagger.dev/hilt/)
* [SQLDelight](https://github.com/cashapp/sqldelight)
* [RecyclerView](https://dl.google.com/dl/android/maven2/index.html)
* [Retrofit](https://github.com/square/retrofit)
* [Glide](https://github.com/bumptech/glide)
* [Firebase Analytics](https://firebase.google.com/docs/analytics/)
* [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics/)
* [Jsoup](https://github.com/jhy/jsoup/)
* [Calligraphy](https://github.com/chrisjenx/Calligraphy)
* [MaterialShowcaseView](https://github.com/deano2390/MaterialShowcaseView)
* [stetho](https://github.com/facebook/stetho)
* [Timber](https://github.com/JakeWharton/timber)
* [Mockito-Kotlin](https://github.com/nhaarman/mockito-kotlin)
* [AssertJ](https://github.com/joel-costigliola/assertj-core)

## Devlopment Environment

* Put your `google-services.json` for Google Analytics
* Set up your settings in ~/.gradle/gradle.properties or edit gradle.properties in this project
  * Set admob ID value like `ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy` to `ADMOB_ID_MYCURATION`
  * Set release keystore settings to `RELEASE_KEY_ALIAS`, `RELEASE_STORE_FILE_PATH`, `RELEASE_KEY_PASSWORD` and `RELEASE_STORE_PASSWORD`
* If you want to copy debug build to somewhere, set the destination in ~/.gradle/gradle.properties or edit gradle.properties in this project

```
COPY_BUILD_DESTINATION=/your/copy/destination
```

## License

The MIT License (MIT)

Copyright (c) 2018 phicdy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
