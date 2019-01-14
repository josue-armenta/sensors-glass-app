package mx.edu.uabc.sensores;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        createCards();

        mCardScrollView = new CardScrollView(this);
        ExampleCardScrollAdapter mAdapter = new ExampleCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();
        setupClickListener();
        setContentView(mCardScrollView);
    }

    private void setupClickListener() {
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                intent.putExtra(RecordActivity.POINT_POSITION, i);
                startActivity(intent);
            }
        });
    }

    private void createCards() {

        mCards = new ArrayList<>();

        String[] points = {"windshield", "central rearview mirror",
                "left rearview mirror", "right rearview mirror", "radio"};

        for (int i = 0; i < points.length; i++) {
            mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                    .setText("Record " + points[i] + " coordinates")
                    .setFootnote("S" + String.valueOf(i)));
        }
    }

    private class ExampleCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }

}
