package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;
import com.nucc.hackwinds.ConditionArrayAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import android.net.Uri;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;


public class currentFragment extends ListFragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    String[] temp = {"3-5 feet"};
    Condition swell;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ArrayList<Condition> conditionValues = new ArrayList<Condition>();
        conditionValues.add(new Condition(Condition.ConditionTypes.WAVEHEIGHT, temp));
        ConditionArrayAdapter adapter = new ConditionArrayAdapter(getActivity(), conditionValues);
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
        //     R.layout.list_item, R.id.itemHeader, conditionHeaders);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        try {
            // Create the VideoView
            VideoView streamView = (VideoView) V.findViewById(R.id.currentVideoStreamView);

            // Change the MediaController
            //MediaController mediaController = new MediaController(this.getActivity());
            //mediaController.setAnchorView(streamView);

            // Specify the URI of the live stream
            Uri uri = Uri.parse(streamURL);

            // Setting the media controller
            //streamView.setMediaController(mediaController);
            streamView.setVideoURI(uri);
            streamView.requestFocus();

            // Start the video
            streamView.start();
        }
        catch (Exception ex) {
            Log.e(this.toString(), ex.toString());
        }

        return V;
    }

}