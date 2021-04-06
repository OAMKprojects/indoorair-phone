package indair.basic;

public interface NetworkCallBack
{
    void messageReceived(String message);
    void connected();
    void networkError(int err_code);
}
