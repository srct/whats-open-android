package srct.whatsopen.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import srct.whatsopen.R;


public class NotificationDialogFragment extends DialogFragment {

    @BindView(R.id.type_closing_check) CheckBox typeClosingCheckBox;
    @BindView(R.id.type_opening_check) CheckBox typeOpeningCheckBox;
    @BindView(R.id.interval_on_check) CheckBox intervalOnCheckBox;
    @BindView(R.id.interval_15_check) CheckBox interval15CheckBox;
    @BindView(R.id.interval_30_check) CheckBox interval30CheckBox;
    @BindView(R.id.interval_hour_check) CheckBox intervalHourCheckBox;
    @BindView(R.id.save_button) Button saveButton;
    @BindView(R.id.cancel_button) Button cancelButton;

    public NotificationDialogFragment() {
    }

    public static NotificationDialogFragment newInstance(String title) {
        NotificationDialogFragment frag = new NotificationDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.save_button)
    public void onSave() {
        Toast.makeText(getContext(), "Notifications set.", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @OnClick(R.id.cancel_button)
    public void onCancel() {
        Toast.makeText(getContext(), "Canceled.", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
