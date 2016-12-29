package red.redspark.redspark;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HeadlinesAdapter extends RecyclerView.Adapter<HeadlinesAdapter.HeadlinesViewHolder> {
    private ArrayList<Story> stories;
    private FragmentActivity context;

    public HeadlinesAdapter(FragmentActivity context, ArrayList<Story> stories) {
        this.context = context;
        this.stories = stories;
    }

    @Override
    public HeadlinesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.news_item, parent, false);

        return new HeadlinesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HeadlinesViewHolder holder, final int position) {
        Story story = stories.get(position);
        holder.storyTitle.setText(story.title);
        if (story.date != null)
            holder.storyDate.setText(DateFormat.getMediumDateFormat(context).format(story.date));

        if (story.thumbnailUrl != null) {
            Bitmap thumbnail = BitmapCache.getInstance().getBitmap(story.thumbnailUrl);
            if (thumbnail != null) {
                BitmapDrawable bd = new BitmapDrawable(context.getResources(), thumbnail);
                bd.setAlpha(75);
                holder.storyLayout.setBackground(bd);
            } else {
                if (story.thumbnailUrl != null) {
                    try {
                        new URLFetch(new URLFetch.Callback() {
                            @Override
                            public void fetchStart() {

                            }

                            @Override
                            public void fetchComplete(String result) {
                                Log.d("stories", "got bitmap, length " + result.length());
                                Bitmap bitmap = BitmapFactory.decodeByteArray(result.getBytes(), 0, result.length());
                                if (bitmap == null)
                                    Log.w("stories", "bitmap is null!");
                                BitmapCache.getInstance().setBitmap(stories.get(position).thumbnailUrl, bitmap);
                                notifyItemChanged(position);
                            }

                            @Override
                            public void fetchCancel(String url) {

                            }
                        }, new URL(story.thumbnailUrl)).fetchBitmap();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            try {
                URL media = new URL(story.mediaUrl);
                new URLFetch(new URLFetch.Callback() {
                    @Override
                    public void fetchStart() {

                    }

                    @Override
                    public void fetchComplete(String result) {
                        if (result != null) {
                            try {
                                stories.get(position).addMediaData(result);
                                notifyItemChanged(position);
                            } catch (JSONException e) {
                                Log.w("stories", "JSON parsing media from url failed: " + stories.get(position).mediaUrl, e);
                            }
                        }
                    }

                    @Override
                    public void fetchCancel(String url) {

                    }
                }, media).fetch();
            } catch (MalformedURLException e) {
                Log.w("stories", "malformed media URL: " + story.mediaUrl, e);
            }
        }

        if (holder.storyLayout.getBackground() == null) {
            BitmapDrawable bd = new BitmapDrawable(context.getResources(), BitmapCache.makePlaceholderImage());
            bd.setAlpha(75);
            holder.storyLayout.setBackground(bd);
        }
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    class HeadlinesViewHolder extends RecyclerView.ViewHolder {
        LinearLayout storyLayout;
        TextView storyTitle;
        TextView storyDate;

        public HeadlinesViewHolder(View itemView) {
            super(itemView);
            storyLayout = (LinearLayout) itemView.findViewById(R.id.storyLayout);
            storyTitle = (TextView) itemView.findViewById(R.id.storyTitle);
            storyDate = (TextView) itemView.findViewById(R.id.storyDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Story story = stories.get(position);

                        FragmentManager fragmentManager = context.getSupportFragmentManager();
                        StoryFragment storyFragment = StoryFragment.newInstance(story);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.hide(fragmentManager.findFragmentById(R.id.mainFragment));
                        fragmentTransaction.add(R.id.mainFragment, storyFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
            });
        }
    }
}
