package com.example.ea;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminMainActivity";
    FirebaseFirestore db;
    String current_user, current_uid, current_ea;
    private Context context = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        context = getApplicationContext();
        db = FirebaseFirestore.getInstance();

        final TableLayout stk = (TableLayout) findViewById(R.id.table_layout_admin);

        Bundle extras = getIntent().getExtras();
        Log.v(TAG, "Get Params passed");
        if (extras == null) {
            return;
        }
        String ea = extras.getString("ea");
        if (ea != null) {
            Log.v(TAG, "EA: " + ea + "- Admin");
            current_ea = ea;
            setTitle(current_ea + "- Admin");
        }
        String value1 = extras.getString("user");
        if (value1 != null) {
            Log.v(TAG, "Admin user : " + value1);
            current_user = value1;
        }
        String uid1 = extras.getString("uid");
        if (uid1 != null) {
            Log.v(TAG, "Admin UID: " + uid1);
            current_uid = uid1;
        }
    }

}
