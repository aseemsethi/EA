package com.example.ea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class start_main extends AppCompatActivity {
    private static final String TAG = "SMMainActivity";
    FirebaseUser user;
    private Context context = null;
    FirebaseFirestore db;
    String current_user, current_uid, current_ea;
    String current_admin = null;
    private static final int REQUEST_GET_SINGLE_FILE = 202;
    Integer found = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_main_new);
        context = getApplicationContext();
        db = FirebaseFirestore.getInstance();

        final TableLayout stk = (TableLayout) findViewById(R.id.table_layout_table);

        Bundle extras = getIntent().getExtras();
        Log.v(TAG, "Get Params passed");
        if (extras == null) {
            return;
        }
        String ea = extras.getString("ea");
        if (ea != null) {
            Log.v(TAG, "EA: " + ea);
            current_ea = ea;
            setTitle(current_ea);
            if (ea.equals("Register New Establishment")) {
                Toast.makeText(start_main.this,
                        "Please contact App Administrator\n For Registering a New Establishment",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
        String value1 = extras.getString("user1");
        if (value1 != null) {
            Log.v(TAG, "user found: " + value1);
            current_user = value1;
        }
        addNewContact();
        // Check whether this current_user is continuing as admin
        Integer isAdmin = extras.getInt("adminLogin");
        if (isAdmin == 1) {
            current_admin = current_user;
            Log.v(TAG, "admin status: " + current_admin);
            // Check if current_user is the admin of this Establishment current_ea
            // The admin setting of True for an EA is done manually by the App Developer
            DocumentReference docRef = db.collection(current_ea).document(current_user);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if ((document.getData().get("adminFor")) == null) {
                                Log.d(TAG, "User is not an admin");
                                Toast toast = Toast.makeText(start_main.this, "User is not Admin",
                                        Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                                finish(); return;
                            }
                            Log.d(TAG, "Admin for: " + document.getData().get("adminFor").toString());
                            Log.d(TAG, "User is an admin");
                            if (document.getData().get("adminFor").equals(current_ea)) {
                                startAdmin();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        String uid1 = extras.getString("uid1");
        if (uid1 != null) {
            Log.v(TAG, "UID: " + uid1);
            current_uid = uid1;
        }


        // DB Get from FireCloud - Gallery
        Button getRowButton = (Button) findViewById(R.id.table_layout_get_row_button);
        getRowButton.setOnClickListener(new View.OnClickListener() {
            List<String> photos = new ArrayList<String>();
            @Override public void onClick(View v) {
                Log.v(TAG, "Get Database entries");
                v.startAnimation(buttonClick);
                deleteAllTableRows();
                String cp = current_ea + "/" + current_user + "/Photos";
                db.collection(cp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        photos.add(document.getData().get("photoNo").toString());
                                        photos.add(document.getData().get("photoText").toString());
                                        addRow(photos, 1);
                                        photos.clear();
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });

        // Add a new photo
        Button addRowButton = (Button) findViewById(R.id.table_layout_add_row_button);
        addRowButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Create a new table row.
                v.startAnimation(buttonClick);
                deleteAllTableRows();
                TableRow tbrow = new TableRow(start_main.this);
                Log.v(TAG, "Adding Row");
                tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                TextView t1v = new EditText(context);
                t1v.setText("No.");
                t1v.setTextColor(Color.BLUE);
                t1v.setGravity(Gravity.START);
                TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                lparams.weight = 0.75f;
                lparams.width = 0;
                t1v.setLayoutParams(lparams);
                tbrow.addView(t1v, 0);

                TextView t2v = new EditText(context);
                t2v.setText("  ");
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
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
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
                                            Log.v(TAG, "Adding ID: " + v1.getText().toString());
                                            TextView v2 = (TextView) tableRow.getChildAt(1);
                                            Log.v(TAG, "Adding Photo: " + v2.getText().toString());
                                            addNewPhoto(v1.getText().toString(), v2.getText().toString(), "no", "1", "Passport");
                                            stk.removeViewAt(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        }                    }
                });
            }
        });

        // Non Gallery New Order
        Button newOrderRowButton = (Button) findViewById(R.id.table_layout_newOrder_row_button);
        newOrderRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                deleteAllTableRows();
                // r is the layout where your inflated layout will be added
                final RelativeLayout r = findViewById(R.id.guest_layout_main);
                LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View myView = linflater.inflate(R.layout.templates, (ViewGroup) r, false); //here item is the the layout you want to inflate
                r.addView(myView);
                Button orderRowButton = (Button) findViewById(R.id.orderPhoto);
                orderRowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final RelativeLayout layout = findViewById(R.id.guest_layout);
                        EditText e1 = findViewById(R.id.PhotoNameE);
                        EditText c1 = findViewById(R.id.copiesE);
                        EditText s1 = findViewById(R.id.sizeE);
                        addNewPhoto("000", e1.getText().toString(), "yes",
                                c1.getText().toString(), s1.getText().toString());
                        r.removeView(layout);
                    }
                });
                Button cancelRowButton = (Button) findViewById(R.id.cancelPhoto);
                cancelRowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final RelativeLayout layout = findViewById(R.id.guest_layout);
                        r.removeView(layout);
                    }
                });
                Button s = (Button) findViewById(R.id.select);
                s.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickFromGallery();
                    }
                });
            }
        });

        // Gallery to Orders to DB
        Button orderRowButton = (Button) findViewById(R.id.table_layout_order_row_button);
        orderRowButton.setOnClickListener(new View.OnClickListener() {
            String photoNum = null, photoName = null;

            @Override public void onClick(View v) {
                v.startAnimation(buttonClick);
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
                                    Log.v(TAG, "Ordering ID: " + v1.getText().toString());
                                    TextView v2 = (TextView) tableRow.getChildAt(1);
                                    Log.v(TAG, "Ordering Photo: " + v2.getText().toString());
                                    photoNum = v1.getText().toString();
                                    photoName = v2.getText().toString();
                                    break;
                                }
                            }
                        }
                    }
                }

                deleteAllTableRows();
                // Bring up the Order View
                if ((photoName == null) || (photoNum == null)) return;
                final RelativeLayout r = findViewById(R.id.guest_layout_main);
                LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View myView = linflater.inflate(R.layout.order, (ViewGroup) r, false); //here item is the the layout you want to inflate
                r.addView(myView);
                TextView t1 = findViewById(R.id.photoID);
                t1.setText("PhotoID: " + photoNum);
                TextView t2 = findViewById(R.id.photoName);
                t2.setText("Photo Name: " + photoName);
                final EditText e1 = findViewById(R.id.copies);
                final EditText e2 = findViewById(R.id.size);

                Button orderRowButton = (Button) findViewById(R.id.orderPhotoG);
                orderRowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final RelativeLayout layout = findViewById(R.id.order_layout);
                        final String cp = current_ea + "/" + current_user + "/Photos";
                        Log.v(TAG, "Searching for : " + photoNum);
                        db.collection(cp).whereEqualTo("photoNo", photoNum)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Log.v(TAG, "Search Ordered Photos - success");
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.v(TAG, document.getId() + " => " + document.getData());
                                                db.collection(cp).document(document.getId()).update("order", "yes");
                                                db.collection(cp).document(document.getId()).update("copies", e1.getText().toString());
                                                db.collection(cp).document(document.getId()).update("size", e2.getText().toString());
                                                Toast toast = Toast.makeText(start_main.this, "Photo Ordered",
                                                        Toast.LENGTH_LONG);
                                            }
                                        } else {
                                            Log.v(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                        r.removeView(layout);
                    }
                });
                Button cancelRowButton = (Button) findViewById(R.id.cancelPhotoG);
                cancelRowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final RelativeLayout layout = findViewById(R.id.order_layout);
                        r.removeView(layout);
                    }
                });
            }
        });

        // Get delete table row button.
        Button deleteRowButton = (Button) findViewById(R.id.table_layout_delete_row_button);
        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get table row count.
                int rowCount = stk.getChildCount();
                v.startAnimation(buttonClick);
                Log.v(TAG, "Deleting Row, count:" + rowCount);
                // Save delete row number list.
                List<Integer> deleteRowNumberList = new ArrayList<Integer>();
                // Loope each table rows.
                for (int i = 0; i < rowCount; i++) {
                    // Get table row.
                    View rowView = stk.getChildAt(i);
                    if (rowView instanceof TableRow) {
                        TableRow tableRow = (TableRow) rowView;
                        // Get row column count.
                        int columnCount = tableRow.getChildCount();
                        // Loop all columns in row.
                        for (int j = 0; j < columnCount; j++) {
                            View columnView = tableRow.getChildAt(j);
                            if (columnView instanceof CheckBox) {
                                // If columns is a checkbox and checked then save the row number in list.
                                CheckBox checkboxView = (CheckBox) columnView;
                                if (checkboxView.isChecked()) {
                                    final String cp = current_ea + "/" + current_user + "/Photos";
                                    TextView firstTextView = (TextView) tableRow.getChildAt(0);
                                    String firstText = firstTextView.getText().toString();
                                    Log.v(TAG, "Searching for : " + firstText);
                                    db.collection(cp).whereEqualTo("photoNo", firstText)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.v(TAG, "Search success");
                                                        for (DocumentSnapshot document : task.getResult()) {
                                                            Log.v(TAG, document.getId() + " => " + document.getData());
                                                            db.collection(cp).document(document.getId()).delete();
                                                        }
                                                    } else {
                                                        Log.v(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                    deleteRowNumberList.add(i);
                                    break;
                                }
                            }
                        }
                    }
                }

                // Remove all rows by the selected row number.
                for (int rowNumber : deleteRowNumberList) {
                    stk.removeViewAt(rowNumber);
                    return;
                }
            }
        });


        // DB Get All Orders from FireCloud
        Button getAllButton = (Button) findViewById(R.id.table_layout_allOrder_row_button);
        getAllButton.setOnClickListener(new View.OnClickListener() {
            List<String> photos = new ArrayList<String>();
            @Override public void onClick(View v) {
                Log.v(TAG, "Get Database entries");
                v.startAnimation(buttonClick);
                deleteAllTableRows();
                final String cp = current_ea + "/" + current_user + "/Photos";
                db.collection(cp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        if (document.getData().get("order").toString().equals("yes")) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            photos.add(document.getData().get("photoNo").toString());
                                            photos.add(document.getData().get("photoText").toString());
                                            addRow(photos, 0);
                                            photos.clear();
                                        }
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

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }

                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    selectedImage = getResizedBitmap(selectedImage, 400);
                    ImageView iv = findViewById(R.id.faceImage);
                    iv.setImageBitmap(selectedImage);

                    // Set the image in ImageView
                    // ImageView iv = findViewById(R.id.faceImage);
                    // iv.setImageURI(selectedImageUri);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void startAdmin() {
        Intent i = new Intent(start_main.this, AdminActivity.class);
        i.putExtra("user", current_user);
        i.putExtra("uid", current_uid);
        i.putExtra("ea", current_ea);
        Log.v(TAG, "Starting Admin Activity");
        startActivity(i);
        finish();
        // finish the current activity, so a return from admin activity returns to home
    }

    private void deleteAllTableRows() {
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_table);
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
    private void addRow(List<String> photos, Integer checkbox) {
        // Create a new table row.
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_table);

        TableRow tbrow = new TableRow(start_main.this);
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

        if (checkbox == 1) {
            // Add a checkbox in the third column.
            CheckBox checkBox = new CheckBox(context);
            checkBox.setGravity(Gravity.RIGHT);
            lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            lparams.weight = 0.5f;
            lparams.width = 0;
            checkBox.setLayoutParams(lparams);
            tbrow.addView(checkBox, 2);
        }

        stk.addView(tbrow);
        tbrow.setBackgroundResource(R.drawable.row_border);
    }

    private void addNewContact() {
        Log.v(TAG, "Adding Contact to DB");
        DocumentReference docRef = db.collection(current_ea).document(current_user);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if (document.getData().get("Name").equals(current_user)) {
                            Log.v(TAG, "User already exsists: " + current_user);
                        } else {
                            Map<String, String> newContact = new HashMap<>();
                            newContact.put("Name", current_user);
                            newContact.put("id", current_uid);
                            newContact.put("adminFor", null);
                            db.collection(current_ea).document(current_user).set(newContact)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override public void onSuccess(Void aVoid) {
                                            Log.v(TAG, "Added New Contact to DB: " + current_user);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(start_main.this, "ERROR" + e.toString(),
                                                    Toast.LENGTH_SHORT).show();
                                            Log.v(TAG, e.toString());
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void addNewPhoto(String photoNo, String photoText, String order, String copies, String size) {
        Log.v(TAG, "Adding Photo to DB");
        Map<String, String> newPhoto = new HashMap<>();
        newPhoto.put("photoNo", photoNo);
        newPhoto.put("photoText", photoText);
        newPhoto.put("order", order);
        newPhoto.put("copies", copies);
        newPhoto.put("size", size);
        //String docId = db.collection("User").document(current_user).getId();
        //String cp = current_ea + "/Photos/" + docId;
        String cp = current_ea + "/" + current_user + "/Photos";
        Log.v(TAG, "Sub Collection at: " + cp);
        db.collection(cp).document().set(newPhoto)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void aVoid) {
                        Toast toast = Toast.makeText(start_main.this, "Photo Added to Gallery",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast toast = Toast.makeText(start_main.this, "ERROR" + e.toString(),
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                        Log.v(TAG, e.toString());
                    }
                });
    }
}
