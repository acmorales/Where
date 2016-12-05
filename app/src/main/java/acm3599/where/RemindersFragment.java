package acm3599.where;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Andrew on 11/30/2016.
 */

public class RemindersFragment extends Fragment {

    private View view;
    private RecyclerView rView;

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        rView = (RecyclerView) view.findViewById(R.id.reminder_list);
        final ArrayList<Reminder> list = ReminderManager.getInstance().getReminders();
        final ReminderAdapter adapter = new ReminderAdapter(list);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext());
        rView.setLayoutManager(layout);
        rView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if(swipeDir == ItemTouchHelper.RIGHT) {
                    ReminderManager.getInstance().remove(list.get(viewHolder.getAdapterPosition()));
                    list.remove(viewHolder.getAdapterPosition());
                    adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.reminders_fragment, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
