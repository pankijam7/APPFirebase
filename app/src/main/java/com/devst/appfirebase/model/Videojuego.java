package com.devst.appfirebase.model;

// Clase modelo que representa la estructura de cada documento en Cloud Firestore
public class Videojuego {
    private String id;
    private String titulo;
    private String plataforma;
    private String genero;
    private String uidUsuario; // Guarda el ID único del usuario dueño de este registro

    // Constructor vacío obligatorio: Firestore lo necesita para convertir documentos en objetos Java
    public Videojuego() {
    }

    public Videojuego(String id, String titulo, String plataforma, String genero, String uidUsuario) {
        this.id = id;
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.genero = genero;
        this.uidUsuario = uidUsuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }
}