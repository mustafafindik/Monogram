package findik.mustafa.monogram;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelectLocationActivity extends AppCompatActivity {

    //Değişkenleri tanımladık
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ImageView reloadLocations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        //Toolbar
        Toolbar mToolbar = findViewById(R.id.selectlocation_toolbar);
        mToolbar.setTitle("Konum Seç : "); // Toolbar title.
        setSupportActionBar(mToolbar);
        //Toolbar geri botonu . Manifeste parent activitiy eklemeyi unutma.
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(); // Geri dönüş butonuna basılınca Add Activity'ye RESULT_CANCELED olarak dönecek.
                setResult(RESULT_CANCELED, i);
                finish();
            }
        });

        //initalize.
        listView= findViewById(R.id.locationlist);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        reloadLocations = findViewById(R.id.reflesh_locations);


        //Bulunduğumuz lokasyonlar listes çağrılırr.
        CallLocations();



        reloadLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLocations(); // Reloada basınca Bulunduğumuz lokasyonlar listes çağrılırr.
            }
        });


    }

    private void CallLocations() {
        // Konuma erişim izni var mı ?
        if (ActivityCompat.checkSelfPermission(SelectLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectLocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yoksa izin al.
            ActivityCompat.requestPermissions(SelectLocationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            //Varsa Konumu Lokasyon olarak al ve Liste için LocationService metodunu çağır.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location== null){
                location= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            LocationService(location);
        }
    }

    private void LocationService(Location location) {

        if (location != null){
            // Konumdaki yerlerin isimleri için  Geocoder oluşturduk
            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
            final ArrayList<String> Address = new ArrayList<>(); // Yerlerin isimleri için Adress listesi.

            try {
                // Adress listesine geocoderden gelen veriler atılır.
                List<android.location.Address> addressList = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addressList.size() > 0) { // Veri varsa

                    if (addressList.get(0).getCountryName() !=null){
                        Address.add(addressList.get(0).getCountryName()); //Ülke adı.
                    }
                    if (addressList.get(0).getAdminArea() !=null){
                        Address.add(addressList.get(0).getAdminArea()); //Şehir.
                        Address.add(addressList.get(0).getAdminArea() + " Province"); // Şehir Province
                    }
                    if (addressList.get(0).getAdminArea() !=null && addressList.get(0).getCountryName() !=null  ){ //Şehir / Ülke
                        Address.add(addressList.get(0).getAdminArea() + " / "  + addressList.get(0).getCountryName());
                    }
                    if (addressList.get(0).getSubAdminArea() !=null && addressList.get(0).getAdminArea() !=null  ){ // Şehir /ilçe
                        Address.add(addressList.get(0).getSubAdminArea() + " / "  + addressList.get(0).getAdminArea());

                    }
                    if (addressList.get(0).getSubAdminArea() !=null){ //ilçe
                        Address.add(addressList.get(0).getSubAdminArea());
                    }
                    if (addressList.get(0).getSubLocality() !=null && addressList.get(0).getSubAdminArea() !=null  ){ //mahalle / ilçe
                        Address.add(addressList.get(0).getSubLocality() +" / "  + addressList.get(0).getSubAdminArea() );
                    }
                    if (addressList.get(0).getSubLocality() !=null){ //Mahalle
                        Address.add(addressList.get(0).getSubLocality());
                    }
                    if (addressList.get(0).getThoroughfare() !=null){ //SOkak
                        Address.add(addressList.get(0).getThoroughfare());
                    }


                    // Liste için adatper oluşturulur ve liste tipi + arraylist.
                    ArrayAdapter adapter =  new ArrayAdapter(this,android.R.layout.simple_list_item_1,Address);
                    listView.setAdapter(adapter); //listeview a adatper eklenir.

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent();
                            i.putExtra("Location", Address.get(position));
                            setResult(RESULT_OK, i); // seçim yapıldılktan sonra AddAct. ye  RESULT_OK ve Lokaston verisi ile dönülür.
                            finish();

                        }
                    });

                } else {
                    // Adress bulunamadıysa bildirim.
                    Toast.makeText(SelectLocationActivity.this, "Adres Bulunamadı", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                // Catch hatası
                Toast.makeText(SelectLocationActivity.this, "Adres Bulunurken Hata Oluştu. Hata : " +e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Konum Bulunamadı.", Toast.LENGTH_SHORT).show();
        }
       
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //İzin verildikten sonra olacaklar.
        if (grantResults.length>0){
            if (requestCode ==1) {
                //Varsa Konumu Lokasyon olarak al ve Liste için LocationService metodunu çağır.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LocationService(location);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
