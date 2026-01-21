package com.xayris.donalduck.ui.archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.databinding.FragmentStatsBinding;

import java.util.List;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding _binding;

    public StatsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        _binding = FragmentStatsBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ComicsRepository repo = ComicsRepository.getInstance();

        int totalComics = repo.getTotalComicsCount();
        int readStories = repo.getTotalReadStoriesCount();
        int unreadStories = repo.getTotalUnreadStoriesCount();
        int comicsCompletion = repo.getComicsCompletionPercentage();
        int storiesCompletion = repo.getStoriesCompletionPercentage();
        List<Integer> missingComics = repo.getMissingComicsInRange(379, 500);

        _binding.totalComicsOwnedTxt.setText(String.valueOf(totalComics));
        _binding.readStoriesCountTxt.setText(String.valueOf(readStories));
        _binding.unreadStoriesCountTxt.setText(String.valueOf(unreadStories));

        _binding.comicsCompletionProgress.setProgress(comicsCompletion);
        _binding.storiesCompletionProgress.setProgress(storiesCompletion);

        _binding.comicsCompletionProgressTxt.setText(comicsCompletion + "%");
        _binding.storiesCompletionProgressTxt.setText(storiesCompletion + "%");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingComics.size(); i++) {
            sb.append(missingComics.get(i));
            if (i < missingComics.size() - 1) {
                sb.append("\n");
            }
        }

        _binding.missingComicsTxt.setText(sb.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).hideNavBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity)requireActivity()).hideNavBar();
    }
}
