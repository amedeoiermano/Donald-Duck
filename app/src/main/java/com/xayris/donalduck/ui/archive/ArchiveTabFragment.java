package com.xayris.donalduck.ui.archive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xayris.donalduck.Category;
import com.xayris.donalduck.R;
import com.xayris.donalduck.adapters.ComicsArchiveAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentArchiveTabBinding;
import com.xayris.donalduck.utils.ItemOffsetDecoration;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;


public class ArchiveTabFragment extends Fragment implements OrderedRealmCollectionChangeListener<RealmResults<Comic>> {

    private FragmentArchiveTabBinding _binding;
    private ComicsArchiveAdapter _adapter;
    private Category _category;
    private boolean _init = false;
    public ArchiveTabFragment() {
    }

    public ArchiveTabFragment(Category type) {
        _category = type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComicsRepository.getInstance().addComicsChangeListener(this);
        if(savedInstanceState != null)
        {
            String archiveType = savedInstanceState.getString(Category.class.getName());
            if(archiveType != null)
                _category = Category.valueOf(savedInstanceState.getString(Category.class.getName()));
        }
        _adapter = new ComicsArchiveAdapter(requireContext(), ComicsRepository.getInstance().getComicsByCategory(_category), (ComicsArchiveAdapter.OnItemClickListener) getParentFragment());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
        {
            String archiveType = savedInstanceState.getString(Category.class.getName());
            if(archiveType != null)
                _category = Category.valueOf(savedInstanceState.getString(Category.class.getName()));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(Category.class.getName(), _category.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(_binding == null)
            _binding = FragmentArchiveTabBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onDestroy() {
        ComicsRepository.getInstance().removeComicsChangeListener(this);
        _binding = null;
        _adapter = null;
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!_init) {
            int spanCount = getResources().getInteger(R.integer.archive_span_count);
            _binding.comicsList.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
            _binding.comicsList.addItemDecoration(new ItemOffsetDecoration(requireContext(), R.dimen.item_offset));
            showArchive();
            _init = true;
        }
    }

    private void showArchive() {
        boolean noComics = ComicsRepository.getInstance().getComicsByCategory(_category).size() == 0;
        _binding.noComicsContainer.setVisibility(noComics ? View.VISIBLE : View.GONE);
        int resId = requireContext().getResources().getIdentifier("no_comics_" + _category.toString().toLowerCase() + "_description", "string", requireContext().getPackageName());
        if(resId != 0)
            _binding.noComicsTxt.setText(requireContext().getString(resId));
        if (noComics) {
            _binding.loadingIndicator.setVisibility(View.GONE);
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (_adapter != null)
                _adapter.updateData(ComicsRepository.getInstance().getComicsByCategory(_category));
            if (_binding.comicsList.getAdapter() == null)
                _binding.comicsList.setAdapter(_adapter);

            new Handler(Looper.getMainLooper()).post(() -> {
                _binding.loadingIndicator.setVisibility(View.GONE);
                _binding.comicsList.animate().alpha(1).setDuration(200).start();
            });
        },200);
    }


    public Category getCategory() {
        return _category;
    }

    @Override
    public void onChange(RealmResults<Comic> comics, OrderedCollectionChangeSet changeSet) {
        if (_adapter != null)
            _adapter.updateData(ComicsRepository.getInstance().getComicsByCategory(_category));
    }
}