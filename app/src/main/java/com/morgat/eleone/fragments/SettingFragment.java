package com.morgat.eleone.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.activities.MainMenuActivity;
import com.morgat.eleone.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false);


        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.logout_txt).setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.logout_txt:
                Logout();
                break;
        }
    }


    // this will erase all the user info store in locally and logout the user
    public void Logout() {
        ElevenApp.Companion.getPreffs().setUserModel(null);
        requireActivity().finish();
        startActivity(new Intent(getActivity(), MainMenuActivity.class));
    }


}