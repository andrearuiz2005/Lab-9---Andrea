package reproductor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.filechooser.FileFilter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import com.mpatric.mp3agic.*;
import java.util.ArrayList;

public class ReproductorMusicalConListaEnlazada extends JFrame {
    private JButton botonReproducir, botonDetener, botonPausar, botonAgregar, botonSeleccionar, botonGuardar, botonEliminar;
    private ListaCanciones listaCanciones;
    private JLabel etiquetaImagenCancion, etiquetaNombreCancion, etiquetaArtista, etiquetaDuracion, etiquetaTipoMusica;
    private Player reproductor;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private Cancion cancionActual;
    private long pausaPosicion = 0;
    private boolean pausado = false;
    private DefaultListModel<String> modeloLista;

    public ReproductorMusicalConListaEnlazada() {
        setTitle("Reproductor de Música");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        modeloLista = new DefaultListModel<>();
        listaCanciones = new ListaCanciones();

        Font fuenteTitulo = new Font("Arial", Font.BOLD, 16);
        Font fuenteTexto = new Font("Arial", Font.PLAIN, 14);

        JList<String> listaCancionesUI = new JList<>(modeloLista);
        listaCancionesUI.setFont(fuenteTexto);
        listaCancionesUI.setBackground(new Color(230, 240, 255));
        JScrollPane panelListaCanciones = new JScrollPane(listaCancionesUI);
        panelListaCanciones.setBorder(BorderFactory.createTitledBorder("Lista de Canciones"));

        etiquetaImagenCancion = new JLabel();
        etiquetaImagenCancion.setPreferredSize(new Dimension(250, 250));
        etiquetaImagenCancion.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Al inicio del programa, limpiar la imagen y mostrar texto
        etiquetaImagenCancion.setIcon(null);  
        etiquetaImagenCancion.setText("No hay imagen disponible");  

        etiquetaNombreCancion = new JLabel("Nombre: ");
        etiquetaArtista = new JLabel("Artista: ");
        etiquetaDuracion = new JLabel("Duración: ");
        etiquetaTipoMusica = new JLabel("Tipo de música: ");

        JPanel panelDetalles = new JPanel();
        panelDetalles.setLayout(new GridLayout(4, 1));
        panelDetalles.setBorder(BorderFactory.createTitledBorder("Detalles de la Canción"));
        panelDetalles.add(etiquetaNombreCancion);
        panelDetalles.add(etiquetaArtista);
        panelDetalles.add(etiquetaDuracion);
        panelDetalles.add(etiquetaTipoMusica);

        botonReproducir = new JButton("Reproducir");
        botonReproducir.setBackground(new Color(50, 205, 50));
        botonReproducir.setFont(fuenteTitulo);

        botonDetener = new JButton("Detener");
        botonDetener.setBackground(new Color(255, 69, 0));
        botonDetener.setFont(fuenteTitulo);

        botonPausar = new JButton("Pausar");
        botonPausar.setBackground(new Color(255, 215, 0));
        botonPausar.setFont(fuenteTitulo);

        botonAgregar = new JButton("Agregar Canción");
        botonAgregar.setBackground(new Color(70, 130, 180));
        botonAgregar.setFont(fuenteTitulo);

        botonSeleccionar = new JButton("Seleccionar Canción");
        botonSeleccionar.setBackground(new Color(255, 165, 0));
        botonSeleccionar.setFont(fuenteTitulo);

        botonGuardar = new JButton("Guardar Lista");
        botonGuardar.setBackground(new Color(100, 149, 237));
        botonGuardar.setFont(fuenteTitulo);

        botonEliminar = new JButton("Eliminar Canción");
        botonEliminar.setBackground(new Color(255, 69, 0));
        botonEliminar.setFont(fuenteTitulo);

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(1, 7, 10, 0));
        panelBotones.add(botonReproducir);
        panelBotones.add(botonDetener);
        panelBotones.add(botonPausar);
        panelBotones.add(botonAgregar);
        panelBotones.add(botonSeleccionar);
        panelBotones.add(botonGuardar);
        panelBotones.add(botonEliminar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(panelListaCanciones, BorderLayout.CENTER);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelDetalles, BorderLayout.CENTER);
        panelDerecho.add(etiquetaImagenCancion, BorderLayout.SOUTH);
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLayout(new BorderLayout());
        add(panelCentral, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        add(panelDerecho, BorderLayout.EAST);

        cargarCancionesGuardadas();

        botonAgregar.addActionListener(e -> {
            JFileChooser fileChooserCancion = new JFileChooser();
            fileChooserCancion.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".mp3") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Archivos MP3";
                }
            });

            int resultadoCancion = fileChooserCancion.showOpenDialog(null);
            if (resultadoCancion == JFileChooser.APPROVE_OPTION) {
                File archivoCancion = fileChooserCancion.getSelectedFile();

                try {
                    Mp3File mp3file = new Mp3File(archivoCancion.getAbsolutePath());
                    String nombreCancion = mp3file.hasId3v2Tag() && mp3file.getId3v2Tag().getTitle() != null
                            ? mp3file.getId3v2Tag().getTitle() : archivoCancion.getName();
                    String duracion = String.format("%d:%02d mins", mp3file.getLengthInSeconds() / 60, mp3file.getLengthInSeconds() % 60);

                    JTextField campoArtista = new JTextField();
                    String[] opcionesTipoMusica = {"Romántica", "Clásica", "Regional Mexicano", "Rock", "Reguetón", "Pop"};
                    JComboBox<String> comboTipoMusica = new JComboBox<>(opcionesTipoMusica);
                    JButton botonSeleccionarImagen = new JButton("Seleccionar Imagen");
                    final File[] imagenSeleccionada = new File[1];

                    botonSeleccionarImagen.addActionListener(ev -> {
                        JFileChooser fileChooserImagen = new JFileChooser();
                        fileChooserImagen.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.getName().toLowerCase().endsWith(".jpg") || f.isDirectory();
                            }

                            @Override
                            public String getDescription() {
                                return "Archivos JPG";
                            }
                        });

                        int resultadoImagen = fileChooserImagen.showOpenDialog(null);
                        if (resultadoImagen == JFileChooser.APPROVE_OPTION) {
                            imagenSeleccionada[0] = fileChooserImagen.getSelectedFile();
                        }
                    });

                    Object[] mensaje = {
                        "Nombre de la canción:", nombreCancion,
                        "Artista:", campoArtista,
                        "Duración:", duracion,
                        "Tipo de música:", comboTipoMusica,
                        botonSeleccionarImagen
                    };

                    int opcion = JOptionPane.showConfirmDialog(null, mensaje, "Agregar Canción", JOptionPane.OK_CANCEL_OPTION);
                    if (opcion == JOptionPane.OK_OPTION) {
                        String artista = campoArtista.getText();
                        String tipoMusica = (String) comboTipoMusica.getSelectedItem();

                        File carpetaImagenes = new File("imagenes_canciones");
                        if (!carpetaImagenes.exists()) {
                            carpetaImagenes.mkdir();
                        }

                        String rutaImagen = null;
                        if (imagenSeleccionada[0] != null) {
                            File destinoImagen = new File(carpetaImagenes, imagenSeleccionada[0].getName());
                            rutaImagen = destinoImagen.getAbsolutePath();

                            try {
                                Files.copy(imagenSeleccionada[0].toPath(), destinoImagen.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        Cancion nuevaCancion = new Cancion(nombreCancion, artista, duracion, tipoMusica, rutaImagen, archivoCancion);
                        listaCanciones.agregarCancion(nuevaCancion);
                        modeloLista.addElement(nuevaCancion.getInfoFormato());

                        etiquetaNombreCancion.setText("Nombre: " + nombreCancion);
                        etiquetaArtista.setText("Artista: " + artista);
                        etiquetaDuracion.setText("Duración: " + duracion);
                        etiquetaTipoMusica.setText("Tipo de música: " + tipoMusica);

                        if (rutaImagen != null) {
                            etiquetaImagenCancion.setIcon(new ImageIcon(rutaImagen));
                            etiquetaImagenCancion.setText(""); // Eliminar el texto si hay imagen
                        } else {
                            etiquetaImagenCancion.setText("No hay imagen disponible.");
                            etiquetaImagenCancion.setIcon(null); // Limpiar imagen si no hay
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Método para seleccionar y mostrar la canción
        botonSeleccionar.addActionListener(e -> {
            int indiceSeleccionado = listaCancionesUI.getSelectedIndex();
            if (indiceSeleccionado != -1) {
                cancionActual = listaCanciones.obtenerCancion(indiceSeleccionado);

                // Mostramos los detalles de la canción en la parte derecha
                etiquetaNombreCancion.setText("Nombre: " + cancionActual.getNombre());
                etiquetaArtista.setText("Artista: " + cancionActual.getArtista());
                etiquetaDuracion.setText("Duración: " + cancionActual.getDuracion());
                etiquetaTipoMusica.setText("Tipo de música: " + cancionActual.getTipo());

                // Mostramos la imagen si existe
                if (cancionActual.getRutaImagen() != null && !cancionActual.getRutaImagen().isEmpty()) {
                    File archivoImagen = new File(cancionActual.getRutaImagen());
                    if (archivoImagen.exists()) {
                        ImageIcon iconoOriginal = new ImageIcon(cancionActual.getRutaImagen());

                        // Ajustamos la imagen al tamaño del JLabel
                        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(
                            etiquetaImagenCancion.getWidth(),
                            etiquetaImagenCancion.getHeight(),
                            Image.SCALE_SMOOTH
                        );

                        etiquetaImagenCancion.setIcon(new ImageIcon(imagenEscalada));
                        etiquetaImagenCancion.setText(""); // Borrar texto si existía
                    } else {
                        etiquetaImagenCancion.setIcon(null);
                        etiquetaImagenCancion.setText("No hay imagen disponible.");
                        System.out.println("La imagen no se encuentra en: " + cancionActual.getRutaImagen());
                    }
                } else {
                    etiquetaImagenCancion.setIcon(null);
                    etiquetaImagenCancion.setText("No hay imagen disponible.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Por favor selecciona una canción de la lista.");
            }
        });

        botonReproducir.addActionListener(e -> {
            if (cancionActual != null) {
                try {
                    if (pausado) {
                        fis = new FileInputStream(cancionActual.getArchivo());
                        fis.skip(pausaPosicion);
                        bis = new BufferedInputStream(fis);
                        reproductor = new Player(bis);
                        pausado = false;
                    } else {
                        fis = new FileInputStream(cancionActual.getArchivo());
                        bis = new BufferedInputStream(fis);
                        reproductor = new Player(bis);
                    }

                    new Thread(() -> {
                        try {
                            reproductor.play();
                        } catch (JavaLayerException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "No se ha seleccionado ninguna canción.");
            }
        });

        botonPausar.addActionListener(e -> {
            if (reproductor != null) {
                try {
                    pausaPosicion = fis.available();
                    reproductor.close();
                    pausado = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        botonDetener.addActionListener(e -> {
            if (reproductor != null) {
                reproductor.close();
                pausaPosicion = 0;
            }
        });

        botonGuardar.addActionListener(e -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("lista_canciones.dat"))) {
                oos.writeObject(listaCanciones.obtenerListaCompleta());
                JOptionPane.showMessageDialog(null, "Lista de canciones guardada correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la lista.");
                ex.printStackTrace();
            }
        });

        botonEliminar.addActionListener(e -> {
            int indiceSeleccionado = listaCancionesUI.getSelectedIndex();
            if (indiceSeleccionado != -1) {
                listaCanciones.eliminarCancion(indiceSeleccionado);
                modeloLista.remove(indiceSeleccionado);

                // Limpiar detalles de la canción y la imagen al eliminar
                etiquetaNombreCancion.setText("Nombre: ");
                etiquetaArtista.setText("Artista: ");
                etiquetaDuracion.setText("Duración: ");
                etiquetaTipoMusica.setText("Tipo de música: ");
                etiquetaImagenCancion.setIcon(null);
                etiquetaImagenCancion.setText("No hay imagen disponible.");
                
                JOptionPane.showMessageDialog(null, "Canción eliminada correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor selecciona una canción para eliminar.");
            }
        });

        setVisible(true);
    }

    private void cargarCancionesGuardadas() {
        File archivoCanciones = new File("lista_canciones.dat");
        if (!archivoCanciones.exists()) {
            System.out.println("El archivo lista_canciones.dat no existe.");
            return; // Salir si el archivo no existe
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivoCanciones))) {
            var cancionesGuardadas = (ArrayList<Cancion>) ois.readObject();
            for (Cancion cancion : cancionesGuardadas) {
                listaCanciones.agregarCancion(cancion);
                modeloLista.addElement(cancion.getInfoFormato());

                if (cancion.getRutaImagen() != null && !cancion.getRutaImagen().isEmpty()) {
                    File archivoImagen = new File(cancion.getRutaImagen());
                    if (archivoImagen.exists()) {
                        etiquetaImagenCancion.setIcon(new ImageIcon(cancion.getRutaImagen()));
                    } else {
                        etiquetaImagenCancion.setIcon(null);
                        System.out.println("La imagen no se encuentra en: " + cancion.getRutaImagen());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "No se pudo cargar la lista de canciones guardada.");
        }
    }

    public static void main(String[] args) {
        try {
            new ReproductorMusicalConListaEnlazada();
        } catch (Exception ex) {
            ex.printStackTrace(); // Muestra la traza completa del error en la consola
            JOptionPane.showMessageDialog(null, "Error inesperado: " + ex.getMessage());
        }
    }
}
