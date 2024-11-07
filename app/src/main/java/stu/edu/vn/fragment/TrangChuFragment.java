package stu.edu.vn.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stu.edu.vn.R;
import stu.edu.vn.network.RouteFetcher;
import stu.edu.vn.utils.GeocoderHelper;

public class TrangChuFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button btnTim;
    private EditText txtViTriBatDau, txtViTriCanDen;

    private static final int quyen_truy_cap_vi_tri = 1;

    public TrangChuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trang_chu, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        btnTim = view.findViewById(R.id.btn_Tim);
        txtViTriBatDau = view.findViewById(R.id.txtViTriBatDau);
        txtViTriCanDen = view.findViewById(R.id.txtViTriCanDen);

        btnTim.setOnClickListener(view1 -> timDuong());

        if (checkLocationPermission()) {
            initMap(view);
        } else {
            requestLocationPermission();
        }

        return view;
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, quyen_truy_cap_vi_tri);
    }

    private void initMap(View view) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions().position(currentLocation).title("Bạn đang ở đây"));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == quyen_truy_cap_vi_tri) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap(getView());
            } else {
                Toast.makeText(getActivity(), "Quyền truy cập vị trí bị từ chối. Không thể hiển thị vị trí của bạn.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Phương thức timDuong để xử lý địa chỉ và vẽ đường
    // Điều chỉnh lại code gọi các hàm mới tạo trong các package
    private void timDuong() {
        String startLocation = txtViTriBatDau.getText().toString().trim();
        String endLocation = txtViTriCanDen.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập cả điểm bắt đầu và điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            LatLng startLatLng = GeocoderHelper.getLatLngFromAddress(getContext(), startLocation);
            LatLng endLatLng = GeocoderHelper.getLatLngFromAddress(getContext(), endLocation);

            if (startLatLng == null || endLatLng == null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Không tìm thấy địa chỉ, vui lòng kiểm tra lại", Toast.LENGTH_SHORT).show());
                return;
            }

            getActivity().runOnUiThread(() -> {
                String url = getDirectionsUrl(startLatLng, endLatLng);
                new RouteFetcher(getContext(), new RouteFetcher.RouteFetchListener() {
                    @Override
                    public void onRouteFound(List<LatLng> routePoints) {
                        PolylineOptions polylineOptions = new PolylineOptions().addAll(routePoints).color(Color.BLUE).width(10);
                        map.addPolyline(polylineOptions);
                    }

                    @Override
                    public void onRouteError(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }).fetchRoute(url);
            });
        });
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "AIzaSyCFeg_acSp8uvVE8DKz5tuBJJs4W_7anmA";  // Replace with your Google Maps API Key
        return "https://maps.googleapis.com/maps/api/directions/json?" + str_origin + "&" + str_dest + "&key=" + key;
    }
}

