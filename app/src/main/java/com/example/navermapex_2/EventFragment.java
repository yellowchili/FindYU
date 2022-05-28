package com.example.navermapex_2;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.metrics.Event;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private EventItem eventItem;
    private String jsonStr;
    private String img;

    private ArrayList<EventItem> list = new ArrayList<>();
    private EventAdapater eventAdapater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_event, container, false);

        jsonStr = getJson(getActivity(), "artcenter_data.json");

        setData(jsonStr);


        eventAdapater = new EventAdapater(list);
        recyclerView = view.findViewById(R.id.event_recycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(eventAdapater);


        return view;
    }

    private String getJson(Context context, String fileName) {
        String jsonStr = "";

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            jsonStr = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonStr;
    }

    private void setData(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject eventObject = jsonArray.getJSONObject(i);

                eventItem = new EventItem();

                eventItem.setMonth(Integer.parseInt(eventObject.getString("month")));
                eventItem.setLocation1(eventObject.getString("location1"));
                eventItem.setLocation2(eventObject.getString("location2"));
                eventItem.setName(eventObject.getString("name"));
                eventItem.setDate(eventObject.getString("date"));
                eventItem.setImage(eventObject.getString("image"));
                eventItem.setLat(eventObject.getDouble("lat"));
                eventItem.setLng(eventObject.getDouble("lng"));

                list.add(eventItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
