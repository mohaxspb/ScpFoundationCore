package ru.kuchanov.scpcore.ui.adapter;

import android.content.Context;
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

/**
 * Created by mohax on 02.04.2017.
 * <p>
 * for scp_ru
 */
public class SettingsSpinnerCardDesignAdapter extends ArrayAdapter<String> {

    private List<String> data;
    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public SettingsSpinnerCardDesignAdapter(
            @NonNull Context context,
            @LayoutRes int resource,
            @NonNull List<String> objects
    ) {
        super(context, resource, objects);
        this.data = objects;

        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        Context context = parent.getContext();

        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.design_list_spinner_item, parent, false);
        }

        TextView textView = (TextView) v;
        textView.setText(data.get(position));
        int padding = DimensionUtils.getDefaultMargin();
        textView.setPadding(padding, padding, padding, padding);

        boolean isNightMode = mMyPreferenceManager.isNightMode();
        int backgroundColorSelected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_selected_dark
                : R.color.settings_spinner_selected_light);
        int backgroundColorUnselected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_unselected_dark
                : R.color.settings_spinner_unselected_light);

//        data.indexOf(mMyPreferenceManager.getListDesignType());
        boolean isSelected = position == data.indexOf(mMyPreferenceManager.getListDesignType());

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

        TextView textView = (TextView) v;
        textView.setText(data.get(position));
        int padding = DimensionUtils.getDefaultMarginSmall();
        textView.setPadding(padding, padding, padding, padding);

        return v;
    }
}
