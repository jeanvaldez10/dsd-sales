package dsd.dsdvansales.views;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.Nullable;

import java.util.List;

import dsd.dsdvansales.R;

public class AlertSingleChoiceAdapter extends ArrayAdapter<CharSequence> {

    Activity activity;
    public AlertSingleChoiceAdapter(Context context, @Nullable String[] objects) {
        super(context, R.layout.custom_alert_choice_view, objects);
        activity = (Activity)context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.custom_alert_choice_view, parent, false);
        }
        int nWidth = parent.getWidth();
        if(nWidth != 0)
        {
            ((CheckedTextView)convertView).setWidth(nWidth);
        }
        ((CheckedTextView)convertView).setText(getItem(position));
        return convertView;
    }
}
