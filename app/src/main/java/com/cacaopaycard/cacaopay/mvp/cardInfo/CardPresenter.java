package com.cacaopaycard.cacaopay.mvp.cardInfo;

import android.content.Context;
import android.util.Log;

import com.cacaopaycard.cacaopay.Modelos.Tarjeta;
import com.cacaopaycard.cacaopay.Modelos.Usuario;

public class CardPresenter implements CardInteractor.CardBalanceRequest{

    private CardView view;
    private CardInteractor interactor;

    public CardPresenter(CardView view, CardInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    public void getCardBalance(Tarjeta card, Usuario usuario){
        Log.i("CARD PRESENTER", "getCardBalance");
        interactor.setCardValues(card, usuario, this);
        view.showProgress();
    }

    public void lockUnlockCard(boolean statusDesired, String cardNumber, Context context){
        interactor.lockCard(statusDesired, cardNumber, context, this);
        view.showProgress();
    }


    @Override
    public void onError(String error) {
        view.hideProgress();
        view.setSwitchPreviousState();
        view.showError(error);
    }

    @Override
    public void onSuccess(Tarjeta card) {
        System.out.println("onSuccess");
        view.hideProgress();
        view.showCardInfo(card);
    }

    @Override
    public void onLockUnlockSuccess(boolean newStatus) {
        view.hideProgress();
        if (newStatus) view.showCardLocked();
        else view.showCardUnLocked();
    }
}
