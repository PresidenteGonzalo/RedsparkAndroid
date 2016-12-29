package red.redspark.redspark;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import java.text.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;


public class StoryFragment extends Fragment {
    private static final String ARG_STORY = "story";

    private Story story;
    private View view;

    public StoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param story The news story to show
     * @return A new instance of fragment StoryFragment.
     */
    public static StoryFragment newInstance(Story story) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STORY, story);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            story = getArguments().getParcelable(ARG_STORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_story, container, false);

        TextView storyTitle = (TextView) view.findViewById(R.id.storyTitle);
        storyTitle.setText(story.title);
        TextView storyDate = (TextView) view.findViewById(R.id.storyDate);
        storyDate.setText(DateFormat.getDateTimeInstance().format(story.date));
        try {
            new URLFetch(new URLFetch.Callback() {
                @Override
                public void fetchStart() {

                }

                @Override
                public void fetchComplete(String result) {
                    ImageView storyPic = (ImageView) view.findViewById(R.id.storyPic);
                    storyPic.setImageBitmap(BitmapCache.getInstance().getBitmap(result));
                }

                @Override
                public void fetchCancel(String url) {

                }
            }, new URL(story.fullImageUrl)).fetchBitmap();
        } catch (MalformedURLException e) {
            Log.w("story", "invalid URL for story image: " + story.fullImageUrl, e);
        }
        WebView storyBody = (WebView) view.findViewById(R.id.storyBody);
        storyBody.loadDataWithBaseURL(story.url, story.body, "text/html", null, null);

        return view;
    }
}
