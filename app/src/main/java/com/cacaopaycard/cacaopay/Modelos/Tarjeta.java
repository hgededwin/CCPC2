package com.cacaopaycard.cacaopay.Modelos;

import android.graphics.Color;

import com.blackcat.currencyedittext.CurrencyTextFormatter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.jar.JarOutputStream;

import static java.util.Currency.getInstance;
import static java.util.Locale.getDefault;

public class Tarjeta implements Serializable{

    private String saldo;
    private String numeroCuenta;
    private String moneda;
    private String decimales;
    private boolean estaBloqueada;
    private boolean tarjetaDeAgregacion = false;
    private String nickname;
    private String name;
    private String estado;
    private String bloqueoDesbloqueo;
    private String stp;
    private int colorYellow = Color.rgb(169,160,60);
    private int colorAqua = Color.rgb(26,76,121);
    private int colorGray = Color.rgb(74,152,156);
    private int[] colors = {colorYellow, colorAqua, colorGray};
    private static int indiceColosr;
    private int color;

    private String descStatus;
    private String fechaVigencia;
    private String ctaCacao;

    public Tarjeta(){
        tarjetaDeAgregacion = true;
    }

    public Tarjeta(String saldo, String numeroCuenta, String moneda, boolean estaBloqueada){
        DecimalFormat df = new DecimalFormat("'$'0.00");
        this.saldo = df.format(Double.parseDouble(saldo));
        this.numeroCuenta = numeroCuenta;
        this.moneda = moneda;
        this.estaBloqueada = estaBloqueada;
        this.decimales = saldo.substring(saldo.length() - 2);

        if (indiceColosr != 2) indiceColosr++; else indiceColosr = 0;
        this.color = colors[indiceColosr];

    }
    public Tarjeta(String saldo, String numeroCuenta, String moneda, String bloqueoDesbloqueo){
        DecimalFormat df = new DecimalFormat("'$'0.00");
        this.saldo = df.format(Double.parseDouble(saldo));
        this.numeroCuenta = numeroCuenta;
        this.moneda = moneda;
        this.bloqueoDesbloqueo = bloqueoDesbloqueo;
        estaBloqueada = bloqueoDesbloqueo.equals("28");
        this.decimales = saldo.substring(saldo.length() - 2);

        if (indiceColosr != 2) indiceColosr++; else indiceColosr = 0;
        this.color = colors[indiceColosr];

    }

    public Tarjeta(String card, String nickname, String name){
        this.numeroCuenta = card;
        this.nickname = nickname;
        this.name = name;
        if (indiceColosr != 2) indiceColosr++; else indiceColosr = 0;
        this.color = colors[indiceColosr];
    }

    public Tarjeta(String card, String descStatus, String ctaCacao, String fechaVigencia, String saldo){
        DecimalFormat df = new DecimalFormat("'$'0.00");
        this.saldo = df.format(Double.parseDouble(saldo));
        this.numeroCuenta = card;
        this.descStatus = descStatus;
        this.ctaCacao = ctaCacao;
        this.fechaVigencia = fechaVigencia;
        if (indiceColosr != 2) indiceColosr++; else indiceColosr = 0;
        this.color = colors[indiceColosr];
        estaBloqueada = !descStatus.equals("ACTIVA");

    }

    public String getSaldo() {
        //return parsearString(saldo
        System.out.println("Saldo"+saldo);

        return saldo;

    }

    public void setSaldo(String saldo) {
        DecimalFormat df = new DecimalFormat("'$'0.00");
        this.saldo = df.format(Double.parseDouble(saldo));
    }

    public String getNumeroCuenta() {
        // reenvía el número de cuenta sin espacios
        //return numeroCuenta.replaceAll("\\s","");
        return  numeroCuenta;
    }

    public String getTarjetaOfuscada(){
        return "**** " + getNumeroCuenta().substring(getNumeroCuenta().length() - 4);
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public boolean isEstaBloqueada() {

        return estaBloqueada;
    }

    public void setEstaBloqueada(boolean estaBloqueada) {

        bloqueoDesbloqueo = estaBloqueada ? "28" : "00";
        this.estaBloqueada = estaBloqueada;
    }


    public static String parsearString(String monto){

        return CurrencyTextFormatter.formatText(monto.replace(".",""),Locale.getDefault(),Locale.getDefault(),2);
        //return NumberFormat.getCurrencyInstance().form
    }

    public String getDecimales() {
        return "." + decimales;
    }

    public void setDecimales(String decimales) {
        this.decimales = decimales;
    }

    public boolean esTarjetaDeAgregacion() {
        return tarjetaDeAgregacion;
    }

    public void setTarjetaDeAgregacion(boolean tarjetaDeAgregacion) {
        this.tarjetaDeAgregacion = tarjetaDeAgregacion;
    }

    public String getNombre() {
        return nickname;
    }

    public void setNombre(String nombre) {
        this.nickname = nombre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloqueoDesbloqueo() {
        return bloqueoDesbloqueo;
    }

    public void setBloqueoDesbloqueo(String bloqueoDesbloqueo) {
        if(bloqueoDesbloqueo.equals("00"))
            estaBloqueada = false;
        else
            estaBloqueada = true;

        this.bloqueoDesbloqueo = bloqueoDesbloqueo;
    }

    public String getStp() {
        return stp;
    }

    public void setStp(String stp) {
        this.stp = stp;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        if (estado.equals("ACTIVA")){
            setEstaBloqueada(false);
        } else {
            setEstaBloqueada(true);
        }
    }

    public String getCardMask(){

        String str1 = numeroCuenta.substring(0,4);
        String str2 = numeroCuenta.substring(4,8);
        String str3 = numeroCuenta.substring(8,12);
        String str4 = numeroCuenta.substring(12,16);

        System.out.println(str1 + " " + str2 + " " + str3 + " " + str4);

        return str1 + " " + str2 + " " + str3 + " " + str4;
    }


    public String getDescStatus() {
        return descStatus;
    }

    public void setDescStatus(String descStatus) {
        this.descStatus = descStatus;
    }

    public String getFechaVigencia() {
        return fechaVigencia;
    }

    public void setFechaVigencia(String fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }

    public String getCtaCacao() {
        return ctaCacao;
    }

    public void setCtaCacao(String ctaCacao) {
        this.ctaCacao = ctaCacao;
    }
}
