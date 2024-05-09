package com.example.mobilalk;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    private FirebaseAuth firebaseAuth;

    EditText usernameET;
    EditText emailET;
    EditText phoneET;
    DatePickerDialog datePickerDialog;
    Button dateButton;
    EditText passwordET;
    EditText rePasswordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().hide();

        initDatePicker();

        usernameET = findViewById(R.id.editTextUsername);
        emailET = findViewById(R.id.editTextEmail);
        phoneET = findViewById(R.id.editTextPhone);
        dateButton = findViewById(R.id.datePickerButton);
        passwordET = findViewById(R.id.editTextPassword);
        rePasswordET = findViewById(R.id.editTextRePassword);

        SharedPreferences preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        usernameET.setText(username);
        passwordET.setText(password);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month += 1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(year, month, dayOfMonth);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = makeDateString(year,month,dayOfMonth);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private String makeDateString(int year, int month, int dayOfMonth) {
        return year + "/" + month + "/" + dayOfMonth;
    }

    public void loginSuccess(){
        Intent intent = new Intent(this, ShopActivity.class);
        startActivity(intent);
    }

    public void register(View view) {
        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = rePasswordET.getText().toString();
        String birthdate = dateButton.getText().toString();

        if(!password.equals(passwordConfirm)){
            return;
        }

        Log.i(LOG_TAG, birthdate);

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "User created");
                    loginSuccess();
                } else {
                    Log.d(LOG_TAG, "User not created");
                    Toast.makeText(RegisterActivity.this, "User not created:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }
}