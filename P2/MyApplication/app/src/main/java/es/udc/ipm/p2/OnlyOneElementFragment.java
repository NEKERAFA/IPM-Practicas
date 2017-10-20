package es.udc.ipm.p2;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OnlyOneElementFragment extends Fragment {

    private String element;
    private String category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Obtenemos el elemento seleccionado y la categoría a la que pertenece
        Bundle args = getArguments();
        element = args.getString(ChildListFragment.SELECTED_ELEMENT);
        category = args.getString(ChildListFragment.PARENT_CATEGORY);

        View view = inflater.inflate(R.layout.fragment_only_one_element, container, false);
        TextView textView = (TextView) view.findViewById(R.id.element_textview);
        textView.setText(element);

        if (!MainActivity.isDualPane()) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(category);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        String title = getActivity().getString(R.string.showing_random_element);
        title += " " + category;
        activity.getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
