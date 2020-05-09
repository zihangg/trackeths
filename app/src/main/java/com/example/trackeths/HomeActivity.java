package com.example.trackeths;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    Button addExpense;
    TextView day, date;

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

        addExpense = findViewById(R.id.add);
        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        HomeActivity.this, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.expense_add_sheet,
                        (LinearLayout)findViewById(R.id.expenseSheet)
                );
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }
}
