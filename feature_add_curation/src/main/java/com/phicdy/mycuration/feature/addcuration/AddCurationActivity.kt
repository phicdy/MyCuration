package com.phicdy.mycuration.feature.addcuration

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.resource.MyCurationTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AddCurationActivity : ComponentActivity() {

    @Inject
    lateinit var initializeAddCurationActionCreator: InitializeAddCurationActionCreator

    private val addCurationStateStore: AddCurationStateStore by viewModels()

    @Inject
    lateinit var addCurationWordActionCreator: AddCurationWordActionCreator

    @Inject
    lateinit var deleteCurationWordActionCreator: DeleteCurationWordActionCreator

    @Inject
    lateinit var updateTextFieldActionCreator: UpdateTextFieldActionCreator

    @Inject
    lateinit var storeCurationActionCreator: StoreCurationActionCreator

    private val storeCurationStateStore: StoreCurationStateStore by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        val id = intent.getIntExtra(EDIT_CURATION_ID, -1)
        val actualDarkThemeUserSetting = PreferenceHelper.theme == PreferenceHelper.THEME_DARK
        setContent {
            MyCurationTheme(darkTheme = actualDarkThemeUserSetting) {
                val view = LocalView.current
                SideEffect {
                    val windowInsetsController = WindowInsetsControllerCompat(window, view)
                    windowInsetsController.isAppearanceLightStatusBars = !actualDarkThemeUserSetting
                }
                val state = addCurationStateStore.state.observeAsState().value
                val storeState = storeCurationStateStore.state.observeAsState().value
                AddCurationFragmentScreen(
                    onBackIconClicked = { finish() },
                    onCheckIconClicked = { title, words ->
                        lifecycleScope.launch {
                            storeCurationActionCreator.run(title, words, id)
                        }
                    },
                    onCloseClicked = { index ->
                        lifecycleScope.launch {
                            deleteCurationWordActionCreator.run(index)
                        }
                    },
                    onWordSent = { word ->
                        lifecycleScope.launch {
                            addCurationWordActionCreator.run(word)
                        }
                    },
                    onTitleFieldChanged = {
                        lifecycleScope.launch {
                            updateTextFieldActionCreator.run(AddCurationTextFieldType.TITLE, it)
                        }
                    },
                    onWordFieldChanged = {
                        lifecycleScope.launch {
                            updateTextFieldActionCreator.run(AddCurationTextFieldType.WORD, it)
                        }
                    },
                    isEdit = id != -1,
                    state = state,
                    storeState = storeState
                )
            }
        }

        lifecycleScope.launchWhenStarted {
            addCurationStateStore.event.collect { event ->
                when (event) {
                    AddCurationEvent.Duplicated -> showDupulicatedWordToast()
                    AddCurationEvent.Empty -> showWordEmptyErrorToast()
                }
            }
        }
        storeCurationStateStore.state.observe(this) { state ->
            when (state) {
                StoreCurationState.EmptyNameError -> handleEmptyCurationNameError()
                StoreCurationState.EmptyWordError -> handleEmptyWordError()
                StoreCurationState.Loading -> { /* Handled by Composable */
                }

                StoreCurationState.SameNameExitError -> handleSameNameCurationError()
                StoreCurationState.SucceedToAdd -> handleAddSuccess()
                StoreCurationState.SucceedToEdit -> handleEditSuccess()
            }
        }
        addCurationStateStore.register()
        lifecycleScope.launch {
            initializeAddCurationActionCreator.run(id)
        }
    }

    private fun handleEmptyCurationNameError() {
        handleInsertResultMessage(false, getString(R.string.empty_curation_name))
    }

    private fun handleEmptyWordError() {
        handleInsertResultMessage(false, getString(R.string.empty_word_list))
    }

    private fun handleSameNameCurationError() {
        TrackerHelper.sendButtonEvent(getString(R.string.add_same_curation_name))
        handleInsertResultMessage(false, getString(R.string.duplicate_curation_name))
    }

    private fun handleAddSuccess() {
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_curation))
        handleInsertResultMessage(true, "")
    }

    private fun handleEditSuccess() {
        TrackerHelper.sendButtonEvent(getString(R.string.update_curation))
        handleInsertResultMessage(true, "")
    }

    private fun handleInsertResultMessage(result: Boolean, errorMessage: String) {
        if (result) {
            showSuccessToast()
            finish()
        } else {
            showToast(errorMessage)
            showErrorToast()
        }
    }

    private fun showSuccessToast() {
        ToastHelper.showToast(this, getString(R.string.curation_added_success), Toast.LENGTH_SHORT)
    }

    private fun showErrorToast() {
        ToastHelper.showToast(this, getString(R.string.curation_added_error), Toast.LENGTH_SHORT)
    }

    private fun showWordEmptyErrorToast() {
        ToastHelper.showToast(this, getString(R.string.empty_word), Toast.LENGTH_SHORT)
    }

    private fun showDupulicatedWordToast() {
        Toast.makeText(this, getString(R.string.duplicate_word), Toast.LENGTH_SHORT).show()
    }

    private fun showToast(text: String) {
        ToastHelper.showToast(this, text, Toast.LENGTH_SHORT)
    }

    companion object {
        const val EDIT_CURATION_ID = "editCurationId"
    }
}

@Composable
fun AddCurationFragmentScreen(
    onBackIconClicked: () -> Unit = {},
    onCheckIconClicked: (String, List<String>) -> Unit = { _, _ -> },
    onCloseClicked: (Int) -> Unit = {},
    onWordSent: (String) -> Unit = {},
    onTitleFieldChanged: (String) -> Unit = {},
    onWordFieldChanged: (String) -> Unit = {},
    isEdit: Boolean = false,
    state: AddCurationState?,
    storeState: StoreCurationState?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = if (isEdit) R.string.title_activity_edit_curation else R.string.title_activity_add_curation)) },
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = {
                    IconButton(
                        onClick = onBackIconClicked
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (state is AddCurationState.Loaded) {
                                onCheckIconClicked(state.titleField, state.words)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val localFocusManager = LocalFocusManager.current
            OutlinedTextField(
                value = if (state is AddCurationState.Loaded) state.titleField else "",
                label = { Text(stringResource(R.string.curation_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                onValueChange = onTitleFieldChanged,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.onPrimary,
                    focusedLabelColor = MaterialTheme.colors.onPrimary,
                    cursorColor = MaterialTheme.colors.onPrimary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                }),
            )
            OutlinedTextField(
                value = if (state is AddCurationState.Loaded) state.wordField else "",
                label = { Text(stringResource(R.string.word_setting)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                onValueChange = onWordFieldChanged,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.onPrimary,
                    focusedLabelColor = MaterialTheme.colors.onPrimary,
                    cursorColor = MaterialTheme.colors.onPrimary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onWordSent(if (state is AddCurationState.Loaded) state.wordField else "")
                })
            )
            if (state is AddCurationState.Loaded && state.words.isNotEmpty()) {
                LazyColumn(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)) {
                    itemsIndexed(state.words) { index, word ->
                        WordRow(word) {
                            onCloseClicked(index)
                        }
                    }
                }
            }
        }
        if (storeState == StoreCurationState.Loading) {
            LoadingIndicatorDialog()
        }
    }
}

@Composable
fun WordRow(
    word: String,
    onCloseClicked: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = word,
            modifier = Modifier
                .weight(1f),
            fontSize = 18.sp
        )
        IconButton(onClick = onCloseClicked) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "",
            )
        }
    }
}

@Composable
private fun LoadingIndicatorDialog() {
    Dialog(
        onDismissRequest = { /* Modal, not dismissable by click outside or back press */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                Text(text = stringResource(R.string.adding_curation))
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AddCurationFragmentScreenLoadingPreview() {
    MyCurationTheme {
        AddCurationFragmentScreen(state = AddCurationState.Loading, storeState = null)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AddCurationFragmentScreenLoadedPreview() {
    MyCurationTheme {
        AddCurationFragmentScreen(
            state = AddCurationState.Loaded(
                titleField = "Curation Title",
                wordField = "Word1, Word2",
                words = listOf("Word1", "Word2")
            ),
            storeState = StoreCurationState.Loading
        )
    }
}
