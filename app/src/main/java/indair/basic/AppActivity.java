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

import org.json.JSONException;
import org.json.JSONObject;

public class AppActivity extends Fragment implements NetworkCallBack {

    private Button logOutButton;
    private TextView textTemperature;
    private TextView textHumidity;
    private TextView logView;
    private Network  net;
    private MainCallBack main;
    private Crypto crypto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.activity_app, container, false);

        textTemperature = view.findViewById(R.id.textViewTemperatureVal);
        textHumidity = view.findViewById(R.id.textViewHumidityVal);
        logOutButton = view.findViewById(R.id.buttonLogout);
        logView = view.findViewById(R.id.logView);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        net.close();
                        main.changeActivity(0);
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void connected()
    {
        //
    }

    @Override
    public void messageReceived(final String message)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parseJSON(crypto.decode(message));
            }
        });

        net.readMessage(0);
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

    private void parseJSON(String json_message)
    {
        try {
            JSONObject json = new JSONObject(json_message);

            if (json.has("temperature")) {
                textTemperature.setText(json.getString("temperature"));
            }
            if (json.has("humidity")) {
                textHumidity.setText(json.getString("humidity"));
            }
        } catch (JSONException e) {
            logView.setText(json_message);
            return;
        }

        if (logView.length() > 0) logView.setText("");
    }

    public void startListenig()
    {
        net.readMessage(0);
    }

    public void init(Network network, MainCallBack callBack)
    {
        net = network;
        main = callBack;
        crypto = new Crypto();
    }
}
