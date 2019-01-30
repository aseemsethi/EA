package com.example.ea;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;

public class start_main extends AppCompatActivity {
    private static final String TAG = "SMMainActivity";
    FirebaseUser user;
    private Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_main_new);
        context = getApplicationContext();
        final TableLayout stk = (TableLayout) findViewById(R.id.table_layout_table);
        setTitle("Photo Shop");

        Bundle extras = getIntent().getExtras();
        Log.v(TAG, "Get Params passed");
        if (extras == null) {
            return;
        }
        String value1 = extras.getString("user1");
        if (value1 != null) {
            Log.v(TAG, "user found: " + value1);
        }
        String uid1 = extras.getString("uid1");
        if (value1 != null) {
            Log.v(TAG, "UID: " + uid1);
        }
        //init_table();
        Button addRowButton = (Button)findViewById(R.id.table_layout_add_row_button);
        addRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new table row.
                TableRow tbrow = new TableRow(start_main.this);
                Log.v(TAG, "Adding Row");
                tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                       TableRow.LayoutParams.WRAP_CONTENT));

                TextView t1v = new EditText(context);
                t1v.setText("No."); t1v.setTextColor(Color.BLUE); t1v.setGravity(Gravity.START);
                TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                lparams.weight=0.75f; lparams.width=0;
                t1v.setLayoutParams(lparams);
                tbrow.addView(t1v,0);

                TextView t2v = new EditText(context);
                t2v.setText("Passport Size"); t2v.setTextColor(Color.BLUE); t2v.setGravity(Gravity.START);
                lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                lparams.weight=2; lparams.width=0;
                t2v.setLayoutParams(lparams);
                tbrow.addView(t2v,1);

                // Add a checkbox in the third column.
                CheckBox checkBox = new CheckBox(context); checkBox.setGravity(Gravity.RIGHT);
                lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                lparams.weight=0.5f; lparams.width=0;
                checkBox.setLayoutParams(lparams);
                tbrow.addView(checkBox,2);

                stk.addView(tbrow);
                tbrow.setBackgroundResource(R.drawable.row_border);
            }
        });

        // Get delete table row button.
        Button deleteRowButton = (Button)findViewById(R.id.table_layout_delete_row_button);
        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get table row count.
                int rowCount = stk.getChildCount();
                Log.v(TAG, "Deleting Row, count:" + rowCount);
                // Save delete row number list.
                List<Integer> deleteRowNumberList = new ArrayList<Integer>();
                // Loope each table rows.
                for(int i =0 ;i < rowCount;i++)
                {
                    // Get table row.
                    View rowView = stk.getChildAt(i);
                    if(rowView instanceof TableRow)
                    {
                        TableRow tableRow = (TableRow)rowView;
                        // Get row column count.
                        int columnCount = tableRow.getChildCount();
                        // Loop all columns in row.
                        for(int j = 0;j<columnCount;j++){
                            View columnView = tableRow.getChildAt(j);
                            if(columnView instanceof CheckBox) {
                                // If columns is a checkbox and checked then save the row number in list.
                                CheckBox checkboxView = (CheckBox)columnView;
                                if(checkboxView.isChecked())
                                {
                                    deleteRowNumberList.add(i);
                                    break;
                                }
                            }
                        }
                    }
                }

                // Remove all rows by the selected row number.
                for(int rowNumber :deleteRowNumberList)
                {
                    stk.removeViewAt(rowNumber);
                }
            }
        });
    }

    public void init_table(){
        TableLayout stk = (TableLayout) findViewById(R.id.table_layout_table);
        TableRow tbrow0 = new TableRow(this);

        TextView tv0 = new TextView(this);
        tv0.setText(" No. "); tv0.setTextColor(Color.WHITE);
        tbrow0.addView(tv0);

        TextView tv1 = new TextView(this);
        tv1.setText(" Photo ID "); tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);

        TextView tv2 = new TextView(this);
        tv2.setText(" Token No. "); tv2.setTextColor(Color.WHITE);
        tbrow0.addView(tv2);

        TextView tv3 = new TextView(this);
        tv3.setText(" Comments "); tv3.setTextColor(Color.WHITE);
        tbrow0.addView(tv3); stk.addView(tbrow0);
        tbrow0.setBackgroundResource(R.drawable.row_border);

        for (int i = 0; i < 5; i++) {
            TableRow tbrow = new TableRow(this);

            TextView t1v = new TextView(this);
            t1v.setText("" + i); t1v.setTextColor(Color.WHITE); t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);

            TextView t2v = new TextView(this);
            t2v.setText("Product " + i); t2v.setTextColor(Color.WHITE); t2v.setGravity(Gravity.CENTER);
            tbrow.addView(t2v);

            TextView t3v = new TextView(this);
            t3v.setText("Rs." + i); t3v.setTextColor(Color.WHITE); t3v.setGravity(Gravity.CENTER);
            tbrow.addView(t3v);

            TextView t4v = new TextView(this);
            t4v.setText("" + i * 15 / 32 * 10); t4v.setTextColor(Color.WHITE);
            t4v.setGravity(Gravity.CENTER); tbrow.addView(t4v);
            stk.addView(tbrow);
            tbrow.setBackgroundResource(R.drawable.row_border);
        }
    }

}
