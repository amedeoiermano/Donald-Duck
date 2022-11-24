package com.xayris.donalduck.ui.archive;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xayris.donalduck.R;
import com.xayris.donalduck.adapters.ComicsArchiveAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.databinding.FragmentArchiveBinding;
import com.xayris.donalduck.databinding.FragmentArchiveTabBinding;
import com.xayris.donalduck.utils.ItemOffsetDecoration;


public class ArchiveTabFragment extends Fragment implements View.OnScrollChangeListener {

    private FragmentArchiveTabBinding _binding;

    private ArchiveFragment.ArchiveType _archiveType;
    private ComicsArchiveAdapter _adapter;
    public ArchiveTabFragment() {
    }

    public ArchiveTabFragment(ArchiveFragment.ArchiveType type) {
        _archiveType = type;
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onCreateView(getLayoutInflater(), null,null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _adapter = new ComicsArchiveAdapter(requireContext(), ComicsRepository.getInstance().getComicsByArchiveType(_archiveType), (ComicsArchiveAdapter.OnItemClickListener) getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _binding = FragmentArchiveTabBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int spanCount = getResources().getInteger(R.integer.archive_span_count);

        _binding.comicsList.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        _binding.comicsList.addItemDecoration(new ItemOffsetDecoration(requireContext(), R.dimen.item_offset));
        _binding.comicsList.setOnScrollChangeListener(this);

        showArchive();
    }

    private void showArchive() {
        boolean noComics = ComicsRepository.getInstance().getComicsByArchiveType(_archiveType).size() == 0;
        _binding.noComicsContainer.setVisibility(noComics ? View.VISIBLE : View.GONE);
        if (noComics) {
            _binding.loadingIndicator.setVisibility(View.GONE);
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (_adapter != null)
                _adapter.updateData(ComicsRepository.getInstance().getComicsByArchiveType(_archiveType));
            if (_binding.comicsList.getAdapter() == null)
                _binding.comicsList.setAdapter(_adapter);

            new Handler(Looper.getMainLooper()).post(() -> {
                _binding.loadingIndicator.setVisibility(View.GONE);
                _binding.comicsList.animate().alpha(1).setDuration(200).start();
                if (lastScrollY > 0)
                    _binding.comicsList.scrollTo(0, lastScrollY);
            });
        },200);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    int lastScrollY = 0;
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        lastScrollY = scrollY;

    }

    public ArchiveFragment.ArchiveType getArchiveType() {
        return _archiveType;
    }
}