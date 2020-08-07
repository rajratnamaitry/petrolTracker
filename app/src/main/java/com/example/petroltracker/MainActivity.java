package com.example.petroltracker;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static String ExtraData ="edit";

    private static final String TAG = "raj";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onRestart() {
        super.onRestart();
        readData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fabBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, addDataActivity.class);
            startActivity(intent);
            }
        });
//     getFileData();
       readData();
    }

    private void readData() {
       Query data = db.collection("dailyData");
//        data.orderBy("date", Query.Direction.ASCENDING)
        data.orderBy("date", Query.Direction.DESCENDING)
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
    //               loadWriteData(task);
                     loadData(task);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
    private void loadWriteData(Task<QuerySnapshot> task) {
        QueryDocumentSnapshot jsonPreObject = null;
        for (QueryDocumentSnapshot document : task.getResult()) {
            Date endDate    = document.getTimestamp("date").toDate();
            Double liter    = document.getDouble("liter");
            Double amount   = document.getDouble("amount");
            Long meter      = document.getLong("meter");
            Long totalKm    = Long.valueOf(0);
            Long preMeter   = Long.valueOf(0);
            Date firstDate;
            Integer days    = 0;
            Double rate     = 0.00;
            Double preRate  = 0.00;
            Double avg     = 0.00;

            if (jsonPreObject != null) {
                preMeter    = jsonPreObject.getLong("meter");
                firstDate   = jsonPreObject.getTimestamp("date").toDate();
                preRate     = jsonPreObject.getDouble("rate");
                days        = printDifference(firstDate, endDate);
                totalKm     = (meter - preMeter)/10;
                rate        = amount / liter;
                preRate     = rate - preRate;
                avg         = totalKm / liter;
            }
            updateData(document.getId(),days,totalKm,rate,preRate,avg);
            jsonPreObject  = document;
        }
    }

    private void updateData(String id, Integer days, Long totalKm, Double rate, Double preRate, Double avg) {
        Map<String, Object> dailyData = new HashMap<>();
        dailyData.put("days", days);
        dailyData.put("totalKm", totalKm);
        dailyData.put("rate", rate);
        dailyData.put("preRate", preRate);
        dailyData.put("avg", avg);
        db.collection("dailyData").document(id)
                .update(dailyData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void loadData(Task<QuerySnapshot> task) {
        RecyclerView recyclerView;
        PetrolAdapter pAdapter;
        final List<Petrol> petrolData = new ArrayList<>();
        recyclerView =  findViewById(R.id.petrolListView);
        for (QueryDocumentSnapshot document : task.getResult()) {
            Petrol petrol = new Petrol (document);
            petrolData.add(petrol);
        }
        pAdapter = new PetrolAdapter(petrolData);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                recyclerView.HORIZONTAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);
        recyclerView.setAdapter(pAdapter);
        pAdapter.notifyDataSetChanged();
    }

    private void writeData(int meter, Double liter, Double amount, Date date, Integer totalKm, Double rate, Integer days, Double rateDiff) {
        // Create a new user with a first and last name
        Map<String, Object> dailyData = new HashMap<>();
        dailyData.put("meter", meter);
        dailyData.put("liter", liter);
        dailyData.put("amount", amount);
        dailyData.put("date", date);
        dailyData.put("petrolPoint", 1);
//        dailyData.put("totalKm", totalKm);
//        dailyData.put("rate", rate);
//        dailyData.put("days", days);
//        dailyData.put("rateDiff", rateDiff);
//        dailyData.put("avg", (totalKm/liter));
        // Add a new document with a generated ID
        db.collection("dailyData")
                .add(dailyData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        readData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void getFileData(){
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(readJSONFromAsset());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy");
        JSONObject jsonPreObject = null;
        for (int i = 0;i< jsonArray.length();i++){
            JSONObject jsonObject = null;
            Date endDate = new Date();
            Integer totalKm  = 0 ,preMeter = 0, meter = 0 ,days  = 0 ;
            Double liter = 0.00,amount = 0.00,rate = 0.00,preRate = 0.00;
            String date = "" , firstDate = "";
            try {
                jsonObject = jsonArray.getJSONObject(i);
                meter = jsonObject.getInt("meter");
                liter = jsonObject.getDouble("liter");
                amount = jsonObject.getDouble("amount");
                date = jsonObject.getString("date");
                endDate = simpleDateFormat.parse(date);
                if(i!=0){
                    preMeter = jsonPreObject.getInt("meter");
                    totalKm = meter - preMeter;
                    firstDate = jsonPreObject.getString("date");
                    Date startDate = simpleDateFormat.parse(firstDate);
                    days = printDifference(startDate, endDate);
                }
                rate = amount / liter;
                preRate = rate - preRate;
                writeData(meter,liter,amount,endDate,totalKm,rate,days,preRate);
                jsonPreObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("bikeData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}