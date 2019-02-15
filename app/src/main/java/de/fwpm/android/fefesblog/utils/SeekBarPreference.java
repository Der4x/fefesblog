package de.fwpm.android.fefesblog.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import de.fwpm.android.fefesblog.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private TextView textValue;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.seekbar, parent, false);
        textValue = view.findViewById(R.id.textValue);
        TextView textTitle = view.findViewById(R.id.textTitle);
        textTitle.setText(getTitle());
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        SharedPreferences preferences = getSharedPreferences();
        int value = preferences.getInt(getKey(), 10);
        textValue.setText(String.format(Locale.getDefault(), "%d", value));
        seekBar.setProgress(value);
        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        textValue.setText(String.format(Locale.getDefault(), "%d", i));
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(getKey(), i);
        editor.apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}