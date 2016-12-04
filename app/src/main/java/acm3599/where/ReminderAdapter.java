package acm3599.where;

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
        public TextView content;
        public TextView location;

        public ReminderViewHolder(View v) {
            super(v);
            view = v;
            checkBox = (CheckBox) v.findViewById(R.id.list_checkbox);
            title = (TextView) v.findViewById(R.id.list_title);
            content = (TextView) v.findViewById(R.id.list_content);
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
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        Reminder r = reminders.get(position);

        holder.title.setText(r.getTitle());
        holder.content.setText(r.getContent());
        holder.location.setText(r.getLocName());

        // perhaps set checkbox listener here or s/t
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }
}
