package es.udc.ipm.p2;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ChildListFragment extends Fragment implements AddDialog.AddDialogListener,
        RemoveDialog.RemoveDialogListener, EditDialog.EditDialogListener {

    public final static String SELECTED_ELEMENT = "selected_element";
    public final static String PARENT_CATEGORY = "parent_category";

    private Model model;
    private int parentIndex;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ActionMode actionMode;
    private SparseBooleanArray checkedItems;
    private int editPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child_list, container, false);

        // Obtenemos el index del padre
        Bundle args = getArguments();
        parentIndex = args.getInt(CategoriesFragment.PARENT_INDEX);

        // Se crea la lista
        listView = (ListView) view.findViewById(R.id.childlist);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            boolean showEdit = true;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {

                // Controlamos si hay que mostrar la acción de editar o no
                int selectedItems = listView.getCheckedItemCount();

                if (selectedItems > 1) {
                    showEdit = false;
                    mode.invalidate();
                } else {
                    showEdit = true;
                    mode.invalidate();
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        // Obtenemos la única posición marcada
                        checkedItems = listView.getCheckedItemPositions();
                        for (int i = 0; i < checkedItems.size(); i++) {
                            int position = checkedItems.keyAt(i);
                            if (checkedItems.get(position)) {
                                editPosition = position;
                            }
                        }
                        EditDialog editDialog = new EditDialog();
                        Bundle args = new Bundle();
                        args.putString("editCategory", adapter.getItem(editPosition));
                        editDialog.setArguments(args);
                        editDialog.setTargetFragment(ChildListFragment.this, 0);
                        editDialog.show(getActivity().getSupportFragmentManager(), "edit_dialog");
                        mode.finish();
                        return true;
                    case R.id.action_delete:
                        checkedItems = listView.getCheckedItemPositions().clone();
                        DialogFragment removeDialog = new RemoveDialog();
                        removeDialog.setTargetFragment(ChildListFragment.this, 0);
                        removeDialog.show(getActivity().getSupportFragmentManager(), "remove_dialog");
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cab_menu, menu);
                adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, model.getChildList(parentIndex));
                listView.setAdapter(adapter);
                actionMode = mode;
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                showEdit = false;
                adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, model.getChildList(parentIndex));
                listView.setAdapter(adapter);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (showEdit) {
                    menu.findItem(R.id.action_edit).setVisible(true);
                    return true;
                } else {
                    menu.findItem(R.id.action_edit).setVisible(false);
                    return true;
                }
            }

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                // Usamos action_delete porque siempre está presente si aparece el menú
                // contextual, al contrario que action_edit
                View contextualElement = activity.findViewById(R.id.action_delete);
                // Si el menú contextual está visible, consideramos que el
                // el usuario pretende cancelar la selección
                if ((contextualElement != null) && contextualElement.isShown() && MainActivity.isDualPane()) {
                    // Terminamos ActionMode de la lista hija
                    ((CategoriesFragment) getFragmentManager().findFragmentById(R.id.categories))
                            .getActionMode().finish();
                }
            }
        });

        // Se crea el modelo
        this.model = Model.getInstance();

        // Se crea el adaptador de la lista
        this.adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, model.getChildList(parentIndex));
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Finalizamos el actionMode
        if(actionMode != null) {
            actionMode.finish();
        }

        if (this.model != null) {
            this.model.saveState(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.model.savePermanent();
    }

    // Callbacks de la toolbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_childlist, menu);
        if (MainActivity.isDualPane()) {
            // Ponemos como título de la barra el nombre de la categoría
            // (a menos que no queden categorías)
            if (!model.getList().isEmpty()) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                String title = getActivity().getString(R.string.showing_title) + " " + model.get(parentIndex);
                activity.getSupportActionBar().setTitle(title);
            }
        } else  {
            // Ponemos como título de la barra el nombre de la categoría
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setTitle(model.get(parentIndex));
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_childlist:
                DialogFragment addDialog = new AddDialog();
                addDialog.setTargetFragment(this, 0);
                addDialog.show(getActivity().getSupportFragmentManager(), "add_dialog");
                return true;
            case android.R.id.home:
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportFragmentManager().popBackStack();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAddClick(String text) {
        if (text.equals("")) {
            model.addChild(parentIndex, getString(R.string.default_name));
        } else {
            model.addChild(parentIndex, text);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRemoveClick() {
        // Lista donde se guardan los seleccionados
        List<String> checkedList = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            // Se recupera la posición de la lista
            int posicion = checkedItems.keyAt(i);
            // Si esta marcado para eliminar se añade en la lista de seleccionados
            if (checkedItems.get(posicion)) {
                checkedList.add(adapter.getItem(posicion));
            }
        }

        // Se procede a eliminar de la lista
        model.removeChildren(parentIndex, checkedList);
        // Se notifican los cambios
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onEditClick(String name) {
        // Se actualiza el modelo
        model.updateChild(parentIndex, editPosition, name);
        // Se notifican los cambios
        adapter.notifyDataSetChanged();
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;

        // Se crea el adaptador de la lista
        this.adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, model.getChildList(parentIndex));
        listView.setAdapter(adapter);
    }

    public ActionMode getActionMode() {
        return actionMode;
    }

}
