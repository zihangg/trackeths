package com.example.trackeths;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackeths.Globals.CategHolder;
import com.example.trackeths.Globals.Category;
import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.trackeths.Globals.Globals.categoryNames;

public class HistoryFragment extends Fragment {

    private DatabaseReference db;
    private FirebaseAuth mAuth;
    private Calendar myCalendar;
    private EditText dateSelect;
    private RecyclerView historyList;
    private String dbDate;
    FirebaseRecyclerAdapter<Model, Holder> adapter;
    String userId;

    public HistoryFragment(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        Log.i("Debugging", "Starting Category Fragment");
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

        historyList= view.findViewById(R.id.recyclerView);
        historyList.setHasFixedSize(true);
        historyList.setLayoutManager(new LinearLayoutManager(getActivity()));

        myCalendar = Calendar.getInstance();
        dateSelect = view.findViewById(R.id.dateSelect);

        Date c = myCalendar.getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateForFirebase = new SimpleDateFormat("yyyy-MM-dd");
        dbDate = dateForFirebase.format(c);
        dateSelect.setText(dbDate);


        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Transactions")){
                    listHistory();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
                listHistory();
            }
        };

        dateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Debug", "Datepicker initialized.");
                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePicker.show();
            }
        });

    }

    private void listHistory() {
        Log.i("Debug", "Running listHistory()");
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        FirebaseRecyclerOptions modelOptions = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(transactionRef, Model.class).build();
        adapter = new FirebaseRecyclerAdapter<Model, Holder>
                (modelOptions) {

            @Override
            protected void onBindViewHolder(@NonNull Holder holder, int i, @NonNull Model model) {
                holder.mDescription.setText(model.getDescription());
                holder.mAmount.setText("-$" + model.getAmount());
            }

            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Log.i("Debug", "Listing past expenses.");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                return new Holder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        historyList.setAdapter(adapter);
    }


    private void updateDate(){
        Log.i("Debug", "Running updateDate()");
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.US);
        dateSelect.setText(df.format(myCalendar.getTime()));
        dbDate = dateSelect.getText().toString();

    }
}
