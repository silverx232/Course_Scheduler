package wgu.c196.rachel.coursescheduler.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Creates an information message.
 *
 * <p> Creates a message used to inform users about things such as missing required information in
 * forms. </p>
 */
public class InformationDialog extends DialogFragment {
    /**
     * Tag used to relay a message for the dialog to display
     */
    public static final String MESSAGE = "message";

    /**
     * Creates the Dialog that will inform the user about the provided message.
     *
     * @param savedInstanceState The last saved instance state of the fragment. Null if not already created.
     * @return The Dialog instance that will be displayed
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = "";

        Bundle args = getArguments();
        if (args != null) {
            message = args.getString(MESSAGE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
