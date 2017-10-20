package es.udc.ipm.p2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditDialog extends DialogFragment {

    // Interfaz del observador del diálogo
    public interface EditDialogListener {
        void onEditClick(String name);
    }

    EditDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Obtener EditText
        View view = inflater.inflate(R.layout.edit_fragment_dialog, null);
        EditText editText = (EditText) view.findViewById(R.id.category_edit);
        // Obtener argumentos
        final Bundle args = getArguments();
        // Poner como texto del EditText el nombre de la categoría a editar
        final String oldElement = args.getString("editCategory");
        editText.setText(oldElement);
        // Construir el diálogo
        builder.setView(view)
                .setTitle(R.string.edit_dialog_title)
                .setPositiveButton(R.string.confirm_edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Se castea el DialogInterface a Dialog para obtener el EditText y pasarlo
                        // al listener
                        Dialog editDialog = (Dialog) dialog;
                        EditText editText = (EditText) editDialog.findViewById(R.id.category_edit);
                        if(editText.getText().toString().equals("")) {
                            listener.onEditClick(oldElement);
                        } else {
                            listener.onEditClick(editText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // No se hace nada
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
            listener = (EditDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implemet EditDialogListener");
        }
    }
}
