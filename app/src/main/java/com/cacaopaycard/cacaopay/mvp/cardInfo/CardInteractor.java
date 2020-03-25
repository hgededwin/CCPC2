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
import com.cacaopaycard.cacaopay.Modelos.Peticion;
import com.cacaopaycard.cacaopay.Modelos.Tarjeta;
import com.cacaopaycard.cacaopay.Modelos.Usuario;
import com.cacaopaycard.cacaopay.Utils.Format;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_BLOQUEAR_TARJETA;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_CARD_BALANCE;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_DESBLOQUEAR_TARJETA;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_LOCK_CARD;

public class CardInteractor {

    private final String TAG = "CardInteractor";

    private RequestQueue requestQueue;
    private Tarjeta mCard;
    private CardBalanceRequest listener;

    public CardInteractor(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void setCardValues(Tarjeta card, final CardBalanceRequest listener){
        this.listener = listener;
        this.mCard = card;
        //mCard.setSaldo(card.getSaldo());
        mCard.setEstado(card.getDescStatus());
        //mCard.setStp(stp);
        this.listener.onSuccess(mCard);
    }

    public void getCardBalance(final Tarjeta card, final CardBalanceRequest listener){
        this.mCard = card;
        this.listener = listener;

        StringRequest request = new StringRequest(Request.Method.POST, URL_CARD_BALANCE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    processResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError("Ocurrió un error al procesar la información del ususario, por favor inténtalo de nuevo.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                listener.onError("Ocurrió un error al obtener la información del ususario, por favor inténtalo de nuevo.");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("card_number", card.getNumeroCuenta());
                return params;
            }
        };

        requestQueue.add(request);
    }


    public void lockCard(final boolean newStatus, final String card, final Context context, final CardBalanceRequest listener){

        final String urllockUnlockDesired = newStatus ? URL_BLOQUEAR_TARJETA: URL_DESBLOQUEAR_TARJETA;
        Log.e(TAG,urllockUnlockDesired);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Tarjeta", card);
            jsonObject.put("Correo", new Usuario(context).getCorreo());
            jsonObject.put("MotivoBloqueo", "004");
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onError("Ocurrió un error al procesar su información");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, urllockUnlockDesired, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, response.toString());
                try {
                    Log.e(TAG,response.toString());
                    //JSONObject newResponse = Format.toSintaxJSON(response);

                    //JSONObject responseCacaoAPI = newResponse.getJSONObject("ResponseCacaoAPI");
                    String codRespuesta = response.getString("CodRespuesta");


                    switch (codRespuesta){
                        case "0000":
                            System.out.println("...Éxito al bloqueardeblowuear");
                            listener.onLockUnlockSuccess(newStatus);
                            break;

                        default:
                            listener.onError("Ocurrió un error inesperado, por favor inténtalo de nuevo.");
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError("Ocurrió un error inesperado, por favor inténtalo de nuevo.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                listener.onError("Ocurrió un error al procesar la solicitud, por favor inténtalo de nuevo.");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Token", new Usuario(context).getToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }


    private void processResponse(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);

        int success = jsonObject.getInt("succes");

        final int SUCCESS = 1;
        final int FAIL = 0;

        switch (success){

            case SUCCESS:
                processCardBalance(jsonObject);
                break;
            case FAIL:
                listener.onError(jsonObject.getString("message"));
                break;
            default:
                listener.onError("Ocurrió un error inesperado, por favor inténtalo de nuevo.");
                break;
        }
    }

    private void processCardBalance(JSONObject jsonObject) throws JSONException {
        String saldo = jsonObject.getString("saldo");
        String estado = jsonObject.getString("estado");
        String stp = jsonObject.getString("stp");
        mCard.setSaldo(saldo);
        mCard.setEstado(estado);
        mCard.setStp(stp);
        listener.onSuccess(mCard);
    }


    interface CardBalanceRequest{
        void onError(String error);
        void onSuccess(Tarjeta card);
        void onLockUnlockSuccess(boolean newStatus);
    }
}
