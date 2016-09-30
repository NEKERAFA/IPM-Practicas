import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

class ListMovie():
    '''
    Clase del modelo con la lista de películas
    '''

    # Crear ListStore
    def __init__(self):
        self.listmodel = Gtk.ListStore(str, str, str, str)
        for i in range(len(movies)):
            self.listmodel.append(movies[i])

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

# Datos para ListStore
movies = [["I, Robot", "2010", "150", "Ciencia Ficción"],
          ["Heidi", "1999", "200", "Animación"]]
