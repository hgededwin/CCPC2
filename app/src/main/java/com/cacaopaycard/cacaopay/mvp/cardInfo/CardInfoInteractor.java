package com.cacaopaycard.cacaopay.mvp.cardInfo;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.cacaopaycard.cacaopay.Constantes;
import com.cacaopaycard.cacaopay.Modelos.Fecha;
import com.cacaopaycard.cacaopay.Modelos.Movimiento;
import com.cacaopaycard.cacaopay.Modelos.Peticion;
import com.cacaopaycard.cacaopay.Modelos.Tarjeta;
import com.cacaopaycard.cacaopay.Modelos.Usuario;
import com.cacaopaycard.cacaopay.Utils.Format;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cacaopaycard.cacaopay.Constantes.APP_ID;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_CARD_MOVEMENTS;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_CONSULTA_TARJETA;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_MOVES;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_USER_DATA;

public class CardInfoInteractor {

    private final String TAG = "CardInfoInteractor";

    private RequestQueue requestQueue;
    private OnFinishCardInfoRequest listener;


    public CardInfoInteractor(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void getCards(final OnFinishCardInfoRequest listener, Usuario user, Context context){
        this.listener = listener;
        Log.i(TAG, "getCards");
        JSONObject params = new JSONObject();
        try {
            params.put("Tarjeta", user.getNumTarjetaInicial());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestData(WebService.USER_DATA, params, context);
    }

    public void getCardMovements(final OnFinishCardInfoRequest listener,final Tarjeta card, Context context){
        Log.i(TAG, "getCardMovements");
        System.out.println("card_movement:" + card.getNumeroCuenta());
        this.listener = listener;

        JSONObject params = new JSONObject();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd"); //SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        String strDate =  mdformat.format(Calendar.getInstance().getTime());
        try {
            params.put("Tarjeta", card.getNumeroCuenta());
            params.put("FechaInicial", "2020-03-01"); // fecha de movmiento
            params.put("FechaFinal", strDate);
            params.put("MaxMovimientos", "20");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestData(WebService.CARD_MOVEMENTS, params, context);

    }

    /**
     * Request handler
     * */
    private void requestData(final WebService service, JSONObject params, final Context context){
        String url = "";

        switch (service){
            case USER_DATA:
                Log.e(TAG, "Consulta tarjeta");
                url = URL_CONSULTA_TARJETA;
                break;
            case CARD_MOVEMENTS:
                Log.e(TAG, "Moves");
                url = URL_MOVES;
                break;
        }


        final Peticion requestN = new Peticion(context, requestQueue);
        requestN.jsonObjectRequest(Request.Method.POST, url, params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                requestN.dismissProgressDialog();
                try {
                    Log.e(TAG,response.toString());
                    JSONObject newResponse = Format.toSintaxJSON(response);

                    processResponse(newResponse, service, context);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError("Ocurrió un error al procesar la información del usuario, por favor inténtalo de nuevo.");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestN.dismissProgressDialog();
                Log.e(TAG, error.toString());
                listener.onError("Ocurrió un error al obtener la información del ususario, por favor inténtalo de nuevo.");
            }
        });

    }

    private void processResponse(JSONObject response, WebService service, Context context) throws JSONException {

        Log.i(TAG,"processResponse");
        String jsonDesc = response.getString("ResponseCode");

        final String SUCCESS = "00";
        //final int FAIL = 0;

        switch (jsonDesc){

            case SUCCESS:

                switch (service){
                    case USER_DATA:
                        //processUserInfo(jsonObject.getJSONObject("message"));
                        processUserCards(response, context);

                        break;
                    case CARD_MOVEMENTS:
                        processCardMovements(response);
                        break;
                }

                break;

            default:
                listener.onError("Ocurrió un error inesperado, por favor inténtalo de nuevo.");
                break;
        }
    }



    private void processUserCards(JSONObject cardInfo,Context context) throws JSONException {

        List<Tarjeta> cardList = new ArrayList<>();

        Log.i("InfoCard", cardInfo.toString());
        JSONObject jsonResponse = cardInfo.getJSONObject("ResponseCacaoAPI");
        JSONObject jsonSaldo = jsonResponse.getJSONArray("SaldoActual").getJSONObject(0);
        System.out.println(jsonSaldo.toString());
        Tarjeta card = new Tarjeta(
                new Usuario(context).getNumTarjetaInicial(),
                jsonResponse.getString("DescripcionStatus"),
                jsonResponse.getString("CuentaCacao"),
                jsonResponse.getString("FechaVigencia"),
                jsonSaldo.getString("Saldo")
        );

        cardList.add(card);
        if (cardList.size() > 0){
            listener.onSuccessCardList(cardList);
        } else{
            listener.onEmptyCards();
        }
    }

    private void processCardMovements(JSONObject jsonObject) throws JSONException {

        List<Movimiento> movimientoList = new ArrayList<>();
        List<Fecha> fechas = new ArrayList<>();
        List<String> fechasTemp = new ArrayList<>();

        //  NO SE ESTAN MOSTRANDO MOVIMIENTOS EN TRANSITO

        JSONArray arrayMovements = jsonObject.getJSONObject("ResponseCacaoAPI").getJSONArray("Movimientos");
        Log.e(TAG,"MOVES_PROCESS:" + arrayMovements.toString());
        if(arrayMovements.length() == 0){
            listener.onEmptyMovements();
            Log.e(TAG,"Empty");
        } else {

            movimientoList = new ArrayList<>();

            for (int i = 0; i < arrayMovements.length(); i++) {
                //Log.i(TAG, arrayMovements.getJSONObject(i).toString());
                String monto = arrayMovements.getJSONObject(i).getString("ImporteMovimiento");
                String tipo = arrayMovements.getJSONObject(i).getString("ConceptoMovimiento");//  Concepto
                String fechaObject = arrayMovements.getJSONObject(i).getString("FechaMovimiento");
                //String dia = fechaObject.substring(6);
                //String mes = fechaObject.substring(4,6);
                //String anio = fechaObject.substring(0,4);
                boolean transferRecibida = !arrayMovements.getJSONObject(i).getString("TipoMovimientoCargoAbono").equals("Cargo");

                Fecha fecha = null;
                try {
                    fecha = new Fecha(fechaObject);
                } catch (ParseException e) {
                    e.printStackTrace();
                    listener.onError("Error al parsear los datos de la fecha.");
                }

                if (!fechasTemp.contains(fecha.getDateFormat())) {
                    fechasTemp.add(fecha.getDateFormat());
                    fechas.add(fecha);
                }

                Movimiento movimiento = new Movimiento(tipo, fecha, monto, transferRecibida);
                movimientoList.add(movimiento);

            }
            listener.onSuccessCardMovements(movimientoList, fechas);

        }
    }

    public interface OnFinishCardInfoRequest {
        void onSuccessCardList(List<Tarjeta> cards);
        void onSuccessCardMovements(List<Movimiento> movimientos, List<Fecha> fechas);
        void onError(String error);
        void onEmptyMovements();
        void onEmptyCards();
    }

    private enum WebService {
        USER_DATA,
        CARD_MOVEMENTS
    }
}
