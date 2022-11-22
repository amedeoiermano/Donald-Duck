package com.xayris.donalduck.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.xayris.donalduck.R;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.data.entities.Story;
import com.xayris.donalduck.utils.Utility;

import java.util.List;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoryViewHolder> {

    private final List<Story> _stories;
    private final OnItemClickListener _clickListener;

    public StoriesAdapter(Comic comic, OnItemClickListener clickListener) {
        _stories = comic.getStories();
        _clickListener = clickListener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.story_list_item, parent, false);

        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = _stories.get(position);
        holder.update(story, position % 2 == 0, _clickListener);
    }

    @Override
    public int getItemCount() {
        return _stories.size();
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        Story _story;
        AppCompatCheckBox _storyReadCheckBox;
        TextView _storyTitleTxt;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            _storyReadCheckBox = itemView.findViewById(R.id.storyReadCheckBox);
            _storyTitleTxt = itemView.findViewById(R.id.storyTitleTxt);
        }

        public void update(Story story, boolean evenRow, OnItemClickListener clickListener) {
            _story = story;
            _storyReadCheckBox.setOnCheckedChangeListener(null);
            _storyReadCheckBox.setChecked(story.getIsRead());
            _storyTitleTxt.setText(story.getTitle());
            _storyTitleTxt.setTextColor(evenRow ? itemView.getContext().getColor(R.color.yellow) : Color.WHITE);
            itemView.setOnClickListener(v -> {
                Utility.performHapticFeedback(v.getContext());
                _storyReadCheckBox.setChecked(!_storyReadCheckBox.isChecked());
            });
            _storyReadCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Utility.performHapticFeedback(buttonView.getContext());
                clickListener.onItemClick(story);
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Story item);
    }
}
