package es.udc.ipm.p2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
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

public class CategoriesFragment extends Fragment implements AddDialog.AddDialogListener,
        RemoveDialog.RemoveDialogListener, EditDialog.EditDialogListener {

    public final static String PARENT_INDEX = "parent_index";

    private Model model;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ActionMode actionMode;
    private SparseBooleanArray checkedItems;
    private int editPosition;
    private OnCategoryListener listener;

    public interface OnCategoryListener {
        void onCategorySelected(int selectedCategory);
        void onNoCategoriesLeft();
        void onFirstCategoryAdded();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Se crea la lista
        listView = (ListView) view.findViewById(R.id.mainlist);
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
                        editDialog.setTargetFragment(CategoriesFragment.this, 0);
                        editDialog.show(getActivity().getSupportFragmentManager(), "edit_dialog");
                        mode.finish();
                        return true;
                    case R.id.action_delete:
                        checkedItems = listView.getCheckedItemPositions().clone();
                        DialogFragment removeDialog = new RemoveDialog();
                        removeDialog.setTargetFragment(CategoriesFragment.this, 0);
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
                        android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, model.getList());
                listView.setAdapter(adapter);
                actionMode = mode;
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                showEdit = false;
                adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, model.getList());
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

        // Creamos un Listener para los items de la lista
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                // Usamos action_delete porque siempre está presente si aparece el menú
                // contextual, al contrario que action_edit
                View contextualElement = activity.findViewById(R.id.action_delete);
                // Si el menú contextual no está, cambiamos de categoría
                if ((contextualElement == null)
                        || ((contextualElement != null) && (!contextualElement.isShown()))) {
                    listener.onCategorySelected(position);
                    // Si el menú contextual está visible, consideramos que el
                    // el usuario pretende cancelar la selección
                } else if (MainActivity.isDualPane()) {
                    // Terminamos ActionMode de la lista hija
                    ((ChildListFragment) getFragmentManager().findFragmentById(R.id.fragment_container2))
                            .getActionMode().finish();
                }
            }
        });

        // Se crea el modelo
        this.model = Model.getInstance(getActivity(), savedInstanceState);

        // Se crea el adaptador de la lista
        this.adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, model.getList());
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (OnCategoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCategoryListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Finalizamos el actionMode
        if (actionMode != null) {
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
        inflater.inflate(R.menu.toolbar, menu);
        if (!MainActivity.isDualPane()) {
            // Ponemos el nombre de la app
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                DialogFragment addDialog = new AddDialog();
                addDialog.setTargetFragment(this, 0);
                addDialog.show(getActivity().getSupportFragmentManager(), "add_dialog");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAddClick(String text) {
        if (text.equals("")) {
            model.add(getString(R.string.default_name));
        } else {
            model.add(text);
        }
        adapter.notifyDataSetChanged();

        if (MainActivity.isDualPane() && (model.getList().size() == 1)) {
            listener.onFirstCategoryAdded();
        }
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
        model.remove(checkedList);
        // Se notifican los cambios
        adapter.notifyDataSetChanged();

        if (MainActivity.isDualPane()) {
            ChildListFragment fragment;
            try {
                fragment = (ChildListFragment) getFragmentManager()
                        .findFragmentById(R.id.fragment_container2);
            } catch (java.lang.ClassCastException e) {
                fragment = new ChildListFragment();
            }

            if (fragment != null) {
                for (int i = 0; i < checkedItems.size(); i++) {
                    // Se recupera la posición de la lista
                    int posicion = checkedItems.keyAt(i);

                    if ((checkedItems.get(posicion)) && (posicion == fragment.getParentIndex())) {
                        // Si se ha eliminado el padre, se reemplaza el fragmento por uno que muestre
                        // los elementos de la primera categoría
                        if (!model.getList().isEmpty()) {
                            ChildListFragment newFragment = new ChildListFragment();
                            Bundle args = new Bundle();
                            args.putInt(CategoriesFragment.PARENT_INDEX, 0);
                            newFragment.setArguments(args);
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container2, newFragment);
                            transaction.commit();
                            break;
                        // Si no quedan categorías, eliminamos el fragmento del lado derecho
                        } else {
                            Fragment oldFragment = getFragmentManager().findFragmentById(R.id.fragment_container2);
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.remove(oldFragment).commit();
                            listener.onNoCategoriesLeft();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEditClick(String name) {
        // Se actualiza el modelo
        int newIndex = model.update(name, editPosition);
        // Se notifican los cambios
        adapter.notifyDataSetChanged();

        if (MainActivity.isDualPane()) {
            ChildListFragment fragment;
            try {
                fragment = (ChildListFragment) getFragmentManager()
                        .findFragmentById(R.id.fragment_container2);
            } catch (java.lang.ClassCastException e) {
                fragment = null;
            }

            if (fragment != null) {
                fragment.setParentIndex(newIndex);
                ((MainActivity) getActivity()).setChild(newIndex);
            }
        }
    }

    public ActionMode getActionMode() {
        return actionMode;
    }
}
