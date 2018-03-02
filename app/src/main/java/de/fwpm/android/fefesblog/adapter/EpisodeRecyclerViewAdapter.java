package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.Episode;
import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 02.03.18.
 */

public class EpisodeRecyclerViewAdapter extends RecyclerView.Adapter<EpisodeRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "EpisodeRecyclerViewAdapter";

    private ArrayList<Episode> mData;
    private Context mContext;
    private OnItemClickListener mListener;

    public EpisodeRecyclerViewAdapter(Context context, final OnItemClickListener listener, final ArrayList<Episode> data) {

        mContext = context;
        mData = data;
        mListener = listener;

    }

    public interface OnItemClickListener {
        void onItemClick(int position, Episode episode);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nr;
        private TextView title;
        private TextView date;

        public ViewHolder(View itemView) {

            super(itemView);
            nr = (TextView) itemView.findViewById(R.id.episode_nr);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);

        }

        void setClickListener(final int position, final Episode episode) {

            final View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position, episode);
                }
            };
            title.setOnClickListener(onClickListener);

        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_episode_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Episode episode = mData.get(position);

        holder.nr.setText("Folge " + episode.getNr());
        holder.title.setText(episode.getTitle().split(" ", 5)[4]);
        holder.date.setText(new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY).format(episode.getDate()));

        holder.setClickListener(position, episode);

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

}
