package com.example.ea;

// A few code samples taken from http://www.androiddeft.com/2018/01/28/android-login-with-google-account

import android.graphics.Color;
import android.support.annotation.ColorLong;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EAMainActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    Spinner spinnerDropDown;
    String ea = null;
    String[] Establishments = {
            "Aseem Photo Shop",
            "Suresh Photo Shop",
            "--------------------------",
            "Register New Establishment"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        Button signOutButton = findViewById(R.id.sign_out_button);
        pDialog = new ProgressDialog(MainActivity.this);

        signInButton.setSize(SignInButton.SIZE_WIDE);// wide button style

        // Configure Google Sign In
        // default_web_client_id is from https://console.developers.google.com/apis/credentials
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        Button continue_guest = (Button)findViewById(R.id.continue_guest);
        continue_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                Log.v(TAG, "Name: " + user.getDisplayName() + " : " + user.getUid());
                Intent i = new Intent(MainActivity.this, start_main.class);
                i.putExtra("user1", user.getDisplayName());
                i.putExtra("uid1", user.getUid());
                i.putExtra("ea", ea);
                i.putExtra("adminLogin", 0);
                if (TextUtils.isEmpty(ea)) {
                    Toast.makeText(getBaseContext(), "Please select Establishment",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                    startActivity(i);
            }
        });
        Button continue_admin = (Button)findViewById(R.id.continue_admin);
        continue_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                Log.v(TAG, "Name: " + user.getDisplayName() + " : " + user.getUid());
                Intent i = new Intent(MainActivity.this, start_main.class);
                i.putExtra("user1", user.getDisplayName());
                i.putExtra("uid1", user.getUid());
                i.putExtra("ea", ea);
                i.putExtra("adminLogin", 1);
                if (TextUtils.isEmpty(ea)) {
                    Toast.makeText(getBaseContext(), "Please select Establishment",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(i);
            }
        });

        spinnerDropDown = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.
                R.layout.simple_spinner_item, Establishments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDropDown.setAdapter(adapter);

        spinnerDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                 int position, long id) {
                // Get select item
                int sid = spinnerDropDown.getSelectedItemPosition();
                Toast.makeText(getBaseContext(), Establishments[sid],
                        Toast.LENGTH_SHORT).show();
                ea = Establishments[sid];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * Display Progress bar while Logging in
     */

    private void displayProgressDialog() {
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.w(TAG, "Google sign in passed");
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        displayProgressDialog();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.v(TAG, "Name: " + user.getDisplayName() + " : " + user.getUid());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed: ", Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }

                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        Log.w(TAG, "UpdateUI: Google sign in passed");
        TextView displayName = findViewById(R.id.displayName);
        ImageView profileImage = findViewById(R.id.profilePic);
        if (user != null) {
            displayName.setText(user.getDisplayName());
            displayName.setVisibility(View.VISIBLE);
            // Loading profile image
            Uri profilePicUrl = user.getPhotoUrl();
            if (profilePicUrl != null) {
                Glide.with(this).load(profilePicUrl)
                        .into(profileImage);
                Log.w(TAG, "UpdateUI: Profile pic is OK");
            } else
                Log.w(TAG, "UpdateUI: Profile pic is null");
            //profileImage.requestLayout(cen);
            profileImage.setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.continue_guest).setVisibility(View.VISIBLE);
            findViewById(R.id.continue_admin).setVisibility(View.VISIBLE);
            findViewById(R.id.textView1).setVisibility(View.VISIBLE);
            findViewById(R.id.spinner1).setVisibility(View.VISIBLE);
            Log.w(TAG, "UpdateUI: update name and pic");
        } else {
            displayName.setVisibility(View.GONE);
            profileImage.setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.continue_guest).setVisibility(View.GONE);
            findViewById(R.id.continue_admin).setVisibility(View.GONE);
            findViewById(R.id.textView1).setVisibility(View.GONE);
            findViewById(R.id.spinner1).setVisibility(View.GONE);
            Log.w(TAG, "UpdateUI: update name and pic - failed");
        }
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

}