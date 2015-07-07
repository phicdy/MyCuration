package com.phicdy.filfeed.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class MyProgressDialogFragment extends DialogFragment{
    private static ProgressDialog progressDialog = null;
    private static final String MESSAGE = "message";

    public MyProgressDialogFragment(){}

    public static MyProgressDialogFragment newInstance(String message){
        MyProgressDialogFragment instance = new MyProgressDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(MESSAGE, message);

        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        if (progressDialog != null)
            return progressDialog;

        String message = getArguments().getString(MESSAGE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);

        return progressDialog;
    }

    @Override
    public Dialog getDialog(){
        return progressDialog;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        progressDialog = null;
    }
}
