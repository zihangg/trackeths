package com.example.trackeths;

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
import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
    EditText category;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        Log.i("Debugging", "Starting Category Fragment");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid());
        db.keepSynced(true);

        categoryList = (RecyclerView) view.findViewById(R.id.recyclerView);
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
            protected void onBindViewHolder(@NonNull CategHolder categHolder, int i, @NonNull Category category) {
                categHolder.category.setText(category.getName());
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
}