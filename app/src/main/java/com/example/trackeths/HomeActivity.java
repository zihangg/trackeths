package com.example.trackeths;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.example.trackeths.Globals.Holder;
import com.example.trackeths.Globals.Model;
import com.example.trackeths.Globals.TransactionClickListener;
import com.example.trackeths.Globals.ViewPagerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class HomeActivity extends AppCompatActivity{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DatabaseReference db;
    private FirebaseAuth mAuth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_home);


        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            createHomeFrag();
        }
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            if (tabletSize){
                //homeFrag already initialized statically in xml
                tabLayout = findViewById(R.id.tabLayout);
                viewPager = findViewById(R.id.viewPager);
                ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

                Log.i("Debug Orientation", "Orientation is Landscape.");
                adapter.addFragment(new CategoryFragment(), "Category");
                adapter.addFragment(new HistoryFragment(), "History");
                adapter.addFragment(new StatisticsFragment(), "Statistics");
                adapter.addFragment(new ProfileFragment(), "Profile");


                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
            }
            else{
                createHomeFrag();
            }

        }


    }


    private void createHomeFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment homeFrag = new HomeFragment();
        fragmentTransaction.replace(R.id.drawer, homeFrag);
        fragmentTransaction.commit();
    }

}