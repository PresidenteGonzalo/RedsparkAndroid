package red.redspark.redspark;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class HeadlinesFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY_NAME = "categoryName";

    private int categoryId = 0;
    private String categoryName;

    private RecyclerView storiesView;
    private HeadlinesAdapter adapter;
    private EditText searchText;
    private LinearLayout loadingLayout;

    private ArrayList<Story> stories = new ArrayList<>();

    private int currentPage = 1;
    private int perPage = 10;

    private StaggeredGridLayoutManager layoutManager;

    public HeadlinesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryId Category of stories to look for, or 0 if none
     * @param categoryName name of the category, if any
     * @return A new instance of fragment HeadlinesFragment.
     */
    public static HeadlinesFragment newInstance(int categoryId, String categoryName) {
        HeadlinesFragment fragment = new HeadlinesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_headlines, container, false);
        storiesView = (RecyclerView) v.findViewById(R.id.storiesView);

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        storiesView.setLayoutManager(layoutManager);

        adapter = new HeadlinesAdapter(getActivity(), stories);
        storiesView.setAdapter(adapter);

        // Infinite scrolling
        storiesView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final int threshold = 3;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastItemPositions[] = layoutManager.findLastCompletelyVisibleItemPositions(null);
                for (int pos : lastItemPositions) {
                    if (pos != RecyclerView.NO_POSITION && pos > currentPage * perPage - threshold) {
                        Log.d("scroll", "scrolling forward, lastItemPosition is " + pos + ", currentPage " + currentPage);
                        currentPage++;
                        fetchStories(false);
                    }
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        loadingLayout = (LinearLayout) v.findViewById(R.id.loadingLayout);

        searchText = (EditText) v.findViewById(R.id.searchText);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    fetchStories(true);
                    handled = true;
                }
                return handled;
            }
        });

        if (categoryId == 0) {
            LinearLayout categoryLayout = (LinearLayout) v.findViewById(R.id.categoryLayout);
            categoryLayout.setVisibility(View.GONE);
        } else {
            LinearLayout categoryLayout = (LinearLayout) v.findViewById(R.id.categoryLayout);
            categoryLayout.setVisibility(View.VISIBLE);
            TextView categoryNameView = (TextView) v.findViewById(R.id.categoryName);
            categoryNameView.setText(String.format(getResources().getString(R.string.current_category), categoryName));
        }

        fetchStories(true);

        return v;
    }

    public void fetchStories(final boolean clearStories) {
        URL url;
        String urlString = "https://www.redspark.nu/wp-json/wp/v2/posts?page=" + currentPage + "&per_page=" + perPage;

        if (categoryId != 0)
            urlString += "&categories=" + categoryId;

        try {
            Editable searchString = searchText.getText();
            if (searchString != null && searchString.toString().trim().length() > 0) {
                try {
                    urlString += "&search=" + URLEncoder.encode(searchString.toString().trim(), "UTF-8");
                } catch (UnsupportedEncodingException e) { // idk why this would ever happen
                    e.printStackTrace();
                    return;
                }
            }
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("stories", "malformed URL for posts: " + urlString);
            return;
        }

        new URLFetch(new URLFetch.Callback() {
            @Override
            public void fetchStart() {
                storiesView.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void fetchComplete(String result) {
                try {
                    storiesView.setVisibility(View.VISIBLE);
                    loadingLayout.setVisibility(View.GONE);
                    if (clearStories)
                        stories.clear();
                    JSONArray posts = new JSONArray(result);
                    for (int i = 0; i < posts.length(); i++) {
                        Story s = new Story(posts.getJSONObject(i));
                        stories.add(s);
                    }
                } catch (JSONException e) {
                    Log.e("stories", "JSON parsing stories failed", e);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void fetchCancel(String url) {

            }
        }, url).fetch();
    }
}
