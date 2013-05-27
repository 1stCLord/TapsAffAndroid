/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ratusapparatus.tapsaff;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class TapsAff extends android.appwidget.AppWidgetProvider
{
    final static public int tapsTemp = 63;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        ComponentName thisWidget = new ComponentName( context, TapsAff.class );
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.main,"ehh...");
        appWidgetManager.updateAppWidget(thisWidget, views);

        RetreiveFeedTask task = new RetreiveFeedTask();
        task.context = context;
        task.appWidgetManager = appWidgetManager;
        task.execute("http://www.taps-aff.co.uk/taps.json");


    }
}

class RetreiveFeedTask extends AsyncTask<String, Void, String>
{

    private Exception exception;

    public AppWidgetManager appWidgetManager = null;
    public Context context = null;

    protected String doInBackground(String... urls)
    {
        String result = "";
        try
        {
            Log.i("tapsaffdoInBackground",urls[0]);
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet get = new HttpGet(urls[0]);

            get.setHeader("Content-type", "application/json");

            InputStream inputStream = null;

            HttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        }catch(Exception e)
        {
            Log.e("tapsaff", "getjson", e);
        }
        return result;
    }

    protected void onPostExecute(String feed)
    {
        ComponentName thisWidget = new ComponentName( context, TapsAff.class );
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Log.i("tapsaffonPostExecuteFeed",feed);
        JSONObject jsonObj;
        try
        {
            jsonObj = new JSONObject(feed);
            /*for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.i(TapsAff.class.getName(), jsonObject.getString("text"));
            }*/
            String oanAff = jsonObj.get("taps").toString();
            Integer itsClose = (Integer)jsonObj.get("temp_f");
            if(itsClose >= TapsAff.tapsTemp - 5 && itsClose <= TapsAff.tapsTemp)
                views.setViewVisibility(R.id.bottom, View.VISIBLE);
            else
                views.setViewVisibility(R.id.bottom, View.GONE);
            String colour = "blue";
            if(oanAff == "Aff")
                colour = "red";
            String text = "taps" + " " + "<font color='" + colour + "'>" + oanAff + "</font>";
            //textView.setText(, TextView.BufferType.SPANNABLE);
            views.setTextViewText(R.id.main,Html.fromHtml(text));
        }catch (Exception e)
        {
            Log.i("tapsaffonPostExecuteException",e.getLocalizedMessage());
        }
        appWidgetManager.updateAppWidget(thisWidget, views);
    }
}