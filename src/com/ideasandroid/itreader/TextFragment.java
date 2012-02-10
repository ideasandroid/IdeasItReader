package com.ideasandroid.itreader;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextFragment extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	 View v = inflater.inflate(R.layout.hello_world, container, false);
	        View tv = v.findViewById(R.id.text);
	        ((TextView)tv).setText(getArguments().getString("text"));
	        //tv.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.gallery_thumb));
	        return v;
    }
    
}
