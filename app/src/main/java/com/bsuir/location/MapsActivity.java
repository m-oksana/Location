package com.bsuir.location;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener{

    private static final String YOU_ARE_HERE_TITLE = "Вы здесь";

    private GoogleMap mMap;
    private ImageView pinUpImage;
    private LatLng initialPlace;

    private Button goHereButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initViews();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getPreviousActivityData();
        googleMap.addMarker(new MarkerOptions()
                .position(initialPlace)
                .title(YOU_ARE_HERE_TITLE)
                .draggable(Boolean.FALSE));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialPlace, 15.0f));
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraIdleListener(this);
    }

        @Override
        public void onCameraIdle() {
            pinUpImage.setVisibility(View.GONE);
            mMap.addMarker(new MarkerOptions()
                    .position(mMap.getCameraPosition().target)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin_up)));
        }

        @Override
        public void onCameraMove() {
            mMap.clear();
            pinUpImage.setVisibility(View.VISIBLE);
        }


        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.goHereButton) {
                LatLng latLng = mMap.getCameraPosition().target;
                Intent intent = new Intent();
                intent.putExtra("LATITUDE", String.valueOf(latLng.latitude));
                intent.putExtra("LONGITUDE", String.valueOf(latLng.longitude));
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        private void initViews() {
            pinUpImage = findViewById(R.id.pinUpImage);
            goHereButton = findViewById(R.id.goHereButton);
            goHereButton.setOnClickListener(this);
        }


        private void getPreviousActivityData() {
            Intent intent = getIntent();
            initialPlace = new LatLng(
                    Double.parseDouble(intent.getStringExtra("LATITUDE")),
                    Double.parseDouble(intent.getStringExtra("LONGITUDE")));
        }
}
