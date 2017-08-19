package com.vinrosa.badtransitapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vinrosa.badtransitapp.model.Item;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

public class FormActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST = 0x100;
    private static final int GALLERY_PERMISSION_REQUEST = 0x200;
    private static final int MY_APP_REQUEST_LOCATION_PERMISSION = 0X1001;
    FusedLocationProviderClient locationProviderClient;
    private LocationCallback mLocarionCallback;
    private ImageView mImageView;
    private EditText mDescriptionEditText;
    private Button mSendButton;
    private EditText form_title;
    private Bitmap bitmap;
    private String selectedImagePath;

    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        mImageView = (ImageView) findViewById(R.id.form_image_view);
        form_title = (EditText) findViewById(R.id.form_title);
        mDescriptionEditText = (EditText) findViewById(R.id.form_description_edit_text);
        mSendButton = (Button) findViewById(R.id.form_send_button);
        mImageView.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        loadLocation();
        mLocarionCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                List<Location> locations = locationResult.getLocations();
                currentLatitude = lastLocation.getLatitude();
                currentLongitude = lastLocation.getLongitude();
                printLocation("Last Location: ", lastLocation);

                for (Location location : locations) {
                    printLocation("Current Location: ", location);
                }

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationProviderClient
                .getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location result = task.getResult();
                        printLocation("Last Location Only: ", result);
                    }
                });

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);

        locationProviderClient.requestLocationUpdates(locationRequest, mLocarionCallback, null);
    }

    /* @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

     }
 */
    private void loadLocation() {
        locationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);

        LocationRequest locationRequest = new LocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_APP_REQUEST_LOCATION_PERMISSION);


            return;
        }
        locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location result = task.getResult();
                if (result != null)
                    printLocation("Resultado: ", result);
            }
        });
    }

    private void printLocation(String indix, Location result) {
        Log.d("MainActivity", indix + result.getLatitude() + " *** " + result.getLongitude());
    }

    @Override
    public void onClick(View view) {
        if (view == mImageView) {
            startDialog();
        } else if (view == mSendButton) {
            sendForm();
        }
    }

    private void sendForm() {
        mSendButton.setEnabled(false);
        mImageView.setDrawingCacheEnabled(true);
        mImageView.buildDrawingCache();
        Bitmap bitmap = mImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Images");
        final String fileName = "IMG_" + new Date().getTime() + ".jpg";

        StorageReference mountainsRef = storageRef.child(fileName);
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("reports");
                Item item = new Item();
                item.image = fileName;
                item.date = new Date();
                item.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                item.description = mDescriptionEditText.getText().toString();
                item.latitude = currentLatitude;
                item.longitude = currentLongitude;
                item.title = form_title.getText().toString();
                myRef.push().setValue(item);
                finish();
            }
        });
    }

    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(R.string.select_pictures_title);
        myAlertDialog.setMessage(R.string.select_pictures_msg);
        myAlertDialog.setPositiveButton(R.string.gallery,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        int result = ContextCompat.checkSelfPermission(FormActivity.this, Manifest.permission_group.STORAGE);
                        if (result == PackageManager.PERMISSION_GRANTED) {
                            launchGallery();
                        } else {
                            ActivityCompat.requestPermissions(FormActivity.this, new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST);
                        }
                    }
                });
        myAlertDialog.setNegativeButton(R.string.camera,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        int result = ContextCompat.checkSelfPermission(FormActivity.this, Manifest.permission.CAMERA);
                        if (result == PackageManager.PERMISSION_GRANTED) {
                            launchCamera();
                        } else {
                            ActivityCompat.requestPermissions(FormActivity.this, new String[]{
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST);
                        }
                    }
                });
        myAlertDialog.show();
    }

    private void launchGallery() {
        Intent pictureActionIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(
                pictureActionIntent,
                GALLERY_PICTURE);
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (CAMERA_PERMISSION_REQUEST == requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        launchCamera();
                    }
                }
            }
        } else if (GALLERY_PERMISSION_REQUEST == requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        launchGallery();
                    }
                }
            }
        } else if (requestCode == MY_APP_REQUEST_LOCATION_PERMISSION) {
            int i = 0;
            boolean locationEnabled = false;
            for (String permission : permissions) {
                if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    locationEnabled &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                i++;
            }
            if (locationEnabled) loadLocation();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(bitmap);
        } else if (resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            if (data != null) {
                Uri pickedImage = data.getData();
                // Let's read picked image path using content resolver
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                selectedImagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                // Do something with the bitmap
                mImageView.setImageBitmap(bitmap);
                // At the end remember to close the cursor or you will end with the RuntimeException!
                cursor.close();
            } else {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
