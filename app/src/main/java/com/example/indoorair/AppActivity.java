package com.example.indoorair;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AppActivity extends Fragment implements NetworkCallBack {

    private Button logOutButton;
    private Button updateButton;
    private EditText currentEdit;
    private String oldEditText;
    private TextView logView;
    private Network  net;
    private MainCallBack main;
    private Crypto crypto;
    private View view;
    private Map<String, TextView> value_map;
    private Map<String, EditText> control_map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.activity_app, container, false);

        logOutButton = view.findViewById(R.id.buttonLogout);
        updateButton = view.findViewById(R.id.buttonUpdate);
        logView = view.findViewById(R.id.logView);
        value_map = new HashMap<>();
        control_map = new HashMap<>();
        oldEditText = new String();

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

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject message_obj = new JSONObject();
                for(Map.Entry<String, EditText> item : control_map.entrySet()) {
                    try {
                        message_obj.put(item.getKey(), item.getValue().getText().toString());
                    } catch (JSONException e) {
                        return;
                    }
                }
                net.sendMessage(crypto.encode(message_obj.toString()));
                updateButton.setEnabled(false);
            }
        });

        net.sendMessage(crypto.encode("{\"controls updated\":\"false\"}"));

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
        if (message.matches("")) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.changeActivity(0);
                }
            });
            return;
        }
        else getActivity().runOnUiThread(new Runnable() {
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
        getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.changeActivity(0);
                    }
                });
    }

    private void parseJSON(String json_message)
    {
        try {
            JSONObject json = new JSONObject(json_message);

            if (json.has("values")) {

                TableLayout table = view.findViewById(R.id.valueTable);
                JSONObject values = json.getJSONObject("values");
                Iterator<String> name_it = values.keys();

                while(name_it.hasNext()) {
                    String name = name_it.next();
                    updateOldRow(name, values.getString(name), table);
                }
                if (!values.has("error")) {
                    if (value_map.containsKey("error")) {
                        value_map.remove("error");
                        for (int i = 0; i < table.getChildCount(); i++) {
                            TableRow row = (TableRow) table.getChildAt(i);
                            TextView text = (TextView)row.getChildAt(0);
                            if (text.getText().toString().matches("error")) {
                                table.removeViewAt(i);
                                break;
                            }
                        }
                    }
                }
            }

            if (json.has("controls")) {

                TableLayout table = view.findViewById(R.id.controlTable);
                JSONObject values = json.getJSONObject("controls");
                Iterator<String> name_it = values.keys();

                while(name_it.hasNext()) {
                    String name = name_it.next();
                    JSONArray control_array = values.getJSONArray(name);
                    JSONObject control_obj = control_array.getJSONObject(0);
                    String control_value = control_obj.getString("value");
                    String control_type = control_obj.getString("type");
                    updateOldControlRow(name, control_value, control_type, table);
                }

                net.sendMessage(crypto.encode("{\"controls updated\":\"true\"}"));
            }
        } catch (JSONException e) {
            logView.setText(json_message);
            return;
        }

        if (logView.length() > 0) logView.setText("");
    }

    private void createNewRow(String name, String value, TableLayout table)
    {
        TableRow new_row = new TableRow(getContext());
        new_row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        new_row.setGravity(Gravity.CENTER);

        TextView new_name = new TextView(getContext());
        new_name.setText(name);
        new_name.setTextSize(18);
        new_name.setTextColor(Color.BLACK);
        new_name.setGravity(Gravity.LEFT);

        Space new_space = new Space(getContext());
        new_space.setLayoutParams(new TableRow.LayoutParams(60, TableRow.LayoutParams.MATCH_PARENT));

        TextView new_value = new TextView(getContext());
        new_value.setText(value);
        new_value.setTextSize(18);
        new_value.setTextColor(Color.BLUE);
        new_value.setGravity(Gravity.RIGHT);

        new_row.addView(new_name);
        new_row.addView(new_space);
        new_row.addView(new_value);

        if (name.matches("error")) table.addView(new_row, 0);
        else table.addView(new_row);

        value_map.put(name, new_value);
    }

    private void createNewControlRow(String name, String value, TableLayout table, int type)
    {
        TableRow new_row = new TableRow(getContext());
        new_row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        new_row.setGravity(Gravity.CENTER);

        final Button new_name = new Button(getContext());
        new_name.setText(name);
        new_name.setTextSize(16);
        new_name.setTextColor(Color.BLUE);
        new_name.setGravity(Gravity.LEFT);

        Space new_space = new Space(getContext());
        new_space.setLayoutParams(new TableRow.LayoutParams(60, TableRow.LayoutParams.MATCH_PARENT));

        EditText new_value = new EditText(getContext());
        new_value.setText(value);
        new_value.setTextSize(24);
        new_value.setGravity(Gravity.RIGHT);
        new_value.setTextColor(Color.WHITE);
        new_value.setClickable(false);
        new_value.setFocusableInTouchMode(false);
        new_value.setBackgroundResource(android.R.color.transparent);

        switch (type) {
            case 0:
                new_value.setTextColor(Color.WHITE);

                new_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeControlValue(new_name.getText().toString());
                    }
                });
                break;
            case 1:
                new_value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                new_value.setTextColor(Color.BLUE);
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(8);
                new_value.setFilters(FilterArray);

                new_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeControlValue(new_name.getText().toString());
                    }
                });
                break;
        }

        new_row.addView(new_name);
        new_row.addView(new_space);
        new_row.addView(new_value);

        table.addView(new_row);
        control_map.put(name, new_value);
    }

    private void changeControlValue(String name)
    {
        if (!control_map.containsKey(name)) return;
        EditText item = control_map.get(name);

        if (item.getCurrentTextColor() == Color.WHITE) {
            if (item.getText().toString().matches("false")) item.setText("true");
                else item.setText("false");
        }
        else {
            oldEditText = item.getText().toString();
            currentEdit = item;
            currentEdit.setHint("hh:mm:ss");
            currentEdit.setText("");
            currentEdit.setFocusableInTouchMode(true);
            currentEdit.requestFocus();
            currentEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 2 || s.length() == 5) {
                        String str = ':' + s.toString();
                        currentEdit.setText(str);
                    }
                    if (s.length() == 8) {
                        String string = currentEdit.getText().toString();
                        DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
                        try {
                            formatter.parse(string);
                        } catch (ParseException e) {
                            currentEdit.setText(oldEditText);
                        }
                        currentEdit.setFocusableInTouchMode(false);
                        currentEdit.clearFocus();
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    }
                }
            });

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        updateButton.setEnabled(true);
    }

    private void updateOldRow(String name, String value, TableLayout table)
    {
        if (value_map.containsKey(name)) {
            TextView old_value = value_map.get(name);
            old_value.setText(value);
        }
        else createNewRow(name, value, table);
    }

    private void updateOldControlRow(String name, String value, String control_type, TableLayout table)
    {
        if (control_map.containsKey(name)) {
            EditText old_value = control_map.get(name);
            old_value.setText(value);
            return;
        }
        int type = 0;

        if (control_type.matches("boolean")) type = 0;
        else if (control_type.matches("time")) type = 1;

        createNewControlRow(name, value, table, type);
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
