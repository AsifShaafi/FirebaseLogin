package tk.apphouse.firebaselogin;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;

public class UserActivity extends AppCompatActivity {

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        UserDetails details = getIntent().getParcelableExtra("user");

        TextView nameText = (TextView) findViewById(R.id.user_name);
        TextView emailText = (TextView) findViewById(R.id.user_email);
        ImageView image = (ImageView) findViewById(R.id.user_image);


        nameText.setText(details.getName());
        emailText.setText(details.getEmail());

        Glide.with(this)
                .load(details.getImage())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image);


        findViewById(R.id.sign_out_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMethods.signOut(FirebaseAuth.getInstance(), UserActivity.this);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        count++;

        if (count == 1)
        {
            Toast.makeText(this, "tap one more time to exit", Toast.LENGTH_SHORT).show();
        }

        if (count == 2)
        {
            startActivity(new Intent(this, UserSignIn.class));
            super.onBackPressed();
            finish();
        }
    }
}
