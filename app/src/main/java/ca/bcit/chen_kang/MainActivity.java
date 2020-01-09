package ca.bcit.chen_kang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLDisplay;

public class MainActivity extends AppCompatActivity {

    // formatting
    SimpleDateFormat dateFormat;
    SimpleDateFormat timeFormat;
    // date EditText
    private TextView tvDate;
    // time EditText
    private TextView tvTime;
    // sr EditText
    private EditText editSr;
    // sr EditText
    private EditText editDr;

    //UserID EditText
    private EditText editUserId;
    // Sr Ave TextView
    private TextView textViewSrAve;
    // Dr Ave TextView
    private TextView textViewDrAve;
    // Condition Ave TextView
    private TextView textViewConditionAve;
    // condition TextView
    private TextView tvCondition;
    // add record button
    private Button buttonAddReading;
    //  Database Reference
    private DatabaseReference databaseRecords;
    // list of records view
    private ListView lvRecords;
    // list of records to be displayed
    private List<Record> recordList;
    //getting hold to the changed sr and ddr
    private String srChange;
    private double doubleSrChange;
    private String drChange;
    private double doubleDrChange;
    //Current Date
    Date curDate;

    // when app runs
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // firebase reference
        databaseRecords = FirebaseDatabase.getInstance().getReference("records");

        // link to view list
        lvRecords = findViewById(R.id.lvRecords);
        recordList = new ArrayList<Record>();


        // formatting
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        timeFormat = new SimpleDateFormat("HH:mm");

        // getting holder to average box
        textViewSrAve = findViewById(R.id.textViewSrAve);
        textViewDrAve = findViewById(R.id.textViewDrAve);
        textViewConditionAve = findViewById(R.id.textViewConditionAve);
        // get hold to widgets
        tvDate = findViewById(R.id.editDate);
        tvTime = findViewById(R.id.editTime);
        editSr = findViewById(R.id.editSr);
        editDr = findViewById(R.id.editDr);
        editUserId=findViewById(R.id.editId);
        tvCondition = findViewById(R.id.tvCondition);
        buttonAddReading = findViewById(R.id.btnAddRecord);

        // defaulting to current date and time
        Calendar calendar = Calendar.getInstance();
        curDate = calendar.getTime();
        String currentDate = dateFormat.format(calendar.getTime());
        String currentTime = timeFormat.format(calendar.getTime());
        tvDate.setText(currentDate);
        tvTime.setText(currentTime);

        // adding task listener
        buttonAddReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord();
                //monthAverage();
            }
        });

        // on sr and dr reading change
       onSrDrChange(editSr, editDr, tvCondition);
        // long click list item
        lvRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Record record = recordList.get(position);

                showUpdateDialog(record.getRecordId(),record.getUserId(),
                        record.getCurDate(), record.getSrReading(), record.getDrReading(), record.getCondition());

                return false;
            }
        });
    }

    // getting the records from firebase
    @Override
    protected void onStart() {
        super.onStart();
        databaseRecords.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recordList.clear();
                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                    Record record = recordSnapshot.getValue(Record.class);
                    recordList.add(record);
                }

                RecordListAdapter adapter = new RecordListAdapter(MainActivity.this, recordList);
                lvRecords.setAdapter(adapter);
                double srSum = 0, drSum = 0;
                int count=0;
                for(int i=0;i<recordList.size();i++){
                    Log.d("month", "onDataChange: "+recordList.get(i).getCurDate().getMonth());
                    if(recordList.get(i).getCurDate().getMonth()==curDate.getMonth()){
                        srSum += recordList.get(i).getSrReading();
                        drSum += recordList.get(i).getDrReading();
                        count++;
                    }
                }

                double srAve = srSum / count;
                double drAve = drSum / count;
                textViewSrAve.setText(String.format("%.2f", srAve));
                textViewDrAve.setText(String.format("%.2f", drAve));
                if(srAve < 120 && drAve < 180)
                    textViewConditionAve.setText("Normal");
                else if(srAve >= 120 && srAve <= 129 && drAve < 80)
                    textViewConditionAve.setText("Elevated");
                else if((srAve >= 130 && srAve <= 139) || (drAve >=80 && drAve <= 89))
                    textViewConditionAve.setText("High blood pressure (stage 1)");
                else if((srAve >= 140 && srAve <= 180) || (drAve >=90 && drAve <= 120))
                    textViewConditionAve.setText("High blood pressure (stage 2)");
                else if(srAve > 180 || drAve > 120)
                    textViewConditionAve.setText("Hypertensive Crisis!! Consult your doctor immediately !!!");





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    }

    // on sr reading change
    private void onSrDrChange(final EditText et1, final EditText et2, final TextView tv) {
        // on edit text change for Sr and Dr reading
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                srChange = et1.getText().toString().trim();
                drChange = et2.getText().toString().trim();
                if (!TextUtils.isEmpty(drChange) && !TextUtils.isEmpty(srChange)) {
                    doubleSrChange = Double.parseDouble(srChange);
                    doubleDrChange = Double.parseDouble(drChange);
                    if (doubleSrChange < 120 && doubleDrChange < 180)
                        tv.setText("Normal");
                    else if (doubleSrChange >= 120 && doubleSrChange <= 129 && doubleDrChange < 80)
                        tv.setText("Elevated");
                    else if ((doubleSrChange >= 130 && doubleSrChange <= 139) || (doubleDrChange >= 80 && doubleDrChange <= 89))
                        tv.setText("High blood pressure (stage 1)");
                    else if ((doubleSrChange >= 140 && doubleSrChange <= 180) || (doubleDrChange >= 90 && doubleDrChange <= 120))
                        tv.setText("High blood pressure (stage 2)");
                    else if (doubleSrChange > 180 || doubleDrChange > 120)
                        tv.setText("Hypertensive Crisis!! \nConsult your doctor immediately !!!");
                }
            }
        });
        // on dr changing
        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                srChange = et1.getText().toString().trim();


                drChange = et2.getText().toString().trim();

                if (!TextUtils.isEmpty(drChange) && !TextUtils.isEmpty(srChange)) {
                    doubleSrChange = Double.parseDouble(srChange);
                    doubleDrChange = Double.parseDouble(drChange);
                    if (doubleSrChange < 120 && doubleDrChange < 180)
                        tv.setText("Normal");
                    else if (doubleSrChange >= 120 && doubleSrChange <= 129 && doubleDrChange < 80)
                        tv.setText("Elevated");
                    else if ((doubleSrChange >= 130 && doubleSrChange <= 139) || (doubleDrChange >= 80 && doubleDrChange <= 89))
                        tv.setText("High blood pressure (stage 1)");
                    else if ((doubleSrChange >= 140 && doubleSrChange <= 180) || (doubleDrChange >= 90 && doubleDrChange <= 120))
                        tv.setText("High blood pressure (stage 2)");
                    else if (doubleSrChange > 180 || doubleDrChange > 120)
                        tv.setText("Hypertensive Crisis!! \nConsult your doctor immediately !!!");
                }
            }
        });
    }

    // adding records into firebase
    private void addRecord() {

        // getting user entry
        String userID=editUserId.getText().toString().trim();
        String sr = editSr.getText().toString().trim();
        String dr = editDr.getText().toString().trim();
        String condition = "";

        // if entry sr is empty
        if (TextUtils.isEmpty(userID)) {
            Toast.makeText(this, "You must enter a User ID.", Toast.LENGTH_LONG).show();
            return;
        }
        // if entry sr is empty
        if (TextUtils.isEmpty(sr)) {
            Toast.makeText(this, "You must enter a Systolic Reading.", Toast.LENGTH_LONG).show();
            return;
        }
        // if entry dr is empty
        if (TextUtils.isEmpty(dr)) {
            Toast.makeText(this, "You must enter a Diastolic Reading.", Toast.LENGTH_LONG).show();
            return;
        }

        // converting String to double
        double srDouble = Double.parseDouble(sr);
        double drDouble = Double.parseDouble(dr);

        if(srDouble < 120 && drDouble < 180)
            condition = "Normal";
        else if(srDouble >= 120 && srDouble <= 129 && drDouble < 80)
            condition = "Elevated";
        else if((srDouble >= 130 && srDouble <= 139) || (drDouble >=80 && drDouble <= 89))
            condition = "High blood pressure (stage 1)";
        else if((srDouble >= 140 && srDouble <= 180) || (drDouble >=90 && drDouble <= 120))
            condition = "High blood pressure (stage 2)";
        else if(srDouble > 180 || drDouble > 120)
            condition = "Hypertensive Crisis!! Consult your doctor immediately !!!";


        // firebase id key
        String id = databaseRecords.push().getKey();
        Record record = new Record(id,userID,curDate ,srDouble, drDouble, condition);
        // adding record to firebase
        Task setValueTask = databaseRecords.child(id).setValue(record);

        // if adding success
        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "Record added.",Toast.LENGTH_LONG).show();
                editUserId.setText("");
                editSr.setText("");
                editDr.setText("");
                tvCondition.setText("Condition:");
            }
        });
        // if adding fails
        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update record
    private void updateRecord(String id,String userId,Date curDate, double srReading, double drReading, String condition) {
        DatabaseReference dbRef = databaseRecords.child(id);

        Record record = new Record(id,userId,curDate, srReading, drReading, condition);

        Task setValueTask = dbRef.setValue(record);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "Record Updated.",Toast.LENGTH_LONG).show();
            }
        });

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // show update dialog after user long click on the list item
    private void showUpdateDialog(final String id, final String userId,final Date curDate2, final Double srReading, final Double drReading,final String condition) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String curDate = dateFormat.format(curDate2);

        final TextView tvDate = dialogView.findViewById(R.id.tvDate);
        tvDate.setText(curDate);

        final EditText updateId = dialogView.findViewById(R.id.editUserId);
        updateId.setText(userId);

        final EditText updateSr = dialogView.findViewById(R.id.editSr);
        updateSr.setText(srReading.toString());

        final EditText updateDr = dialogView.findViewById(R.id.editDr);
        updateDr.setText(drReading.toString());

        final TextView updateCondition = dialogView.findViewById(R.id.tvCondition);
        updateCondition.setText(condition);

        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        dialogBuilder.setTitle("Update Record " );

       final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        onSrDrChange(updateSr, updateDr, updateCondition);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String etId = updateId.getText().toString().trim();
                String srReading = updateSr.getText().toString().trim();
                String drReading = updateDr.getText().toString().trim();
                String condition = updateCondition.getText().toString().trim();


                if (TextUtils.isEmpty(srReading)) {
                    updateSr.setError("Systolic Reading is required");
                    return;
                }
                if (TextUtils.isEmpty(drReading)) {
                    updateSr.setError("Diastolic Reading is required");
                    return;
                }

                updateRecord(id, etId, curDate2, Double.parseDouble(srReading), Double.parseDouble(drReading), condition);
                alertDialog.dismiss();

            }
        });

        // deleting record
        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecord(id);
                alertDialog.dismiss();

            }
        });
    }
    // delete record
    private void deleteRecord(String id) {
        DatabaseReference dbRef = databaseRecords.child(id);

        Task setRemoveTask = dbRef.removeValue();
        setRemoveTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "Record Deleted.",Toast.LENGTH_LONG).show();
            }
        });

        setRemoveTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
