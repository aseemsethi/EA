package com.example.ea;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminMainActivity";
    FirebaseFirestore db;
    String current_user, current_uid, current_ea;
    private Context context = null;
    String checkedUser = null;

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
        // DB Get from FireCloud - Users
        Button usersB = (Button) findViewById(R.id.table_layout_users_row_buttonA);
        usersB.setOnClickListener(new View.OnClickListener() {
            List<String> allUsers = new ArrayList<String>();
            @Override public void onClick(View v) {
                v.startAnimation(buttonClick);
                Log.v(TAG, "Get Database entries");
                deleteAllTableRows();
                // String cp = current_ea + "/Photos/" + current_user;
                String cp = current_ea;
                db.collection(cp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Map<String, Object> map = document.getData();
                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                            //Log.d(TAG, entry.getKey().toString());
                                            if(entry.getKey().toString().equals("Name")) {
                                                Log.d(TAG, entry.getValue().toString());
                                                allUsers.add(entry.getValue().toString());
                                            }
                                        }
                                        addRow(allUsers);
                                        allUsers.clear();
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });

        // DB Get from FireCloud - Clicked User's Photos
        Button photosB = (Button) findViewById(R.id.table_layout_photos_row_buttonA);
        photosB.setOnClickListener(new View.OnClickListener() {
            List<String> allPhotos = new ArrayList<String>();
            @Override public void onClick(View v) {
                v.startAnimation(buttonClick);
                Log.v(TAG, "Get Database entries");
                checkedUser = getCheckedRow();
                deleteAllTableRows();
                String cp = current_ea + "/" + checkedUser + "/Photos";
                db.collection(cp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Map<String, Object> map = document.getData();
                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                            //Log.d(TAG, entry.getKey().toString());
                                            Log.d(TAG, entry.getValue().toString());
                                            allPhotos.add(entry.getValue().toString());
                                        }
                                        addPhotosRow(allPhotos);
                                        allPhotos.clear();
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });

        // DB Get from FireCloud - Ordered User's Photos
        Button ordersB = (Button) findViewById(R.id.table_layout_orders_row_buttonA);
        ordersB.setOnClickListener(new View.OnClickListener() {
            List<String> allPhotos = new ArrayList<String>();
            @Override public void onClick(View v) {
                v.startAnimation(buttonClick);
                Log.v(TAG, "Get Database entries");
                checkedUser = getCheckedRow();
                deleteAllTableRows();
                String cp = current_ea + "/" + checkedUser + "/Photos";
                db.collection(cp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Map<String, Object> map = document.getData();
                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                            Log.d(TAG, entry.getValue().toString());
                                            allPhotos.add(entry.getValue().toString());
                                        }
                                        if (allPhotos.get(2).equals("yes"))
                                            addPhotosRow(allPhotos);
                                        allPhotos.clear();
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    private String getCheckedRow() {
        final TableLayout stk = (TableLayout) findViewById(R.id.table_layout_admin);
        int rowCount = stk.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            View rowView = stk.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow tableRow = (TableRow) rowView;
                int columnCount = tableRow.getChildCount();
                for (int j = 0; j < columnCount; j++) {
                    View columnView = tableRow.getChildAt(j);
                    if (columnView instanceof CheckBox) {
                        CheckBox checkboxView = (CheckBox) columnView;
                        if (checkboxView.isChecked()) {
                            TextView v1 = (TextView) tableRow.getChildAt(0);
                            Log.v(TAG, "ID: " + v1.getText().toString());
                            return v1.getText().toString();
                        }
                    }
                }
            }
        }
        Log.v(TAG, "checkedUser is saved checked state");
        return checkedUser;
    }

    private void deleteAllTableRows() {
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_admin);
        for (;;) {
            int childCount = stk.getChildCount();
            Log.v(TAG, "TableLayout size: " + childCount);

            // Remove all rows except the first one
            if (childCount > 5) {
                stk.removeViewAt(childCount - 1);
            } else
                break;
        }
    }

    private void addRow(List<String> allUsers) {
        // Create a new table row.
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_admin);

        TableRow tbrow = new TableRow(AdminActivity.this);
        Log.v(TAG, "Adding Row");
        tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView t1v = new EditText(context);
        t1v.setText(allUsers.get(0));
        t1v.setTextColor(Color.BLUE);
        t1v.setGravity(Gravity.START);
        TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        lparams.weight = 3f;
        lparams.width = 0;
        t1v.setLayoutParams(lparams);
        tbrow.addView(t1v, 0);

               // Add a checkbox in the third column.
        CheckBox checkBox = new CheckBox(context);
        checkBox.setGravity(Gravity.RIGHT);
        lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        lparams.weight = 0.5f;
        lparams.width = 0;
        checkBox.setLayoutParams(lparams);
        tbrow.addView(checkBox, 1);

        stk.addView(tbrow);
        tbrow.setBackgroundResource(R.drawable.row_border);
    }

    private void addPhotosRow(List<String> photos) {
        // Create a new table row.
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_admin);

        TableRow tbrow = new TableRow(AdminActivity.this);
        Log.v(TAG, "Adding Row");
        tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView t1v = new EditText(context);
        t1v.setText(photos.get(0));
        t1v.setTextColor(Color.BLUE);
        t1v.setGravity(Gravity.START);
        TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        lparams.weight = 0.75f;
        lparams.width = 0;
        t1v.setLayoutParams(lparams);
        tbrow.addView(t1v, 0);

        TextView t2v = new EditText(context);
        t2v.setText(photos.get(1));
        t2v.setTextColor(Color.BLUE);
        t2v.setGravity(Gravity.START);
        lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        lparams.weight = 2;
        lparams.width = 0;
        t2v.setLayoutParams(lparams);
        tbrow.addView(t2v, 1);

        // Add a checkbox in the third column.
        CheckBox checkBox = new CheckBox(context);
        checkBox.setGravity(Gravity.RIGHT);
        lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        lparams.weight = 0.5f;
        lparams.width = 0;
        checkBox.setLayoutParams(lparams);
        tbrow.addView(checkBox, 2);

        stk.addView(tbrow);
        tbrow.setBackgroundResource(R.drawable.row_border);
    }
}
