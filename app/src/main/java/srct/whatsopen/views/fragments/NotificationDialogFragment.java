package srct.whatsopen.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import srct.whatsopen.presenters.NotificationPresenter;
import srct.whatsopen.views.NotificationView;


public class NotificationDialogFragment extends DialogFragment implements NotificationView {

    public interface NotificationDialogListener {
        void onSetNotification();
    }

    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_NAME = "ARG_NAME";

    @BindView(R.id.type_closing_check) CheckBox typeClosingCheckBox;
    @BindView(R.id.type_opening_check) CheckBox typeOpeningCheckBox;
    @BindView(R.id.interval_on_check) CheckBox intervalOnCheckBox;
    @BindView(R.id.interval_15_check) CheckBox interval15CheckBox;
    @BindView(R.id.interval_30_check) CheckBox interval30CheckBox;
    @BindView(R.id.interval_hour_check) CheckBox intervalHourCheckBox;
    @BindView(R.id.save_button) Button saveButton;
    @BindView(R.id.cancel_button) Button cancelButton;

    private String mName;
    private NotificationPresenter mPresenter;
    private boolean inEditMode;
    private Handler mHandler;

    public NotificationDialogFragment() {
    }

    public static NotificationDialogFragment newInstance(String name, boolean inEditMode) {
        NotificationDialogFragment frag = new NotificationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putBoolean(ARG_MODE, inEditMode);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate arguments
        mName = getArguments().getString(ARG_NAME);
        inEditMode = getArguments().getBoolean(ARG_MODE);

        // Set up presenter
        mPresenter = new NotificationPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

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

        if(inEditMode) {
            mPresenter.presentNotifications(mName);
        }
    }

    @OnClick(R.id.save_button)
    public void onSave() {
        mPresenter.saveNotifications(mName, inEditMode, typeOpeningCheckBox.isChecked(),
                typeClosingCheckBox.isChecked(), intervalOnCheckBox.isChecked(),
                interval15CheckBox.isChecked(), interval30CheckBox.isChecked(),
                intervalHourCheckBox.isChecked());

        NotificationDialogListener listener = (NotificationDialogListener) getActivity();
        listener.onSetNotification();
    }

    @OnClick(R.id.cancel_button)
    public void onCancel() {
        Toast.makeText(getActivity(), "Canceled.", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void setNotificationChecks(boolean opening, boolean closing,
                                      boolean interval_on, boolean interval_15,
                                      boolean interval_30, boolean interval_hour) {

        typeOpeningCheckBox.setChecked(opening);
        typeClosingCheckBox.setChecked(closing);
        intervalOnCheckBox.setChecked(interval_on);
        interval15CheckBox.setChecked(interval_15);
        interval30CheckBox.setChecked(interval_30);
        intervalHourCheckBox.setChecked(interval_hour);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
