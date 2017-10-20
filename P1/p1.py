#!/usr/bin/python3
# coding=utf-8

import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk
from gi.repository import GObject

from model import *

import locale
import os
import gettext
import threading
import http.client
import socket

# Funciones de traduccion
_ = gettext.gettext

# Seleccionar locale
locale.setlocale(locale.LC_ALL, '')
APPLET_DIR = os.path.dirname(os.path.abspath(__file__))
LOCALE_DIR = os.path.join(APPLET_DIR, "locale")
locale.bindtextdomain("p1", LOCALE_DIR)
gettext.bindtextdomain("p1", LOCALE_DIR)
gettext.textdomain("p1")

class App():
    '''
    Manejadores de la vista
    '''

    # Boton añadir pelicula
    def on_add(self, w):
        # Obtener dialogo añadir y sus entradas de texto y spinbuttons
        danadir = self.builder.get_object("dialog1")
        titdialog = _("Añadir película")
        danadir.set_title(titdialog)

        etitulo = self.builder.get_object("entry1")
        eano = self.builder.get_object("spinbutton1")
        eduracion = self.builder.get_object("spinbutton2")
        edirector = self.builder.get_object("entry2")

        # Resetear contenido de las entradas
        etitulo.set_text("")
        eano.set_value(2000)
        eduracion.set_value(0)
        edirector.set_text("")

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
                director = edirector.get_text()

                # Comprobar que la pelicula tenga titulo
                if titulo == "":
                    # Dialogo informativo
                    self.builder.get_object("messagedialog2").run()
                    self.builder.get_object("messagedialog2").hide()
                    continue

                # Añadir pelicula al modelo
                # Si estamos en la tab de vistas, se añade como pelicula vista
                if self.builder.get_object("notebook1").get_current_page() == 1:
                    movie = Movie(titulo, ano, duracion, director, True)
                    self.builder.get_object("button10").set_sensitive(True)
                else:
                    movie = Movie(titulo, ano, duracion, director)

                # Muestra la lista antes de añadir si estaba el placeholder
                if self.model.is_empty(0):
                    self.show_movie_list()

                self.model.add_movie(movie)

                # Habilitar botones Eliminar, Editar y Recomendaciones si es necesario
                self.enable_delete_cond()
                self.enable_edit_cond()
                self.enable_rec_cond()

            break

        # Ocultar dialogo
        danadir.hide()

    # Boton eliminar pelicula
    def on_remove(self, w):
        # Averiguar en que tab estamos
        tab = self.builder.get_object("notebook1").get_current_page()
        # Obtener la pelicula seleccionada del treeview correspondiente
        movie = self.builder.get_object("treeview" + str(tab)).get_selection().get_selected()[1]

        # No hacer nada si no hay una pelicula seleccionada
        if movie == None:
            self.no_row_selected()
            return

        deliminar = self.builder.get_object("messagedialog1")

        # Preparar la label secundaria para que muestre el titulo de la pelicula
        deliminar.get_message_area().get_children()[1].set_label(self.model.get_title(movie, tab))

        # Correr el dialogo
        r = deliminar.run()

        # Si se selecciona ok
        if r == 0:
            # Comprobar si la pelicula estaba vista
            if self.model.was_viewed(movie, tab):
                self.builder.get_object("button10").set_sensitive(True)

            # Borrar la pelicula
            self.model.delete_movie(movie, tab)

            # Desactivar los botones borrar, editar y recomedaciones si procede
            self.disable_delete_cond()
            self.disable_edit_cond()
            self.disable_rec_cond()

            # Muestra el placeholder si está vacía la lista
            if self.model.is_empty(0):
                self.show_empty_placeholder()

        # Esconder dialogo
        deliminar.hide()

    # Boton añadir pelicula
    def on_edit(self, w):
        # Averiguar en que tab estamos
        tab = self.builder.get_object("notebook1").get_current_page()
        # Obtener la pelicula seleccionada del treeview correspondiente
        movie_iter = self.builder.get_object("treeview" + str(tab)).get_selection().get_selected()[1]

        # No hacer nada si no hay una pelicula seleccionada
        if movie_iter == None:
            self.no_row_selected()
            return

        # Obtener dialogo editar y sus entradas de texto y spinbuttons
        # Se reutiliza el dialogo de añadir
        deditar = self.builder.get_object("dialog1")
        titdialog = _("Editar película")
        deditar.set_title(titdialog)
        etitulo = self.builder.get_object("entry1")
        eano = self.builder.get_object("spinbutton1")
        eduracion = self.builder.get_object("spinbutton2")
        edirector = self.builder.get_object("entry2")

        # Se muestran las entradas de texto y spinbuttons con los datos originales de la pelicula
        etitulo.set_text(self.model.get_title(movie_iter, tab))
        eano.set_value(int(self.model.get_year(movie_iter, tab)))
        eduracion.set_value(int(self.model.get_duration(movie_iter, tab)))
        edirector.set_text(self.model.get_director(movie_iter, tab))

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
                director = edirector.get_text()

                # Comprobar que la pelicula tenga titulo
                if titulo == "":
                    # Dialogo informativo
                    self.builder.get_object("messagedialog2").run()
                    self.builder.get_object("messagedialog2").hide()
                    continue

                # Añadir pelicula al modelo
                movie = Movie(titulo, ano, duracion, director)
                self.model.set_movie(movie_iter, movie, tab)

            break

        # Ocultar dialogo
        deditar.hide()

    # Al cambiar de tab desactivamos o activamos botones si es necesario
    def on_change_tab(self, w, page, page_num):
        if self.model.is_empty(page_num):
            self.builder.get_object("button2").set_sensitive(False)
            self.builder.get_object("button3").set_sensitive(False)
        else:
            self.builder.get_object("button2").set_sensitive(True)
            self.builder.get_object("button3").set_sensitive(True)

    # Check de pelicula vista
    # p es el path del toggle button que se ha activado en el CellRendererToggle
    def on_viewed_toggled(self, w, p):
        iter = self.builder.get_object("treeview0").get_model().get_iter_from_string(p)
        self.model.change_movie_state(iter)
        # Activar boton de actualizar recomendaciones
        self.builder.get_object("button10").set_sensitive(True)
        # Activar boton de recomendaciones si hay vistas
        if self.model.was_viewed(iter, 0):
            self.enable_rec_cond()
        else:
            self.disable_rec_cond()

    # Boton Recomendaciones
    def on_recomendaciones(self, w):
        win = self.builder.get_object("window2")
        if win.get_visible():
            win.present()
        else:
            win.show_all()
        self.on_actualizar(None)

    # Actualizar recomendaciones
    def on_actualizar(self, w):
        self.builder.get_object("window3").show_all()
        thread = threading.Thread(target=self.actualizar)
        thread.start()

    # Cancelar actualizacion de recomendaciones
    def on_cancelar_actualizacion(self, w):
        self.mutex_cancel.acquire()
        self.cancel_update = True
        self.mutex_cancel.release()

    # Esconder un elemento de la interfaz en lugar de cerrarlo
    # e = evento que dispara el delete-event del elemento
    # devolver True es lo que evita que se destruya
    def on_hide(self, w, e):
        w.hide()
        return True

    # Cierre de aplicacion
    def on_close(self, w, e):
        self.model.save()
        Gtk.main_quit()

    # Cierre de la aplicacion por el acelerador
    def on_close_accelerator(self, a_group, w, key, mod):
        self.model.save()
        Gtk.main_quit()

    '''
    Resto de elementos de la aplicacion
    '''

    # Deshabilitar boton de eliminar si no hay peliculas
    def disable_delete_cond(self):
        tab = self.builder.get_object("notebook1").get_current_page()
        if self.model.is_empty(tab):
            self.builder.get_object("button2").set_sensitive(False)

    # Habilitar boton de eliminar si empieza a haber peliculas
    def enable_delete_cond(self):
        tab = self.builder.get_object("notebook1").get_current_page()
        if not self.model.is_empty(tab):
            self.builder.get_object("button2").set_sensitive(True)

    # Deshabilitar boton de editar si no hay peliculas
    def disable_edit_cond(self):
        tab = self.builder.get_object("notebook1").get_current_page()
        if self.model.is_empty(tab):
            self.builder.get_object("button3").set_sensitive(False)

    # Habilitar boton de editar si empieza a haber peliculas
    def enable_edit_cond(self):
        tab = self.builder.get_object("notebook1").get_current_page()
        if not self.model.is_empty(tab):
            self.builder.get_object("button3").set_sensitive(True)

    # Deshabilitar boton de recomendaciones si no hay peliculas
    def disable_rec_cond(self):
        if self.model.is_empty(1):
            self.builder.get_object("button9").set_sensitive(False)
            self.builder.get_object("button10").set_sensitive(False)

    # Activar boton de recomendaciones si no hay peliculas
    def enable_rec_cond(self):
        if not self.model.is_empty(1):
            self.builder.get_object("button9").set_sensitive(True)
            self.builder.get_object("button10").set_sensitive(True)

    # Mostrar marcador de posicion vacio
    def show_empty_placeholder(self):
        self.builder.get_object("notebook1").set_current_page(0)
        self.builder.get_object("notebook1").hide()
        self.builder.get_object("box9").show_all()

    # Mostrar lista de peliculas
    def show_movie_list(self):
        self.builder.get_object("notebook1").show_all()
        self.builder.get_object("box9").hide()

    # Actualizar lista a traves de la db
    def actualizar(self):
        # Se comprueba que hay conexión a la base de datos
        try:
            # Desactivar boton recomendaciones
            GObject.idle_add(self.builder.get_object("button9").set_sensitive, False)
            GObject.idle_add(self.builder.get_object("button10").set_sensitive, False)

            # Se añade un timming
            check = http.client.HTTPSConnection("api.themoviedb.org", timeout=1)
            check.request("GET", "/")

            # Obtener recomendaciones de la db
            GObject.idle_add(self.model.rec_list.clear)
            for movie in self.model.viewed_filter:
                # Comprobamos si hay que cancelar la actualizacion
                self.mutex_cancel.acquire()
                if self.cancel_update:
                    self.mutex_cancel.release()
                    break
                self.mutex_cancel.release()
                self.model.get_recommendations(movie[0], movie[1])

            # Desactivar la actualizacion de recomendaciones (solo si no se ha cancelado)
            self.mutex_cancel.acquire()
            if self.cancel_update:
                self.cancel_update = False
                GObject.idle_add(self.builder.get_object("button10").set_sensitive, True)
            self.mutex_cancel.release()

            # Reactivar boton recomendaciones
            GObject.idle_add(self.enable_rec_cond)

            # Esconder la ventana informativa
            GObject.idle_add(self.builder.get_object("window3").hide)

        except Exception:
            # Reactivar recomendaciones
            GObject.idle_add(self.builder.get_object("button10").set_sensitive, True)
            GObject.idle_add(self.enable_rec_cond)
            # Se muestra que no hay conexión
            GObject.idle_add(self.internet_connection_error)

    # Informar al usuario de que debe seleccionar una fila
    def no_row_selected(self):
        dialog = self.builder.get_object("messagedialog3")
        dialog.run()
        dialog.hide()

    # Infomar al usuario de que se ha producido un error en la conexión
    def internet_connection_error(self):
            self.builder.get_object("window3").hide()
            self.builder.get_object("messagedialog4").run()
            self.builder.get_object("messagedialog4").hide()
            self.builder.get_object("window2").hide()

    def __init__(self):
        # Se crea el modelo
        self.model = ModelMovie()

        # Se crean las vistas
        self.builder = Gtk.Builder()

        # Dominio de traduccion
        self.builder.set_translation_domain("p1")
        self.builder.add_from_file(os.path.join(APPLET_DIR, "view.glade"))
        self.builder.connect_signals(self)

        # Establecer modelos en los treeview
        self.builder.get_object("treeview0").set_model(self.model.listmodel)
        self.builder.get_object("treeview1").set_model(self.model.viewed_filter)
        self.builder.get_object("treeview2").set_model(self.model.pending_filter)
        self.builder.get_object("treeview3").set_model(self.model.rec_list)

        # Variable y mutex para saber si se tiene que cancelar la actualizacion
        self.cancel_update = False
        self.mutex_cancel = threading.Lock()

        # Desactivar Eliminar, Editar y Recomendaciones si es necesario
        self.disable_delete_cond()
        self.disable_edit_cond()

        # Atajo de teclado
        accel = self.builder.get_object("accelgroup1")
        key_e, mod_e = Gtk.accelerator_parse("<Control>q")
        accel.connect(key_e, mod_e, Gtk.AccelFlags.VISIBLE, self.on_close_accelerator)

        mainwin = self.builder.get_object("window1")
        mainwin.show_all()

        if self.model.is_empty(0):
            self.show_empty_placeholder()
        else:
            self.show_movie_list()

# Ejecucion
App()
Gtk.main()
