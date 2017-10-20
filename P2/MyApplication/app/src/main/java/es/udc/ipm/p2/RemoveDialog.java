package es.udc.ipm.p2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class RemoveDialog extends DialogFragment {
    // Interfaz del observador del diálogo
    public interface RemoveDialogListener {
        void onRemoveClick();
    }

    RemoveDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Se crea el constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Se carga el layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Se construye el diálogo
        builder.setMessage(R.string.remove_dialog_message)
                .setPositiveButton(R.string.confirm_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onRemoveClick();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Al cancelar no se hace nada
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Se verifica si el contexto implementa el interfaz observador y se instancia
        try {
            listener = (RemoveDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RemoveDialogListener");
        }
    }
}
