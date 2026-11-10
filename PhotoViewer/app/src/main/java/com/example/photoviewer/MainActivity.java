package com.example.photoviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    EditText editFilter;

    String site_url = "http://10.0.2.2:8000/";
    JSONObject post_json;
    String imageUrl = null;

    CloadImage taskDownload;
    private List<ImageAdapter.PostItem> allPostItems = new ArrayList<>();

    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        editFilter = findViewById(R.id.editFilter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() > backPressedTime + 2000) {
                    backPressedTime = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, "'뒤로' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download 시작", Toast.LENGTH_SHORT).show();
    }

    public void onClickUpload(View v) {
        PutPost taskUpload = new PutPost();
        taskUpload.execute(site_url + "api_root/Post/");
        Toast.makeText(getApplicationContext(), "Upload 시도 중...", Toast.LENGTH_SHORT).show();
    }

    public void onClickFilter(View v) {
        String keyword = editFilter.getText().toString().trim().toLowerCase();
        if (keyword.isEmpty()) {
            updateRecyclerView(allPostItems, "");
            return;
        }
        List<ImageAdapter.PostItem> filtered = new ArrayList<>();
        for (ImageAdapter.PostItem item : allPostItems) {
            if (item.title.toLowerCase().contains(keyword) || item.text.toLowerCase().contains(keyword)) {
                filtered.add(item);
            }
        }
        updateRecyclerView(filtered, keyword);
    }

    public void onClickResetFilter(View v) {
        editFilter.setText("");
        updateRecyclerView(allPostItems, "");
    }

    private void updateRecyclerView(List<ImageAdapter.PostItem> items, String filterKeyword) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ImageAdapter adapter = new ImageAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);

        if (items.isEmpty()) {
            textView.setText(filterKeyword.isEmpty() ? "불러올 이미지가 없습니다." : "'" + filterKeyword + "' 검색 결과가 없습니다.");
        } else {
            if (filterKeyword.isEmpty()) {
                textView.setText("YOLO 검출 이미지 로드 성공! (총 " + items.size() + "건)");
            } else {
                textView.setText("'" + filterKeyword + "' 필터 결과: " + items.size() + "건 (전체 " + allPostItems.size() + "건)");
            }
        }
    }

    private class CloadImage extends AsyncTask<String, Integer, List<ImageAdapter.PostItem>> {
        @Override
        protected List<ImageAdapter.PostItem> doInBackground(String... urls) {
            List<ImageAdapter.PostItem> itemList = new ArrayList<>();
            try {
                String apiUrl = urls[0];
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();

                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);

                    for (int i = aryJson.length() - 1; i >= 0; i--) {
                        post_json = (JSONObject) aryJson.get(i);

                        int id = post_json.optInt("id", i + 1);
                        String title = post_json.optString("title", "detected");
                        String text = post_json.optString("text", "");
                        String createdDate = post_json.optString("created_date", "");
                        if (createdDate.length() >= 19) {
                            createdDate = createdDate.substring(0, 19).replace("T", " ");
                        }
                        imageUrl = post_json.optString("image", "null");

                        if (imageUrl != null && !imageUrl.equals("null") && !imageUrl.isEmpty()) {
                            imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");
                            imageUrl = imageUrl.replace("localhost", "10.0.2.2");

                            URL myImageUrl = new URL(imageUrl);
                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();

                            InputStream imgStream = imgConn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);

                            if (imageBitmap != null) {
                                itemList.add(new ImageAdapter.PostItem(id, title, text, createdDate, imageBitmap));
                            }
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ImageAdapter.PostItem> items) {
            allPostItems = items;
            updateRecyclerView(allPostItems, "");
        }
    }

    private class PutPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"author\": 1, \"title\": \"안드로이드 업로드\", \"text\": \"앱에서 작성된 테스트 글입니다.\"}";

                java.io.OutputStream os = conn.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    return "업로드 성공!";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "업로드 실패";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            onClickDownload(null);
        }
    }
}
