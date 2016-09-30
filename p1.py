import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

# Estructura para ListStore
columns = ["Título", "Año", "Duración", "Género"]
movies = [["I, Robot", "2004", "115", "Ciencia Ficción"],
          ["Heidi", "1999", "500", "Animación"],
          ["Heidi", "1999", "500", "Animación"],
          ["Heidi", "1999", "500", "Animación"],
          ["Heidi", "1999", "500", "Animación"]]

# Crear ListStore
listmodel = Gtk.ListStore(str, str, str, str)
for i in range(len(movies)):
    listmodel.append(movies[i])

class MainWindow(Gtk.Window):
    def __init__(self):
        Gtk.Window.__init__(self, title="Main")
        self.set_border_width(10)
        # (hijos homogeneos, espaciado entre hijos)
        self.box1 = Gtk.VBox(False, 10)
        self.treeview = Gtk.TreeView(model = listmodel)
        #self.treeview.set_vadjustment(Gtk.Adjustment(value = 50, lower = 50, upper = 50))
        # Establecer columnas
        for i in range(len(columns)):
            # Cellrenderer de texto
            cell = Gtk.CellRendererText()
            # (nombreColumna, cellRenderer, text=numero de la columna del modelo de la que tiene que tomar el texto)
            col = Gtk.TreeViewColumn(columns[i], cell, text=i)
            col.set_expand(True)
            self.treeview.append_column(col)

        # Señal emitida cuando se selecciona una fila
        # treeview.get_selection().connect("changed", self.on_changed)

        # Añadir treeview al Vbox mediante ScrolledWindow
        self.swin = Gtk.ScrolledWindow()
        self.swin.add_with_viewport(self.treeview)
        self.swin.set_min_content_height(100)
        self.swin.set_min_content_width(200)
        # (elemento, expandir, rellenar, espaciado)
        self.box1.pack_start(self.swin, True, True, 0)
        #self.box1.pack_start(self.treeview, True, True, 0)

        # Crear HBox para los botones
        self.box2 = Gtk.HBox(True, 35)

        # Botones
        image = Gtk.Image(stock = Gtk.STOCK_ADD)
        self.b1 = Gtk.Button(label = "Añadir", image = image)
        image = Gtk.Image(stock = Gtk.STOCK_DELETE)
        self.b2 = Gtk.Button(label = "Eliminar", image = image)
        image = Gtk.Image(stock = Gtk.STOCK_EDIT)
        self.b3 = Gtk.Button(label = "Editar", image = image)

        # Meter botones en HBox
        self.box2.pack_start(self.b1, True, True, 0)
        self.box2.pack_start(self.b2, True, True, 0)
        self.box2.pack_start(self.b3, True, True, 0)

        # Meter box2 en box1
        self.box1.pack_start(self.box2, False, True, 0)

        # Añadir box a la ventana
        self.add(self.box1)

        # Comportamiento de los botones
        self.b1.connect("clicked", self.on_anadir)

    def on_anadir(self, w):
        # Crear ventana añadir
        anadir = AnadirDialog(self)
        anadir.run()
        anadir.destroy()

class AnadirDialog(Gtk.Dialog):
    def __init__(self, parent):
        Gtk.Dialog.__init__(self, "Añadir", parent, None,
            (Gtk.STOCK_OK, Gtk.ResponseType.OK,
             Gtk.STOCK_CANCEL, Gtk.ResponseType.CANCEL))

        self.set_transient_for(parent)
        self.set_modal(True)

        # Se le añade un Gril
        self.grid = Gtk.Grid()

        # Labels
        self.label1 = Gtk.Label("Título")
        self.label2 = Gtk.Label("Año")
        self.label3 = Gtk.Label("Duración")
        self.label4 = Gtk.Label("Género")

        # Entries y spinbutton
        self.entry1 = Gtk.Entry()
        self.entry2 = Gtk.Entry()

        sadj = Gtk.Adjustment(lower = 1900, upper = 2038, step_incr = 1)

        self.sbutton1 = Gtk.SpinButton(adjustment = sadj, climb_rate = 1)
        self.sbutton1.set_value(2000)
        self.sbutton2 = Gtk.SpinButton(adjustment = sadj, climb_rate = 1)
        self.sbutton2.set_value(2000)

        # Se añaden los elementos al Grid
        self.grid.attach(self.label1, 0, 0, 1, 1)
        self.grid.attach(self.label2, 0, 1, 1, 1)
        self.grid.attach(self.label3, 0, 2, 1, 1)
        self.grid.attach(self.label4, 0, 3, 1, 1)
        self.grid.attach(self.entry1, 1, 0, 1, 1)
        self.grid.attach(self.sbutton1, 1, 1, 1, 1)
        self.grid.attach(self.sbutton2, 1, 2, 1, 1)
        self.grid.attach(self.entry2, 1, 3, 1, 1)

        box = self.get_content_area()
        box.pack_start(self.grid, True, True, 0)
        self.show_all()

w = MainWindow()
w.connect("delete-event", Gtk.main_quit)
w.show_all()
Gtk.main()
