package com.natembook.monopdroid.dialogs;

import android.os.Bundle;

import com.natembook.monopdroid.board.TradeOfferType;

public interface MonopolyDialogListener {
    /**
     * Called when the user submits a String value into the MonopolyDialog. 
     * @param value The value submitted.
     */
    public void onDialogEnterName(String value, Bundle dialogArgs);

    public void onDialogEnterMoney(int value, Bundle dialogArgs);

    public void onDialogChooseTradeType(TradeOfferType tradeOfferType, Bundle dialogArgs);
    
    public void onDialogChooseItem(int playerId, Bundle dialogArgs);
    
    public void onDialogQuit(Bundle dialogArgs);

    public void onDialogReconnect(Bundle dialogArgs);

    public void onDialogConfirmQuit(Bundle dialogArgs);

    public void onDialogDismiss();
}
