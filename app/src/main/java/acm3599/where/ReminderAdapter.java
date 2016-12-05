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
        Reminder r = reminders.get(position);

        holder.title.setText(r.getTitle());
        holder.location.setText(r.getLocName());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()) {
                    holder.title.setTextColor(0xff757575);
                    holder.location.setTextColor(0xff757575);
                } else {
                    holder.title.setTextColor(0xff212121);
                    holder.location.setTextColor(0xff212121);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }
}
