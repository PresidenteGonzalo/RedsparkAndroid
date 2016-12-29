package red.redspark.redspark;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category {
    public int id;
    public int count;
    public String name;
    public int parent;
    public List<Category> children;

    public Category() {

    }

    public Category(JSONObject data) throws JSONException {
        name = data.getString("name");
        id = data.getInt("id");
        parent = data.getInt("parent");
        count = data.getInt("count");
        children = new ArrayList<Category>();
        Log.d("categories", name + " id " + id + " parent " + parent + " count " + count);
    }
}
