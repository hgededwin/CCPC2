package com.cacaopaycard.cacaopay.Modelos;

import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Fecha {

    private String dia;
    private String mes;
    private String anio;
    private String date = null;

    public Fecha(String dia, String mes, String anio){
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;

    }

    public Fecha(String dateString) throws ParseException {

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
        Date dateTmp = input.parse(dateString);
        this.date = output.format(dateTmp);
    }

    public String getDateFormat(){
        return this.date.toString();
    }


    public String getDate(){
        return dia + "/" + mes +"/" + anio;
    }


    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }
}
