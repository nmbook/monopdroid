package com.natembook.monopdroid.dialogs;

import android.support.v4.app.FragmentManager;

public interface MonopolyDialogHost {
    public boolean isRunning();
    public MonopolyDialog getCurrentDialog();
    public FragmentManager getSupportFragmentManager();
}
