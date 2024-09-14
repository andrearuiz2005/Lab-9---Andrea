package reproductor;


import java.io.File;
import java.io.Serializable;

public class Cancion implements Serializable {
    private String nombre;
    private String artista;
    private String duracion;
    private String tipo;
    private String rutaImagen;
    private File archivo;

    public Cancion(String nombre, String artista, String duracion, String tipo, String rutaImagen, File archivo) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.tipo = tipo;
        this.rutaImagen = rutaImagen;
        this.archivo = archivo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public File getArchivo() {
        return archivo;
    }

    public String getInfoFormato() {
        return nombre + " - " + artista;
    }
}

