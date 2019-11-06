package com.fire.base.login.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fire.base.database.Flag;
import com.fire.base.login.R;
import com.fire.base.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlagsAdapter extends RecyclerView.Adapter<FlagsAdapter.ViewHolder>{

    private final Context mContext;
    private final ArrayList<Flag> mItems = new ArrayList<>();
    private final String mCurrentCountry;
    private Listener mListener;

    public FlagsAdapter(Context context, List<Flag> flags, String currentCountry){
        mContext = context;
        mItems.addAll(flags);
        mCurrentCountry = currentCountry;
    }

    public void setListener(Listener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public FlagsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_flag_item, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.flagContainer.setOnClickListener(view -> {
            mListener.onFlagClicked(mItems.get(holder.getAdapterPosition()));
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FlagsAdapter.ViewHolder holder, int position) {
        Flag flag = mItems.get(position);
        holder.flagTextView.setText(Utils.getEmoji(flag.unicode));
        String title = String.format(Locale.US, "%s (%s)", flag.name, flag.dialCode);
        holder.flagTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface Listener{
        void onFlagClicked(Flag flag);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView flagTextView;
        public TextView flagTitle;
        public View flagContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            flagContainer= itemView.findViewById(R.id.flag_container);
            flagTextView = itemView.findViewById(R.id.flag);
            flagTitle = itemView.findViewById(R.id.flag_title);
        }
    }
}
