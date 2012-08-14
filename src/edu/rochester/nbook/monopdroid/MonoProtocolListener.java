package edu.rochester.nbook.monopdroid;

public interface MonoProtocolListener {
    void onException(String description, Exception ex);

    void onClose(boolean remote);
}
