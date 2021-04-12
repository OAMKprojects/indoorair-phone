package indair.basic;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AppActivity extends Fragment implements NetworkCallBack {

    private Button logOutButton;
    private TextView logView;
    private Network  net;
    private MainCallBack main;
    private Crypto crypto;
    private View view;
    private Map<String, TextView> value_map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.activity_app, container, false);

        logOutButton = view.findViewById(R.id.buttonLogout);
        logView = view.findViewById(R.id.logView);
        value_map = new HashMap<String, TextView>();

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

            if (json.has("values")) {

                JSONObject values = json.getJSONObject("values");
                Iterator<String> name_it = values.keys();

                while(name_it.hasNext()) {
                    String name = name_it.next();
                    updateOldRow(name, values.getString(name));
                }
            }
        } catch (JSONException e) {
            logView.setText(json_message);
            return;
        }

        if (logView.length() > 0) logView.setText("");
    }

    private void createNewRow(String name, String value)
    {
        TableRow new_row = new TableRow(getContext());
        new_row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        new_row.setGravity(Gravity.CENTER);

        TextView new_name = new TextView(getContext());
        new_name.setText(name);
        new_name.setTextSize(24);
        new_name.setTextColor(Color.BLACK);
        new_name.setGravity(Gravity.LEFT);

        Space new_space = new Space(getContext());
        new_space.setLayoutParams(new TableRow.LayoutParams(60, TableRow.LayoutParams.MATCH_PARENT));

        TextView new_value = new TextView(getContext());
        new_value.setText(value);
        new_value.setTextSize(24);
        new_value.setTextColor(Color.BLUE);
        new_value.setGravity(Gravity.RIGHT);

        new_row.addView(new_name);
        new_row.addView(new_space);
        new_row.addView(new_value);

        TableLayout tb = view.findViewById(R.id.valueTable);
        tb.addView(new_row);

        value_map.put(name, new_value);
    }

    private void updateOldRow(String name, String value)
    {
        if (value_map.containsKey(name)) {
            TextView old_value = value_map.get(name);
            old_value.setText(value);
        }
        else createNewRow(name, value);
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
