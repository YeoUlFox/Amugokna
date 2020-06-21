package com.example.inha_capston;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.inha_capston.utility_class.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * for setting options fragment
 *
 */
public class OptionFragment extends Fragment {
    private static String TAG = "OptionFragment";

    //
    private Context mContext;
    private Context mActivity;

    private NavController navController;
    private FloatingActionButton floatingActionButton;
    private RadioGroup ScoringOption_radioGroup;

    public OptionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_option, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        floatingActionButton = view.findViewById(R.id.option_frag_floatingActionButton);
        ScoringOption_radioGroup = view.findViewById(R.id.option_frag_Scoring_radioGroup);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(ScoringOption_radioGroup.getCheckedRadioButtonId()) {
                    case R.id.ScoringOption_radioBtn1:
                        // hard
                        SharedPreferencesManager.setScoreOptionValue(mContext, 0);
                        break;
                    case R.id.ScoringOption_radioBtn2:
                        // easy
                        SharedPreferencesManager.setScoreOptionValue(mContext, 1);
                        break;
                }
                navController.navigate(R.id.action_optionFragment_to_recordFragment);
            }
        });
    }
}
