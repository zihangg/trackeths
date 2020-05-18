package com.example.trackeths;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.trackeths.Globals.Model;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatisticsFragment extends Fragment {

    private DatabaseReference db;
    private FirebaseAuth mAuth;
    private HashMap<String, Double> categoryAmounts = new HashMap<String, Double>();
    private ArrayList<String> categories = new ArrayList<>();
    private String userId;
    private double sum = 0.00;
    private ArrayList<String> days = new ArrayList<>();
    private Spinner statisticSelect;
    private ArrayList<PieEntry> statisticEntry = new ArrayList<>();
    private PieChart pieChart;

    public StatisticsFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        String[] dayMonthYear = new String[]{
                "Today",
                "Past 7 Days",
                "Past 30 Days"
        };
        statisticSelect = view.findViewById(R.id.statisticSelect);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.statistics_spinner, dayMonthYear);
        adapter.setDropDownViewResource(R.layout.statistics_spinner_dropdown);
        statisticSelect.setAdapter(adapter);


        mAuth = FirebaseAuth.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if(account != null){
            userId = account.getId();
        }
        else{
            userId = mAuth.getCurrentUser().getUid();
        }

        db = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        db.keepSynced(true);

        pieChart = view.findViewById(R.id.pieChart);

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().
                getReference().child("Users").child(userId).child("Categories");

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryAmounts.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    String key = postSnapshot.getKey();
                    categoryAmounts.put(key, 0.00);
                    Log.i("TAG", "Added: " + categoryAmounts.keySet());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String statisticsSelected = statisticSelect.getSelectedItem().toString();


        statisticSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(String keys : categoryAmounts.keySet()){
                    categoryAmounts.put(keys, 0.00);
                    Log.i("TAG", "Checks: " + categoryAmounts.values());
                }

                days.clear();

                int daysBack = 0;
                if(statisticSelect.getSelectedItem().toString().equals("Today")){
                    daysBack = 0;
                }
                else if (statisticSelect.getSelectedItem().toString().equals("Past 7 Days")){
                    daysBack = 7;
                }
                else if (statisticSelect.getSelectedItem().toString().equals("Past 30 Days")){
                    daysBack = 30;
                }


                @SuppressLint("SimpleDateFormat") DateFormat dateForFirebase = new SimpleDateFormat("yyyy-MM-dd");


                if(daysBack == 0){
                    Calendar cal = Calendar.getInstance();
                    String date = dateForFirebase.format(cal.getTime());
                    days.add(date);
                    Log.i("TAG", date);
                    Log.i("TAG", "Added: " + days);
                }
                for (int i = 0; i<daysBack; i++){
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -i);
                    String date = dateForFirebase.format(cal.getTime());
                    days.add(date);
                    Log.i("TAG", date);
                    Log.i("TAG", "Added: " + days);
                }


                loadStatistics(categories);



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void loadStatistics(final ArrayList<String> categories){
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().
                getReference().child("Users").child(userId).child("Categories");


        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    String key = postSnapshot.getKey();
                    categories.add(key);
                    Log.i("DEBUG", "Size = " + categories.size());
                }

                //nesting value listener due to async
                for (int i = 0; i<days.size(); i++){
                    sum = 0.00;
                    Log.i("TAG", "Value of sum: " + sum);
                    for (int x = 0; x<categories.size(); x++){
                        final String categName = categories.get(x);
                        DatabaseReference transactionRef = db.child("Transactions").child(days.get(i));
                        transactionRef.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                    Model record = postSnapshot.getValue(Model.class);
                                    if(record.getCategory().equals(categName)){
                                        sum = categoryAmounts.get(categName);
                                        sum += Double.parseDouble(record.getAmount());
                                        categoryAmounts.put(categName, sum);
                                    }


                                    Log.i("TAG", "Values: " + categoryAmounts.entrySet());

                                }
                                statisticEntry.clear();
                                for (Map.Entry element : categoryAmounts.entrySet()){
                                    String category = element.getKey().toString();
                                    Log.i("TAG", category);
                                    double values = (double)element.getValue();
                                    Log.i("TAG", "Value: " + values);
                                    if (values > 0){
                                        statisticEntry.add(new PieEntry((float)values, category));
                                        Log.i("TAG", "Added: " + statisticEntry);
                                    }
                                }

                                Log.i("TAG", "For PieChart: " + statisticEntry);
                                PieDataSet pieDataSet = new PieDataSet(statisticEntry, "Expenses");
                                pieDataSet.setColors(ColorTemplate.LIBERTY_COLORS);
                                pieDataSet.setValueTextColor(Color.BLACK);
                                pieDataSet.setValueTextSize(16f);

                                Log.i("TAG", "For PieChart: " + pieDataSet);
                                PieData pieData = new PieData((pieDataSet));

                                Log.i("TAG", "For PieChart: " + pieData);
                                pieChart.setData(pieData);
                                pieChart.getDescription().setEnabled(false);
                                pieChart.getLegend().setEnabled(false);
                                pieChart.setCenterText("Expenses");
                                pieChart.setCenterTextSize(22f);
                                pieChart.setEntryLabelColor(Color.BLACK);
                                pieChart.animate();
                                pieChart.notifyDataSetChanged();
                                pieChart.invalidate();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
