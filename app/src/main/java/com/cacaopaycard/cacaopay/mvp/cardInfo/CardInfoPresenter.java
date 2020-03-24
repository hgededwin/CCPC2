package com.cacaopaycard.cacaopay.mvp.cardInfo;

import android.content.Context;
import android.util.Log;

import com.cacaopaycard.cacaopay.Modelos.Fecha;
import com.cacaopaycard.cacaopay.Modelos.Movimiento;
import com.cacaopaycard.cacaopay.Modelos.Tarjeta;
import com.cacaopaycard.cacaopay.Modelos.Usuario;

import java.util.List;

public class CardInfoPresenter implements CardInfoInteractor.OnFinishCardInfoRequest {
    private final String TAG = "CardInfoPresenter";

    private CardInfoView view;
    private CardInfoInteractor interactor;

    public CardInfoPresenter(CardInfoView view, CardInfoInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    public void getCards(Usuario usuario, Context context){
        view.showProgress();
        interactor.getCards(this, usuario, context);
    }

    public void getCardMovements(Tarjeta card, Context context){
        view.showProgress();
        interactor.getCardMovements(this, card, context);
    }

    @Override
    public void onSuccessCardList(List<Tarjeta> cards) {
        Log.i(TAG, "CArd is :" + cards.get(0).isEstaBloqueada());
        view.dismissProgress();
        view.showCards(cards);
    }

    @Override
    public void onSuccessCardMovements(List<Movimiento> movimientos, List<Fecha> fechas) {
        System.out.println("onSuccessMoves");
        view.dismissProgress();
        view.showMovements(movimientos, fechas);
    }

    @Override
    public void onError(String error) {
        view.dismissProgress();
        view.showError(error);
    }

    @Override
    public void onEmptyMovements() {
        view.dismissProgress();
        view.showEmptyMovements();
    }

    @Override
    public void onEmptyCards() {
        view.dismissProgress();
        view.showEmptyCards();
    }
}
