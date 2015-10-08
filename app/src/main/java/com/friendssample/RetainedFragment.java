package com.friendssample;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class RetainedFragment extends Fragment {
    WriteDbTask writeDbTask;
    String sortOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (writeDbTask != null)
            writeDbTask.attach((MainActivity)activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (writeDbTask != null)
            writeDbTask.detach();
    }
};