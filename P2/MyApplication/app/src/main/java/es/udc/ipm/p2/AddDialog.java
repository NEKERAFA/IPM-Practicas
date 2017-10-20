package es.udc.ipm.p2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

public class AddDialog extends DialogFragment {

    // Interfaz del observador del diálogo
    public interface AddDialogListener {
        void onAddClick(String title);
    }

    AddDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Se crea el constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Se carga el layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Se construye el diálogo
        builder.setView(inflater.inflate(R.layout.add_fragment_dialog, null))
                .setTitle(R.string.add_dialog_title)
                .setPositiveButton(R.string.confirm_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Se castea el DialogInterface a Dialog para obtener el EditText y pasarlo
                        // Al listener
                        Dialog addDialog = (Dialog) dialog;
                        EditText text = (EditText) addDialog.findViewById(R.id.category_add);
                        listener.onAddClick(text.getText().toString());
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
        // Se verifica si el contexto implementa el interfaz observador
        // Y se instancia
        try {
            listener = (AddDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddDialogListener");
        }
    }
}
