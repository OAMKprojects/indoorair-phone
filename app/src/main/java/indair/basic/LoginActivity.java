package indair.basic;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class LoginActivity extends Fragment implements NetworkCallBack
{
    private Button loginButton;
    private EditText passwordEdit;
    private EditText ipEdit;
    private EditText portEdit;
    private TextView txtLogIn;
    private Network  net;
    private State    net_state;
    private MainCallBack main;

    private enum State{
        connect
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.activity_login, container, false);

        txtLogIn = view.findViewById(R.id.textLogging);
        loginButton = view.findViewById(R.id.buttonLogin);
        loginButton.setEnabled(false);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });

        passwordEdit = view.findViewById(R.id.editPassword);
        ipEdit = view.findViewById(R.id.editIP);
        portEdit = view.findViewById(R.id.editPort);

        ipEdit.setText("192.168.1.100");
        portEdit.setText("8080");

        TextWatcher logWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordEdit.getText().length() > 0 && ipEdit.getText().length() > 0 && portEdit.getText().length() > 0) {
                    loginButton.setEnabled(true);
                }
                else if (loginButton.isEnabled()) {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        passwordEdit.clearFocus();
        passwordEdit.addTextChangedListener(logWatcher);
        ipEdit.addTextChangedListener(logWatcher);
        portEdit.addTextChangedListener(logWatcher);

        return view;
    }


    @Override
    public void connected()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                net.readMessage(2000);
            }
        });
    }

    @Override
    public void messageReceived(final String s)
    {
        switch (net_state) {
            case connect:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String s2 = "Welcome to Server";
                        if (s.equals(s2)) txtLogIn.setText(s);
                        else txtLogIn.setText("Incorrect password!");
                    }
                });
                break;
        }
    }

    @Override
    public void networkError(int err_code)
    {
        switch (net_state) {
            case connect:
                switch (err_code) {
                    case Network.UNKNOWN_HOST:
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtLogIn.setText("Unknown host!");
                                loginButton.setEnabled(true);
                                main.changeActivity(1);
                            }
                        });
                        break;
                    case Network.CONNECTION_ERROR:
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtLogIn.setText("Connection error!");
                                loginButton.setEnabled(true);
                            }
                        });
                        break;
                    case Network.CONNECTION_TIMEOUT:
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtLogIn.setText("Connection timeout!");
                                loginButton.setEnabled(true);
                            }
                        });
                        break;
                }
                break;
        }
    }

    public void init(Network network, MainCallBack callBack)
    {
        net = network;
        main = callBack;
    }

    private void logIn()
    {
        loginButton.setEnabled(false);
        txtLogIn.setText("Logging in...");
        net_state = State.connect;
        net.connect(ipEdit.getText().toString().trim(), Integer.valueOf(portEdit.getText().toString().trim()));
    }
}
