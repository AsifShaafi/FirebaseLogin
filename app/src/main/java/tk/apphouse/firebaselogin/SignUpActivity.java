package tk.apphouse.firebaselogin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    private static final String TAG = "SignUpActivityActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseMethods mFirebaseMethods;

    private Button  btnSignUpEmail;
    private SignInButton btnSignUpGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //adding the home/back button on the action_bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance(); //getting the firebase instance of the app

        //  google configure starts
        /***********************************************************************************
         Configure Google Sign In. setting the google api for sign in and then requesting
         for tokenId for the user using the firebase server client id. this token is later
         used to get an unique id of firebase to keep the track of the user. later on the token
         id will be used to verify the user
        ************************************************************************************/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //setting up the api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //google configure ends

        //customizing the google default sign in button..start

        btnSignUpEmail = (Button) findViewById(R.id.sign_up_btn_email);
        btnSignUpGoogle = (SignInButton) findViewById(R.id.sign_up_btn_google);
        btnSignUpGoogle.setSize(SignInButton.SIZE_WIDE);
        btnSignUpGoogle.setColorScheme(SignInButton.COLOR_DARK);

        //customizing ends


        btnSignUpGoogle.setOnClickListener(this);

        btnSignUpEmail.setOnClickListener(this);

        /***********************************************************************************
            this auth listener checks if there is a user already loggin or not. if loggin then
         get the user and go to user page. this lister is also called when a user is successfully
         signed in or sign up in the app. it gets automatically called. in apps onStart method
         this listener gets attached with the firebase auth
        ************************************************************************************/
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.i(TAG, "onAuthStateChanged: " + user.getToken(true).toString());

                    UserDetails userDetails =
                            new UserDetails(user.getDisplayName() + "--" + user.getToken(true).toString(),
                                    user.getEmail(), user.getPhotoUrl());

                    Intent intent = new Intent(SignUpActivity.this, UserActivity.class);

                    intent.putExtra("user", userDetails);

                    startActivity(intent);

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Toast.makeText(SignUpActivity.this, "Please sign in!", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();

        //attaching the listener
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        //detaching the listener
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************
        when user tries to login or sign up with google then google api starts an intent
     which is expected to return a value. the intent(called by google api) 1st checks if
     there are any google user already signed up with the app or not.. if such user is not
     found then a pick up account appears and user selects a account. then the intent send
     back a result to calling activity(from where the call was made.in this case this activity,
     and this happens in both cases-signed up user/new user) and the activity checks for the
     request code and then do the next part as needed.
    ************************************************************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: in side activity result: " + requestCode);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            //getting the result from the data send back to the activity
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                UserDetails userDetails = new UserDetails(account.getDisplayName(), account.getEmail(), account.getPhotoUrl());

                Intent intent = new Intent(SignUpActivity.this, UserActivity.class);

                intent.putExtra("user", userDetails);

                startActivity(intent);

                //assigning the user to the firebase user list for future use as a signed up user
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                FirebaseMethods.showErrorDialog(this, "Sign up failed! try again!");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //getting the credential for firebase using the user token id..

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        /***********************************************************************************
            the onCompleteListener listens for the sign in/up process of firebase and returns
         a result set, named as task. if the sign up/in is successful then the result/task
         returns a success boolean. other wise it send a exception containing the error of
         the process. this method also calls the auth listener automatically when it's work
         is done.
        ************************************************************************************/

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            FirebaseMethods.showErrorDialog(SignUpActivity.this, task.getException().getLocalizedMessage());
                        }
                        // ...
                    }
                });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.sign_up_btn_google:

                Toast.makeText(this, "signing up with Google", Toast.LENGTH_SHORT).show();
                FirebaseMethods.signInWithGoogle(this,mGoogleApiClient);

                break;

            case R.id.sign_up_btn_email:
                Toast.makeText(this, "Sign up with Email", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SignUpWithEmail.class));
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "sorry could not connect", Toast.LENGTH_SHORT).show();
    }
}
