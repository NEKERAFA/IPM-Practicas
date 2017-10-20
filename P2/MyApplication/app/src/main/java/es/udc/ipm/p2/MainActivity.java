package es.udc.ipm.p2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements CategoriesFragment.OnCategoryListener {

    private static boolean dualPane;

    private Model model;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private OrientationDetector orientationDetector;
    private FaceDownDetector faceDownDetector;
    private int child = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = model.getInstance(this, savedInstanceState);

        // Inicialización del ShakeDetector
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Comprobamos si tenemos acelerómetro e inicializamos el ShakeDetector
        if (accelerometer != null) {
            shakeDetector = new ShakeDetector();
            shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                @Override
                public void onShake() {
                    showRandomElement();
                }
            });
        }

        // Inicialización del FaceDownDetector
        if (accelerometer != null) {
            faceDownDetector = new FaceDownDetector();
            faceDownDetector.setOnFaceDownListener(new FaceDownDetector.OnFaceDownListener() {

                @Override
                public void onFaceDown() {
                    showRandomElement();
                }
            });
        }

        // Inicialización del OrientationDetector
        orientationDetector = new OrientationDetector(this, SensorManager.SENSOR_DELAY_NORMAL);
        // Comprobamos si se puede usar el sensor
        if (orientationDetector.canDetectOrientation()) {
            orientationDetector.setOnOrientationListener(new OrientationDetector.OnOrientationListener() {

                @Override
                public void onOrientationChanged() {
                    showRandomElement();
                }
            });
        }

        if (findViewById(R.id.fragment_container) != null) {

            // Si no estábamos en dualPane (es decir, estábamos en móvil), restauramos el estado
            // as always :3
            if ((savedInstanceState != null) && (!dualPane)) {
                return;
            }

            // Si llegamos aquí es porque estábamos anteriormente en dualPane
            this.dualPane = false;

            // Create a new Fragment to be placed in the activity layout
            CategoriesFragment firstFragment = new CategoriesFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            Fragment oldFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container2);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (oldFragment != null) {
                transaction.remove(oldFragment);
                getSupportFragmentManager().popBackStack(null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            transaction.replace(R.id.fragment_container, firstFragment).commit();

        // Se comprueba si la aplicación usa el layout de dos paneles
        } else if (findViewById(R.id.fragment_container2) != null) {

            this.dualPane = true;

            int indexParent;
            try {
                indexParent = savedInstanceState.getInt(CategoriesFragment.PARENT_INDEX);
            } catch (java.lang.NullPointerException e) {
                indexParent = 0;
            }

            if (!model.isEmpty()) {
                ChildListFragment fragment = new ChildListFragment();
                Bundle args = new Bundle();
                args.putInt(CategoriesFragment.PARENT_INDEX, indexParent);
                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment oldFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (oldFragment != null) {
                    transaction.remove(oldFragment);
                }

                transaction.add(R.id.fragment_container2, fragment);
                transaction.commit();
            }

            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onCategorySelected(int selectedCategory) {

        boolean randomElement;
        try {
            OnlyOneElementFragment fragment = (OnlyOneElementFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container2);
            randomElement = true;
        } catch (ClassCastException e) {
            randomElement = false;
        }

        // Si estás en dual panel en la misma categoría y sin un random element, no se puede cambiar
        if(!(dualPane && (selectedCategory == child) && !randomElement)) {

            ChildListFragment fragment = new ChildListFragment();
            Bundle args = new Bundle();
            args.putInt(CategoriesFragment.PARENT_INDEX, selectedCategory);
            fragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (dualPane) {
                transaction.replace(R.id.fragment_container2, fragment);
            } else {
                transaction.replace(R.id.fragment_container, fragment);
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            if (!dualPane) {
                transaction.addToBackStack(null);
            }
            transaction.commit();

            child = selectedCategory;
        }

    }

    @Override
    public void onNoCategoriesLeft() {
        // Cambiamos el título de la app bar
        getSupportActionBar().setTitle(R.string.app_name);

    }

    @Override
    public void onFirstCategoryAdded() {
        this.child = 0;
        ChildListFragment fragment = new ChildListFragment();
        Bundle args = new Bundle();
        args.putInt(CategoriesFragment.PARENT_INDEX, 0);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container2, fragment);
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        faceDownDetector.onSaveInstanceState(savedInstanceState);
        orientationDetector.onSaveInstanceState(savedInstanceState);

        if(!dualPane) {
            ChildListFragment childListFragment;
            try {
                 childListFragment = (ChildListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
            } catch (ClassCastException e) {
                return;
            }
            int parentIndex = childListFragment.getParentIndex();
            savedInstanceState.putInt(CategoriesFragment.PARENT_INDEX, parentIndex);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Se restauran los sensores
        if (orientationDetector.canDetectOrientation()) {
            orientationDetector.onRestoreInstanceState(savedInstanceState);
        }
        if (accelerometer != null) {
            faceDownDetector.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(faceDownDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (orientationDetector.canDetectOrientation()) {
            orientationDetector.enable();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(shakeDetector);
            sensorManager.unregisterListener(faceDownDetector);
        }
        if (orientationDetector.canDetectOrientation()) {
            orientationDetector.disable();
        }
    }

    public static boolean isDualPane() {
        return dualPane;
    }

    public void setChild(int child) {
        this.child = child;
    }

    public void showRandomElement() {

        int parentIndex;
        try {
            if (dualPane) {
                parentIndex = ((ChildListFragment) getSupportFragmentManager().
                        findFragmentById(R.id.fragment_container2)).getParentIndex();
            } else {
                parentIndex = ((ChildListFragment) getSupportFragmentManager().
                        findFragmentById(R.id.fragment_container)).getParentIndex();
            }
        } catch (ClassCastException e) {
            return;
        }

        if (!model.isEmptyChild(parentIndex)) {
            // Creamos un generador de números aleatorios a partir del tiempo actual
            Random random = new Random(Calendar.getInstance().getTime().getTime());
            // Generamos un número aleatorio entre 0 y el tamaño-1 de la lista de hijos
            // en la que nos encontramos (para no salirnos de los índices de la lista)
            int selected = random.nextInt(model.getChildList(parentIndex).size());

            // Mostrar el elemento seleccionado
            String selectedElement = model.getChild(parentIndex, selected);
            String parentCategory = model.get(parentIndex);

            OnlyOneElementFragment fragment = new OnlyOneElementFragment();
            Bundle args = new Bundle();
            args.putString(ChildListFragment.PARENT_CATEGORY, parentCategory);
            args.putString(ChildListFragment.SELECTED_ELEMENT, selectedElement);
            fragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (dualPane) {
                transaction.replace(R.id.fragment_container2, fragment);
            } else {
                transaction.replace(R.id.fragment_container, fragment);
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }
}
