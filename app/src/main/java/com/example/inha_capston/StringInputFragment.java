package com.example.inha_capston;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;


/**
 * for Input String
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StringInputFragment extends Fragment
{
    // key
    private final String key = "INPUT_STRING";

    // UIs
    private NavController navController;
    private ExtendedFloatingActionButton floatingActionButton;
    private TextInputEditText textInputEditText;

    public StringInputFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_string_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        floatingActionButton = view.findViewById(R.id.stringInputFrag_floating_action_button);
        textInputEditText = view.findViewById(R.id.stringInputFrag_EditText);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputted = textInputEditText.getText().toString();

//                if(isValidFileName(inputted)) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString(key, inputted);
//                    return;
//                }

                Toast.makeText(getContext(), "입력을 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
