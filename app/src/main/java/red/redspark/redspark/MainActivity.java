package red.redspark.redspark;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SparseArray<Category> categories = new SparseArray<Category>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getCategories();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        BitmapCache.cacheSize = maxMemory / 4;
        // Get the size of the display so we properly size bitmaps
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        BitmapCache.maxW = size.x;
        BitmapCache.maxH = size.y;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HeadlinesFragment headlinesFragment = HeadlinesFragment.newInstance(0, null);
        fragmentTransaction.add(R.id.mainFragment, headlinesFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getCategories() {
        try {
            URL url = new URL("https://www.redspark.nu/wp-json/wp/v2/categories?per_page=100");
            new URLFetch(new URLFetch.Callback() {
                @Override
                public void fetchStart() {

                }

                @Override
                public void fetchComplete(String result) {
                    if (result != null) {
                        try {
                            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                            Menu menu = navigationView.getMenu();

                            JSONArray cats = new JSONArray(result);
                            for (int i = 0; i < cats.length(); i++) {
                                Category category = new Category(cats.getJSONObject(i));
                                categories.put(category.id, category);
                            }
                            // Put children with their parents
                            for (int i = 0; i < categories.size(); i++) {
                                Category c = categories.valueAt(i);
                                if (c.parent != 0) {
                                    categories.get(c.parent).children.add(c);
                                }
                            }

                            // Add everything to the menu
                            for (int i = 0; i < categories.size(); i++) {
                                Category c = categories.valueAt(i);
                                if (c.parent == 0) {
                                    if (c.children.size() == 0)
                                        menu.add(Menu.NONE, c.id, 5, c.name);
                                    else {
                                        // TODO: figure out how to make these submenu headers clickable
                                        SubMenu subMenu = menu.addSubMenu(Menu.NONE, c.id, 100, null);
                                        for (Category child : c.children) {
                                            subMenu.add(Menu.NONE, child.id, Menu.NONE, child.name);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.w("categories", "JSON parsing categories failed", e);
                        }
                    }
                }

                @Override
                public void fetchCancel(String url) {

                }
            }, url).fetch();
        } catch (MalformedURLException e) {
            Log.w("categories", "malformed URL", e);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HeadlinesFragment headlinesFragment = HeadlinesFragment.newInstance(0, null);
            fragmentTransaction.add(R.id.mainFragment, headlinesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            // ID is a category
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HeadlinesFragment headlinesFragment = HeadlinesFragment.newInstance(id, item.getTitle().toString());
            fragmentTransaction.add(R.id.mainFragment, headlinesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void closeCategory(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HeadlinesFragment headlinesFragment = HeadlinesFragment.newInstance(0, null);
        fragmentTransaction.add(R.id.mainFragment, headlinesFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
