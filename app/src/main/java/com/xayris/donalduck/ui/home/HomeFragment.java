package com.xayris.donalduck.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.adapters.ComicsHomeAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentHomeBinding;
import com.xayris.donalduck.ui.archive.ArchiveFragment;

import java.util.Objects;

public class HomeFragment extends Fragment implements ComicsHomeAdapter.ComicActionListener, View.OnScrollChangeListener {

    private FragmentHomeBinding _binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentHomeBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity)requireActivity()).hideMenu();
        _binding.comicsList.setOnScrollChangeListener(this);
        _binding.comicsList.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(new ComicsHomeAdapter.SwipeCallback(requireContext()));
        itemTouchhelper.attachToRecyclerView(_binding.comicsList);
        new Handler(Looper.myLooper()).post(() -> {
            _binding.comicsList.setAdapter(new ComicsHomeAdapter(requireContext(), ComicsRepository.getInstance().getComicsInProgress(), HomeFragment.this));
            new Handler(Looper.getMainLooper()).post(() -> {
                _binding.comicsList.animate().alpha(1).setDuration(200).start();
                if(lastScrollY > 0)
                    _binding.comicsList.scrollTo(0, lastScrollY);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    @Override
    public void onItemClick(Comic item) {
        ((MainActivity)requireActivity()).openComic(item, ArchiveFragment.ArchiveType.InProgress);
    }

    @Override
    public void onSetNextStoryRead(Comic item, int listPosition) {
        ComicsRepository.getInstance().setStoryRead(item.getNextUnreadStory());
        ((ComicsHomeAdapter) Objects.requireNonNull(_binding.comicsList.getAdapter())).updateData(ComicsRepository.getInstance().getComicsInProgress());
    }

    int lastScrollY;
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        lastScrollY = scrollY;
    }
}