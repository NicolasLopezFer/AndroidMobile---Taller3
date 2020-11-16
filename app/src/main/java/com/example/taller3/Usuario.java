package com.example.taller3;

public class Usuario {

    private String name;
    private String email;
    private String apellido;
    private int identificacion;
    private long latitud;
    private long longitud;

    public Usuario(String name, String email, String apellido, int identificacion, long latitud, long longitud) {
        this.name = name;
        this.email = email;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(int identificacion) {
        this.identificacion = identificacion;
    }

    public long getLatitud() {
        return latitud;
    }

    public void setLatitud(long latitud) {
        this.latitud = latitud;
    }

    public long getLongitud() {
        return longitud;
    }

    public void setLongitud(long longitud) {
        this.longitud = longitud;
    }
}
