package sample.com.cats;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by seanchuang on 5/23/16.
 */
public class HyperLinkFragment extends Fragment {
    private static final String TAG = "HyperLinkFragment";
    private static final String EXAMPLE_LINK = "migme://post/create?message=hello/&referrer=migcats/&hashtag=MigCats";
    private EditText mUrlEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hyper, container, false);
        mUrlEditText = (EditText) rootView.findViewById(R.id.url_EditText);
        rootView.findViewById(R.id.send_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressSend();
            }
        });
        ((TextView) rootView.findViewById(R.id.example_TextView)).setText(EXAMPLE_LINK);
        return rootView;
    }

    private void pressSend() {
        if (mUrlEditText.getText().length() > 0) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(mUrlEditText.getText().toString()));
                startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
