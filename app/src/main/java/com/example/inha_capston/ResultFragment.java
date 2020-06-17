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
import android.widget.Button;
import android.widget.TextView;

import com.example.inha_capston.handling_audio.Scoring;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {

    static final String TAG = "ResultFragment";

    // context
    private Context mContext;       // getContext()
    private Activity mActivity;     // getActivity()

    private NavController navController;

    private Scoring score;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        TextView result_percentTextView = view.findViewById(R.id.result_frag_percentTextView);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.result_frag_floatingActionButton);

        // load result
        if(getArguments() != null) {
            score = (Scoring)getArguments().getSerializable("RESULT");
            result_percentTextView.setText(score.getResult() + "%");
        }
        else
            result_percentTextView.setText("Error");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_resultFragment_to_audioListFragment);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }
}
