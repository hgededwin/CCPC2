package com.cacaopaycard.cacaopay.Registro;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.cacaopaycard.cacaopay.Constantes;
import com.cacaopaycard.cacaopay.Modelos.Peticion;
import com.cacaopaycard.cacaopay.Modelos.Singleton;
import com.cacaopaycard.cacaopay.Modelos.Usuario;
import com.cacaopaycard.cacaopay.R;
import com.cacaopaycard.cacaopay.mvp.util.URLCacao;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static com.cacaopaycard.cacaopay.Constantes.APP_ID;
import static com.cacaopaycard.cacaopay.Constantes.REGISTRO;
import static com.cacaopaycard.cacaopay.Constantes.USER_REGISTERED;

public class VerificacionActivity extends AppCompatActivity {


    private OtpView otpCodigo;
    private RequestQueue requestQueue;
    private Singleton singleton;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion);

        singleton = Singleton.getInstance(this);
        requestQueue = singleton.getmRequestQueue();

        otpCodigo = findViewById(R.id.otp_codigo);
        usuario = new Usuario(this);

        Log.e("CORREO -->", usuario.getCorreo());
        Log.e("numero cel -->", usuario.getTelefono());
        Log.e("NUMERO TARJETA --->", usuario.getNumTarjetaInicial());

        otpCodigo.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                // validar código

            }
        });
    }

    public void onClickMandarCodigo(View view) {
        if(otpCodigo.getText().toString().length() == 8) {
            confirmarCuenta();
        }
    }

    public void confirmarCuenta(){

        final Peticion confirmarCuenta = new Peticion(this, requestQueue);
        confirmarCuenta.addParamsString("NumeroCelular", usuario.getTelefono());
        confirmarCuenta.addParamsString("Correo", usuario.getCorreo());
        confirmarCuenta.addParamsString("Tarjeta", usuario.getNumTarjetaInicial());
        confirmarCuenta.addParamsString("OTP", otpCodigo.getText().toString());

confirmarCuenta.jsonObjectRequest(Request.Method.POST, URLCacao.URL_CONFIRMAR, new Response.Listener() {
    @Override
    public void onResponse(Object response) {
        confirmarCuenta.dismissProgressDialog();
        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(response));
            Log.e(Constantes.TAG, response.toString());

            String codeResponse = jsonObject.getString("ResponseCode");
            String message = jsonObject.getString("Mensaje");

            if (codeResponse.equals("00")){
                usuario.registrarUsuario();
                Intent intent = new Intent(VerificacionActivity.this, RegistroExitosoActivity.class);
                intent.putExtra("flujo", REGISTRO);
                startActivityForResult(intent, REGISTRO);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);

            } else {
                Log.e(Constantes.TAG, message);
                new  MaterialDialog.Builder(VerificacionActivity.this)
                        .title("¡Error!")
                        .content(message)
                        .positiveText("Ok")
                        .show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
});
        /*
         final Peticion peticionPin = new Peticion(this,requestQueue);
        peticionPin.addParams(getString(R.string.phone_params), usuario.getTelefono());
        peticionPin.addParams(getString(R.string.app_id_params),APP_ID);
        peticionPin.stringRequest(Request.Method.POST, getString(R.string.url_reenvio_pin_registro), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                peticionPin.dismissProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("succes");
                    String message = jsonObject.getString("message");

                    if(success == 1){
                        // pin reenviado
                        Log.i(Constantes.TAG, message);
                        Toast.makeText(VerificacionActivity.this,"Se mandó el código a su teléfono",Toast.LENGTH_SHORT).show();

                    } else
                        Log.e(Constantes.TAG, message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
         */
    }

    public void onClickCodigoEmail(View view) {
      // Toast.makeText(VerificacionActivity.this,"Se mandó el código a su email y teléfono",Toast.LENGTH_SHORT).show();

       // reenvioPinVerificacionRequest();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REGISTRO && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
        if(requestCode == REGISTRO && resultCode == USER_REGISTERED){
            setResult(RESULT_CANCELED);
            finish();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }

    /*public void reenvioPinVerificacionRequest(){
        final Peticion peticionPin = new Peticion(this,requestQueue);
        peticionPin.addParams(getString(R.string.phone_params), usuario.getTelefono());
        peticionPin.addParams(getString(R.string.app_id_params),APP_ID);
        peticionPin.stringRequest(Request.Method.POST, getString(R.string.url_reenvio_pin_registro), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                peticionPin.dismissProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("succes");
                    String message = jsonObject.getString("message");

                    if(success == 1){
                        // pin reenviado
                        Log.i(Constantes.TAG, message);
                        Toast.makeText(VerificacionActivity.this,"Se mandó el código a su teléfono",Toast.LENGTH_SHORT).show();

                    } else
                        Log.e(Constantes.TAG, message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    } */

    public void onClickObtenerAyuda(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cacaohelp@cacao.com"});
        emailIntent.putExtra(Intent.EXTRA_TITLE,"CacaoPayCard");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Ayuda AppCacao");
        //emailIntent.setType("message/rfc822");
        //emailIntent.setType("text/plain");
        startActivity(Intent.createChooser(emailIntent,"Email ..."));
    }
}
