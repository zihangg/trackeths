package com.example.trackeths;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.example.trackeths.Globals.TransactionClickListener;
import com.example.trackeths.Globals.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.example.trackeths.Globals.Globals.categoryNames;

public class HomeFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener{


    private final static String TAG = "Testing";
    private static final int PERMISSION_CODE = 42 ;
    private static final int PICTURE_CODE = 43;
    private static final int GALLERY_REQUEST_CODE = 44;
    private RecyclerView expenseList;
    private FirebaseRecyclerAdapter<Model, Holder> adapter;
    private DatabaseReference db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private DrawerLayout drawerLayout;
    private Button addExpense, menu;
    private TextView day, date, profileName, profileEmail;
    private ImageView profilePicture, receipt;
    private EditText spentDescription, spentAmount, editDescription, editAmount;
    private String dbDate, userId, imageUrl;
    private ArrayList<String> categories = new ArrayList<>();
    private Spinner categorySelect;
    private Uri uri;
    private String imagePath = null;

    //required constructor
    public HomeFragment(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view =  inflater.inflate(R.layout.fragment_home, parent, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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

        //remove menu since landscape has slide view for tablets
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && tabletSize){
            menu.setVisibility(View.GONE);
        }

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        db = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        db.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference().child("Images");
        loadCategories(categories);

        expenseList= view.findViewById(R.id.recyclerView);
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
        for (UserInfo user: mAuth.getCurrentUser().getProviderData()){
            Log.i("TAG", user.getProviderId());
        }
        Log.i("TAG", mAuth.getCurrentUser().getUid());
        //open nav bar
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileName = drawerLayout.findViewById(R.id.profileName);
                profileEmail = drawerLayout.findViewById(R.id.profileEmail);
                profilePicture = drawerLayout.findViewById(R.id.profilePicture);

                drawerLayout.openDrawer(GravityCompat.START);


                for (UserInfo user: mAuth.getCurrentUser().getProviderData()){
                    //since firebase is always top provider and google second:
                    if (user.getProviderId().length() > 1 ){
                        profileName.setText(mAuth.getCurrentUser().getDisplayName());
                        profileEmail.setText(mAuth.getCurrentUser().getEmail());
                        if(mAuth.getCurrentUser().getPhotoUrl() == null){
                            //if no profile picture, use default image
                            profilePicture.setImageResource(R.drawable.profile_picture);
                        }
                        else{
                            Uri profilePictureUrl = mAuth.getCurrentUser().getPhotoUrl();
                            Glide.with(getActivity()).load(String.valueOf(profilePictureUrl)).into(profilePicture);
                        }
                    }
                    else{
                        db.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                profileName.setText(user.getName());
                                profileEmail.setText(user.getEmail());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

            }
        });



        //open bottom sheet for transactions adding
        //Todo: add receipt(camera/photo album)
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

                receipt = bottomSheetView.findViewById(R.id.receipt);
                spentDescription = bottomSheetView.findViewById(R.id.spentDescription);
                spentAmount = bottomSheetView.findViewById(R.id.spentAmount);

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                        R.layout.dropdown_category_spinner);
                categorySelect = bottomSheetView.findViewById(R.id.categorySelect);
                categorySelect.setAdapter(dataAdapter);
                loadAdapters(dataAdapter);

                receipt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permission, PERMISSION_CODE);
                        }
                        else{
                            final CharSequence[] options = {"Camera", "Gallery", "Cancel"};
                            AlertDialog.Builder cameraGallery = new AlertDialog.Builder(getActivity());
                            cameraGallery.setTitle("Select Image Source");
                            cameraGallery.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (options[which].equals("Camera")){
                                        startCamera();
                                    }
                                    else if (options[which].equals("Gallery")){
                                        openGallery();
                                    }
                                    else{
                                        dialog.dismiss();
                                    }
                                }
                            });
                            cameraGallery.show();
                        }
                    }
                });

                bottomSheetView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String enterAmount = spentAmount.getText().toString();
                        final String enterDescription = spentDescription.getText().toString();
                        final String enterCategory = categorySelect.getSelectedItem().toString();

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
                            saveTransaction(enterAmount, enterDescription, enterCategory);
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

/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getActivity(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("TAG", "Running onActivityResult");
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case PICTURE_CODE:
                    receipt.setImageURI(uri);
                    break;

                case GALLERY_REQUEST_CODE:
                    uri = data.getData();
                    receipt.setImageURI(uri);
                    break;
            }

        }
    }

    private void startCamera() {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if(camera.resolveActivity(getActivity().getPackageManager())!=null){
            File imageFile = null;

            try{
                imageFile = getImageFile();
            } catch (IOException e){
                e.printStackTrace();
            }

            if (imageFile != null){
                uri = FileProvider.getUriForFile(getActivity(), "com.example.trackeths.fileprovider", imageFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(camera, PICTURE_CODE);
            }
        }





    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void openGallery(){
        Intent gallery = new Intent (Intent.ACTION_PICK);
        gallery.setType("image/*");
        String[] types = {"image/jpeg", "image/png"};
        gallery.putExtra(Intent.EXTRA_MIME_TYPES, types);
        startActivityForResult(gallery, GALLERY_REQUEST_CODE);

    }

    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+timeStamp+"_";
        File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", directory);
        imagePath = imageFile.getAbsolutePath();
        return imageFile;
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

                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                                R.layout.dropdown_category_spinner);
                        categorySelect = editSheetView.findViewById(R.id.categorySelect);
                        categorySelect.setAdapter(dataAdapter);
                        loadAdapters(dataAdapter);
                        Log.i("DEBUG", "Size = " + categories.size());
                        for(int i = 0; i<categories.size(); i++){
                            if(model.getCategory().equals(categories.get(i))){
                                categorySelect.setSelection(i);
                                Log.i("DEBUG", categories.get(i) + i);
                            }
                        }

                        editDescription = editSheetView.findViewById(R.id.editDescription);
                        editAmount = editSheetView.findViewById(R.id.editAmount);
                        receipt = editSheetView.findViewById(R.id.receipt);

                        editDescription.setText(model.getDescription());
                        editAmount.setText(model.getAmount());
                        Glide.with(getActivity()).load(String.valueOf(model.getImageUrl())).into(receipt);

                        receipt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                                    requestPermissions(permission, PERMISSION_CODE);
                                }
                                else{
                                    final CharSequence[] options = {"Camera", "Gallery", "Cancel"};
                                    AlertDialog.Builder cameraGallery = new AlertDialog.Builder(getActivity());
                                    cameraGallery.setTitle("Select Image Source");
                                    cameraGallery.setItems(options, new DialogInterface.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (options[which].equals("Camera")){
                                                startCamera();
                                            }
                                            else if (options[which].equals("Gallery")){
                                                openGallery();
                                            }
                                            else{
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                    cameraGallery.show();
                                }
                            }
                        });


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
                                final String eCategory = categorySelect.getSelectedItem().toString();

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
                                    saveEdit(model.getId(), eAmount, eDescription, eCategory);
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

    private void saveTransaction(final String amount, final String description, final String category){
        final DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        final String id = transactionRef.push().getKey();
        if (uri != null){
            StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(uri));
            fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            Model newEntry = new Model(id, description, amount, category, imageUrl);
                            transactionRef.child(id).setValue(newEntry);
                        }
                    });

                }
            });
        }

    }

    private void saveEdit(String id, String amount, String description, String category){
        DatabaseReference transactionRef = db.child("Transactions").child(dbDate);
        transactionRef.child(id).child("amount").setValue(amount);
        transactionRef.child(id).child("description").setValue(description);
        transactionRef.child(id).child("category").setValue(category);
    }


    //get extension from file to be uploaded (e.g jpg)
    private String getFileExtension(Uri uri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
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


            case R.id.menuHistory:
                HistoryFragment historyFragment = new HistoryFragment();
                fragmentTransaction.add(R.id.drawer, historyFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.menuStatistics:
                StatisticsFragment statisticsFragment = new StatisticsFragment();
                fragmentTransaction.add(R.id.drawer, statisticsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.menuSignOut:
                signOut();
                break;

            default:
                fragmentClass = HomeFragment.class;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadAdapters(final ArrayAdapter<String> adapter){
        adapter.clear();
        for(int i = 0; i<categories.size(); i++){
            adapter.add(categories.get(i));
        }
    }

    private void loadCategories(final ArrayList<String> categories){
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void signOut(){

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());

        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle("CONFIRMATION");
        ad.setMessage("Are you sure you want to sign out?");
        ad.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });

        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }



}
