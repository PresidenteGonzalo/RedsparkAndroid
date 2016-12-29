package red.redspark.redspark;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class URLFetch {
    public interface Callback {
        void fetchStart();
        void fetchComplete(String result);
        void fetchCancel(String url);
    }
    protected Callback callback = null;
    protected URL url;

    public URLFetch(Callback callback, URL url) {
        this.callback = callback;
        this.url = url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return url.equals(((URLFetch)obj).url);
    }

    public void fetch() {
        new AsyncDownloader().execute(url);
    }

    public void fetchBitmap() { new AsyncBitmapDownloader().execute(url); }

    public class AsyncDownloader extends AsyncTask<URL, Integer, String> {
        URL url;
        protected String doInBackground(URL... urls) {
            url = urls[0];
            Log.d("fetch", "Fetching " + url);

            try {
                URLConnection urlConn = url.openConnection();

                urlConn.addRequestProperty("Accept-Language", Locale.getDefault().toString());

                return outputFromConnection(urlConn);
            } catch (IOException e) {
                e.printStackTrace();
                callback.fetchCancel(url.toString());
                return null;
            }
        }

        // Turn the output from the connection into a string
        protected String outputFromConnection(URLConnection connection) throws IOException {
            InputStream in = new BufferedInputStream(connection.getInputStream());

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);

            return sb.toString();
        }

        protected void onPostExecute(String result) {
            Log.d("fetch", "Finished fetching " + url.toString());
            callback.fetchComplete(result);
        }
    }

    public class AsyncBitmapDownloader extends AsyncDownloader {
        @Override
        protected String outputFromConnection(URLConnection connection) throws IOException {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null) {
                BitmapCache.getInstance().setBitmap(url.toString(), BitmapCache.errorImageBitmap);
                throw new IOException("Failed to decode bitmap from " + url.toString());
            }
            BitmapCache.getInstance().setBitmap(url.toString(), bitmap);
            return url.toString();
        }
    }
}
