package com.vitmas.womansafetyfinal.MainActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.vitmas.womansafetyfinal.LocationsActivities.MapsActivity;
import com.vitmas.womansafetyfinal.R;

public class MainActivity extends AppCompatActivity  {
    private SignInButton signInButton;
    private GoogleSignInClient mgoogleSignInClient;
    private String TAG ="MainActivity";
    private FirebaseAuth mAuth;
    private Button signoutButton;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton=(SignInButton)  findViewById(R.id.sign_in_button);
        mAuth=FirebaseAuth.getInstance();
        signoutButton=(Button) findViewById(R.id.sign_out_button);

        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIN();
                signInButton.setVisibility(View.INVISIBLE);
            }
        });
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MainActivity.this,OpenLocation.class));
            }
        });
    }
    private void signIN(){
        Intent signInIntent = mgoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task =GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    protected void handleSignInResult(Task<GoogleSignInAccount> completeTask){
        try {
            GoogleSignInAccount acc= completeTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Sign IN Successful",Toast.LENGTH_SHORT).show();
            signoutButton.setVisibility(View.VISIBLE);
            FirebaseGoogleAuth(acc);
        }
        catch (ApiException e){
            Toast.makeText(MainActivity.this,"Sign IN Failed",Toast.LENGTH_LONG).show();
            FirebaseGoogleAuth(null);
        }
    }
    private void FirebaseGoogleAuth(GoogleSignInAccount acct){
        AuthCredential authCredential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                            FirebaseUser user=mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else {
                            Toast.makeText(MainActivity.this,"Sign IN Failed",Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser fuser){
        signoutButton.setVisibility(View.VISIBLE);
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account!=null){
            String personName= account.getDisplayName();
            String personGivenName= account.getGivenName();
            String personFamilyName=account.getFamilyName();
            String personEmail=account.getEmail();
            String personId =account.getId();
            Uri personPhoto =account.getPhotoUrl();
            //Toast.makeText(MainActivity.this,personName+" : "+personEmail,Toast.LENGTH_LONG).show();
        }
        else {

        }
    }
}