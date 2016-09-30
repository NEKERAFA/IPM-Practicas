import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

# Datos para ListStore
movies = [["I, Robot", "2010", "150", "Ciencia Ficción"],
          ["Heidi", "1999", "200", "Animación"]] 

# Crear ListStore
listmodel = Gtk.ListStore(str, str, str, str)
for i in range(len(movies)):
    listmodel.append(movies[i])

# Añadir pelicula a la lista
def add_movie(movie):
    listmodel.append(movie)

# Eliminar pelicula de la lista
def delete_movie(iter):
    listmodel.remove(iter)

# Obtener titulo de una pelicula
def get_title(iter):
    return listmodel[iter][0]

# Obtener año de una pelicula
def get_year(iter):
    return listmodel[iter][1]

# Saber si la lista esta vacia
def is_empty():
    return (len(listmodel) == 0)
