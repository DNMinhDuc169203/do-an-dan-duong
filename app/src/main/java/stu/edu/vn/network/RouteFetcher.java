package stu.edu.vn.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import stu.edu.vn.utils.PolylineDecoder;

public class RouteFetcher {
    private Context context;
    private RouteFetchListener listener;

    public interface RouteFetchListener {
        void onRouteFound(List<LatLng> routePoints);
        void onRouteError(String errorMessage);
    }

    public RouteFetcher(Context context, RouteFetchListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void fetchRoute(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                // Kiểm tra trạng thái phản hồi từ API
                if (!jsonObject.has("routes") || jsonObject.getJSONArray("routes").length() == 0) {
                    listener.onRouteError("Không tìm thấy tuyến đường.");
                    return;
                }

                JSONArray routes = jsonObject.getJSONArray("routes");

                // Lấy tuyến đường đầu tiên
                JSONObject route = routes.getJSONObject(0);
                if (!route.has("overview_polyline")) {
                    listener.onRouteError("Tuyến đường không có polyline.");
                    return;
                }

                JSONObject polyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = polyline.getString("points");

                // Sử dụng PolylineDecoder để giải mã polyline
                List<LatLng> decodedPath = PolylineDecoder.decodePoly(encodedPolyline);

                if (decodedPath.isEmpty()) {
                    listener.onRouteError("Không thể giải mã tuyến đường.");
                } else {
                    listener.onRouteFound(decodedPath);
                }

            } catch (JSONException e) {
                Log.e("RouteFetcher", "JSON Parsing error: " + e.getMessage());
                listener.onRouteError("Lỗi trong việc xử lý dữ liệu đường đi.");
            }
        }, error -> {
            Log.e("RouteFetcher", "Network error: " + error.getMessage());
            listener.onRouteError("Không thể tải đường đi. Kiểm tra kết nối mạng.");
        });

        Volley.newRequestQueue(context).add(stringRequest);
    }
}
