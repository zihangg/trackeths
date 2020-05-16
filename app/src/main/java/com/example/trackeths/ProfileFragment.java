package com.example.trackeths;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    TextView profileName, profileEmail;
    ImageView profilePicture;
    Button exit;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profilePicture = view.findViewById(R.id.profilePicture);
        exit = view.findViewById(R.id.exit);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (account != null){
            profileName.setText(account.getDisplayName());
            profileEmail.setText(account.getEmail());
            if(account.getPhotoUrl() == null){
                //if no profile picture, use default image
                profilePicture.setImageResource(R.drawable.profile_picture_white);
            }
            else{
                Uri profilePictureUrl = account.getPhotoUrl();
                Glide.with(getActivity()).load(String.valueOf(profilePictureUrl)).into(profilePicture);
            }
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        return view;
    }

    private void signOut(){

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());

        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle("CONFIRMATION");
        ad.setMessage("Are you sure you want to sign out?");
        ad.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (account != null){
                    GoogleSignInClient signInClient = GoogleSignIn.getClient(getActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
                    signInClient.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), "Signed out successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    mAuth.signOut();
                }
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
