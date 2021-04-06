package indair.basic;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Network
{
    private Socket sock;
    private BufferedWriter printer;
    private BufferedReader reader;
    private NetworkCallBack callBack;
    private Timer timer;
    private Readmessage message_thread;

    public final static int UNKNOWN_HOST       = 0;
    public final static int CONNECTION_ERROR   = 1;
    public final static int CONNECTION_TIMEOUT = 2;

    public Network()
    {
        timer = new Timer();
        message_thread = new Readmessage();
    }

    public void setCallBack(NetworkCallBack c)
    {
        this.callBack = c;
    }

    public void connect(final String ip_address, final int port)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sock = new Socket(ip_address, port);
                    reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    printer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                } catch (UnknownHostException e) {
                    callBack.networkError(UNKNOWN_HOST);
                    return;
                } catch (IOException e) {
                    callBack.networkError(CONNECTION_ERROR);
                    return;
                }
                callBack.connected();
            }
        }).start();
    }

    public int close()
    {
        if (sock.isClosed()) return 0;

        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int sendMessage(String message)
    {
        try {
            printer.write(message);
        } catch (IOException e) {
            callBack.networkError(CONNECTION_ERROR);
            return -1;
        }

        return 0;
    }

    public void readMessage(final int timeout_ms)
    {

        /*if (timeout_ms > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    message_thread.stopRunning();
                    callBack.networkError(CONNECTION_TIMEOUT);
                    return;
                }
            }, timeout_ms);
        }*/

        try {
            sock.setSoTimeout(timeout_ms);
        } catch (SocketException e) {
            callBack.networkError(CONNECTION_ERROR);
        }
        new Thread(message_thread).start();
    }

    private class Readmessage implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            StringBuilder message = new StringBuilder();
            char c[] = new char[1024];
            while (running) {
                try {
                        //if (reader.ready()) {
                            reader.read(c, 0, 1024);
                            message.append(c);
                        //}

                } catch (SocketTimeoutException e) {
                    callBack.networkError(CONNECTION_TIMEOUT);
                    return;
                } catch (IOException e) {
                    callBack.networkError(CONNECTION_ERROR);
                    return;
                }
                if (message.toString() != "") {
                    callBack.messageReceived(message.toString().trim());
                    timer.cancel();
                    return;
                }
            }
        }

        public void stopRunning() {
            running = false;
        }
    }
}
