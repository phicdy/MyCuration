package com.phicdy.mycuration.feature.addcuration

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class MyProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (progressDialog != null) return progressDialog as ProgressDialog

        val message = arguments?.getString(MESSAGE) ?: ""
        return ProgressDialog(activity).apply {
            setMessage(message)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            isCancelable = false
        }
    }

    override fun getDialog(): Dialog? {
        return progressDialog
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog = null
    }

    companion object {
        private var progressDialog: ProgressDialog? = null
        private const val MESSAGE = "message"

        fun newInstance(message: String): MyProgressDialogFragment {
            val instance = MyProgressDialogFragment()
            val arguments = Bundle().apply {
                putString(MESSAGE, message)
            }
            instance.arguments = arguments
            return instance
        }
    }
}
