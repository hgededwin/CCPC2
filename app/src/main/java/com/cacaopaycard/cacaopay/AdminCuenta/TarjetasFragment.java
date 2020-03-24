package com.cacaopaycard.cacaopay.AdminCuenta;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cacaopaycard.cacaopay.Adapters.TarjetaAdapter;
import com.cacaopaycard.cacaopay.AdminCuenta.Tarjetas.AgregarTarjetaActivity;
import com.cacaopaycard.cacaopay.Constantes;
import com.cacaopaycard.cacaopay.Modelos.Dialog;
import com.cacaopaycard.cacaopay.Modelos.DialogFragmentPIN;
import com.cacaopaycard.cacaopay.Modelos.Peticion;
import com.cacaopaycard.cacaopay.Modelos.Singleton;
import com.cacaopaycard.cacaopay.Modelos.Tarjeta;
import com.cacaopaycard.cacaopay.Modelos.Usuario;
import com.cacaopaycard.cacaopay.R;
import com.cacaopaycard.cacaopay.mvp.util.URLCacao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cacaopaycard.cacaopay.Constantes.AGREGAR_TARJETA;
import static com.cacaopaycard.cacaopay.Constantes.APP_ID;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_BLOQUEAR_TARJETA;
import static com.cacaopaycard.cacaopay.mvp.util.URLCacao.URL_DESBLOQUEAR_TARJETA;


public class TarjetasFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView rvTarjetas;
    private TarjetaAdapter tarjetaAdapter;
    private List<Tarjeta> tarjetas = new ArrayList<>();
    private Toolbar toolbar;
    private Boolean editando = false;
    //private Button btnEditarTarjetas;
    private TextView btnAgregarTarjeta;
    private ImageView imgAgregarTarjetas;

    // Selección de tarjetas
    private TarjetaAdapter adaptadorSeleccion;
    private Singleton singleton;
    private RequestQueue requestQueue;
    private Usuario usuario;

    private static final String TAG = "TarjetasFragment";

    private TarjetaAdapter.EditionCardListener listener = new TarjetaAdapter.EditionCardListener() {
        @Override
        public void eliminarTarjeta(Context context, final int indice, final String numeroTarjeta) {
            //removeCard(numeroTarjeta, indice);
            boolean confirmElim = false;
            new MaterialDialog.Builder(getContext())
                    .positiveText(getString(R.string.str_aceptar))
                    .negativeText(getString(R.string.str_cancelar))
                    .content("¿Desea eliminar la tarjeta con terminación **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4) + "?")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            System.out.println("removing");
                            //removeCard(numeroTarjeta, indice);
                            removeCard(numeroTarjeta,indice);

                        }
                    })
                    .show();


        }

        @Override
        public void lockUnlockCard(Context context, final int indice, final String numeroTarjeta, final boolean isBlocked) {
            System.out.println("lock/unlock");
            //bloqueoDesbloqueoTarjeta(numeroTarjeta,indice,isBlocked);
            lockUnlock(numeroTarjeta,indice,isBlocked);


        }
    };

    public void lockUnlockcard(final boolean isLock, String currentCard, final int indice){

        String urlLockUnlockDesired = isLock ? URL_DESBLOQUEAR_TARJETA: URL_BLOQUEAR_TARJETA;
        final Peticion peticionLockunlock = new Peticion(view.getContext(), requestQueue);
        peticionLockunlock.addParamsString("Tarjeta", currentCard);
        peticionLockunlock.addParamsString("MotivoBloqueo", "004");

        peticionLockunlock.jsonObjectRequest(Request.Method.POST, urlLockUnlockDesired, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                peticionLockunlock.dismissProgressDialog();
                try {

                    String sinDiag = response.toString().replaceAll("\\\\", "");
                    String reformatRight = sinDiag.replaceAll("\"[{]","{");
                    String reformatleftt = reformatRight.replaceAll("[}]\"","}");
                    Log.e(TAG, "reformat" + reformatleftt);
                    JSONObject newResponse = new JSONObject(reformatleftt);

                    JSONObject responseCacaoAPI = newResponse.getJSONObject("ResponseCacaoAPI");
                    String codRespuesta = responseCacaoAPI.getString("CodRespuesta");
                    if(codRespuesta.equals("0000")){
                        System.out.println("bloqueo desbloqueo exitoso");

                        tarjetas.get(indice).setEstaBloqueada(!isLock);
                        tarjetaAdapter.notifyDataSetChanged();

                    } else {
                        String descRespuesta = responseCacaoAPI.getString("DescRespuesta");
                        Log.e(Constantes.TAG, descRespuesta);
                        new MaterialDialog.Builder(view.getContext())
                                .content(descRespuesta)
                                .positiveText("OK")
                                .show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();


                }

            }
        });
    }

    public TarjetasFragment() {
        // Required empty public constructor
    }


    public static TarjetasFragment newInstance(String param1, String param2) {
        TarjetasFragment fragment = new TarjetasFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tarjetas, container, false);

        singleton = Singleton.getInstance(view.getContext());
        requestQueue = singleton.getmRequestQueue();
        usuario = new Usuario(view.getContext());

        //toolbar = view.findViewById(R.id.toolbar_mis_tarjetas);
        rvTarjetas = view.findViewById(R.id.rv_tarjetas);
        btnAgregarTarjeta = view.findViewById(R.id.txt_agregar_tarjetas);
        imgAgregarTarjetas = view.findViewById(R.id.img_agregar_tarjetas);
        btnAgregarTarjeta.getDrawingRect(new Rect(20,20,20,20));
        imgAgregarTarjetas.setOnClickListener(this);
        btnAgregarTarjeta.setOnClickListener(this);

        rvTarjetas.setHasFixedSize(true);
        // servicio para llenar tarjetas.


        tarjetaAdapter = new TarjetaAdapter(tarjetas, listener, getContext());
        rvTarjetas.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvTarjetas.setAdapter(tarjetaAdapter);

        getInfoCard();
        //getUserData();


        return view;
    }



    public void onClickAgregarTarjetas(View view) {



        Intent intentAddCard = new Intent(view.getContext(), AgregarTarjetaActivity.class);
        startActivityForResult(intentAddCard,AGREGAR_TARJETA);
        ((Activity) getContext()).overridePendingTransition(R.anim.left_in,R.anim.left_out);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == AGREGAR_TARJETA && resultCode == RESULT_OK){

            //getUserData();
            getInfoCard();

            if(data != null) {
                /*System.out.println("Se recibió tarjeta.....");
                //updateControl(false);
                Tarjeta tarjetaAgregada = (Tarjeta) data.getSerializableExtra("nueva_tarjeta");

                tarjetas.add(tarjetaAgregada);
                System.out.println("....tarjetas" + tarjetas);

                tarjetaAdapter = new TarjetaAdapter(tarjetas, listener);
                rvTarjetas.setLayoutManager(new LinearLayoutManager(view.getContext()));
                rvTarjetas.setAdapter(tarjetaAdapter);*/
            }

            //finish();
        }
    }



    public void updateControl(Boolean editar){
        if(editar){
            rvTarjetas.setAdapter(new TarjetaAdapter(tarjetas, getContext()));
            //btnEditarTarjetas.setText(R.string.str_editar);
            editando = false;
        } else {


            adaptadorSeleccion = new TarjetaAdapter(tarjetas, listener, getContext());
            rvTarjetas.setAdapter(adaptadorSeleccion);
            //btnEditarTarjetas.setText(R.string.str_cancelar);
            editando = true;

        }
    }

    public void removeCard(String numTarjeta, final int indice){

        new Dialog(getContext()).showDialog("Por el momento no puedes eliminar tu tarjeta");

    }

    public void getInfoCard(){
        tarjetas.clear();
        final Peticion requestN = new Peticion(getContext(), requestQueue);
        requestN.addParamsString("Tarjeta", usuario.getNumTarjetaInicial());
        requestN.jsonObjectRequest(Request.Method.POST, URLCacao.URL_CONSULTA_TARJETA,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                requestN.dismissProgressDialog();
                try {
                    Log.e(TAG,response.toString());
                    String sinDiag = response.toString().replaceAll("\\\\", "");
                    String reformatRight = sinDiag.replaceAll("\"[{]","{");
                    String reformatleftt = reformatRight.replaceAll("[}]\"","}");
                    Log.e(TAG, "reformat" + reformatleftt);
                    JSONObject newResponse = new JSONObject(reformatleftt);

                    String jsonDesc = newResponse.getString("ResponseCode");
                    JSONObject jsonResponse = newResponse.getJSONObject("ResponseCacaoAPI");
                    String codRespuesta = jsonResponse.getString("CodRespuesta");

                    if(codRespuesta.equals("0000")){
                        //List<Tarjeta> cardList = new ArrayList<>();
                        JSONObject jsonSaldo = jsonResponse.getJSONArray("SaldoActual").getJSONObject(0);
                        System.out.println(jsonSaldo.toString());
                        Tarjeta card = new Tarjeta(
                                new Usuario(getContext()).getNumTarjetaInicial(),
                                jsonResponse.getString("DescripcionStatus"),
                                jsonResponse.getString("CuentaCacao"),
                                jsonResponse.getString("FechaVigencia"),
                                String.valueOf(jsonSaldo.getString("Saldo"))
                        );
                        tarjetas.add(card);
                        tarjetaAdapter.notifyDataSetChanged();
                    } else {
                        String descRespuesta = jsonResponse.getString("DescRespuesta");
                        new MaterialDialog.Builder(view.getContext())
                                .positiveText("OK")
                                .content(descRespuesta)
                                .show();
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void getCardBalance(final String  numCard, final Tarjeta tarjeta){

        tarjetas.clear();
        final Peticion requestCardBalance = new Peticion(view.getContext(), requestQueue);
        requestCardBalance.addParams(getString(R.string.card_number_param),numCard);

        requestCardBalance.stringRequest(Request.Method.POST, getString(R.string.url_card_balance), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                requestCardBalance.dismissProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("succes");
                    System.out.println("Solicitud exitosa, respuesta en proceso.....");

                    if(success == 1){
                        Log.i(Constantes.TAG, "CardBalance");
                        String saldo = jsonObject.getString("saldo");
                        String estado = jsonObject.getString("estado");
                        String stp = jsonObject.getString("stp");

                        //finishRequestCards.onFinishRequestCard(saldo,estado,stp);
                        tarjeta.setSaldo(saldo);
                        tarjeta.setEstado(estado);
                        tarjeta.setStp(stp);
                        tarjetas.add(tarjeta);
                        tarjetaAdapter.notifyDataSetChanged();

                    } else{
                        String message = jsonObject.getString("message");
                        Log.e(Constantes.TAG, "Error CardBalance");

                        new MaterialDialog.Builder(view.getContext())
                                .positiveText("OK")
                                .content(message)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }


    public void lockUnlock(final String numeroTarjeta, final int indice, final boolean isLock){

        if(isLock){
            new MaterialDialog.Builder(view.getContext())
                    .title("Desbloquear tarjeta")
                    .content("¿Seguro que deseas desbloquear la tarjeta con terminación **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4) + "?")
                    .positiveText(R.string.str_continuar)
                    .negativeText("Deshacer")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // Servicio que debloquea tarjetas.

                            lockUnlockcard(isLock,numeroTarjeta, indice);

                        }
                    })
                    .show();
        } else {
            new MaterialDialog.Builder(view.getContext())
                    .title("Bloquear tarjeta")
                    .content("¿Seguro que deseas bloquear la tarjeta con terminación **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4) + "?")
                    .positiveText(R.string.str_continuar)
                    .negativeText("Deshacer")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // Servicio que bloquea tarjetas.
                            lockUnlockcard(isLock,numeroTarjeta, indice);

                        }
                    })
                    .show();
        }
    }

    public void bloqueoDesbloqueoTarjeta(String numeroTarjeta, final int indice, boolean isBlocked){

        if(isBlocked){
            new MaterialDialog.Builder(view.getContext())
                    .title("Desbloquear tarjeta")
                    .content("¿Seguro que deseas desbloquear la tarjeta con terminación **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4) + "?")
                    .positiveText(R.string.str_continuar)
                    .negativeText("Deshacer")
                    .negativeColorRes(R.color.blue_color_contraste)
                    .positiveColorRes(R.color.blue_color_contraste)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // Servicio que debloquea tarjetas.

                            DialogFragmentPIN dialogDesbloqueo = DialogFragmentPIN.instanceDialog(view.getContext(), new DialogFragmentPIN.DialogListener() {
                                @Override
                                public void calbackOnComplete(DialogFragment dialog, String otp) {

                                    tarjetas.get(indice).setEstaBloqueada(false);
                                    tarjetaAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            }, null, "Ingrese su PIN para continuar", null);

                            dialogDesbloqueo.show(((AppCompatActivity) view.getContext()).getSupportFragmentManager(), "CacaoPay");


                        }
                    })
                    .show();
        } else {
            new MaterialDialog.Builder(view.getContext())
                    .title("Bloquear tarjeta")
                    .content("¿Seguro que deseas bloquear la tarjeta con terminación **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4) + "?")
                    .positiveText(R.string.str_continuar)
                    .negativeText("Deshacer")
                    .negativeColorRes(R.color.blue_color_contraste)
                    .positiveColorRes(R.color.blue_color_contraste)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // Servicio que bloquea tarjetas.

                            DialogFragmentPIN dialogDesbloqueo = DialogFragmentPIN.instanceDialog(view.getContext(), new DialogFragmentPIN.DialogListener() {
                                @Override
                                public void calbackOnComplete(DialogFragment dialog, String otp) {
                                    tarjetas.get(indice).setEstaBloqueada(true);
                                    tarjetaAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            }, null, "Ingrese su PIN para continuar", null);

                            dialogDesbloqueo.show(((AppCompatActivity) view.getContext()).getSupportFragmentManager(), "CacaoPay");


                        }
                    })
                    .show();
        }
    }

    public void onClickEditarMisTarjetas(View view) {
        updateControl(editando);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.txt_agregar_tarjetas:
                onClickAgregarTarjetas(view);
                break;
            case R.id.img_agregar_tarjetas:
                onClickAgregarTarjetas(view);
                break;

        }

    }
}
