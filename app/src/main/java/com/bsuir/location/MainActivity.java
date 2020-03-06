package com.bsuir.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 123;
    private static final String FIND_LOCATION_ERROR = "Нет возможности определить местоположение";

    private static final String TRY_AGAIN = "Попробуйте нажать еще раз";

    private static final String LANGUAGE_TAG_RU = "ru";

    private EditText streetFrom;
    private EditText houseFrom;

    private EditText streetTo;
    private EditText houseTo;

    private Button findLocation;
    private Button setLocation;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findLocationButton:
                findLocation();
                break;
            case R.id.setLocationButton:
                setLocation();
                break;
        }
    }


    private void initViews() {
        streetFrom = findViewById(R.id.streetFromInput);
        houseFrom = findViewById(R.id.houseFromInput);

        streetTo = findViewById(R.id.streetToInput);
        houseTo = findViewById(R.id.houseToInput);

        findLocation = findViewById(R.id.findLocationButton);
        findLocation.setOnClickListener(this);

        setLocation = findViewById(R.id.setLocationButton);
        setLocation.setOnClickListener(this);
    }

    private void findLocation() {
        getAddress()
                .map(address -> {
                    streetFrom.setText(address.getThoroughfare());
                    houseFrom.setText(address.getSubThoroughfare());
                    return address;
                });
    }

    private void setLocation() {
        final String[] latitude = {"0"};
        final String[] longitude = {"0"};
        getAddress().map(adr -> {
            latitude[0] = String.valueOf(adr.getLatitude());
            longitude[0] = String.valueOf(adr.getLongitude());
            return adr;
        });
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("LATITUDE", latitude[0]);
        intent.putExtra("LONGITUDE", longitude[0]);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        try {
            Address address = getAddress(
                    Double.parseDouble(data.getStringExtra("LATITUDE")),
                    Double.parseDouble(data.getStringExtra("LONGITUDE")));
            streetTo.setText(address.getThoroughfare());
            houseTo.setText(address.getSubThoroughfare());
        } catch (Throwable throwable) {
            Toast.makeText(this, FIND_LOCATION_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    private Optional<Address> getAddress() {
        if (!checkPermission()) {
            Toast.makeText(this, TRY_AGAIN, Toast.LENGTH_SHORT).show();
            return Optional.empty();
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Address address = getAddress(location.getLatitude(), location.getLongitude());
        return address == null ? Optional.empty() : Optional.of(address);
    }

    private Address getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.forLanguageTag(LANGUAGE_TAG_RU));
        try {
            List<Address> addresses = geocoder
                    .getFromLocation(latitude, longitude, 5);
            for (int i = 0; i < addresses.size(); i++) {
                Address adr = addresses.get(i);
                if (adr.getThoroughfare() != null && adr.getSubThoroughfare() != null) {
                    return adr;
                }
            }
        } catch (Throwable throwable) {
            Toast.makeText(this, FIND_LOCATION_ERROR, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private boolean checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_CODE);
            return false;
        }

        return true;
    }
}
