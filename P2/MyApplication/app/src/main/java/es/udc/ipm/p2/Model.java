package es.udc.ipm.p2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase que implementa el modelo de la aplicación
 */
public class Model {

    /* Atributos */
    // Instancia del modelo
    private static Model instanceModel = null;
    // Atributos del objeto
    private List<String> categoryList;
    private List<List<String>> childrenList;
    private Activity activity; // Actividad principal

    /* Metodos */

    /**
     * Se encarga de incializar el modelo de la lista
     */
    private Model(Activity activity, Bundle savedInstanceState) {
        // Se enlaza la actividad
        this.activity = activity;

        if(savedInstanceState != null) {
            // Se carga la lista de categorías
            this.categoryList = savedInstanceState.getStringArrayList("CategoryModelKey");
            // Se carga la lista de hijos
            this.childrenList = new ArrayList<>();
            for (int i = 0; i < this.categoryList.size(); i++) {
                // Secreala clave y se obtiene la lista de hijos
                String childKey = this.categoryList.get(i) + "Key";
                this.childrenList.add(savedInstanceState.getStringArrayList(childKey));
            }

        } else {
            // Se inicializa la lista
            this.categoryList = new ArrayList<>();
            this.childrenList = new ArrayList<>();

            // Se cargan los archivos de preferencias
            SharedPreferences preferences = activity.getSharedPreferences("CategoryModelKey", Activity.MODE_PRIVATE);
            Map<String, String> list = (Map<String, String>) preferences.getAll();
            Set<String> keyEntries = list.keySet();

            if (keyEntries.isEmpty()) {
                // Si el archivo está vacio, se introducen los valores a manito :3

                int parent;

                // CATEGORÍA DE INFORMÁTICA
                parent = this.add("Informática");
                this.addChild(parent, "Inteligencia Artifical");
                this.addChild(parent, "Big data");
                this.addChild(parent, "Robótica");
                this.addChild(parent, "DDoS");

                // CATEGORÍA DE COCHES
                parent = this.add("Coches");
                this.addChild(parent, "Lamborghini Gallardo");
                this.addChild(parent, "Porsche Cayman");

                // CATEGORÍA DE FIRE EMBLEM
                this.add("Fire emblem");

                // CATEGORÍA DE BARCOS
                parent = this.add("Barcos");
                this.addChild(parent, "Falcon");
                this.addChild(parent, "Galeón");
                this.addChild(parent, "Bergantín");
                this.addChild(parent, "Galera");
                this.addChild(parent, "Acorazado");
                this.addChild(parent, "Fragata");

                // CATEGORÍA DE CIUDADES
                parent = this.add("Ciudades");
                this.addChild(parent, "A Coruña");
                this.addChild(parent, "Madrid");
                this.addChild(parent, "Barcelona");
                this.addChild(parent, "New York");
                this.addChild(parent, "Berlín");
                this.addChild(parent, "Londres");

            } else {
                // Se rellena la lista para tener posiciones disponibles
                for(String key: keyEntries) {
                    this.categoryList.add("");
                }

                // Se introducen en la lista los valores del archivo
                for(String key : keyEntries) {
                    this.categoryList.set(Integer.parseInt(key), list.get(key));
                }

                // Se empieza a recuperar a los hijos
                for (int i = 0; i < this.categoryList.size(); i++) {
                    // Se crea el hijo en la lista
                    this.childrenList.add(new ArrayList<String>());

                    // Se crea la clave del hijo y se carga su archivo
                    String childKey = this.categoryList.get(i) + "Key";
                    SharedPreferences childPref = activity.getSharedPreferences(childKey,
                            Activity.MODE_PRIVATE);
                    Map<String, String> childList = (Map<String, String>) childPref.getAll();
                    Set<String> childKeyEntries = childList.keySet();

                    // Se rellena la lista para tener posiciones disponibles
                    for (String key : childKeyEntries) {
                        this.childrenList.get(i).add("");
                    }

                    // Se introducen en la lista los valores del archivo
                    for (String key : childKeyEntries) {
                        this.childrenList.get(i).set(Integer.parseInt(key), childList.get(key));
                    }
                }
            }
        }
    }

    /**
     * Se obtiene una instancia del modelo y se inicializa si procediera
     * @param activity Actividad principal
     * @param savedInstanceState Estado de la aplicación
     * @return instancia Model
     */
    public static Model getInstance(Activity activity, Bundle savedInstanceState) {
        if (instanceModel == null) {
            instanceModel = new Model(activity, savedInstanceState);
        }
        return instanceModel;
    }

    /**
     * Se obtiene una instancia del modelo
     *
     * @return instancia Model o null si aún no se ha inicializado
     */
    public static Model getInstance() {
        return instanceModel;
    }

    /**
     * Se encarga de guardar la lista entre estados
     * @param savedInstanceState Bundle con el estado de la app
     */
    public void saveState(Bundle savedInstanceState) {
        // Se guardan la lista de categorías
        savedInstanceState.putStringArrayList("CategoryModelKey",
                (ArrayList<String>) this.categoryList);

        // Se guardan la lista de nodos
        for (int i = 0; i < this.categoryList.size(); i++) {
            // Se crea la clave del hijo
            String childKey = this.categoryList.get(i) + "Key";
            savedInstanceState.putStringArrayList(childKey,
                    (ArrayList<String>) this.childrenList.get(i));
        }
    }

    /**
     * Se encarga de guardar la lista permanentemente
     */
    public void savePermanent() {
        // Se cargan las preferencias de las categorías
        SharedPreferences pref = activity.getSharedPreferences("CategoryModelKey",
                Activity.MODE_PRIVATE);
        // Se carga el editor
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        // Se preparan los datos guardandolos con clave la posición en la lista
        for(int i = 0; i < this.categoryList.size(); i++) {
            editor.putString(String.valueOf(i), this.categoryList.get(i));
        }

        // Se guardan los cambios
        editor.apply();

        // Se guardan ahora los hijos
        for (int i = 0; i < this.childrenList.size(); i++) {
            // Se crea la clave del hijo y se carga su archivo
            String childKey = this.categoryList.get(i) + "Key";
            SharedPreferences childPref = activity.getSharedPreferences(childKey,
                    Activity.MODE_PRIVATE);
            // Se carga el editor del hijo y se limpia el contenido del archivo
            SharedPreferences.Editor childEditor = childPref.edit();
            childEditor.clear();
            // Se preparan los datos guardandolos con clave la posición en la lista
            for (int j = 0; j < this.childrenList.get(i).size(); j++) {
                childEditor.putString(String.valueOf(j), this.childrenList.get(i).get(j));
            }
            // Se aplican los cambios
            childEditor.apply();
        }
    }

    /**
     * Devuelve la lista de categorías guardada actualmente
     * @return La lista de categorías
     */
    public List<String> getList() {
        return this.categoryList;
    }

    /**
     * Devuelve un elemento padre
     * @param position Posición a devolver
     */
    public String get(int position) {
        return this.categoryList.get(position);
    }

    /**
     * Devuelve si el modelo está vacio
     * @return true si está vacio
     */
    public boolean isEmpty() {
        return this.categoryList.isEmpty();
    }

    /**
     * Introduce un elemento en la lista
     * @param element String a introducir
     * @return position added
     */
    public int add(String element) {
        this.categoryList.add(element);
        Collections.sort(this.categoryList, String.CASE_INSENSITIVE_ORDER);
        int parentIndex = this.categoryList.indexOf(element);
        this.childrenList.add(parentIndex, new ArrayList<String>());

        return parentIndex;
    }

    /**
     * Elimina todas las categorias en la lista
     * @param elements Colección de elementos a eliminar
     */
    public void remove(Collection<String> elements) {
        // Posiciones a elminiar
        int[] positionsToRemove = new int[elements.size()];

        // Se va recorriendo todos los nodos hasta encontrar las categorías a eliminar
        int r = 0;
        for (String elem : elements) {
            for (int i = 0; i < this.categoryList.size(); i++) {
                if (elem == this.categoryList.get(i)) {
                    // Se guardan las posiciones a eliminar
                    positionsToRemove[r++] = i;
                }
            }
        }

        // Se recorren los datos a eliminar
        for (int i = 0; i < positionsToRemove.length; i++) {
            int actPos = positionsToRemove[i];
            this.categoryList.remove(actPos);
            this.childrenList.remove(actPos);
            // Se comprueba si las posiciones a eliminar han cambiado
            for (int j = i; j < positionsToRemove.length; j++) {
                // Si las posiciones a eliminar restantes están después de la
                // que se acaba de eliminar, hay que restarles 1
                if (positionsToRemove[j] > positionsToRemove[i]) {
                    positionsToRemove[j]--;
                }
            }
        }
    }

    /**
     * Actualiza una categoria en la lista
     * @param element nombre de la categoría nueva
     * @param position posicion a modificar
     */
    public int update(String element, int position) {
        this.categoryList.set(position, element);
        Collections.sort(this.categoryList, String.CASE_INSENSITIVE_ORDER);
        int newPosition = this.categoryList.indexOf(element);
        List<String> children = this.childrenList.remove(position);
        this.childrenList.add(newPosition, children);
        return newPosition;
    }

    /**
     * Devuelve la lista de un hijo
     *
     * @param parentIndex Indice del padre
     * @return La lista de elementos del hijo
     */
    public List<String> getChildList(int parentIndex) {
        if (this.childrenList.isEmpty()) {
            this.childrenList.add(new LinkedList<String>());
        }
        return this.childrenList.get(parentIndex);
    }

    /**
     * Devuelve el hijo en una posición dada
     * @param parentIndex posición padre
     * @param childIndex posición hijo
     * @return Nombre del hijo
     */
    public String getChild(int parentIndex, int childIndex) {
        return this.childrenList.get(parentIndex).get(childIndex);
    }

    /**
     * Añade un hijo a la lista de hijos
     *
     * @param parentIndex Index del padre
     * @param child String con el título del hijos
     * @return posición del hijo
     */
    public int addChild(int parentIndex, String child) {
        if (this.childrenList.isEmpty()) {
            this.childrenList.add(new ArrayList<String>());
        }

        this.childrenList.get(parentIndex).add(child);
        Collections.sort(this.childrenList.get(parentIndex), String.CASE_INSENSITIVE_ORDER);
        return this.childrenList.get(parentIndex).indexOf(child);
    }

    /**
     * Elimina un hijo de la lista de hijos
     *
     * @param parentIndex Index del padre
     * @param elements    Colection de elementos a añadir
     */
    public void removeChildren(int parentIndex, Collection<String> elements) {
        int[] positionsToRemove = new int[elements.size()];
        List<String> list = this.childrenList.get(parentIndex);

        // Se van recorriendo todos los nodos hasta encontrar los elementos a eliminar
        int r = 0;
        for (String elem : elements) {
            for (int i = 0; i < list.size(); i++) {
                if (elem == list.get(i)) {
                    // Se guardan las posiciones a eliminar
                    positionsToRemove[r++] = i;
                }
            }
        }

        // Se recorren los datos a eliminar
        for (int i = 0; i < positionsToRemove.length; i++) {
            int actPos = positionsToRemove[i];
            list.remove(actPos);
            // Se comprueba si las posiciones a eliminar han cambiado
            for (int j = i; j < positionsToRemove.length; j++) {
                // Si las posiciones a eliminar restantes están después de la
                // que se acaba de eliminar, hay que restarles 1
                if (positionsToRemove[j] > positionsToRemove[i]) {
                    positionsToRemove[j]--;
                }
            }
        }
    }

    /**
     * Actualiza un hijo
     * @param parentIndex Index del padre
     * @param childIndex Index del hijo
     * @param name Nombre del hijo
     */
    public int updateChild(int parentIndex, int childIndex, String name) {
        this.childrenList.get(parentIndex).set(childIndex, name);
        Collections.sort(this.childrenList.get(parentIndex), String.CASE_INSENSITIVE_ORDER);
        return this.childrenList.get(parentIndex).indexOf(name);
    }

    public boolean isEmptyChild(int parentIndex) {
        return  this.childrenList.get(parentIndex).isEmpty();
    }
}
