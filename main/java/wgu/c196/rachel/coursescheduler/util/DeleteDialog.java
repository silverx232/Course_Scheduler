package wgu.c196.rachel.coursescheduler.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import wgu.c196.rachel.coursescheduler.R;

/**
 * Creates a confirmation message about deleting objects.
 *
 * <p> This class creates a message allowing the user to either confirm or cancel when they are
 * deleting assessments, courses, instructors, and terms. Contains a DeleteDialogListener interface
 * that lets implementing activities choose what to do when the user confirms or cancels. </p>
 */
public class DeleteDialog extends DialogFragment {
    /**
     * Tag used to relay a message for the dialog to display
     */
    public static final String MESSAGE = "message";

    private DeleteDialogListener listener;

    /**
     * Creates the Dialog that will ask for confirmation before deleting items.
     *
     * @param savedInstanceState The last saved instance state of the fragment. Null if not already created.
     * @return The Dialog instance that will be displayed
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = getString(R.string.delete_general);

        Bundle arg = getArguments();
        if (arg != null) {
            message = arg.getString(MESSAGE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDeleteDialogPositive(DeleteDialog.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDeleteDialogNegative(DeleteDialog.this);
                    }
                });

        return builder.create();
    }

    /**
     * Attaches the DeleteDialogListener implemented in the context, or throws an exception if not implemented.
     *
     * @param context The context implementing the DeleteDialog class
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            listener = (DeleteDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement DeleteDialogListener");
        }
    }

    /**
     * Overrides onCancel() so that a cancel is treated as a negative button press.
     *
     * @param dialog The dialog that was canceled
     */
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        listener.onDeleteDialogNegative(DeleteDialog.this);
    }

    /**
     * Interface that lets activities choose what to do when the user confirms or cancels.
     */
    public interface DeleteDialogListener {
        void onDeleteDialogPositive(DialogFragment dialog);
        void onDeleteDialogNegative(DialogFragment dialog);
    }
}
