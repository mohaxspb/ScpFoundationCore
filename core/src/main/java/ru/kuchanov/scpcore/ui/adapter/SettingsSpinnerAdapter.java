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
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.util.DimensionUtils;

/**
 * Created by mohax on 02.04.2017.
 * <p>
 * for scp_ru
 */
public class SettingsSpinnerAdapter extends ArrayAdapter<String> {

    private final List<String> data;
    private final List<String> fontsPathsList;
    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public SettingsSpinnerAdapter(
            @NonNull final Context context,
            @LayoutRes final int resource,
            @NonNull final List<String> objects,
            final List<String> fontsPathsList
    ) {
        super(context, resource, objects);
        this.data = objects;
        this.fontsPathsList = fontsPathsList;

        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        View v = convertView;

        final Context context = parent.getContext();

        if (v == null) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.design_list_spinner_item_font, parent, false);
        }

        final String fontPath = fontsPathsList.get(position);
        final TextView textView = (TextView) v;
        textView.setText(data.get(position));
        final int padding = DimensionUtils.getDefaultMargin();
        textView.setPadding(padding, padding, padding, padding);

        textView.setTypeface(FontUtils.getTypeFaceFromName(fontPath));

        final boolean isNightMode = mMyPreferenceManager.isNightMode();
        final int backgroundColorSelected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_selected_dark
                : R.color.settings_spinner_selected_light);
        final int backgroundColorUnselected = ContextCompat.getColor(context, isNightMode
                ? R.color.settings_spinner_unselected_dark
                : R.color.settings_spinner_unselected_light);

        final boolean isSelected = position == fontsPathsList.indexOf(mMyPreferenceManager.getFontPath());

        v.setBackgroundColor(isSelected ? backgroundColorSelected : backgroundColorUnselected);

        return v;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        View v = convertView;
        final Context context = parent.getContext();

        if (v == null) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.design_list_spinner_item_font, parent, false);
        }

        final String fontPath = fontsPathsList.get(position);
        final TextView textView = (TextView) v;
        textView.setText(data.get(position));
        final int padding = DimensionUtils.getDefaultMarginSmall();
        textView.setPadding(padding, padding, padding, padding);

        textView.setTypeface(FontUtils.getTypeFaceFromName(fontPath));

        return v;
    }
}
