package stu.edu.vn.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeocoderHelper extends AsyncTask<String, Void, LatLng> {
    private Context context;
    private String apiKey;
    private GeocodingListener listener;

    // Interface để nhận kết quả trả về
    public interface GeocodingListener {
        void onGeocodingSuccess(LatLng latLng);
        void onGeocodingError(String errorMessage);
    }

    public GeocoderHelper(Context context, String apiKey, GeocodingListener listener) {
        this.context = context;
        this.apiKey = apiKey;
        this.listener = listener;
    }

    @Override
    protected LatLng doInBackground(String... addresses) {
        // Kiểm tra xem địa chỉ có được cung cấp không
        if (addresses.length == 0) {
            return null;
        }

        String address = addresses[0];
        String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey;

        HttpURLConnection urlConnection = null;
        StringBuilder response = new StringBuilder();

        try {
            // Tạo URL và gửi yêu cầu HTTP GET
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Đọc phản hồi từ Google Geocoding API
            Scanner scanner = new Scanner(urlConnection.getInputStream());
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            Log.e("GeocodingTask", "Error while fetching geocoding data", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        // Xử lý kết quả JSON trả về từ API
        try {
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray results = jsonObject.getJSONArray("results");

            if (results.length() > 0) {
                // Lấy tọa độ của địa chỉ đầu tiên trong kết quả
                JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                return new LatLng(lat, lng);
            }
        } catch (JSONException e) {
            Log.e("GeocodingTask", "JSON Parsing error", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        if (latLng != null) {
            // Nếu lấy được tọa độ, gọi phương thức onGeocodingSuccess
            listener.onGeocodingSuccess(latLng);
        } else {
            // Nếu có lỗi hoặc không tìm thấy kết quả, gọi phương thức onGeocodingError
            listener.onGeocodingError("Không thể chuyển đổi địa chỉ thành tọa độ.");
        }
    }
}
