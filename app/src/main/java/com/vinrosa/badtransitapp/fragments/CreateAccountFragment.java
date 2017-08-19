package com.vinrosa.badtransitapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vinrosa.badtransitapp.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateAccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateAccountFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CreateAccountFragment";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;

    private Button createAccountButton;

    private OnFragmentInteractionListener mListener;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateAccountFragment.
     */
    public static CreateAccountFragment newInstance() {
        CreateAccountFragment fragment = new CreateAccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_create_account, container, false);

        usernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
        passwordEditText = (EditText) view.findViewById(R.id.password_edit_text);
        passwordConfirmEditText = (EditText) view.findViewById(R.id.password_confirm_edit_text);

        createAccountButton = (Button) view.findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(this);

        return view;
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        String email = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (view == createAccountButton) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Toast.makeText(getActivity(), R.string.create_account_failed,  Toast.LENGTH_SHORT).show();
                            } else {
                                mListener.onAccountSuccessfullyCreated();
                            }
                        }
                    });
        }
    }

    public interface OnFragmentInteractionListener {
        void onAccountSuccessfullyCreated();
    }
}
