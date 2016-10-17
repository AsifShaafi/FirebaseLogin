package tk.apphouse.firebaselogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class UserSignIn extends AppCompatActivity implements View.OnClickListener{

    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;

    private EditText emailEt, passEt;
    private TextInputLayout passwordInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mFirebaseMethods = new FirebaseMethods(this, this);

        mFirebaseAuth = mFirebaseMethods.getFirebaseAuth();
        mAuthStateListener = mFirebaseMethods.getAuthListener();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, mFirebaseMethods /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(this);

        SignInButton signInButtonGoogle = (SignInButton) findViewById(R.id.signInButtonGoogle);
        signInButtonGoogle.setSize(SignInButton.SIZE_WIDE);
        signInButtonGoogle.setColorScheme(SignInButton.COLOR_DARK);
        signInButtonGoogle.setOnClickListener(this);

        findViewById(R.id.sign_in_btn_email).setOnClickListener(this);

        emailEt = (EditText) findViewById(R.id.email_edit_text);
        passEt = (EditText) findViewById(R.id.password_edit_text);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_edit_text_input_layout);

    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    //activity result for google account sign in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == FirebaseMethods.RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                UserDetails userDetails = new UserDetails(account.getDisplayName(), account.getEmail(), account.getPhotoUrl());

                Intent intent = new Intent(UserSignIn.this, UserActivity.class);

                intent.putExtra("user", userDetails);

                FirebaseMethods.progressDialog(this, mProgressDialog, false);

                startActivity(intent);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                FirebaseMethods.progressDialog(this, mProgressDialog, false);
                Toast.makeText(this, "Sorry could not sign in !", Toast.LENGTH_SHORT).show();
            }
        }

        FirebaseMethods.progressDialog(this, mProgressDialog, false);
    }

    void proceedToEmailSignIn()
    {
        if (!validSignIn())
        {
            return;
        }

        mFirebaseMethods.signInWithEmailPassword(emailEt.getText().toString(),
                passEt.getText().toString(), mProgressDialog);
    }

    boolean validSignIn()
    {
        String email = emailEt.getText().toString().trim();
        String pass = passEt.getText().toString().trim();

        boolean validEmail, validPass;

        if (TextUtils.isEmpty(email))
        {
            emailEt.setError("Email is required");
            validEmail = false;
        }
        else {
            emailEt.setError(null);
            validEmail = true;
        }

        if (TextUtils.isEmpty(pass))
        {
            passwordInputLayout.setError("Password is required");
            validPass = false;
        }
        else {
            validPass = true;
            passwordInputLayout.setError(null);
        }

        return validEmail && validPass;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.sign_up_button:
                startActivity(new Intent(this, SignUpActivity.class));
                break;

            case R.id.signInButtonGoogle:
                mProgressDialog = FirebaseMethods.progressDialog(this, mProgressDialog, true);
                FirebaseMethods.signInWithGoogle(this, mGoogleApiClient);
                break;

            case R.id.sign_in_btn_email:
                proceedToEmailSignIn();

                break;
        }
    }
}
