package com.vinrosa.badtransitapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vinrosa.badtransitapp.R;
import com.vinrosa.badtransitapp.adapter.ItemAdapter;
import com.vinrosa.badtransitapp.model.Item;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ItemsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemsFragment extends Fragment {

    private static final String TAG =  "ItemsFragment";

    private OnFragmentInteractionListener mListener;
    private ItemAdapter mItemAdapter;

    public ItemsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemsFragment.
     */
    public static ItemsFragment newInstance() {
        ItemsFragment fragment = new ItemsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.items_recycler_viewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mItemAdapter = new ItemAdapter(getActivity());
        recyclerView.setAdapter(mItemAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            loadItems();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    private void loadItems(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("reports");
        myRef.limitToLast(100).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Item value = dataSnapshot.getValue(Item.class);
                value.key = dataSnapshot.getKey();
                mItemAdapter.addItem(value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Item value = dataSnapshot.getValue(Item.class);
                value.key = dataSnapshot.getKey();
                mItemAdapter.updateItem(value);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Item value = dataSnapshot.getValue(Item.class);
                value.key = dataSnapshot.getKey();
                mItemAdapter.removeItem(value);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        void onItemSelected();
        void onItemSelected(String itemId);
    }
}
