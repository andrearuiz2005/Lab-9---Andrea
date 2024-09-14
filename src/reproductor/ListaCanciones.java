package reproductor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListaCanciones implements Serializable {
    private List<Cancion> canciones;

    public ListaCanciones() {
        canciones = new ArrayList<>();
    }

    public void agregarCancion(Cancion cancion) {
        canciones.add(cancion);
    }

    public Cancion obtenerCancion(int indice) {
        return canciones.get(indice);
    }

    public void eliminarCancion(int indice) {
        canciones.remove(indice);
    }

    public int obtenerTamano() {
        return canciones.size();
    }

    public List<Cancion> obtenerListaCompleta() {
        return canciones;
    }
}
