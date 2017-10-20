import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

class MyWindow(Gtk.Window):
    def __init__(self):
        Gtk.Window.__init__(self, title='Hello World!')
        self.button = Gtk.Button(label='Press me!')
        self.button.connect('clicked', self.on_button_clicked)
        self.add(self.button)

    def on_button_clicked(self, widget):
        win = Gtk.Window(title='Hola Mundo!')
        label = Gtk.Label()
        label.set_markup('<span size="32000"><b>Hola Mundo!</b></span>')
        win.add(label)
        win.show_all()

win = MyWindow()
win.connect('delete-event', Gtk.main_quit)
win.show_all()
Gtk.main()
