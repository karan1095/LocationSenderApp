package com.example.android.sendinglocation;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class sender extends Activity implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 5;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    String lat=null,lng=null,dir=null;
    float speed;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
       /* if (!isGooglePlayServicesAvailable()) {
            finish();
        }*/
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

      //  setContentView(R.layout.activity_sender);
        tvLocation = (TextView) findViewById(R.id.tvLocation);


    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

 /*   private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }*/

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
             lat = String.valueOf(mCurrentLocation.getLatitude());
             lng = String.valueOf(mCurrentLocation.getLongitude());
             speed = mCurrentLocation.getSpeed();
             //speed = Double.parseDouble(speed)* 1.6;
             dir = "EAST";
            tvLocation.setText("At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider());
            if(lat!=null&&lng!=null)
               // if(mCurrentLocation.getAccuracy()<2000.00) {
                    metro_locator();

        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }


    private void metro_locator() {

        String tag_string_req = "upload";

        //pDialog.setMessage("Registering ...");


        StringRequest strReq = new StringRequest(Request.Method.POST,
                "http://cgossip.in/karan.php", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());




                Toast.makeText(getApplicationContext(), "successfully sent", Toast.LENGTH_LONG).show();





            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Host", "cgossip.in");

                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat",lat);
                params.put("lon",lng);
                params.put("speed", speed+"");
                params.put("dir", dir);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    void send() {
        JsonArrayRequest movieReq = new JsonArrayRequest("http://166.62.28.86/movies1.json",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        //hidePDialog();
                        // pbar.setVisibility(View.GONE);
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Log.d("karan ka",""+obj);
                                Toast.makeText(getApplicationContext(), "Connected" +
                                        "r", Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // VolleyLog.d(TAG, "Error: " + error.getMessage());
                // hidePDialog();
                // re.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Error" + error.getMessage(), Toast.LENGTH_LONG).show();


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Host", "cgossip.in");

                return headers;
            }
        };

        // Adding request to request queue
        //Toast.makeText(getActivity(), "Times : " + times, Toast.LENGTH_LONG).show();

        AppController.getInstance().addToRequestQueue(movieReq);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sender, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
