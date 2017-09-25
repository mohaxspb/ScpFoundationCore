package ru.kuchanov.scpcore.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.util.DimensionUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 02.04.2017.
 * <p>
 * for scp_ru
 */
public class SettingsSpinnerAdapter extends ArrayAdapter<String> {

    private List<String> data;
    private List<String> fontsPathsList;
    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public SettingsSpinnerAdapter(
            @NonNull Context context,
            @LayoutRes int resource,
            @NonNull List<String> objects,
            List<String> fontsPathsList
    ) {
        super(context, resource, objects);
        this.data = objects;
        this.fontsPathsList = fontsPathsList;

        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        Context context = parent.getContext();

        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.design_list_spinner_item_font, parent, false);
        }

        String fontPath = fontsPathsList.get(position);
        TextView textView = (TextView) v;
        textView.setText(data.get(position));
        int padding = DimensionUtils.getDefaultMargin();
        textView.setPadding(padding, padding, padding, padding);
        CalligraphyUtils.applyFontToTextView(context, textView, fontPath);

        boolean isNightMode = mMyPreferenceManager.isNightMode();
        int backgroundColorSelected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_selected_dark
                : R.color.settings_spinner_selected_light);
        int backgroundColorUnselected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_unselected_dark
                : R.color.settings_spinner_unselected_light);

        boolean isSelected = position == fontsPathsList.indexOf(mMyPreferenceManager.getFontPath());

        v.setBackgroundColor(isSelected ? backgroundColorSelected : backgroundColorUnselected);

        return v;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            v = inflater.inflate(R.layout.design_list_spinner_item_font, parent, false);
        }

        String fontPath = fontsPathsList.get(position);
        TextView textView = (TextView) v;
        textView.setText(data.get(position));
        int padding = DimensionUtils.getDefaultMarginSmall();
        textView.setPadding(padding, padding, padding, padding);
        CalligraphyUtils.applyFontToTextView(parent.getContext(), textView, fontPath);

        return v;
    }
}
