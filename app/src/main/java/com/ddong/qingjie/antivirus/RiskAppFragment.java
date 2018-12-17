package com.ddong.qingjie.antivirus;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ddong.qingjie.R;


/**
 * Created by Antony on 2016/1/9.
 */
public class RiskAppFragment extends Fragment{
    private Context context;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = View.inflate(context, R.layout.riskapp_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Button riskapp_back = (Button)view.findViewById(R.id.riskapp_back);

        riskapp_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                while (fragmentManager.getBackStackEntryCount() > 1) {
                    fragmentManager.popBackStackImmediate();
                }
            }
        });

        super.onActivityCreated(savedInstanceState);
    }
}
