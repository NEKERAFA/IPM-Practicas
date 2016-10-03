# coding=utf-8

import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

import io
import os
import json

APPLET_DIR = os.path.dirname(os.path.abspath(__file__))
JSON_FILE = os.path.join(APPLET_DIR, 'movies.json')

class Movie():

    # Crear pelicula
    def __init__(self, title, year, duration, director):
        self.title = title
        self.year = year
        self.duration = duration
        self.director = director

    # Obtener titulo de una pelicula
    def get_title(self):
        return self.title

    # Obtener año de una pelicula
    def get_year(self):
        return self.year

    # Obtener duración de una pelicula
    def get_duration(self):
        return self.duration

    # Obtener género de una pelicula
    def get_director(self):
        return self.director

class ListMovie():
    '''
    Clase del modelo con la lista de películas
    '''

    # Crear ListStore
    def __init__(self):
        self.listmodel = Gtk.ListStore(str, str, str, str)
        with io.open(JSON_FILE, 'r', encoding='utf8') as json_fd:
            try:
                movies = json.load(json_fd)
            except ValueError:
                movies = []
        for i in range(len(movies)):
            self.listmodel.append([movies[i]["title"], movies[i]["year"], movies[i]["duration"], movies[i]["director"]])
        json_fd.close()

    # Añadir pelicula a la lista
    def add_movie(self, movie):
        self.listmodel.append([movie.get_title(), movie.get_year(), movie.get_duration(), movie.get_director()])

    # Eliminar pelicula de la lista
    def delete_movie(self, iter):
        self.listmodel.remove(iter)

    # Reescribe una película de la lista
    def set_movie(self, iter, movie):
        self.listmodel.set_value(iter, 0, movie.get_title())
        self.listmodel.set_value(iter, 1, movie.get_year())
        self.listmodel.set_value(iter, 2, movie.get_duration())
        self.listmodel.set_value(iter, 3, movie.get_director())

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
    def get_director(self, iter):
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
                'director': self.get_director(i)
            })

        # Guardamos el diccionario como archivo json
        with io.open(JSON_FILE, 'w+', encoding='utf8') as json_fd:
            json_fd.write(json.dumps(movies, indent=4, sort_keys=True, ensure_ascii=False))
            json_fd.flush()
            json_fd.close()
