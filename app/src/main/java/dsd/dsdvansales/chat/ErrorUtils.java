package dsd.dsdvansales.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.quickblox.core.exception.QBResponseException;

import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.MainApplication;
import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;

public class ErrorUtils {

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ErrorUtils() {
    }

    public static Snackbar showSnackbar(View view, @StringRes int errorMessage, Exception e,
                                        @StringRes int actionLabel, View.OnClickListener clickListener) {
        String error = (e == null) ? "" : e.getMessage();
        boolean noConnection = Str.NoConnectionError[Global.LANG].equals(error);
        boolean timeout = error.startsWith(Str.NoResponseTimedOut[Global.LANG]);
        if (noConnection || timeout) {
            return showSnackbar(view, Str.NoInternetConnection[Global.LANG], actionLabel, clickListener);
        } else if (errorMessage == 0) {
            return showSnackbar(view, error, actionLabel, clickListener);
        } else if (error.equals("")) {
            return showSnackbar(view, errorMessage, Str.NoInternetConnection[Global.LANG], actionLabel, clickListener);
        } else {
            return showSnackbar(view, errorMessage, error, actionLabel, clickListener);
        }
    }

    public static Snackbar showSnackbar(View view, @StringRes int errorMessage, String error,
                                        @StringRes int actionLabel, View.OnClickListener clickListener) {
        String errorMessageString = MainApplication.getInstance().getString(errorMessage);
        String message = String.format(Locale.ENGLISH, "%s: %s", errorMessageString, error);
        return showSnackbar(view, message, actionLabel, clickListener);
    }

    public static Snackbar showSnackbar(View view, @StringRes int message,
                                        @StringRes int actionLabel,
                                        View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(actionLabel, clickListener);
        snackbar.show();
        return snackbar;
    }

    private static Snackbar showSnackbar(View view, String message,
                                         @StringRes int actionLabel,
                                         View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message.trim(), Snackbar.LENGTH_INDEFINITE);
        if (clickListener != null) {
            snackbar.setAction(actionLabel, clickListener);
        }
        snackbar.show();
        return snackbar;
    }

    public static void showErrorToast(QBResponseException exception) {
        Toast.makeText(MainApplication.getInstance().getApplicationContext(),String.format(Locale.ENGLISH, Str.RequestCompletedWithError[Global.LANG], exception.getErrors()
                + ", code: " + exception.getHttpStatusCode()), Toast.LENGTH_LONG).show();
    }

    public static void showErrorDialog(Context context, @StringRes int errorMessage, String error) {
        showErrorDialog(context, context.getString(errorMessage), error);
    }

    public static void showErrorDialog(Context context, @StringRes int errorMessage, List<String> errors) {
        showErrorDialog(context, context.getString(errorMessage), errors.toString());
    }

    private static void showErrorDialog(Context context, String errorMessage, String error) {
        showErrorDialog(context, String.format(Locale.ENGLISH, "%s: %s", errorMessage, error));
    }

    private static void showErrorDialog(final Context context, final String errorMessage) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle("Dialog Error")
                        .setMessage(errorMessage)
                        .create()
                        .show();
            }
        });
    }
}
