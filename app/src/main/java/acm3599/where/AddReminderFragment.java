package acm3599.where;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;


public class AddReminderFragment extends Fragment {

    public interface OnSubmitCallback {
        public void submit(Reminder reminder);
    }

    private String TAG = "AddReminderFragment";
    private OnSubmitCallback callback;
    private View view;
    private Place reminderLoc;
    private boolean hasLocation;
    private EditText titleET;
    private EditText contentET;
    private EditText locationET;
    private Button submitButton;

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        titleET = (EditText) view.findViewById(R.id.reminder_title);
        contentET = (EditText) view.findViewById(R.id.reminder_content);
        locationET = (EditText) view.findViewById(R.id.reminder_location);
        submitButton = (Button) view.findViewById(R.id.submit_button);

        // listens for when user clicks on edit text field
        // from http://stackoverflow.com/questions/2119072/how-to-do-something-after-user-clicks-on-my-edittext
        locationET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    launchAutoComplete();
                }
                return false;
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(titleET.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Must enter a title", Toast.LENGTH_SHORT).show();
                    return;
                }
                callback.submit(new Reminder
                        (titleET.getText().toString(), contentET.getText().toString(), reminderLoc));
            }
        });
    }

    public void launchAutoComplete() {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(getActivity());

            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        reminderLoc = PlaceAutocomplete.getPlace(getContext(), data);
        if(reminderLoc != null) {
            locationET.setText(reminderLoc.getName());
            Log.i(TAG, "Place selected: " + reminderLoc.getName());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (OnSubmitCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_add_reminder, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        titleET = null;
        contentET = null;
        locationET = null;
    }
}
