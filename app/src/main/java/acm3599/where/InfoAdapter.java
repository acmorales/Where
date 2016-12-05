package acm3599.where;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 11/30/2016.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {

    public class InfoViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView title;

        public InfoViewHolder(View v) {
            super(v);
            view = v;
            title = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    private List<String> items;

    public InfoAdapter(List<Reminder> r) {
        items = new ArrayList<>();
        for(int i = 0; i < r.size(); i++) {
            items.add(r.get(i).getTitle());
        }
    }

    @Override
    public InfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new InfoViewHolder(item);
    }

    @Override
    public void onBindViewHolder(InfoViewHolder holder, int position) {
        String s = items.get(position);
        holder.title.setText(s);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
