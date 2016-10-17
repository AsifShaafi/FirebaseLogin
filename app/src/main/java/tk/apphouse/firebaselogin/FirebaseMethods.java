package tk.apphouse.firebaselogin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by Asif Imtiaz Shaafi on 10/17/2016.
 * Email: a15shaafi.209@gmail.com
 */

public class FirebaseMethods implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "FirebaseMethods";

    public static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private Context mContext;
    private AppCompatActivity mActivity;

    FirebaseMethods(Context context, AppCompatActivity activity) {
        mContext = context;
        mActivity = activity;

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.i(TAG, "onAuthStateChanged: " + user.getToken(true).toString());

                    UserDetails userDetails =
                            new UserDetails(user.getDisplayName() + "--" + user.getToken(true).toString(),
                                    user.getEmail(), user.getPhotoUrl());

                    Intent intent = new Intent(mContext, UserActivity.class);

                    intent.putExtra("user", userDetails);

                    mContext.startActivity(intent);

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    //Toast.makeText(SignUpActivity.this, "Please sign in!", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    public FirebaseAuth.AuthStateListener getAuthListener() {
        return mAuthListener;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId() + " + " + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    void signUpWithEmailPassword(String email, String password, final String name) {
        email = email.trim();
        password = password.trim();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            updateUserName(user, name);

                            UserDetails details =
                                    new UserDetails(name, user.getEmail(), user.getPhotoUrl());

                            Intent intent = new Intent(mContext, UserActivity.class);
                            intent.putExtra("user", details);
                            mContext.startActivity(intent);
                        }
                        // ...
                    }
                });
    }

    void updateUserName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    void signInWithEmailPassword(String email, String password, ProgressDialog dialog) {

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        email = email.trim();
        password = password.trim();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "signInWithEmail:failed: " + task.getException() +
                                    "  " + task.getException().getLocalizedMessage() + "  " +
                            task.getException().getMessage());
                            progressDialog.cancel();
                            showErrorDialog(mContext, task.getException().getLocalizedMessage());

                        } else {
                            Toast.makeText(mContext, "Sign in successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            UserDetails details =
                                    new UserDetails(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());

                            Intent intent = new Intent(mActivity, UserActivity.class);
                            progressDialog.cancel();
                            intent.putExtra("user", details);
                            mContext.startActivity(intent);
                        }
                    }
                });

        progressDialog.cancel();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    static ProgressDialog progressDialog(Context context, ProgressDialog dialog, boolean showDialog) {

        if (showDialog) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...Please Wait");

            dialog.show();
        } else {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
            }
        }
        return dialog;
    }

    static void showErrorDialog(Context context, String msg)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Error!!")
                .setMessage(msg)
                .setPositiveButton("OK", null);

        Dialog dialog = builder.create();
        dialog.show();
    }

    static void signInWithGoogle(AppCompatActivity activity, GoogleApiClient googleApiClient) {

        Toast.makeText(activity, "signing in function", Toast.LENGTH_SHORT).show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    static void signOut(FirebaseAuth auth, AppCompatActivity activity) {
        auth.signOut();
        activity.startActivity(new Intent(activity, UserSignIn.class));
        activity.finish();
    }
}
