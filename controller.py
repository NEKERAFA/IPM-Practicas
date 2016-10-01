import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

from model import ListMovie

class Handler():
    '''
    Clase del controlador con los manejadores de la vistas
    '''

    # Boton añadir pelicula
    def on_add(self, w):

        # Obtener dialogo añadir y sus entradas de texto y spinbuttons
        danadir = builder.get_object("dialog1")
        danadir.set_title("Añadir Película")

        etitulo = builder.get_object("entry1")
        eano = builder.get_object("spinbutton1")
        eduracion = builder.get_object("spinbutton2")
        egenero = builder.get_object("entry2")

        # Resetear contenido de las entradas
        etitulo.set_text("")
        eano.set_value(2000)
        eduracion.set_value(0)
        egenero.set_text("")

        # Esperar por una respuesta valida (aceptar con datos correctos o cancelar)
        while True:
            # Correr el dialogo
            r = danadir.run()

            # Si se selecciona ok
            if r == 0:
                # Obtener datos introducidos
                titulo = etitulo.get_text()
                ano = str(eano.get_value_as_int())
                duracion = str(eduracion.get_value_as_int())
                genero = egenero.get_text()

                # Comprobar que la pelicula tenga titulo
                if titulo == "":
                    # Dialogo informativo
                    builder.get_object("messagedialog2").run()
                    builder.get_object("messagedialog2").hide()
                    continue

                # Habilitar botones Eliminar y Editar si es necesario
                enable_delete_cond()
                enable_edit_cond()

                # Añadir pelicula al modelo
                model.add_movie([titulo, ano, duracion, genero])
                break
            else:
                break

        # Ocultar dialogo
        danadir.hide()

    # Boton eliminar pelicula
    def on_remove(self, w):

        movie = builder.get_object("treeview1").get_selection().get_selected()[1]

        # No hacer nada si no hay una pelicula seleccionada
        if movie == None:
            return

        deliminar = builder.get_object("messagedialog1")

        # Preparar la label secundaria para que muestre el titulo de la pelicula
        deliminar.get_message_area().get_children()[1].set_label(model.get_title(movie))

        # Correr el dialogo
        r = deliminar.run()

        # Si se selecciona ok
        if r == 0:
            model.delete_movie(movie)
            disable_delete_cond()
            disable_edit_cond()

        # Esconder dialogo
        deliminar.hide()

    # Boton añadir pelicula
    def on_edit(self, w):

        movie = builder.get_object("treeview1").get_selection().get_selected()[1]

        # No hacer nada si no hay una pelicula seleccionada
        if movie == None:
            return

        # Obtener dialogo editar y sus entradas de texto y spinbuttons
        # Se reutiliza el dialogo de añadir
        deditar = builder.get_object("dialog1")
        deditar.set_title("Editar Película")
        etitulo = builder.get_object("entry1")
        eano = builder.get_object("spinbutton1")
        eduracion = builder.get_object("spinbutton2")
        egenero = builder.get_object("entry2")

        # Se muestran las entradas de texto y spinbuttons con los datos originales de la pelicula
        etitulo.set_text(model.get_title(movie))
        eano.set_value(int(model.get_year(movie)))
        eduracion.set_value(int(model.get_duration(movie)))
        egenero.set_text(model.get_genre(movie))

        # Esperar por una respuesta valida (aceptar con datos correctos o cancelar)
        while True:
            # Correr el dialogo
            r = deditar.run()

            # Si se selecciona ok
            if r == 0:
                # Obtener datos introducidos
                titulo = etitulo.get_text()
                ano = str(eano.get_value_as_int())
                duracion = str(eduracion.get_value_as_int())
                genero = egenero.get_text()

                # Comprobar que la pelicula tenga titulo
                if titulo == "":
                    # Dialogo informativo
                    builder.get_object("messagedialog2").run()
                    builder.get_object("messagedialog2").hide()
                    continue

                # Añadir pelicula al modelo
                model.set_movie(movie, [titulo, ano, duracion, genero])
                break
            else:
                break

        # Ocultar dialogo
        deditar.hide()

    # Cierre de aplicacion
    def on_close(self, w, e):
        model.save()
        Gtk.main_quit()

# Deshabilitar boton de eliminar si no hay peliculas
def disable_delete_cond():
    if model.is_empty():
        builder.get_object("button2").set_sensitive(False)

# Habilitar boton de eliminar si empieza a haber peliculas
def enable_delete_cond():
    if model.is_empty():
        builder.get_object("button2").set_sensitive(True)

# Deshabilitar boton de editar si no hay peliculas
def disable_edit_cond():
    if model.is_empty():
        builder.get_object("button3").set_sensitive(False)

# Habilitar boton de editar si empieza a haber peliculas
def enable_edit_cond():
    if model.is_empty():
        builder.get_object("button3").set_sensitive(True)

# Se crea el modelo
model = ListMovie()

# Se crean las vistas
builder = Gtk.Builder()
builder.add_from_file("view.glade")
builder.connect_signals(Handler())

# Establecer modelo en la vista
builder.get_object("treeview1").set_model(model.listmodel)

# Desactivar Eliminar y Editar si es necesario
disable_delete_cond()
disable_edit_cond()

mainwin = builder.get_object("window1")
mainwin.show_all()
Gtk.main()
