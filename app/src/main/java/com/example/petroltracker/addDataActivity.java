package com.example.petroltracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.parseDouble;
public class addDataActivity extends Activity {
    DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;
    EditText txtInMeter,txtInAmount,txtInLiter,txtInDate;
    Spinner dropdown;
    String getId;
    private static final String TAG = "raj";
    CollectionReference db = FirebaseFirestore.getInstance().collection("dailyData");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);
        Button saveBtn = findViewById(R.id.btnSave);
        Button cancelBtn = findViewById(R.id.btnCancel);
        Button deleteBtn = findViewById(R.id.btnDelete);
        txtInMeter = findViewById(R.id.txtInMeter);
        txtInAmount = findViewById(R.id.txtInAmount);
        txtInLiter = findViewById(R.id.txtInLiter);
        txtInDate = findViewById(R.id.txtInDate);
        datePicker = findViewById(R.id.datePicker);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        //get the spinner from the xml.
        dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"1", "2", "three"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        showDate(year, month+1, day);
        saveBtn.setOnClickListener(onClickListenerSave);
        cancelBtn.setOnClickListener(onClickListenerCancel);
        deleteBtn.setOnClickListener(onClickListenerDelete);
//      setDateBtn.setOnClickListener(onClickListenerSetDateBtn);
        txtInLiter.setOnFocusChangeListener(OnFocusChangeListenerLiter);
        showDatePickerDialog(datePicker);
        Intent intent = getIntent();
        String getMeter = intent.getStringExtra("getMeter");
        if(getMeter != null){
            String getDate = intent.getStringExtra("getDate");
            String getLiter = intent.getStringExtra("getLiter");
            String getAmount = intent.getStringExtra("getAmount");
            getId = intent.getStringExtra("getId");
            txtInMeter.setText(getMeter);
            txtInDate.setText(getDate);
            txtInLiter.setText(getLiter);
            txtInAmount.setText(getAmount);
           // saveBtn.setText("@string/SAVE");
        }
    }

    private void writeData(final int meter, final Double liter, final Double amount, final String date, final Double rate, String point) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
        // Create a new user with a first and last name
        final Date endDate = simpleDateFormat.parse(date);
        final Map<String, Object> dailyData = new HashMap<>();
        dailyData.put("meter", meter);
        dailyData.put("liter", liter);
        dailyData.put("amount", amount);
        dailyData.put("date", endDate);
        dailyData.put("rate", rate);
        dailyData.put("petrolPoint", point);
        db.whereLessThan("date",endDate)
        .orderBy("date", Query.Direction.DESCENDING)
        .limit(1)
        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot preDoc = task.getResult().getDocuments().get(0);
                    long preMeter  = preDoc.getLong("meter");
                    Date firstDate = preDoc.getTimestamp("date").toDate();
                    Double preRate = preDoc.getDouble("rate");
                    Integer days = printDifference(firstDate, endDate);
                    long totalKm = (meter - preMeter) / 10;
                    preRate     = rate - preRate;
                    double avg = totalKm / liter;

                    dailyData.put("days", days);
                    dailyData.put("totalKm", totalKm);
                    dailyData.put("preRate", preRate);
                    dailyData.put("avg", avg);

                    if(getId == null) {
                        addData(dailyData);
                    }else{
                        updateData(getId,dailyData);
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    private void updateData(String getId, Map<String, Object> dailyData) {
        db.document(getId).set(dailyData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "DocumentSnapshot Updated added with ID: " );
                Context context = getApplicationContext();
                CharSequence text = "Hello toast! Updated DocumentSnapshot added with ID: " ;
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        });
    }

    private void addData(Map<String, Object> dailyData) {
        db.add(dailyData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Context context = getApplicationContext();
                        CharSequence text = "Hello toast! DocumentSnapshot added with ID: " + documentReference.getId() ;
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Context context = getApplicationContext();
                        CharSequence text = "Error adding document" ;
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
    }

    private void showDate(int year, int month, int day) {
        txtInDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
        datePicker.setVisibility(View.GONE);
    }
    public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
        showDate(datePicker.getYear(),(datePicker.getMonth() + 1), datePicker.getDayOfMonth());
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setShowsDialog(true);
    }
    View.OnClickListener onClickListenerSetDateBtn = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            datePicker.setVisibility(View.VISIBLE);
        }
    };

    View.OnClickListener onClickListenerSave = new View.OnClickListener() {
            @Override
        public void onClick(View view) {
            int meter = Integer.parseInt(txtInMeter.getText().toString());
            Double amount = parseDouble(txtInAmount.getText().toString());
            Double liter = parseDouble(txtInLiter.getText().toString());
            String date = txtInDate.getText().toString();
            Double rate = amount/ liter;
            String point   = dropdown.getSelectedItem().toString();
            try {
                writeData(meter,liter,amount,date,rate,point);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };
    View.OnFocusChangeListener OnFocusChangeListenerLiter = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Double amount = parseDouble(txtInAmount.getText().toString());
            String tmp = txtInLiter.getText().toString();
            if(!tmp.matches("")){
                Double liter = parseDouble(tmp);
                String rate = String.valueOf(amount/ liter);
                TextView textView = findViewById(R.id.txtRate);
                textView.setText(rate);
            }
        }
    };
    View.OnClickListener onClickListenerCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
    View.OnClickListener onClickListenerDelete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            db.document(getId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "DocumentSnapshot Deleted" );
                    Context context = getApplicationContext();
                    CharSequence text = "Hello toast! Deleted" +task.getResult().toString();
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            });
        }
    };
    public Integer printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long elapsedDays = different / daysInMilli;
        return Math.toIntExact(elapsedDays);
    }
}
