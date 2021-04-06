package indair.basic;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AppActivity extends Fragment implements NetworkCallBack {

    private Button logOutButton;
    private TextView textTemperature;
    private TextView textHumidity;
    private Network  net;
    private MainCallBack main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.activity_app, container, false);

        textTemperature = view.findViewById(R.id.textViewTemperatureTxt);
        textHumidity = view.findViewById(R.id.textViewHumidityTxt);
        logOutButton = view.findViewById(R.id.buttonLogout);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.changeActivity(0);
            }
        });

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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTemperature.setText(s);
            }
        });
    }

    @Override
    public void networkError(int err_code)
    {
        switch (err_code) {
            case Network.UNKNOWN_HOST:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.changeActivity(0);
                    }
                });
                break;
            case Network.CONNECTION_ERROR:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.changeActivity(0);
                    }
                });
                break;
            case Network.CONNECTION_TIMEOUT:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.changeActivity(0);
                    }
                });
                break;
        }
    }

    public void init(Network network, MainCallBack callBack)
    {
        net = network;
        main = callBack;
    }
}
