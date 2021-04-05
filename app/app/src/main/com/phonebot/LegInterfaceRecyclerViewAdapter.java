package main.com.phonebot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import main.com.phonebot.LegInterfaceFragment.OnListFragmentInteractionListener;
import main.com.phonebot.LegViewData.LegItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LegItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class LegInterfaceRecyclerViewAdapter extends RecyclerView.Adapter<LegInterfaceRecyclerViewAdapter.ViewHolder> {

    private final List<LegItem> mItems;
    private final OnListFragmentInteractionListener mListener;

    public LegInterfaceRecyclerViewAdapter(List<LegItem> items, OnListFragmentInteractionListener listener) {
        mItems = items;
        mListener = listener;
    }

    public void UpdateView(final ViewHolder holder) {
        holder.mValueView.setText(Integer.toString(holder.mItem.legValue));
        holder.mSeekBar.setProgress(holder.mItem.legValue);

        if (null != mListener) {
            // Notify the active callbacks interface that the item has been changed.
            mListener.ListFragmentInteraction(holder.mItem);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_leginterface, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mNameView.setText(mItems.get(position).legName);
        holder.mValueView.setText(Integer.toString(mItems.get(position).legValue));

        // Configure buttons
        holder.mIncrementButton.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ++holder.mItem.legValue;
                        UpdateView(holder);
                    }
                }
        );
        holder.mDecrementButton.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        --holder.mItem.legValue;
                        UpdateView(holder);
                    }
                }
        );

        // Configure seek bar
        holder.mSeekBar.setMax(180);
        holder.mSeekBar.setProgress((int) mItems.get(position).legValue);
        holder.mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        holder.mItem.legValue = i; // Set the leg value to the new seek bar value
                        UpdateView(holder);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mValueView;
        public final SeekBar mSeekBar;
        public final Button mIncrementButton;
        public final Button mDecrementButton;

        public LegItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.leg_name);
            mValueView = (TextView) view.findViewById(R.id.leg_value);
            mSeekBar = (SeekBar) view.findViewById(R.id.leg_seek_bar);
            mIncrementButton = (Button) view.findViewById(R.id.leg_increment_button);
            mDecrementButton = (Button) view.findViewById(R.id.leg_decrement_button);
        }

    }
}
