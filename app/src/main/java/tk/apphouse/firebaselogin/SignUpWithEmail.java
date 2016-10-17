package tk.apphouse.firebaselogin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpWithEmail extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SignUpWithEmail";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseMethods mFirebaseMethods;

    private Button mEmailSignUpBtn;
    private EditText mNameEt, mEmailEt, mPassEt, mRePassEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseMethods = new FirebaseMethods(this, this);

        mAuth = mFirebaseMethods.getFirebaseAuth();
        mAuthStateListener = mFirebaseMethods.getAuthListener();

        setContentView(R.layout.activity_sign_up_with_email);

        //setting the button to click listener
        mEmailSignUpBtn = (Button) findViewById(R.id.sign_up_et_button);
        mEmailSignUpBtn.setOnClickListener(this);

        //edit texts
        mNameEt = (EditText) findViewById(R.id.sign_up_et_name_edit_text);
        mEmailEt = (EditText) findViewById(R.id.sign_up_et_email);
        mPassEt = (EditText) findViewById(R.id.sign_up_et_password);
        mRePassEt = (EditText) findViewById(R.id.sign_up_et_retype_password);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void proceedToFirebaseSignUp() {

        if (!validForm())
        {
            return;
        }

        String name = mNameEt.getText().toString().trim();
        String email = mEmailEt.getText().toString().trim();
        String pass = mPassEt.getText().toString().trim();
        String rePass = mRePassEt.getText().toString().trim();

        if (pass.equals(rePass))
        {
            mFirebaseMethods.signUpWithEmailPassword(email, pass, name);
        }
        else {
            mRePassEt.setError("Password didn't match");
        }
    }

    boolean validForm()
    {
        boolean validName, validEmail, validPass, validRePass, matchPassRePass;

        if (TextUtils.isEmpty(mNameEt.getText().toString()))
        {
            mNameEt.setError("Please enter your full name");
            validName = false;
        }
        else {
            mNameEt.setError(null);
            validName = true;
        }

        if (TextUtils.isEmpty(mEmailEt.getText().toString())){
            validEmail = false;
            mEmailEt.setError("Email is required");
        }
        else {
            validEmail = true;
            mEmailEt.setError(null);
        }

        if (TextUtils.isEmpty(mPassEt.getText().toString())){
            validPass = false;
            mPassEt.setError("password is required");
        }
        else if (mPassEt.getText().toString().length() < 6){
            validPass = false;
            mPassEt.setError("Password must be at least 6 letters");
        }
        else {
            validPass = true;
            mPassEt.setError(null);
        }

        if (TextUtils.isEmpty(mRePassEt.getText().toString())){
            validRePass = false;
            mRePassEt.setError("re enter password");
        }
        else {
            validRePass = true;
            mRePassEt.setError(null);
        }

        if (validPass && validRePass) {

            if (mPassEt.getText().toString().equals(mRePassEt.getText().toString())) {
                matchPassRePass = true;
                mRePassEt.setError(null);
            } else {
                matchPassRePass = false;
                mRePassEt.setError("Password didn't match");
            }
        }else {
            matchPassRePass = false;
        }

        return validName && validEmail && validPass && validRePass && matchPassRePass;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.sign_up_et_button:
                proceedToFirebaseSignUp();
                break;
        }
    }
}
