package red.redspark.redspark;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Story implements Parcelable {
    public String title;
    public String body;
    public Date date;
    public int id;
    public String url;
    public String mediaUrl;
    public String thumbnailUrl;
    public String fullImageUrl;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public Story() {

    }

    public Story(JSONObject data) throws JSONException {
        id = data.getInt("id");
        title = data.getJSONObject("title").getString("rendered");
        // Unescape HTML stuff
        if (title.contains("&")) {
            title = Html.fromHtml(title).toString();
        }
        body = data.getJSONObject("content").getString("rendered");
        try {
            date = dateFormat.parse(data.getString("date_gmt"));
        } catch (ParseException e) {
            Log.w("parse", "Failed to parse date " + data.getString("date_gmt") + ", error " + e.getMessage());
        }
        url = data.getString("link");

        mediaUrl = data.getJSONObject("_links").getJSONArray("wp:featuredmedia").getJSONObject(0).getString("href");
        //mediaUrl = data.getJSONObject("_links").getJSONObject("\"wp:attachment\"").getString("href");
    }

    public void addMediaData(String json) throws JSONException {
        JSONObject mediaData = new JSONObject(json).getJSONObject("media_details");
        thumbnailUrl = mediaData.getJSONObject("sizes").getJSONObject("thumbnail").getString("source_url");
        fullImageUrl = mediaData.getJSONObject("sizes").getJSONObject("full").getString("source_url");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeLong(date.getTime());
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(mediaUrl);
        dest.writeString(thumbnailUrl);
        dest.writeString(fullImageUrl);
    }

    private Story(Parcel in) {
        title = in.readString();
        body = in.readString();
        date = new Date(in.readLong());
        id = in.readInt();
        url = in.readString();
        mediaUrl = in.readString();
        thumbnailUrl = in.readString();
        fullImageUrl = in.readString();
    }

    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel source) {
            return new Story(source);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };
}
