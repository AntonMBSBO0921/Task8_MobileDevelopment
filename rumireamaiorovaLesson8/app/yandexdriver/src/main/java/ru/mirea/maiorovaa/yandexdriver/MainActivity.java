package ru.mirea.maiorovaa.yandexdriver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

import ru.mirea.maiorovaa.yandexdriver.R;
import ru.mirea.maiorovaa.yandexdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener {

    private ActivityMainBinding binding;
    private final String MAPKIT_API_KEY = "873211c9-bbf5-4f2b-9c31-0a32c2b98871";
    private final Point ROUTE_END_LOCATION = new Point(55.711423, 37.544276);
    private final int REQUEST_CODE_PERMISSION = 123;

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0x00FFBBBB, 0xFF0000FF};
    private Point userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.mapview;
        mapView.getMap().setRotateGesturesEnabled(false);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        } else {
            getUserLocation();
        }
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                userLocation = new Point(latitude, longitude);
                                submitRequest();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Доступ к местоположению запрещен", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void submitRequest() {
        if (userLocation != null) {
            Point startPoint = userLocation;
            DrivingOptions drivingOptions = new DrivingOptions();
            VehicleOptions vehicleOptions = new VehicleOptions();
            drivingOptions.setRoutesCount(4);
            ArrayList
                    <RequestPoint> requestPoints = new ArrayList<>();
            requestPoints.add(new RequestPoint(startPoint, RequestPointType.WAYPOINT, null));
            requestPoints.add(new RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null));
            drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
        } else {
            Toast.makeText(this, "Позиция пользователя не определена", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        int color;
        for (int i = 0; i < list.size(); i++) {
            color = colors[i];
            mapObjects.addPolyline(list.get(i).getGeometry()).setStrokeColor(color);
        }

        Point destinationPoint = new Point(ROUTE_END_LOCATION.getLatitude(), ROUTE_END_LOCATION.getLongitude());

        PlacemarkMapObject destinationMarker = (PlacemarkMapObject) mapObjects.addPlacemark(destinationPoint);

        destinationMarker.setOpacity(0.8f);
        destinationMarker.setIcon(ImageProvider.fromResource(this, R.drawable.marker_icon));


        destinationMarker.addTapListener(new MapObjectTapListener() {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                Toast.makeText(MainActivity.this, "Красивое место, особенно зимой, лед на речке, крута 11/10", Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

}

