package com.example.taller3.Model;

import android.graphics.Bitmap;

public class Usuario {

    private String uid;
    private String nombre;
    private String apellido;
    private String identificacion;
    private Bitmap imagenUsuario;

    public Usuario(){}


    public Usuario(String uid, String email, String apellido, String identificacion) {
        this.uid = uid;
        this.nombre = email;
        this.apellido = apellido;
        this.identificacion = identificacion;
    }

    public Usuario(String uid,String nombre, String email, String apellido, String identificacion) {
        this.uid = uid;
        this.nombre = nombre;
        this.nombre = email;
        this.apellido = apellido;
        this.identificacion = identificacion;
    }

    public Usuario(String uid, String email, String apellido, String identificacion, Bitmap imagen) {
        this.uid = uid;
        this.nombre = email;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.imagenUsuario = imagen;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public Bitmap getImagenUsuario() {
        return imagenUsuario;
    }

    public void setImagenUsuario(Bitmap imagenUsuario) {
        this.imagenUsuario = imagenUsuario;
    }
}
