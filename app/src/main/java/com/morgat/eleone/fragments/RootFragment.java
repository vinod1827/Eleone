package com.morgat.eleone.fragments;

import androidx.fragment.app.Fragment;

import com.morgat.eleone.listeners.OnBackPressListener;
import com.morgat.eleone.utils.BackPressImplimentation;

/**
 * Created by AQEEL on 3/30/2018.
 */

public class RootFragment extends Fragment implements OnBackPressListener {

    @Override
    public boolean onBackPressed() {
        return new BackPressImplimentation(this).onBackPressed();
    }
}