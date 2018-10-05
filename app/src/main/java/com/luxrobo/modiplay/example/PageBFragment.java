package com.luxrobo.modiplay.example;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.luxrobo.modiplay.api.core.ModiManager;
import com.luxrobo.modiplay.api.enums.State;
import com.luxrobo.modiplay.api.utils.ModiLog;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageBFragment extends Fragment implements Button.OnTouchListener {

    public ModiManager mModiManager;

    private TextView buzzerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_b, container, false);

        buzzerTextView = (TextView) view.findViewById(R.id.buzzer_text);

        mModiManager = ModiManager.getInstance();

        view.findViewById(R.id.joystick_button_up).setOnTouchListener(this);
        view.findViewById(R.id.joystick_button_down).setOnTouchListener(this);
        view.findViewById(R.id.joystick_button_left).setOnTouchListener(this);
        view.findViewById(R.id.joystick_button_right).setOnTouchListener(this);
        view.findViewById(R.id.button_press).setOnTouchListener(this);

        return view;
    }

    public void onBuzzerState(final State.Buzzer state) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (state == State.Buzzer.ON) {
                    buzzerTextView.setText("On");
                } else {
                    buzzerTextView.setText("Off");
                }
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (view.getId()) {
                case R.id.joystick_button_up:
                    ModiLog.d("joystick_button_up down");
                    mModiManager.sendJoystickState(State.Joystick.UP);
                    break;

                case R.id.joystick_button_down:
                    ModiLog.d("joystick_button_down down");
                    mModiManager.sendJoystickState(State.Joystick.DOWN);
                    break;

                case R.id.joystick_button_left:
                    ModiLog.d("joystick_button_left down");
                    mModiManager.sendJoystickState(State.Joystick.LEFT);
                    break;

                case R.id.joystick_button_right:
                    ModiLog.d("joystick_button_right down");
                    mModiManager.sendJoystickState(State.Joystick.RIGHT);
                    break;

                case R.id.button_press:
                    ModiLog.d("button_press down");
                    mModiManager.sendButtonState(State.Button.PRESSED);
                    break;
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            switch (view.getId()) {
                case R.id.joystick_button_up:
                case R.id.joystick_button_down:
                case R.id.joystick_button_left:
                case R.id.joystick_button_right:
                    ModiLog.d("joystick_button up");
                    mModiManager.sendJoystickState(State.Joystick.UNPRESSED);
                    break;

                case R.id.button_press:
                    ModiLog.d("button_press up");
                    mModiManager.sendButtonState(State.Button.UNPRESSED);
                    break;
            }
            return true;
        }
        return false;
    }
}
