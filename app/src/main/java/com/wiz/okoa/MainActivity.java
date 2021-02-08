package com.wiz.okoa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wiz.okoa.Model.User;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                                .setFontAttrId(R.attr.fontPath)
                                                .build());
        setContentView(R.layout.activity_main);

        //Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db =  FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //Initialize view
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });


    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN ");
        dialog.setMessage("Please use email to sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final EditText edEmail = login_layout.findViewById(R.id.etEmail);
        final EditText edPassword = login_layout.findViewById(R.id.etPassword);


        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //set disable button sign in if is processing
                btnSignIn.setEnabled(false);


                //Check validation
                if(TextUtils.isEmpty(edEmail.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edPassword.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter password",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.show();
                //Login
                auth.signInWithEmailAndPassword(edEmail.getText().toString(),edPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                startActivity(new Intent(MainActivity.this, Welcome.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                       Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT)
                               .show();

                       //Active button
                        btnSignIn.setEnabled(true);
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER ");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final EditText edEmail = register_layout.findViewById(R.id.etEmail);
        final EditText edPassword = register_layout.findViewById(R.id.etPassword);
        final EditText edName = register_layout.findViewById(R.id.etName);
        final EditText edPhone = register_layout.findViewById(R.id.etPhone);

        dialog.setView(register_layout);

        //set button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //Check validation
                if(TextUtils.isEmpty(edEmail.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edName.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter full name",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edPassword.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter password",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edPassword.getText().toString().length() < 6){

                    Snackbar.make(rootLayout,"Password too short !!!",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edPhone.getText().toString())){

                    Snackbar.make(rootLayout,"Please enter Phone Number",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //Register new user
                auth.createUserWithEmailAndPassword(edEmail.getText().toString(),edPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Save user to db
                                User user = new User();
                                user.setEmail(edEmail.getText().toString());
                                user.setName(edName.getText().toString());
                                user.setPassword(edPassword.getText().toString());
                                user.setPhone(edPhone.getText().toString());

                                //Use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Snackbar.make(rootLayout,"Registered Succesfully",Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Snackbar.make(rootLayout,"Failed"+ e.getMessage(),Snackbar.LENGTH_SHORT).show();

                                            }
                                        });
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed"+ e.getMessage(),Snackbar.LENGTH_SHORT).show();

                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
}
