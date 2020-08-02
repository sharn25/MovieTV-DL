package com.sb.movietvdl.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.sb.movietvdl.R;

    /*
    * Created by Sharn25
    * date 06/06/2020.
    */

public class CustomDialog
{
    private static Toast mToast;
    private static String c = "";
    public static void popinfo(Context context, String message){
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton("OK", null)
                .create()
                .show();
    }

    public static void popSnackinfo(View view, String msg){
        Snackbar mySnackbar = Snackbar.make(view,
               msg, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    public static void showToast(Context mActivity, CharSequence text) {
        if (mToast == null) {
            mToast = Toast.makeText(mActivity, text, Toast.LENGTH_SHORT);
        } else {
            mToast.cancel();
            mToast = Toast.makeText(mActivity, text, Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
