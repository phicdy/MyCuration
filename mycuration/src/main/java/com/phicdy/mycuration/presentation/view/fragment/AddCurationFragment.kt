package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.presenter.AddCurationPresenter
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import com.phicdy.mycuration.presentation.view.AddCurationView
import java.lang.ref.WeakReference

import java.util.ArrayList

class AddCurationFragment : Fragment(), AddCurationView {

    companion object {
        const val EDIT_CURATION_ID = "editCurationId"
    }
    private lateinit var presenter: AddCurationPresenter
    private lateinit var curationWordListView: ListView
    private lateinit var etInput: EditText
    private lateinit var etName: EditText
    private lateinit var curationWordListAdapter: CurationWordListAdapter
    private lateinit var progressDialog: MyProgressDialogFragment

    private lateinit var handler: InsertResultHandler

    private class InsertResultHandler(presenter: AddCurationPresenter) : Handler() {
        val reference = WeakReference<AddCurationPresenter>(presenter)
        override fun handleMessage(msg: Message) {
            val result = msg.obj as Boolean
            val errorMessage = if (result) { "" } else {
                msg.data.getString(AddCurationPresenter.INSERT_ERROR_MESSAGE)
            }
            val presenter = reference.get() ?: return
            presenter.handleInsertResultMessage(result, errorMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = DatabaseAdapter.getInstance()
        presenter = AddCurationPresenter(this, adapter)
        handler = InsertResultHandler(presenter)
        presenter.create()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
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
            val btnAdd = it.findViewById(R.id.btn_add_word) as Button
            btnAdd.setOnClickListener { presenter.onAddWordButtonClicked() }
            etInput = it.findViewById(R.id.et_curation_word) as EditText
            etName = it.findViewById(R.id.et_curation_name) as EditText
            curationWordListView = it.findViewById(R.id.lv_curation_word) as ListView
        }
    }

    override fun refreshList(addedWords: ArrayList<String>) {
        activity?.let {
            curationWordListAdapter = CurationWordListAdapter(addedWords, it)
            curationWordListView.adapter = curationWordListAdapter
            curationWordListAdapter.notifyDataSetChanged()
        }
    }

    override fun editCurationId(): Int {
        return activity?.intent?.getIntExtra(EDIT_CURATION_ID, AddCurationPresenter.NOT_EDIT_CURATION_ID)?: AddCurationPresenter.NOT_EDIT_CURATION_ID
    }

    override fun inputWord(): String {
        return etInput.text.toString()
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
        val msg = Message.obtain()
        val bundle = Bundle()
        msg.obj = false
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.empty_curation_name))
        msg.data = bundle
        handler.sendMessage(msg)
    }

    override fun handleEmptyWordError() {
        val msg = Message.obtain()
        val bundle = Bundle()
        msg.obj = false
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.empty_word_list))
        msg.data = bundle
        handler.sendMessage(msg)
    }

    override fun handleSameNameCurationError() {
        val msg = Message.obtain()
        val bundle = Bundle()
        msg.obj = false
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.duplicate_curation_name))
        msg.data = bundle
        handler.sendMessage(msg)
        TrackerHelper.sendButtonEvent(getString(R.string.add_same_curation_name))
    }

    override fun handleAddSuccess() {
        val msg = Message.obtain()
        msg.obj = true
        handler.sendMessage(msg)
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_curation))
    }

    override fun handleEditSuccess() {
        val msg = Message.obtain()
        msg.obj = true
        handler.sendMessage(msg)
        TrackerHelper.sendButtonEvent(getString(R.string.update_curation))
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
        progressDialog.show(activity?.supportFragmentManager, null)
    }

    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    override fun finish() {
        activity?.finish()
    }

    internal inner class CurationWordListAdapter(words: ArrayList<String>, context: Context) : ArrayAdapter<String>(context, R.layout.curation_word_list, words) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var holder = ViewHolder()

            // Use contentView and setup ViewHolder
            activity?.let {
                var row = convertView
                if (convertView == null) {
                    val inflater = it.layoutInflater
                    row = inflater.inflate(R.layout.curation_word_list, parent, false)
                    holder.tvWord = row.findViewById(R.id.tv_word) as TextView
                    holder.btnDelete = row.findViewById(R.id.btn_delete) as Button
                    row.tag = holder
                } else {
                    holder = convertView.tag as ViewHolder
                }

                val word = this.getItem(position)
                holder.tvWord!!.text = word
                holder.btnDelete!!.setOnClickListener {
                    presenter.onDeleteButtonClicked(position)
                }
                return row!!
            }
            return convertView!!
        }

        private inner class ViewHolder {
            internal var tvWord: TextView? = null
            internal var btnDelete: Button? = null
        }
    }

    fun onAddMenuClicked() {
        showProgressDialog()
        object : Thread() {
            override fun run() {
                Thread.sleep(5000)
                presenter.onAddMenuClicked()
            }
        }.start()
    }

}
