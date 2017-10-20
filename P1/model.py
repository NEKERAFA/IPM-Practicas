# coding=utf-8

import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk, GObject

import io
import os
import json
import http.client
import locale

# Ruta del archivo JSON
JSON_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'movies.json')

class ListNotFound(Exception):
    '''
        Excepción cuando se introduce una lista no valida
    '''
    pass

class Movie():
    '''
        Clase con la película
    '''

    # Crear pelicula
    def __init__(self, title, year, duration, director, viewed = False):
        self.title = title
        self.year = year
        self.duration = duration
        self.director = director
        self.viewed = viewed

    # Obtener titulo de una pelicula
    def get_title(self):
        return self.title

    # Obtener año de una pelicula
    def get_year(self):
        return self.year

    # Obtener duración de una pelicula
    def get_duration(self):
        return self.duration

    # Obtener director de una pelicula
    def get_director(self):
        return self.director

    # Obtener si la película fue vista
    def was_viewed(self):
        return self.viewed

class ModelMovie():
    '''
        Clase del modelo con la lista de películas
    '''

    # Crear ListStore
    def __init__(self):
        self.listmodel = Gtk.ListStore(str, str, str, str, bool)
        self.viewed_filter = self.listmodel.filter_new()
        self.viewed_filter.set_visible_func(self.show_viewed)
        self.pending_filter = self.listmodel.filter_new()
        self.pending_filter.set_visible_func(self.show_pending)
        self.rec_list = Gtk.ListStore(str, str)
        self.API_KEY = "319b2a220cae741a5e4195dd7278ad5a"

        with io.open(JSON_FILE, 'r', encoding='utf-8') as json_fd:
            try:
                movies = json.load(json_fd)
            except ValueError:
                movies = []
        for i in range(len(movies)):
            self.listmodel.append([movies[i]["title"], movies[i]["year"], movies[i]["duration"], movies[i]["director"], movies[i]["viewed"]])
        json_fd.close()

    # Añade a la lista de Recomendaciones
    def add_movie_to_rec(self, title, year):
        self.rec_list.append([title, year])
        return False

    # Buscar id de una pelicula introducida por el titulo
    def search_id(self, title, year):
        # Valor que se retorna
        id = ""

        # Conexion con la db
        connection = http.client.HTTPSConnection("api.themoviedb.org")

        # Cabeceras
        payload = "{}"
        header = {'content-type': "application/json"}
        lang = locale.getlocale()[0].replace("_", "-")
        urltitle = title.replace(" ", "%20")

        # Se hace una petición
        connection.request("GET", "/3/search/movie?query=" + urltitle + "&language=" + lang + "&year=" + year + "&api_key=" + self.API_KEY)
        # Se obtiene la respuesta
        res = connection.getresponse()
        data = res.read()

        # Se convierte el JSON en un dict
        try:
            movies = json.loads(data.decode("utf-8"))
        except ValueError:
            movies = []

        # Se comprueba que no haya ningún error en el data recibido (si no lo hay, no hay campo status_code)
        if "status_code" in movies:
            # Se ignora la pelicula
            pass
        else:
            # Se recorren los resultados y se meten en la lista
            for result in movies["results"]:
                if ((result["title"].lower() == title.lower()) or (result["original_title"].lower() == title.lower())) and (result["release_date"][0:4] == year):
                    id = str(result["id"])
                    break

        # Cerramos la conexion
        connection.close()

        # Devolvemos el ID
        return id

    # Obtiene las recomendaciones de una película
    def get_recommendations(self, title, year):
        # Se obtiene primero el id de la película
        movie_id = self.search_id(title, year)

        # Conexion con la db
        connection = http.client.HTTPSConnection("api.themoviedb.org")

        # Cabeceras
        payload = "{}"
        header = {'content-type': "application/json"}
        lang = locale.getlocale()[0].replace("_", "-")

        # Se hace una petición
        connection.request("GET", "/3/movie/" + movie_id + "/recommendations?language=" + lang + "&api_key=" + self.API_KEY)
        # Se obtiene la respuesta
        res = connection.getresponse()
        data = res.read()

        # Se convierte el JSON en un dict
        recommendations = json.loads(data.decode("utf-8"))

        # Se comprueba que no haya ningún error en el data recibido (si no lo hay, no hay campo status_code)
        if "status_code" in recommendations:
            # Se ignora la pelicula
            pass
        else:
            # Se recorren los resultados y se meten en la lista
            for result in recommendations["results"]:
                GObject.idle_add(self.add_movie_to_rec, result["title"], result["release_date"])

        # Cerramos la conexion
        connection.close()

    # Añadir pelicula a la lista
    def add_movie(self, movie):
        self.listmodel.append([movie.get_title(), movie.get_year(), movie.get_duration(), movie.get_director(), movie.was_viewed()])

    # Eliminar pelicula de la lista
    def delete_movie(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        self.listmodel.remove(iter)

    # Reescribe una película de la lista
    def set_movie(self, iter, movie, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        self.listmodel.set_value(iter, 0, movie.get_title())
        self.listmodel.set_value(iter, 1, movie.get_year())
        self.listmodel.set_value(iter, 2, movie.get_duration())
        self.listmodel.set_value(iter, 3, movie.get_director())

    # Cambia una pelicula vista a no vista y viceversa
    def change_movie_state(self, iter):
        self.listmodel.set_value(iter, 4, not self.was_viewed(iter, 0))

    # Obtener titulo de una pelicula
    def get_title(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        return self.listmodel[iter][0]

    # Obtener año de una pelicula
    def get_year(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        return self.listmodel[iter][1]

    # Obtener duración de una pelicula
    def get_duration(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        return self.listmodel[iter][2]

    # Obtener género de una pelicula
    def get_director(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        return self.listmodel[iter][3]

    # Saber si una pelicula se ha visto
    def was_viewed(self, iter, list):
        if list == 1:
            iter = self.viewed_filter.convert_iter_to_child_iter(iter)
        elif list == 2:
            iter = self.pending_filter.convert_iter_to_child_iter(iter)
        elif list != 0:
            raise ListNotFound

        return self.listmodel[iter][4]

    # Saber si la lista esta vacia
    def is_empty(self, list):
        if list == 0:
            current_list = self.listmodel
        elif list == 1:
            current_list = self.viewed_filter
        elif list == 2:
            current_list = self.pending_filter

        return (len(current_list) == 0)

    # Funcion de filtrado: mostrar solo las peliculas vistas
    def show_viewed(self, model, iter, data):
        return model[iter][4]

    # Funcion de filtrado: mostrar solo las peliculas no vistas
    def show_pending(self, model, iter, data):
        return not model[iter][4]

    # Sobreescribe el fichero de películas
    def save(self):
        # Creamos el diccionario con la listmodel
        movies = []
        for i in range(len(self.listmodel)):
            movies.append({
                'title': self.get_title(i, 0),
                'year': self.get_year(i, 0),
                'duration': self.get_duration(i, 0),
                'director': self.get_director(i, 0),
                'viewed': self.was_viewed(i, 0)
            })

        # Guardamos el diccionario como archivo json
        with io.open(JSON_FILE, 'w+', encoding='utf8') as json_fd:
            json_fd.write(json.dumps(movies, indent=4, sort_keys=True, ensure_ascii=False))
            json_fd.flush()
            json_fd.close()
