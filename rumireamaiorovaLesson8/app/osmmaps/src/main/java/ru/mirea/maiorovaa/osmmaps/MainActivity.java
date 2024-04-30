package ru.mirea.maiorovaa.osmmaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 123;
    private MyLocationNewOverlay locationNewOverlay;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        } else {
            initializeMap();
        }
    }

    private void initializeMap() {
        mapView.getController().setZoom(15.0);

        GeoPoint startPoint1 = new GeoPoint(55.794229, 37.700772);
        GeoPoint startPoint2 = new GeoPoint(55.714799, 38.957585);
        GeoPoint startPoint3 = new GeoPoint(55.670036, 37.480882);

        mapView.getController().setCenter(startPoint1);

        addMarker(startPoint1, "МИРЭА Стромынка", org.osmdroid.library.R.drawable.osm_ic_follow_me_on);
        addMarker(startPoint2, "Лавочки, и фонтанчик высотой в метр... зато с подстветкой \uD83D\uDE0E", org.osmdroid.library.R.drawable.osm_ic_follow_me_on);
        addMarker(startPoint3, "МИРЭА не Стромынка(Вернадка)", org.osmdroid.library.R.drawable.osm_ic_follow_me_on);

        final Context context = this.getApplicationContext();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(getApplicationContext(), new InternalCompassOrientationProvider(getApplicationContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        locationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mapView);
        locationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(locationNewOverlay);
    }

    private void addMarker(GeoPoint position, String title, int iconResource) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setIcon(getResources().getDrawable(iconResource));
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Доступ к местоположению запрещен", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView.onPause();
    }
}
