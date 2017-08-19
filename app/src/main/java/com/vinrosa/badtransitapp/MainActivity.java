package com.vinrosa.badtransitapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vinrosa.badtransitapp.fragments.CreateAccountFragment;
import com.vinrosa.badtransitapp.fragments.ItemsFragment;
import com.vinrosa.badtransitapp.fragments.LoginFragment;
import com.vinrosa.badtransitapp.fragments.ReportDetailFragment;

public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnFragmentInteractionListener,
        CreateAccountFragment.OnFragmentInteractionListener,
        ItemsFragment.OnFragmentInteractionListener, ReportDetailFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormActivity.class);
                startActivity(intent);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_frame_layout, ItemsFragment.newInstance())
                            .addToBackStack(null)
                            .commit();
                    mFab.setVisibility(View.VISIBLE);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_frame_layout, LoginFragment.newInstance())
                            .commit();
                    mFab.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.sharemenuItem: // Share option of the report detail fragment
                // Find the current fragment by its tag
                ReportDetailFragment detailFragment = (ReportDetailFragment) getSupportFragmentManager().findFragmentByTag(ReportDetailFragment.Tag);
                // detailFragment.ShareReport();
                Toast.makeText(this, detailFragment.ShareReport(), Toast.LENGTH_LONG).show();

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getSupportFragmentManager()
                   .popBackStackImmediate();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // LoginFragment.OnFragmentInteractionListener.
    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, R.string.success_label, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailed() {
        Toast.makeText(this, R.string.failed_label,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateAccount() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, CreateAccountFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onItemSelected(String itemID) {
        Toast.makeText(this, itemID, Toast.LENGTH_LONG).show();
    }
    // CreateAccountFragment.OnFragmentInteractionListener.
    @Override
    public void onAccountSuccessfullyCreated() {

    }
    // ItemsFragment.OnFragmentInteractionListener.
    @Override
    public void onItemSelected() {

    }

    public void logout() {
        if (mAuth != null) mAuth.signOut();
    }

}
