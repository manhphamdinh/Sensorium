package com.example.blackbox;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class Puzzle15Fragment extends PuzzleBaseFragment {

    private LocationManager locationManager;
    private Location lastLocation = null;
    private double totalDistance = 0; // Đơn vị: Mét
    private DistanceRadarView radarView;
    private SharedPreferences prefs;

    private boolean[] isSolved = new boolean[4];

    @Override
    public int getPuzzleId() { return 15; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle15, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radarView = view.findViewById(R.id.radarView);

        prefs = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
        totalDistance = prefs.getFloat("puzzle15_distance", 0f);
        radarView.setDistance(totalDistance);

        if (totalDistance >= 10) { isSolved[0] = true; radarView.setSolved(0); }
        if (totalDistance >= 100) { isSolved[1] = true; radarView.setSolved(1); }
        if (totalDistance >= 1000) { isSolved[2] = true; radarView.setSolved(2); }
        if (totalDistance >= 10000) { isSolved[3] = true; radarView.setSolved(3); }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 100);
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2f, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (lastLocation != null) {
                totalDistance += lastLocation.distanceTo(location);
                prefs.edit().putFloat("puzzle15_distance", (float) totalDistance).apply();

                if (radarView != null) radarView.setDistance(totalDistance);
                checkWinConditions();
            }
            lastLocation = location;
        }
    };

    private void checkWinConditions() {
        if (totalDistance >= 10 && !isSolved[0]) {
            isSolved[0] = true;
            radarView.setSolved(0);
            animation(0);
        }
        if (totalDistance >= 100 && !isSolved[1]) {
            isSolved[1] = true;
            radarView.setSolved(1);
            animation(1);
        }
        if (totalDistance >= 1000 && !isSolved[2]) {
            isSolved[2] = true;
            radarView.setSolved(2);
            animation(2);
        }
        if (totalDistance >= 10000 && !isSolved[3]) {
            isSolved[3] = true;
            radarView.setSolved(3);
            animation(3);
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (getActivity() != null && isAdded()) {
                    getActivity().finish();
                }
            }, 1500);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}