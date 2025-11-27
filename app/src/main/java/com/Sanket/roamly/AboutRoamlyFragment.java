package com.Sanket.roamly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AboutRoamlyFragment extends Fragment {

    public AboutRoamlyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about__roamly, container, false);

        ImageView logo = view.findViewById(R.id.app_logo);
        TextView name = view.findViewById(R.id.app_name);

        Animation fade = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        fade.setDuration(800);

        logo.startAnimation(fade);
        name.startAnimation(fade);

        return view;
    }
}
