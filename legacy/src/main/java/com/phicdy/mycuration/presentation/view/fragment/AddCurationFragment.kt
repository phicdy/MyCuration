package com.phicdy.mycuration.presentation.view.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.phicdy.mycuration.feature.addcuration.AddCurationErrorEvent
import com.phicdy.mycuration.feature.addcuration.AddCurationState
import com.phicdy.mycuration.feature.addcuration.AddCurationStateStore
import com.phicdy.mycuration.feature.addcuration.AddCurationWordActionCreator
import com.phicdy.mycuration.feature.addcuration.DeleteCurationWordActionCreator
import com.phicdy.mycuration.feature.addcuration.InitializeAddCurationActionCreator
import com.phicdy.mycuration.feature.addcuration.StoreCurationActionCreator
import com.phicdy.mycuration.feature.addcuration.StoreCurationState
import com.phicdy.mycuration.feature.addcuration.StoreCurationStateStore
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddCurationFragment : Fragment() {

    companion object {
        const val EDIT_CURATION_ID = "editCurationId"
    }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_curation_word_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addCurationStateStore.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                AddCurationState.Loading -> TODO()
                is AddCurationState.Loaded -> {
                    initView()
                    setCurationName(state.name)
                    refreshList(state.words)
                    resetInputWord()
                }
                is AddCurationState.Deleted -> {
                    refreshList(state.words)
                }
            }
        })
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            addCurationStateStore.event.collect { event ->
                when (event) {
                    AddCurationErrorEvent.Duplicated -> showDupulicatedWordToast()
                    AddCurationErrorEvent.Empty -> showWordEmptyErrorToast()
                }
            }
        }
        storeCurationStateStore.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                StoreCurationState.EmptyNameError -> handleEmptyCurationNameError()
                StoreCurationState.EmptyWordError -> handleEmptyWordError()
                StoreCurationState.Loading -> showProgressDialog()
                StoreCurationState.SameNameExitError -> handleSameNameCurationError()
                StoreCurationState.SucceedToAdd -> handleAddSuccess()
                StoreCurationState.SucceedToEdit -> handleEditSuccess()
            }
        })
        val id = activity?.intent?.getIntExtra(EDIT_CURATION_ID, -1) ?: -1
        viewLifecycleOwner.lifecycleScope.launch {
            initializeAddCurationActionCreator.run(id)
        }
    }

    private fun initView() {
        activity?.let {
            etInput = it.findViewById<TextInputEditText>(R.id.et_curation_word)
            etInput.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && event?.action != KeyEvent.ACTION_UP) {
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        addCurationWordActionCreator.run(v.text.toString())
                    }
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
            etName = it.findViewById(R.id.et_curation_name) as TextInputEditText
            curationWordRecyclerView = it.findViewById(R.id.rv_curation_word) as RecyclerView
        }
    }

    private fun refreshList(addedWords: List<String>) {
        curationWordListAdapter = CurationWordListAdapter(addedWords)
        curationWordRecyclerView.adapter = curationWordListAdapter
        curationWordRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
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
        ToastHelper.showToast(activity, getString(R.string.curation_added_success), Toast.LENGTH_SHORT)
    }

    private fun showErrorToast() {
        ToastHelper.showToast(activity, getString(R.string.curation_added_error), Toast.LENGTH_SHORT)
    }

    private fun showWordEmptyErrorToast() {
        ToastHelper.showToast(activity, getString(R.string.empty_word), Toast.LENGTH_SHORT)
    }

    private fun showDupulicatedWordToast() {
        Toast.makeText(activity, getString(R.string.duplicate_word), Toast.LENGTH_SHORT).show()
    }

    private fun showToast(text: String) {
        ToastHelper.showToast(activity, text, Toast.LENGTH_SHORT)
    }

    private fun showProgressDialog() {
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation))
        activity?.supportFragmentManager?.let {
            progressDialog.show(it, null)
        }
    }

    private fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    private fun finish() {
        activity?.finish()
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
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        deleteCurationWordActionCreator.run(position)
                    }
                }
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal val tvWord = itemView.findViewById(R.id.tv_word) as TextView
            internal val deleteIcon = itemView.findViewById<ImageView>(R.id.iv_delete)
        }
    }

    fun onAddMenuClicked() {
        val current = addCurationStateStore.state.value
        val id = activity?.intent?.getIntExtra(EDIT_CURATION_ID, -1) ?: -1
        when (current) {
            is AddCurationState.Deleted -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(5000)
                    storeCurationActionCreator.run(current.name, current.words, id)
                }
            }
            is AddCurationState.Loaded -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(5000)
                    storeCurationActionCreator.run(current.name, current.words, id)
                }
            }
            else -> {
            }
        }
    }
}
