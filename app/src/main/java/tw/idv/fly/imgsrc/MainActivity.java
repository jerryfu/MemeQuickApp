package tw.idv.fly.imgsrc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    ImageView myImageView;
    ListView lv_menu_item;
    //Hello!  Say Hi!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_menu_item = (ListView) this.findViewById(R.id.listView);
        CallApiTask task = new CallApiTask();
        task.execute("");
    }

    protected void finishGetData(String jsonstr) {
        Log.i("finishGetData", jsonstr);

        Gson gson = new Gson();

        Prod[] p = gson.fromJson(jsonstr, Prod[].class);

        Log.i("Check p", Integer.toString(p.length));

        BAdapter adapter = new BAdapter(MainActivity.this, R.layout.menu_item, p);

        lv_menu_item.setAdapter(adapter);
        //adapter.notifyDataSetChanged();
    }

    private class BAdapter extends ArrayAdapter {

        private Context act_context;
        private LayoutInflater inflater;
        private int resourceId;

        private String[] imageUrls;
        private Prod[] dataDefs;

        public BAdapter(Context context, int resource, Prod[] objects) {
            super(context, resource, objects);

            this.act_context = context;
            this.resourceId = resource;
            this.dataDefs = (Prod[]) objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView img_res;
            TextView txt_intro;
            TextView txt_price;

            if (convertView == null) {

                LayoutInflater inflater = ((Activity) act_context).getLayoutInflater();
                convertView = inflater.inflate(this.resourceId, parent, false);
                //convertView = inflater.inflate(this.resourceId, parent, false);
                img_res = (ImageView) convertView.findViewById(R.id.imageMenu);
                txt_intro = (TextView) convertView.findViewById(R.id.textMenuContext);
                txt_price = (TextView) convertView.findViewById(R.id.textPrice);

            } else {
                img_res = (ImageView) convertView.findViewById(R.id.imageMenu);
                txt_intro = (TextView) convertView.findViewById(R.id.textMenuContext);
                txt_price = (TextView) convertView.findViewById(R.id.textPrice);
            }

            Prod def = this.dataDefs[position];

            txt_intro.setText(def.prod_name);
            txt_price.setText(String.valueOf(def.price));
            Picasso.with(act_context).load(def.imgsrc).resize(360, 360).into(img_res);

            return convertView;
        }
    }


    //讀取網路圖片，型態為Bitmap
    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            //Bitmap bitmap = BitmapFactory.decodeStream(input);
            //return bitmap;
            return BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                //InputStream in = new java.net.URL(urldisplay).openStream(); // 從網址上下載
                //mIcon11 = BitmapFactory.decodeStream(in);

                URL url = new URL(urldisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(input);


            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result); // 下載完成後載入結果
        }
    }

    private class CallApiTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ResponseResult = "";
            HttpURLConnection conn = null;
            try {

                String url_str = "http://menuquick.fly.idv.tw/api/prod";
                Log.i("URL", url_str);
                URL url = new URL(url_str);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String str;
                    while ((str = reader.readLine()) != null) {
                        ResponseResult = ResponseResult + str;
                    }
                    // Log.i("Result", ResponseResult);
                } else {
                    Log.i("getResponseCode", "ERROR");
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseResult;
        }

        protected void onPostExecute(String result) {
            finishGetData(result);
        }
    }


    private class Prod {
        public int prod_id;
        public int item;
        public String prod_name;
        public int price;
        public String imgsrc;
    }
}
