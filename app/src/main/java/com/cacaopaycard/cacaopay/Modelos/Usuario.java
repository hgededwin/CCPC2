package com.cacaopaycard.cacaopay.Modelos;

import android.content.Context;
import android.content.SharedPreferences;

public class Usuario {

    private String telefono;
    private String correo;
    private String nombreUsuario;
    private String birthDate;
    private String clabe;
    private String urlAvatar;
    private String token;

    private String numTarjetaInicial;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public Usuario(Context context){
        preferences = context.getSharedPreferences("cuenta", Context.MODE_PRIVATE);
        editor = preferences.edit();

    }

    public String getTelefono(){
        return  preferences.getString("telefono","");
    }

    public String getNombreUsuario(){
        return preferences.getString("nombre_usuario","");
    }

    public String getClabe(){
        return  preferences.getString("clabe","");
    }

    public String getCorreo(){
        return preferences.getString("correo","");
    }

    public String getNombreFoto(){
        return "";
    }

    public boolean estaRegistrado(){
        return  preferences.getBoolean("esta_registrado",false);
    }

    public void registrarUsuario(){
        editor.putBoolean("esta_registrado", true);
        editor.apply();
    }


    public void setTelefono(String telefono) {
        editor.putString("telefono", telefono);
        editor.apply();
    }

    public void setCorreo(String correo) {
        editor.putString("correo", correo);
        this.correo = correo;
    }

    public void setNombreUsuario(String nombreUsuario) {
        editor.putString("nombre_usuario", nombreUsuario);
        editor.apply();

        this.nombreUsuario = nombreUsuario;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        editor.putString("bithdate", birthDate);
        editor.apply();
        this.birthDate = birthDate;
    }


    public void setClabe(String clabe) {

        this.clabe = clabe;
    }

    public String getAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        // config url
        editor.putString("urlBitmap", urlAvatar);
        editor.apply();
        this.urlAvatar = urlAvatar;
    }


    public String getNumTarjetaInicial() {
        return preferences.getString("numTarjetaInicial", "");
    }

    public void setNumTarjetaInicial(String numTarjetaInicial) {

        editor.putString("numTarjetaInicial", numTarjetaInicial.replaceAll("\\s",""));
        editor.apply();
        this.numTarjetaInicial = numTarjetaInicial;
    }

    public String getToken() {
        return preferences.getString("token", "");
    }

    public void setToken(String token) {
        editor.putString("token", token);
        editor.apply();
        this.token = token;
    }
}
