package com.example.trackeths;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;


public class HomeActivity extends AppCompatActivity {

    private final static String TAG = "Testing";
    RecyclerView expenseList;
    FirebaseRecyclerAdapter<Model, Holder> adapter;
    private DatabaseReference db;
    private FirebaseAuth mAuth;
    Button addExpense;
    TextView day, date;
    EditText spentDescription, spentAmount;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Date c = Calendar.getInstance().getTime();
        String currentDay = LocalDate.now().getDayOfWeek().name();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String currentDate = df.format(c).toUpperCase();


        day = findViewById(R.id.day);
        date = findViewById(R.id.date);

        day.setText(currentDay);
        date.setText(currentDate);



        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid());
        db.keepSynced(true);

        expenseList=(RecyclerView)findViewById(R.id.recyclerView);
        expenseList.setHasFixedSize(true);
        expenseList.setLayoutManager(new LinearLayoutManager(this));

        addExpense = findViewById(R.id.add);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Transactions")){
                    listExpense();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.i(TAG, "On Start.");
        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        HomeActivity.this, R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.expense_add_sheet,
                        (LinearLayout)findViewById(R.id.expenseSheet)
                );

                spentDescription = bottomSheetView.findViewById(R.id.spentDescription);
                spentAmount = bottomSheetView.findViewById(R.id.spentAmount);

                bottomSheetView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String enterAmount = spentAmount.getText().toString();
                        final String enterDescription = spentDescription.getText().toString();

                        if (enterAmount.isEmpty() && enterDescription.isEmpty()){
                            Toast.makeText(HomeActivity.this, "Please enter all details.", Toast.LENGTH_SHORT).show();
                        }
                        else if (enterAmount.isEmpty()){
                            Toast.makeText(HomeActivity.this, "Please enter the amount.", Toast.LENGTH_SHORT).show();
                        }
                        else if (enterDescription.isEmpty()){
                            Toast.makeText(HomeActivity.this, "Please enter the description.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Model newEntry = new Model(enterDescription, enterAmount);
                            DatabaseReference transactionRef = db.child("Transactions");
                            transactionRef.push().setValue(newEntry);
                            Toast.makeText(HomeActivity.this, "Transaction added!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }

    private void listExpense(){
        DatabaseReference transactionRef = db.child("Transactions");
        FirebaseRecyclerOptions modelOptions = new FirebaseRecyclerOptions.Builder<Model>().setQuery(transactionRef, Model.class).build(); //can arrange by date using .query()

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
                Log.i(TAG, "Creating list.");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                return new Holder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        expenseList.setAdapter(adapter);

    }
}
