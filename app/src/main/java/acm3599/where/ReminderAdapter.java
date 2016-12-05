package acm3599.where;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Andrew on 11/30/2016.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public class ReminderViewHolder extends RecyclerView.ViewHolder {

        public CheckBox checkBox;
        public View view;
        public TextView title;
        public TextView location;

        public ReminderViewHolder(View v) {
            super(v);
            view = v;
            checkBox = (CheckBox) v.findViewById(R.id.list_checkbox);
            title = (TextView) v.findViewById(R.id.list_title);
            location = (TextView) v.findViewById(R.id.list_location);
        }
    }

    private List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> r) {
        reminders = r;
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_item, parent, false);

        return new ReminderViewHolder(item);
    }

    @Override
    public void onBindViewHolder(final ReminderViewHolder holder, int position) {
        final Reminder r = reminders.get(position);

        holder.title.setText(r.getTitle());
        holder.location.setText(r.getLocName());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activate(holder, r);
            }
        });
    }

    public void activate(ReminderViewHolder holder, Reminder r) {
        if(holder.checkBox.isChecked()) {
            changeColor(holder, 0xff757575);
            r.setActive(false);
        } else {
            changeColor(holder, 0xff212121);
            r.setActive(true);
        }
    }

    public void changeColor(ReminderViewHolder holder, int color) {
        holder.title.setTextColor(color);
        holder.location.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }
}
