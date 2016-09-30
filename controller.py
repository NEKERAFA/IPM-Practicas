import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

import model

class Handler():

    # Boton añadir pelicula
    def on_anadir(self, w):

        # Obtener dialogo añadir y sus entradas de texto y spinbuttons
        danadir = builder.get_object("dialog1")
        etitulo = builder.get_object("entry1")
        eano = builder.get_object("spinbutton1")
        eduracion = builder.get_object("spinbutton2")
        egenero = builder.get_object("entry2")

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

                # Habilitar boton Eliminar si es necesario
                enable_delete_cond()

                # Añadir pelicula al modelo
                model.add_movie([titulo, ano, duracion, genero])
                break
            else:
                break

        # Resetear contenido de las entradas y ocultar dialogo
        etitulo.set_text("")
        eano.set_value(2000)
        eduracion.set_value(0)
        egenero.set_text("")
        danadir.hide()

    # Boton eliminar pelicula
    def on_eliminar(self, w):

        # No hacer nada si no hay una pelicula seleccionada
        if builder.get_object("treeview1").get_selection().get_selected()[1] == None:
            return

        deliminar = builder.get_object("messagedialog1")

        # Preparar la label secundaria para que muestre el titulo de la pelicula
        movie = builder.get_object("treeview1").get_selection().get_selected()[1]
        deliminar.get_message_area().get_children()[1].set_label(model.get_title(movie))

        # Correr el dialogo
        r = deliminar.run()

        # Si se selecciona ok
        if r == 0:
            model.delete_movie(movie)
            disable_delete_cond()

        # Esconder dialogo
        deliminar.hide()

    # Cierre de aplicacion
    def on_close(self, w, e):
        Gtk.main_quit()

# Deshabilitar boton de eliminar si no hay peliculas
def disable_delete_cond():
    if model.is_empty():
        builder.get_object("button2").set_sensitive(False)

# Habilitar boton de eliminar si empieza a haber peliculas
def enable_delete_cond():
    if model.is_empty():
        builder.get_object("button2").set_sensitive(True)

builder = Gtk.Builder()
builder.add_from_file("view.glade")
builder.connect_signals(Handler())

# Establecer modelo en la vista
builder.get_object("treeview1").set_model(model.listmodel)

# Desactivar Eliminar si es necesario
disable_delete_cond()

mainwin = builder.get_object("window1")
mainwin.show_all()
Gtk.main()
