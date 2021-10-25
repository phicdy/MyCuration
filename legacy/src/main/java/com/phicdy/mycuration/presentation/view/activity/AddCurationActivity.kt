package com.phicdy.mycuration.presentation.view.activity

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.phicdy.mycuration.feature.addcuration.AddCurationEvent
import com.phicdy.mycuration.feature.addcuration.AddCurationState
import com.phicdy.mycuration.feature.addcuration.AddCurationStateStore
import com.phicdy.mycuration.feature.addcuration.AddCurationWordActionCreator
import com.phicdy.mycuration.feature.addcuration.DeleteCurationWordActionCreator
import com.phicdy.mycuration.feature.addcuration.InitializeAddCurationActionCreator
import com.phicdy.mycuration.feature.addcuration.StoreCurationActionCreator
import com.phicdy.mycuration.feature.addcuration.StoreCurationState
import com.phicdy.mycuration.feature.addcuration.StoreCurationStateStore
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.view.fragment.MyProgressDialogFragment
import com.phicdy.mycuration.resource.MyCurationTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AddCurationActivity : AppCompatActivity() {

    @Inject
    lateinit var initializeAddCurationActionCreator: InitializeAddCurationActionCreator

    private val addCurationStateStore: AddCurationStateStore by viewModels()

    @Inject
    lateinit var addCurationWordActionCreator: AddCurationWordActionCreator

    @Inject
    lateinit var deleteCurationWordActionCreator: DeleteCurationWordActionCreator

    @Inject
    lateinit var storeCurationActionCreator: StoreCurationActionCreator

    private val storeCurationStateStore: StoreCurationStateStore by viewModels()

    private lateinit var curationWordRecyclerView: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var etName: TextInputEditText
    private lateinit var curationWordListAdapter: CurationWordListAdapter
    private lateinit var progressDialog: MyProgressDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_curation)
        initView()
        initToolbar()

        addCurationStateStore.state.observe(this, { state ->
            when (state) {
                AddCurationState.Loading -> TODO()
                is AddCurationState.Loaded -> {
                    setCurationName(state.name)
                    refreshList(state.words)
                }
            }
        })
        lifecycleScope.launchWhenStarted {
            addCurationStateStore.event.collect { event ->
                when (event) {
                    AddCurationEvent.Duplicated -> showDupulicatedWordToast()
                    AddCurationEvent.Empty -> showWordEmptyErrorToast()
                    AddCurationEvent.ResetWordInput -> resetInputWord()
                }
            }
        }
        storeCurationStateStore.state.observe(this, { state ->
            when (state) {
                StoreCurationState.EmptyNameError -> handleEmptyCurationNameError()
                StoreCurationState.EmptyWordError -> handleEmptyWordError()
                StoreCurationState.Loading -> showProgressDialog()
                StoreCurationState.SameNameExitError -> handleSameNameCurationError()
                StoreCurationState.SucceedToAdd -> handleAddSuccess()
                StoreCurationState.SucceedToEdit -> handleEditSuccess()
            }
        })
        val id = intent.getIntExtra(EDIT_CURATION_ID, -1)
        lifecycleScope.launch {
            initializeAddCurationActionCreator.run(id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_add_curation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_curation -> onAddMenuClicked()
            // For arrow button on toolbar
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_add_curation)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            val id = intent.getIntExtra(EDIT_CURATION_ID, -1)
            if (id == -1) {
                actionBar.title = getString(R.string.title_activity_add_curation)
            } else {
                actionBar.title = getString(R.string.title_activity_edit_curation)
            }
        }
    }

    private fun initView() {
        etInput = findViewById<TextInputEditText>(R.id.et_curation_word)
        etInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && event?.action != KeyEvent.ACTION_UP) {
                lifecycleScope.launchWhenStarted {
                    addCurationWordActionCreator.run(v.text.toString())
                }
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
        etName = findViewById(R.id.et_curation_name)
        curationWordRecyclerView = findViewById(R.id.rv_curation_word)
    }

    private fun refreshList(addedWords: List<String>) {
        curationWordListAdapter = CurationWordListAdapter(addedWords)
        curationWordRecyclerView.adapter = curationWordListAdapter
        curationWordRecyclerView.layoutManager = LinearLayoutManager(this)
        curationWordListAdapter.notifyDataSetChanged()
    }

    private fun setCurationName(name: String) {
        etName.setText(name)
    }

    private fun resetInputWord() {
        etInput.setText("")
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
            dismissProgressDialog()
            finish()
        } else {
            showToast(errorMessage)
            showErrorToast()
            dismissProgressDialog()
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

    private fun showProgressDialog() {
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation))
        progressDialog.show(supportFragmentManager, null)
    }

    private fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    private inner class CurationWordListAdapter(private val words: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.curation_word_list, parent, false)
            return ViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return words.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val word = words[position]
            if (holder is ViewHolder) {
                holder.tvWord.text = word
                holder.deleteIcon.setOnClickListener {
                    lifecycleScope.launchWhenStarted {
                        deleteCurationWordActionCreator.run(position)
                    }
                }
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWord = itemView.findViewById(R.id.tv_word) as TextView
            val deleteIcon: ImageView = itemView.findViewById(R.id.iv_delete)
        }
    }

    private fun onAddMenuClicked() {
        val current = addCurationStateStore.state.value
        val name = etName.text.toString()
        val id = intent?.getIntExtra(EDIT_CURATION_ID, -1) ?: -1
        when (current) {
            is AddCurationState.Loaded -> {
                lifecycleScope.launch {
                    storeCurationActionCreator.run(name, current.words, id)
                }
            }
            else -> {
            }
        }
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
    words: List<String> = emptyList()
) {
    val title: MutableState<String> = rememberSaveable {
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_activity_add_curation)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackIconClicked
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onCheckIconClicked(title.value, words) }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) {
        Column {
            OutlinedTextField(
                value = title.value,
                label = { Text(stringResource(R.string.curation_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                onValueChange = onTitleFieldChanged,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Black900,
                    focusedLabelColor = Black900
                )
            )
            val currentWord: MutableState<String> = rememberSaveable {
                mutableStateOf("")
            }
            OutlinedTextField(
                value = currentWord.value,
                label = { Text(stringResource(R.string.word_setting)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                onValueChange = onWordFieldChanged,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Black900,
                    focusedLabelColor = Black900
                ),
                keyboardActions = KeyboardActions(onSend = {
                    onWordSent(currentWord.value)
                })
            )
            LazyColumn(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)) {
                itemsIndexed(words) { index, word ->
                    WordRow(word) {
                        onCloseClicked(index)
                    }
                }
            }
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

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun AddCurationLightPreview() {
    MyCurationTheme {
        AddCurationFragmentScreen(words = listOf("aaa", "bbb"))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AddCurationDarkPreview() {
    MyCurationTheme {
        AddCurationFragmentScreen(words = listOf("aaa", "bbb"))
    }
}
