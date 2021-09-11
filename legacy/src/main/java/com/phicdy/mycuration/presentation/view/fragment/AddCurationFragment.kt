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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.presenter.AddCurationPresenter
import com.phicdy.mycuration.presentation.view.AddCurationView
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class AddCurationFragment : Fragment(), AddCurationView {

    companion object {
        const val EDIT_CURATION_ID = "editCurationId"
    }

    @Inject
    lateinit var presenter: AddCurationPresenter

    private lateinit var curationWordRecyclerView: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var etName: TextInputEditText
    private lateinit var curationWordListAdapter: CurationWordListAdapter
    private lateinit var progressDialog: MyProgressDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.create()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            presenter.resume()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_curation_word_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.activityCreated()
    }

    override fun initView() {
        activity?.let {
            etInput = it.findViewById<TextInputEditText>(R.id.et_curation_word)
            etInput.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && event?.action != KeyEvent.ACTION_UP) {
                    presenter.onAddWordButtonClicked(v.text.toString())
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
            etName = it.findViewById(R.id.et_curation_name) as TextInputEditText
            curationWordRecyclerView = it.findViewById(R.id.rv_curation_word) as RecyclerView
        }
    }

    override fun refreshList(addedWords: ArrayList<String>) {
        curationWordListAdapter = CurationWordListAdapter(addedWords)
        curationWordRecyclerView.adapter = curationWordListAdapter
        curationWordRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        curationWordListAdapter.notifyDataSetChanged()
    }

    override fun editCurationId(): Int {
        return activity?.intent?.getIntExtra(EDIT_CURATION_ID, AddCurationPresenter.NOT_EDIT_CURATION_ID)
                ?: AddCurationPresenter.NOT_EDIT_CURATION_ID
    }

    override fun curationName(): String {
        return etName.text.toString()
    }

    override fun setCurationName(name: String) {
        etName.setText(name)
    }

    override fun resetInputWord() {
        etInput.setText("")
    }

    override fun handleEmptyCurationNameError() {
        presenter.handleInsertResultMessage(false, getString(R.string.empty_curation_name))
    }

    override fun handleEmptyWordError() {
        presenter.handleInsertResultMessage(false, getString(R.string.empty_word_list))
    }

    override fun handleSameNameCurationError() {
        TrackerHelper.sendButtonEvent(getString(R.string.add_same_curation_name))
        presenter.handleInsertResultMessage(false, getString(R.string.duplicate_curation_name))
    }

    override fun handleAddSuccess() {
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_curation))
        presenter.handleInsertResultMessage(true, "")
    }

    override fun handleEditSuccess() {
        TrackerHelper.sendButtonEvent(getString(R.string.update_curation))
        presenter.handleInsertResultMessage(true, "")
    }

    override fun showSuccessToast() {
        ToastHelper.showToast(activity, getString(R.string.curation_added_success), Toast.LENGTH_SHORT)
    }

    override fun showErrorToast() {
        ToastHelper.showToast(activity, getString(R.string.curation_added_error), Toast.LENGTH_SHORT)
    }

    override fun showWordEmptyErrorToast() {
        ToastHelper.showToast(activity, getString(R.string.empty_word), Toast.LENGTH_SHORT)
    }

    override fun showDupulicatedWordToast() {
        Toast.makeText(activity, getString(R.string.duplicate_word), Toast.LENGTH_SHORT).show()
    }

    override fun showToast(text: String) {
        ToastHelper.showToast(activity, text, Toast.LENGTH_SHORT)
    }

    override fun showProgressDialog() {
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation))
        activity?.supportFragmentManager?.let {
            progressDialog.show(it, null)
        }
    }

    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    override fun finish() {
        activity?.finish()
    }

    private inner class CurationWordListAdapter(private val words: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                    presenter.onDeleteButtonClicked(position)
                }
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal val tvWord = itemView.findViewById(R.id.tv_word) as TextView
            internal val deleteIcon = itemView.findViewById<ImageView>(R.id.iv_delete)
        }
    }

    fun onAddMenuClicked() {
        showProgressDialog()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)
            presenter.onAddMenuClicked()
        }
    }

    @Module
    @InstallIn(FragmentComponent::class)
    object AddCurationModule {
        @FragmentScoped
        @Provides
        fun provideAddCurationView(fragment: Fragment): AddCurationView = fragment as AddCurationView
    }
}
