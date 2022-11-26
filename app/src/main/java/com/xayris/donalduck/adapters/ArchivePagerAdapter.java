package com.xayris.donalduck.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.xayris.donalduck.Category;
import com.xayris.donalduck.ui.archive.ArchiveTabFragment;

import java.util.ArrayList;
import java.util.List;

public class ArchivePagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter  {
    private final List<ArchiveTabFragment> _fragments = new ArrayList<>();

    public ArchivePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void addFragment(ArchiveTabFragment fragment) {
        _fragments.add(fragment);
    }

    public Category getFragmentCategory(int position) {
        return _fragments.get(position).getCategory();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return _fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return _fragments.size();
    }
}