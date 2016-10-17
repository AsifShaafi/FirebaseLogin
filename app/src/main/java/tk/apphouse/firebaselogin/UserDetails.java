package tk.apphouse.firebaselogin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Asif Imtiaz Shaafi on 10/17/2016.
 * Email: a15shaafi.209@gmail.com
 */

public class UserDetails implements Parcelable {
    private String name;
    private String email;
    private Uri image;

    public UserDetails(String name, String email, Uri image) {
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeParcelable(this.image, flags);
    }

    protected UserDetails(Parcel in) {
        this.name = in.readString();
        this.email = in.readString();
        this.image = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel source) {
            return new UserDetails(source);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };
}
