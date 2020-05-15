package com.example.trackeths;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.trackeths.Globals.CategHolder;
import com.example.trackeths.Globals.Category;
import com.example.trackeths.Globals.CategoryClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {

    private DatabaseReference db;
    private FirebaseAuth mAuth;
    Button addCategory;
    RecyclerView categoryList;
    FirebaseRecyclerAdapter<Category, CategHolder> adapter;
    EditText category, editCategory;
    String userId;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
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

        categoryList = view.findViewById(R.id.recyclerView);
        categoryList.setHasFixedSize(true);
        categoryList.setLayoutManager(new LinearLayoutManager(getActivity()));

        addCategory = view.findViewById(R.id.add);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Categories")){
                    listCategories();
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

        addCategory.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        Objects.requireNonNull(getActivity()), R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(
                        R.layout.category_add_sheet,
                        (LinearLayout)getView().findViewById(R.id.categorySheet)
                );

                category = bottomSheetView.findViewById(R.id.categoryName);

                bottomSheetView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String enterCategory = category.getText().toString();

                        if(enterCategory.isEmpty()){
                            Toast.makeText(getActivity(), "Please enter the category.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            saveCategory(enterCategory);
                            Toast.makeText(getActivity(), "Category added!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
                BottomSheetBehavior bottomSheetBehavior = new BottomSheetBehavior();
                bottomSheetBehavior.setPeekHeight(50);
            }
        });

    }

    private void listCategories() {
        DatabaseReference categoryReference = db.child("Categories");
        FirebaseRecyclerOptions cateOptions = new FirebaseRecyclerOptions.Builder<Category>().setQuery(categoryReference, Category.class).build();

        adapter = new FirebaseRecyclerAdapter<Category, CategHolder>
                (cateOptions) {
            @NonNull
            @Override
            public CategHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Log.i("Debugging", "Creating list.");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row, parent, false);
                return new CategHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CategHolder categHolder, int i, @NonNull final Category category) {
                categHolder.category.setText(category.getName());

                categHolder.setCategoryClickListener(new CategoryClickListener() {
                    @Override
                    public void onCategoryClickListener(View v, int position) {
                        final BottomSheetDialog editCategoryDialog = new BottomSheetDialog(
                                getActivity(), R.style.BottomSheetDialogTheme);

                        final View editCategoryView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(
                                R.layout.category_edit_sheet,
                                (LinearLayout)getView().findViewById(R.id.categoryEditSheet)
                        );

                        editCategory = editCategoryView.findViewById(R.id.editCategoryName);
                        editCategory.setText(category.getName());

                        editCategoryView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //confirmation dialog
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                alertDialog.setTitle("CONFIRMATION");
                                alertDialog.setMessage("Are you sure you want to delete?");
                                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteCategory(category.getName());
                                        editCategoryDialog.dismiss();
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

                        editCategoryView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String currentName = category.getName();
                                final String eCategory = editCategory.getText().toString();

                                if (eCategory.isEmpty()){
                                    Toast.makeText(getActivity(), "Please enter all details.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    saveEdit(currentName, eCategory);
                                    Toast.makeText(getActivity(), "Transaction saved!", Toast.LENGTH_SHORT).show();
                                    editCategoryDialog.dismiss();
                                }
                            }
                        });
                        editCategoryDialog.setContentView(editCategoryView);
                        editCategoryDialog.show();
                        BottomSheetBehavior bottomSheetBehavior = new BottomSheetBehavior();
                        bottomSheetBehavior.setPeekHeight(50);
                    }
                });
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        categoryList.setAdapter(adapter);
    }

    private void saveCategory(String name){
        DatabaseReference transactionRef = db.child("Categories");
        Category newCat = new Category(name);
        transactionRef.child(name).setValue(newCat);
    }

    private void deleteCategory(String name){
        DatabaseReference transactionRef = db.child("Categories").child(name);
        transactionRef.removeValue();
    }

    private void saveEdit(String curName, String newName){
        deleteCategory(curName);
        saveCategory(newName);
    }
}