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

import com.xayris.donalduck.Category;
import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.adapters.ComicsHomeAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentHomeBinding;

import java.util.Objects;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

public class HomeFragment extends Fragment implements ComicsHomeAdapter.ComicActionListener, OrderedRealmCollectionChangeListener<RealmResults<Comic>> {

    private FragmentHomeBinding _binding;
    private ComicsHomeAdapter _adapter;
    private boolean _init = false;
    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComicsRepository.getInstance().addComicsChangeListener(this);
        _adapter = new ComicsHomeAdapter(requireContext(), ComicsRepository.getInstance().getComicsInProgress(), HomeFragment.this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(_binding == null)
            _binding = FragmentHomeBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!_init) {
            _binding.comicsList.setLayoutManager(new LinearLayoutManager(getContext()));
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(new ComicsHomeAdapter.SwipeCallback(requireContext()));
            itemTouchhelper.attachToRecyclerView(_binding.comicsList);
            showHome();
            _init = true;
        }
    }

    private void showHome() {
        new Handler(Looper.myLooper()).post(() -> {
            boolean noComics = ComicsRepository.getInstance().getComicsInProgress().size() == 0;
            _binding.noComicsContainer.setVisibility(noComics ? View.VISIBLE : View.GONE);
            int resId = requireContext().getResources().getIdentifier("no_comics_" + Category.InProgress.toString().toLowerCase() + "_description", "string", requireContext().getPackageName());
            if(resId != 0)
                _binding.noComicsTxt.setText(requireContext().getString(resId));
            if (_adapter != null)
                _adapter.updateData(ComicsRepository.getInstance().getComicsByCategory(Category.InProgress));
            if (_binding.comicsList.getAdapter() == null)
                _binding.comicsList.setAdapter(_adapter);
            new Handler(Looper.getMainLooper()).post(() -> {
                _binding.comicsList.animate().alpha(1).setDuration(200).start();
            });
        });
    }


    @Override
    public void onDestroy() {
        ComicsRepository.getInstance().removeComicsChangeListener(this);
        _binding = null;
        _adapter = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).showNavBar();
    }

    @Override
    public void onItemClick(Comic item) {
        ((MainActivity)requireActivity()).openComic(item, Category.InProgress);
    }

    @Override
    public void onSetNextStoryRead(Comic item, int listPosition) {
        ComicsRepository.getInstance().setStoryRead(item.getNextUnreadStory());
        ((ComicsHomeAdapter) Objects.requireNonNull(_binding.comicsList.getAdapter())).updateData(ComicsRepository.getInstance().getComicsInProgress());
    }

    @Override
    public void onChange(RealmResults<Comic> comics, OrderedCollectionChangeSet changeSet) {
        if (_adapter != null)
            _adapter.updateData(ComicsRepository.getInstance().getComicsByCategory(Category.InProgress));
    }
}