package com.xayris.donalduck.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.xayris.donalduck.R;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.ComicArchiveListItemBinding;
import com.xayris.donalduck.utils.Utility;

import java.util.List;

public class ComicsArchiveAdapter extends RecyclerView.Adapter<ComicsArchiveAdapter.ComicViewHolder> {

    private List<Comic> _comics;
    private final OnItemClickListener _clickListener;
    private final Size _coverSize;
    public ComicsArchiveAdapter(Context context, List<Comic> comics, OnItemClickListener clickListener) {
        _comics = comics;
        _clickListener = clickListener;
        DisplayMetrics metrics = Utility.getDisplayMetrics(context);
        TypedValue outValue = new TypedValue();
        context.getResources().getValue(R.dimen.archive_item_width_downscale_factor, outValue, true);
        float itemWidthDownscaleFactor = outValue.getFloat();
        outValue = new TypedValue();
        context.getResources().getValue(R.dimen.archive_item_height_downscale_factor, outValue, true);
        float itemHeightDownscaleFactor = outValue.getFloat();
        _coverSize = new Size((int)(metrics.widthPixels / itemWidthDownscaleFactor), (int)(metrics.widthPixels / itemHeightDownscaleFactor));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Comic> comics) {
        _comics = comics;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_archive_list_item, parent, false);

        return new ComicViewHolder(view, _coverSize);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        Comic story = _comics.get(position);
        holder.update(story, _clickListener);
    }

    @Override
    public int getItemCount() {
        return _comics != null ? _comics.size() : 0;
    }

    public static class ComicViewHolder extends RecyclerView.ViewHolder {
        final ComicArchiveListItemBinding _binding;
        final Size _coverSize;
        public ComicViewHolder(@NonNull View itemView, Size coverSize) {
            super(itemView);
            _coverSize = coverSize;
            itemView.getLayoutParams().width = _coverSize.getWidth();
            itemView.getLayoutParams().height = _coverSize.getHeight();
            _binding = ComicArchiveListItemBinding.bind(itemView);
        }

        public void update(Comic comic, OnItemClickListener clickListener) {
            _binding.issueTxt.setText(itemView.getContext().getString(R.string.issue_number,comic.getIssue().toUpperCase()));
            Glide.with(itemView.getContext().getApplicationContext()).load(comic.getCoverUrl()).transition(DrawableTransitionOptions.withCrossFade()).override(_coverSize.getWidth(),_coverSize.getHeight()).placeholder(R.drawable.cover_placeholder).into(_binding.coverImg);
            itemView.setOnClickListener(v -> clickListener.onItemClick(comic));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Comic item);
    }
}
