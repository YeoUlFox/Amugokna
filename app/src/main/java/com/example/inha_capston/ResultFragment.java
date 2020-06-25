package com.example.inha_capston;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
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

import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.handling_audio.Scoring;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    private LineChart chart;

    private AnswerSheet answerSheet;

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

        chart =  view.findViewById(R.id.result_frag_chart);

        TextView result_percentTextView = view.findViewById(R.id.result_frag_percentTextView);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.result_frag_floatingActionButton);

        // load result
        if(getArguments() != null) {
            score = (Scoring)getArguments().getSerializable("RESULT");
            answerSheet = score.getAnswerSheet();
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

        makeGraph();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    private LineData generateResultArr(ArrayList<Integer> noteArr,ArrayList<Float> timeArr,ArrayList<Integer> noteArr2,ArrayList<Float> timeArr2){
        float timeNow;
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        List<Entry> tmp_entries = new ArrayList<>();
        tmp_entries.add(new Entry(0, 0));
        LineDataSet set0 = new LineDataSet(tmp_entries,"0");
        dataSets.add(set0);

        for(int i = 0; i < timeArr.size() - 2; i = i + 2){

            List<Entry> entries = new ArrayList<>();
            timeNow = 0;

            while(timeNow <= timeArr.get(i)) {
                timeNow += 0.05f;
            }

            while(timeNow <= timeArr.get(i + 1)){
                entries.add(new Entry(timeNow, noteArr.get(i)));
                timeNow += 0.05f;
            }

            LineDataSet set1 = new LineDataSet(entries,"Music Note");
            set1.setDrawCircleHole(false);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);

            set1.setColor(Color.WHITE);
            set1.setLineWidth(3);

            dataSets.add(set1);
        }

        for(int i = 0; i < timeArr2.size() - 2; i = i + 2){

            List<Entry> entries = new ArrayList<>();
            timeNow = 0;

            while(timeNow <= timeArr2.get(i)) {
                timeNow += 0.05f;
            }

            while(timeNow <= timeArr2.get(i + 1)){
                entries.add(new Entry(timeNow, noteArr2.get(i)));
                timeNow += 0.05f;
            }

            LineDataSet set2 = new LineDataSet(entries,"User Music Note");
            set2.setDrawCircleHole(false);
            set2.setDrawCircles(false);
            set2.setDrawValues(false);

            set2.setColor(Color.RED);
            set2.setLineWidth(3);

            dataSets.add(set2);
        }

        LineData resultData = new LineData(dataSets);
        //모든 데이터셋을 resultData에 추가
        return resultData;
    }

    private void makeGraph(){
        // get Song duration
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(answerSheet.getPlayFile());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int SongTime = Integer.parseInt(durationStr);

        // data formattin
        ArrayList<Float> tmp_float_TimeStamps = new ArrayList<>();
        ArrayList<Integer> tmp_Pitches = new ArrayList<>(answerSheet.getPitches());
        ArrayList<Double> tmp_double_TimeStamps = new ArrayList<>(answerSheet.getTimeStamps());

        ArrayList<Float> tmp_float_TimeStamps_Usr=new ArrayList<>();
        ArrayList<Integer> tmp_Pitches_Usr = new ArrayList<>(score.getUsr_pitches());
        ArrayList<Double> tmp_double_TimeStamps_Usr = new ArrayList<>(score.getUsr_timeStamps());


        for(int i = 0; i < tmp_Pitches.size(); i++) {
            tmp_Pitches.set(i, (tmp_Pitches.get(i) % 10) * 12 + (tmp_Pitches.get(i) / 10));
            tmp_float_TimeStamps.add(tmp_double_TimeStamps.get(i).floatValue());
        }

        for(int i = 0; i < tmp_Pitches_Usr.size(); i++) {
            tmp_Pitches_Usr.set(i, (tmp_Pitches_Usr.get(i) % 10) * 12 + (tmp_Pitches_Usr.get(i) / 10));
            tmp_float_TimeStamps_Usr.add(tmp_double_TimeStamps_Usr.get(i).floatValue());
        }

        chart.setBackgroundColor(Color.rgb(87,87,87));///그래프 디자인
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);

        Description description=new Description();
        description.setTextColor(Color.WHITE);
        description.setText("Red : User   White : Answer");

        chart.setDescription(description);
        chart.setDrawBorders(true);
        chart.setBorderWidth(1);
        chart.setBorderColor(Color.rgb(192,189,186));

        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        //chart.getXAxis().setDrawLabels(false);

        // set Data to chart
        LineData resultData = generateResultArr(tmp_Pitches, tmp_float_TimeStamps,tmp_Pitches_Usr,tmp_float_TimeStamps_Usr);

        chart.setData(resultData);
        chart.setFocusable(false);
        chart.setVisibleXRangeMaximum(3);

        chart.setData(resultData);


        chart.getXAxis().setLabelCount(2, true);
        chart.getXAxis().setAxisMinimum(0.0f);
        chart.getXAxis().setAxisMaximum(SongTime / 1000.0f);


        // chart.moveViewToAnimated(resultData.getDataSetCount(), 0, YAxis.AxisDependency.LEFT, SongTime); //화면 이동 애니메이션
    }
}
