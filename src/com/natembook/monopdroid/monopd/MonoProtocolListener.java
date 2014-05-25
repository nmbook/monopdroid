package com.natembook.monopdroid.monopd;

public interface MonoProtocolListener {
    void onException(String description, Exception ex);

    void onClose(boolean remote);
}
