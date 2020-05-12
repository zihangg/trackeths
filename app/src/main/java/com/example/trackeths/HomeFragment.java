package com.example.trackeths;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.example.trackeths.Globals.TransactionClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
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
import java.util.Objects;

public class HomeFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener{


    private final static String TAG = "Testing";
    RecyclerView expenseList;
    FirebaseRecyclerAdapter<Model, Holder> adapter;
    private DatabaseReference db;
    private FirebaseAuth mAuth;
    DrawerLayout drawerLayout;
    Button addExpense, menu;
    TextView day, date;
    EditText spentDescription, spentAmount, editDescription, editAmount;
    String dbDate;

    //required constructor
    public HomeFragment(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view =  inflater.inflate(R.layout.fragment_home, parent, false);

        Date c = Calendar.getInstance().getTime();
        String currentDay = LocalDate.now().getDayOfWeek().name();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String currentDate = df.format(c).toUpperCase();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateForFirebase = new SimpleDateFormat("yyyy-MM-dd");
        dbDate = dateForFirebase.format(c);


        day = view.findViewById(R.id.day);
        date = view.findViewById(R.id.date);

        day.setText(currentDay);
        date.setText(currentDate);


        drawerLayout = view.findViewById(R.id.drawer);
        menu = view.findViewById(R.id.menu);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid());
        db.keepSynced(true);

        expenseList=(RecyclerView)view.findViewById(R.id.recyclerView);
        expenseList.setHasFixedSize(true);
        expenseList.setLayoutManager(new LinearLayoutManager(getActivity()));


        //TODO: consider making nav drawer nicer (i.e. refer to XD)
        //make corner for nav drawer rounded
        float radius = getResources().getDimension(R.dimen.roundCorner);
        NavigationView navView = view.findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        MaterialShapeDrawable navBackground = (MaterialShapeDrawable) navView.getBackground();
        navBackground.setShapeAppearanceModel(navBackground.getShapeAppearanceModel()
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                .build());

        addExpense = view.findViewById(R.id.add);
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("TAG", "onViewCreated");

        //open nav bar
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        //open bottom sheet for transactions adding
        //Todo: add receipt(camera/photo album) and category(hard-coded list(?))
        addExpense.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        Objects.requireNonNull(getActivity()), R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(
                        R.layout.expense_add_sheet,
                        (LinearLayout)getView().findViewById(R.id.expenseSheet)
                );

                spentDescription = bottomSheetView.findViewById(R.id.spentDescription);
                spentAmount = bottomSheetView.findViewById(R.id.spentAmount);


                bottomSheetView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String enterAmount = spentAmount.getText().toString();
                        final String enterDescription = spentDescription.getText().toString();

                        if (enterAmount.isEmpty() && enterDescription.isEmpty()){
                            Toast.makeText(getActivity(), "Please enter all details.", Toast.LENGTH_SHORT).show();
                        }
                        else if (enterAmount.isEmpty()){
                            Toast.makeText(getActivity(), "Please enter the amount.", Toast.LENGTH_SHORT).show();
                        }
                        else if (enterDescription.isEmpty()){
                            Toast.makeText(getActivity(), "Please enter the description.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            saveTransaction(enterAmount, enterDescription);
                            Toast.makeText(getActivity(), "Transaction added!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }


    //show all transactions
    private void listExpense(){
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        FirebaseRecyclerOptions modelOptions = new FirebaseRecyclerOptions.Builder<Model>().setQuery(transactionRef, Model.class).build(); //can arrange by date using .query()

        adapter = new FirebaseRecyclerAdapter<Model, Holder>
                (modelOptions) {

            @Override
            protected void onBindViewHolder(@NonNull Holder holder, int i, @NonNull final Model model) {
                holder.mDescription.setText(model.getDescription());
                holder.mAmount.setText("-$" + model.getAmount());

                holder.setTransactionClickListener(new TransactionClickListener() {
                    @Override
                    public void onTransactionClickListener(View v, int position) {
                        final BottomSheetDialog editSheetDialog = new BottomSheetDialog(
                                getActivity(), R.style.BottomSheetDialogTheme);

                        final View editSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(
                                R.layout.expense_edit_sheet,
                                (LinearLayout)getView().findViewById(R.id.expense_edit_Sheet)
                        );

                        editDescription = editSheetView.findViewById(R.id.editDescription);
                        editAmount = editSheetView.findViewById(R.id.editAmount);

                        editDescription.setText(model.getDescription());
                        editAmount.setText(model.getAmount());

                        editSheetView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //confirmation dialog
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                alertDialog.setTitle("CONFIRMATION");
                                alertDialog.setMessage("Are you sure you want to delete?");
                                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteTransaction(model.getId());
                                        editSheetDialog.dismiss();
                                    }
                                });
                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                alertDialog.show();
                            }
                        });


                        //save the edit
                        editSheetView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String eAmount = editAmount.getText().toString();
                                final String eDescription = editDescription.getText().toString();

                                if (eAmount.isEmpty() && eDescription.isEmpty()){
                                    Toast.makeText(getActivity(), "Please enter all details.", Toast.LENGTH_SHORT).show();
                                }
                                else if (eAmount.isEmpty()){
                                    Toast.makeText(getActivity(), "Please enter the amount.", Toast.LENGTH_SHORT).show();
                                }
                                else if (eDescription.isEmpty()){
                                    Toast.makeText(getActivity(), "Please enter the description.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    saveEdit(model.getId(), eAmount, eDescription);
                                    Toast.makeText(getActivity(), "Transaction saved!", Toast.LENGTH_SHORT).show();
                                    editSheetDialog.dismiss();
                                }
                            }
                        });

                        editSheetDialog.setContentView(editSheetView);
                        editSheetDialog.show();
                    }
                });
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

    private void deleteTransaction(String id){
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate).child(id);
        transactionRef.removeValue();
    }

    private void saveTransaction(String amount, String description){
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        String id = transactionRef.push().getKey();
        Model newEntry = new Model(id, description, amount);
        transactionRef.child(id).setValue(newEntry);
    }

    private void saveEdit(String id, String amount, String description){
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        transactionRef.child(id).child("amount").setValue(amount);
        transactionRef.child(id).child("description").setValue(description);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class fragmentClass;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (item.getItemId()){
            case R.id.menuCategories:
                CategoryFragment categoryFragment = new CategoryFragment();
                fragmentTransaction.add(R.id.drawer, categoryFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            default:
                fragmentClass = HomeFragment.class;
        }



        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
