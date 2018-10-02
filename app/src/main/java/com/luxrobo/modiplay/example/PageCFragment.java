package com.luxrobo.modiplay.example;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.luxrobo.modiplay.api.core.ModiManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageCFragment extends Fragment {

    private TextView receiveTextView;
    private EditText sendEditText;
    private Button sendButton;

    public ModiManager mModiManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_c, container, false);
        receiveTextView = (TextView) view.findViewById(R.id.receive_text);
        sendEditText = (EditText) view.findViewById(R.id.send_edit_text);
        sendButton = (Button) view.findViewById(R.id.send_button);

        mModiManager = ModiManager.getInstance();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = sendEditText.getText().toString();
                if (text.length() != 0) {
                    mModiManager.sendUserData(Integer.parseInt(text));
                }
            }
        });


        return view;
    }

    public void onReceivedUserData(final int data) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                receiveTextView.setText(String.valueOf(data));
            }
        });
    }

}
