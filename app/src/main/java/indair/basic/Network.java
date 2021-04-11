package indair.basic;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
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
    private Thread read_thread;

    public final static int UNKNOWN_HOST       = 0;
    public final static int CONNECTION_ERROR   = 1;
    public final static int CONNECTION_TIMEOUT = 2;

    public Network()
    {
        timer = new Timer();
        message_thread = new Readmessage();
        read_thread = new Thread();
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

    public void close()
    {
        if (sock.isClosed()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (read_thread.isAlive()) read_thread.interrupt();
                    reader.close();
                    printer.close();
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return ;
                }
            }
        }).start();
    }

    public void sendMessage(final String message)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    printer.write(message);
                    printer.flush();
                } catch (IOException e) {
                    callBack.networkError(CONNECTION_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void readMessage(final int timeout_ms)
    {
        try {
            sock.setSoTimeout(timeout_ms);
        } catch (SocketException e) {
            callBack.networkError(CONNECTION_ERROR);
        }
        read_thread = new Thread(message_thread);
        read_thread.start();
    }

    private class Readmessage implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            StringBuilder message = new StringBuilder();
            char c[] = new char[1024];
            while (running) {
                try {
                    reader.read(c, 0, 1024);
                    message.append(c);

                } catch (InterruptedIOException e) {
                    if (e instanceof SocketTimeoutException) {
                        callBack.networkError(CONNECTION_ERROR);
                        return;
                    }
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
