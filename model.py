# coding=utf-8

import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

import io
import os
import json

APPLET_DIR = os.path.dirname(os.path.abspath(__file__))
JSON_FILE = os.path.join(APPLET_DIR, 'movies.json')

class ListMovie():
    '''
    Clase del modelo con la lista de películas
    '''

    # Crear ListStore
    def __init__(self):
        self.listmodel = Gtk.ListStore(str, str, str, str)
        with io.open(JSON_FILE, 'r', encoding='utf8') as json_fd:
            movies = json.load(json_fd, indent=4, sort_keys=True)
        for i in range(len(movies)):
            self.listmodel.append([movies[i]["title"], movies[i]["year"], movies[i]["duration"], movies[i]["genre"]])
        json_fd.close()

    # Añadir pelicula a la lista
    def add_movie(self, movie):
        self.listmodel.append(movie)

    # Eliminar pelicula de la lista
    def delete_movie(self, iter):
        self.listmodel.remove(iter)

    # Reescribe una película de la lista
    def set_movie(self, iter, movie):
        for i in range(len(movie)):
            self.listmodel.set_value(iter, i, movie[i])

    # Obtener titulo de una pelicula
    def get_title(self, iter):
        return self.listmodel[iter][0]

    # Obtener año de una pelicula
    def get_year(self, iter):
        return self.listmodel[iter][1]

    # Obtener duración de una pelicula
    def get_duration(self, iter):
        return self.listmodel[iter][2]

    # Obtener género de una pelicula
    def get_genre(self, iter):
        return self.listmodel[iter][3]

    # Saber si la lista esta vacia
    def is_empty(self):
        return (len(self.listmodel) == 0)

    # Sobreescribe el fichero de películas
    def save(self):
        # Creamos el diccionario con la listmodel
        movies = []
        for i in range(len(self.listmodel)):
            movies.append({
                'title': self.get_title(i),
                'year': self.get_year(i),
                'duration': self.get_duration(i),
                'genre': self.get_genre(i)
            })

        # Guardamos el diccionario como archivo json
        with io.open(JSON_FILE, 'w+', encoding='utf8') as json_fd:
            json_fd.write(json.dumps(movies))
            json_fd.flush()
            json_fd.close()


# Datos para ListStore
movies = [["I, Robot", "2010", "150", "Ciencia Ficción"],
          ["Heidi", "1999", "200", "Animación"]]
