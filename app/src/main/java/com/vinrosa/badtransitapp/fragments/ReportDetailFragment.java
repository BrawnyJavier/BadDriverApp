package com.vinrosa.badtransitapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.vinrosa.badtransitapp.R;
import com.vinrosa.badtransitapp.model.Item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String Tag = "ReportDetailFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView Detail;
    private TextView Email;
    private ImageView Image;
    private TextView Date;
    private TextView Title;
    private Uri imageDownloadUrl;
    // TODO: Rename and change types of parameters
    private String item_key;
    private Item currItem;

    MapView mMapView;
    private GoogleMap googleMap;

    private OnFragmentInteractionListener mListener;

    public ReportDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportDetailFragment newInstance(String param1, String param2) {
        ReportDetailFragment fragment = new ReportDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_menu, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            item_key = bundle.getString("itemId");
           // Toast.makeText(this.getContext(), bundle.getString("itemId"), Toast.LENGTH_LONG).show();
            setHasOptionsMenu(true);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("reports").child(item_key);
            // Query the firebase value for its details
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Item item = dataSnapshot.getValue(Item.class);
                    final Intent share = new Intent(Intent.ACTION_SEND);
                    if (item != null) {
                        currItem = item;
                        Email.setText(currItem.email);
                        Detail.setText(currItem.description);
                        Title.setText(currItem.title);
                        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
                        Date.setText(dateFormat.format(item.date));
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        if (isAdded())
                            storage.getReference("Images").child(item.image).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getActivity().getApplicationContext()).load(uri).into(Image);
                                    imageDownloadUrl = uri;
                                    //Uri screenshotUri = Uri.parse(item.image);
/*
                                    share.setType("image/png");
                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    String message = "Text I want to share.";

                                    // share.setType("text/plain");
                                    share.putExtra(Intent.EXTRA_TEXT, message);

                                    mListener.onLoadedReport(share);*/


                                }
                            });
                        Log.d("ReportDetail", item.toString());
                        mMapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap mMap) {
                                googleMap = mMap;

                                // For showing a move to my location button
                                //  googleMap.setMyLocationEnabled(true);

                                // For dropping a marker at a point on the Map
                                LatLng sydney = new LatLng(currItem.latitude, currItem.longitude);
                                googleMap.addMarker(new MarkerOptions().position(sydney).title("Lugar del Accidente").snippet(currItem.title));

                                // For zooming automatically to the location of the marker
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        this.Email = (TextView) view.findViewById(R.id.item_email_FDET);
        this.Detail = (TextView) view.findViewById(R.id.item_desc_FDET);
        this.Image = (ImageView) view.findViewById(R.id.item_img_FDET);
        this.Date = (TextView) view.findViewById(R.id.date_FDET);
        this.Title = (TextView) view.findViewById(R.id.item_title_FDET);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //  mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public String ShareReport() {
        Image.buildDrawingCache();
        Bitmap bm = Image.getDrawingCache();

        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "FoodMana" + File.separator);
            root.mkdirs();
            Calendar now = Calendar.getInstance();

            File sdImageMainDirectory = new File(root, now.getTime().toString() + "_IMG");
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "We are sorry, an error has occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "We are sorry, an error has occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        PackageManager pm = getActivity().getPackageManager();

        try {
          //  Toast.makeText(getActivity(), "Sharing Via Whats app !", Toast.LENGTH_LONG).show();
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("image/*");
            waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            waIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory()
                    + File.separator + "FoodMana" + File.separator+"myPicName.jpg"));
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
            waIntent.putExtra(Intent.EXTRA_TEXT, currItem.title+" ("+dateFormat.format(currItem.date)+")"
                    +"\n---\n"+currItem.description+"\n\n Image Link: "+imageDownloadUrl.toString()+ "\n\n - Shared via "+getResources().getString(R.string.app_name));
            startActivity(Intent.createChooser(waIntent, "Share with"));
        } catch (/*PackageManager.NameNotFoundException*/ Exception e) {
            Intent EmailShareIntent = new Intent(Intent.ACTION_SEND);
            EmailShareIntent.setData(Uri.parse("mailto:"));
            EmailShareIntent.setType("image/*");
            EmailShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory()
                    + File.separator + "BadDriver" + File.separator + item_key));
            EmailShareIntent.putExtra(Intent.EXTRA_TEXT, currItem.title + " - " + getResources().getString(R.string.SharedVia));
            startActivity(EmailShareIntent);
            try {
                startActivity(Intent.createChooser(EmailShareIntent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.ShareNoAppFoundError), Toast.LENGTH_SHORT).show();
            }

        }
        return "Success";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }
}
