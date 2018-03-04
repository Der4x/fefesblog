package de.fwpm.android.fefesblog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.adapter.EpisodeRecyclerViewAdapter;
import de.fwpm.android.fefesblog.data.ALDataFetcher;
import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.EpisodeActivity.INTENT_NR;

public class AlternativlosActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<Episode> mEpisodeList;

    private Context mContext;
    private Handler mHandler;
    private RecyclerView.LayoutManager mLayoutManager;
    private EpisodeRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternativlos);

        mHandler = new Handler();
        mContext = this;

        initView();
        getData();

        new ALDataFetcher(this).execute();


    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    public void getData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if(mEpisodeList==null){
                    mEpisodeList = new ArrayList<>();
                }else mEpisodeList.clear();

                mEpisodeList.addAll((ArrayList<Episode>) AppDatabase.getInstance(mContext).episodeDao().getAllEpisodes());

                updateUI();

            }
        }).start();

    }

    private void initView() {

        setTitle(getString(R.string.alternativlos));

        mRecyclerView = (RecyclerView) findViewById(R.id.episodes_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mEpisodeList = new ArrayList<>();

        recyclerViewAdapter = new EpisodeRecyclerViewAdapter(mContext,
                new EpisodeRecyclerViewAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position, Episode episode) {

                        Intent intent  = new Intent(mContext, EpisodeActivity.class);
                        intent.putExtra(INTENT_NR, episode.getNr());
                        startActivity(intent);

                    }

                },mEpisodeList);

        mRecyclerView.setAdapter(recyclerViewAdapter);

    }

    public void updateUI() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                recyclerViewAdapter.notifyDataSetChanged();

            }
        });

    }

}
