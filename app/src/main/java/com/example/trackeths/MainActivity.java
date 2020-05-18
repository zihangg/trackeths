package com.example.trackeths;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackeths.Globals.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.trackeths.Globals.Globals.categoryNames;

public class MainActivity extends AppCompatActivity {

    EditText email, password, regEmail, regPassword, regName;
    TextView register;
    Button login, google;
    GoogleSignInClient mGoogleSignInClient;

    FirebaseAuth mAuth;
    DatabaseReference db;
    int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        google = findViewById(R.id.googleButton);

        register = findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        //gso code taken from google api page
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).
                requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.googleButton) {
                    googleSignIn();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userEmail = email.getText().toString();
                final String pw = password.getText().toString();
                if (userEmail.isEmpty() && pw.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please ensure all fields are filled in.", Toast.LENGTH_SHORT).show();
                }
                else if (userEmail.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                }
                else if (pw.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                }
                else{
                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            signIn(userEmail, pw);
                            progressDialog.dismiss();
                        }
                    }, 3000);

                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        MainActivity.this, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.register_sheet,
                        (LinearLayout)findViewById(R.id.registerSheet)
                );
                regEmail = bottomSheetView.findViewById(R.id.regEmail);
                regPassword = bottomSheetView.findViewById(R.id.regPassword);
                regName = bottomSheetView.findViewById(R.id.regName);
                bottomSheetView.findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String email = regEmail.getText().toString();
                        final String password = regPassword.getText().toString();
                        final String name = regName.getText().toString();

                        if (email.isEmpty() && password.isEmpty() && name.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please ensure all fields are filled in.", Toast.LENGTH_SHORT).show();
                        }
                        else if (email.isEmpty() && password.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
                        }
                        else if (email.isEmpty() && name.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your email and full name.", Toast.LENGTH_SHORT).show();
                        }
                        else if (password.isEmpty() && name.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your password and full name.", Toast.LENGTH_SHORT).show();
                        }
                        else if (password.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                        }
                        else if (email.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                        }
                        else if (name.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please enter your full name.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog);
                                        progressDialog.setIndeterminate(true);
                                        progressDialog.setMessage("Signing you up...");
                                        progressDialog.show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                FirebaseUser fUser = mAuth.getCurrentUser();
                                                User newUser = new User(email, name);
                                                db.child("Users").child(fUser.getUid()).setValue(newUser);
                                                progressDialog.dismiss();
                                                bottomSheetDialog.dismiss();
                                                Toast.makeText(MainActivity.this, "Sign up successful! Please sign in!", Toast.LENGTH_SHORT).show();
                                            }
                                        }, 2000);
                                    }
                                }
                            });
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }

    private void signIn(final String userEmail, String pw){
        mAuth.signInWithEmailAndPassword(userEmail, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //SaveSharedPreferences.setUsername(MainActivity.this, userEmail);
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        }
    }


    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Users");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final FirebaseUser fUser = mAuth.getCurrentUser();
                    accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(fUser.getUid())){
                                User newUser = new User(fUser.getEmail(), fUser.getDisplayName());
                                db.child("Users").child(fUser.getUid()).setValue(newUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }, 2000);
            //GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
            //SaveSharedPreferences.setUsername(MainActivity.this, acct.getEmail());
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            });
            /*Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);*/
        } catch (ApiException e){
            Log.w("Failed google sign in: ", "Failure code: " + e.getStatusCode() );
        }
    }
}

