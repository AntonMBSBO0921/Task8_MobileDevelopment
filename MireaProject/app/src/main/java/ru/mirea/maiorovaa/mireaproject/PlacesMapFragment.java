package ru.mirea.maiorovaa.mireaproject;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

public class PlacesMapFragment extends Fragment implements LocationListener {
    private MapView mapView;
    private LocationManager locationManager;
    private PlacemarkMapObject userLocationMarker;
    private MapObjectCollection mapObjects;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final String MAPKIT_API_KEY = "873211c9-bbf5-4f2b-9c31-0a32c2b98871";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(getContext());
        return inflater.inflate(R.layout.fragment_places_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapview);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        checkLocationPermission();
        initUserLocationMarker();
        addInterestingPlaces();

        Button btnZoomIn = view.findViewById(R.id.btnZoomIn);
        Button btnZoomOut = view.findViewById(R.id.btnZoomOut);

        btnZoomIn.setOnClickListener(v -> adjustZoom(0.5f));
        btnZoomOut.setOnClickListener(v -> adjustZoom(-0.5f));
    }
    //Метод для работы с картой(уменьшение и увеличение карты)
    private void adjustZoom(float deltaZoom) {
        if (mapView != null && mapView.getMap() != null) {
            float currentZoom = mapView.getMap().getCameraPosition().getZoom();
            CameraPosition newCameraPosition = new CameraPosition(
                    mapView.getMap().getCameraPosition().getTarget(),
                    currentZoom + deltaZoom,
                    0,
                    0
            );

            mapView.getMap().move(
                    newCameraPosition,
                    new Animation(Animation.Type.SMOOTH, 1),
                    null
            );
        }
    }



    private void addInterestingPlaces() {
        addPlace(new Point(55.755826, 37.6172999), "Красная площадь", R.drawable.mark);
        addPlace(new Point(55.793959, 37.700807), "МИРЭА на Стромынке", R.drawable.mark);
        addPlace(new Point(59.933425, 30.310942), "Питер-Санкт", R.drawable.mark);
        addPlace(new Point(43.584492, 39.723857), "Сочи, тепло....", R.drawable.mark);
        addPlace(new Point(55.715864, 37.553855), "Лужники, стадион", R.drawable.mark);
    }

    private void addPlace(Point location, String description, int iconResourceId) {
        PlacemarkMapObject place = mapObjects.addPlacemark(location);
        place.setIcon(ImageProvider.fromResource(getActivity(), iconResourceId));
        place.setUserData(description);
        place.addTapListener((mapObject, point) -> {
            String desc = (String) mapObject.getUserData();
            Toast.makeText(getContext(), desc, Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private void initUserLocationMarker() {
        userLocationMarker = mapView.getMap().getMapObjects().addPlacemark(new Point(0, 0));
        userLocationMarker.setIcon(ImageProvider.fromResource(getActivity(), R.drawable.user_location));
        userLocationMarker.setVisible(false);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
                }
            } else {
                Toast.makeText(getContext(), "Доступ отсутствует", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Point userLocation = new Point(location.getLatitude(), location.getLongitude());
        userLocationMarker.setGeometry(userLocation);
        userLocationMarker.setVisible(true);
        mapView.getMap().move(new CameraPosition(userLocation, 14.0f, 0, 0), new Animation(Animation.Type.SMOOTH, 0), null);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }
}
