package com.xayris.donalduck.ui.home;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.xayris.donalduck.R;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.data.entities.Story;
import com.xayris.donalduck.databinding.FragmentRandomStoryBinding;
import com.xayris.donalduck.utils.Utility;

import java.util.AbstractMap;

public class RandomStoryFragment extends DialogFragment implements View.OnClickListener {

    private FragmentRandomStoryBinding _binding;
    Comic _comic;
    Story _story;
    Size _coverImageSize;
    public RandomStoryFragment() { }

    public static RandomStoryFragment newInstance() {
        return new RandomStoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (_binding == null)
            _binding = FragmentRandomStoryBinding.inflate(inflater, container, false);
        DisplayMetrics metrics = Utility.getDisplayMetrics(requireContext());
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.detail_width_downscale_factor, outValue, true);
        float itemWidthDownscaleFactor = outValue.getFloat();
        outValue = new TypedValue();
        getResources().getValue(R.dimen.detail_height_downscale_factor, outValue, true);
        float itemHeightDownscaleFactor = outValue.getFloat();
        _coverImageSize = new Size((int)(metrics.widthPixels / itemWidthDownscaleFactor),(int)(metrics.widthPixels / itemHeightDownscaleFactor));
        _binding.coverImg.getLayoutParams().width = _coverImageSize.getWidth();
        _binding.coverImg.getLayoutParams().height = _coverImageSize.getHeight();

        _binding.setStoryReadButton.setOnClickListener(this);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AbstractMap.SimpleEntry<Comic, Story> randomUnreadStory =
                ComicsRepository.getInstance().getRandomUnreadStory();

        _comic = randomUnreadStory.getKey();
        _story = randomUnreadStory.getValue();
        _binding.issueTxt.setText(getString(R.string.issue_number, _comic.getIssue()));
        _binding.issueDateTxt.setText(_comic.getIssueDateFormatted());
        _binding.storyTitleTxt.setText(_story.getTitle());

        Glide.with(requireContext().getApplicationContext())
                .load(_comic.getCoverUrl())
                .placeholder(R.drawable.cover_placeholder)
                .override(_coverImageSize.getWidth(), _coverImageSize.getHeight())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(_binding.coverImg);
    }

    @Override
    public void onDestroyView() {
        _binding = null;
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.setStoryReadButton) {
            boolean isRead = !_story.getIsRead();

            ComicsRepository.getInstance().setStoryRead(_story);

            if (isRead) {
                _binding.setStoryReadButton.setIconResource(R.drawable.ic_close);
                _binding.setStoryReadButton.setText(R.string.unset_story_read);
            } else {
                _binding.setStoryReadButton.setIconResource(R.drawable.ic_check);
                _binding.setStoryReadButton.setText(R.string.set_story_read);
            }
        }
    }
}
