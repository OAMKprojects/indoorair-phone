package com.example.indoorair;

public interface NetworkCallBack
{
    void messageReceived(String message);
    void connected();
    void networkError(int err_code);
}
