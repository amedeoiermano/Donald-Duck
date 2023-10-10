package com.xayris.donalduck.ui.archive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.xayris.donalduck.Category;
import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.R;
import com.xayris.donalduck.adapters.ArchivePagerAdapter;
import com.xayris.donalduck.adapters.ComicsArchiveAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentArchiveBinding;

public class ArchiveFragment extends Fragment implements  View.OnClickListener, ComicsArchiveAdapter.OnItemClickListener {

    private FragmentArchiveBinding _binding;
    ViewPager2 _viewPager;
    ArchivePagerAdapter _adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         _viewPager = view.findViewById(R.id.viewpager);
        _viewPager.setOffscreenPageLimit(3);
        _viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        _adapter = new ArchivePagerAdapter(getChildFragmentManager(), getLifecycle());
        _adapter.addFragment(new ArchiveTabFragment(Category.Unstarted));
        _adapter.addFragment(new ArchiveTabFragment(Category.Completed));
        _adapter.addFragment(new ArchiveTabFragment(Category.All));

        _viewPager.setAdapter(_adapter);
        TabLayout _tabLayout = view.findViewById(R.id.tablayout);
        _tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        _tabLayout.setTabMode(TabLayout.MODE_AUTO);
        new TabLayoutMediator(_tabLayout, _viewPager, (tab, position) -> {
            Context context = requireContext();
            if(_viewPager.getAdapter() == null)
                return;
            Category category = ((ArchivePagerAdapter)_viewPager.getAdapter()).getFragmentCategory(position);
            @SuppressLint("DiscouragedApi") int resIdentifier = context.getResources().getIdentifier(category.toString().toLowerCase() + "_comics", "string", context.getPackageName());
            tab.setText(getString(resIdentifier, ComicsRepository.getInstance().getComicsByCategory(category).size()));
        }).attach();
        _binding.addComicBtn.setOnClickListener(ArchiveFragment.this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).showNavBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity)requireActivity()).showNavBar();
    }

    private void addComic() {
        ((MainActivity)requireActivity()).addComic();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.addComicBtn) {
            addComic();
        }
    }

    @Override
    public void onItemClick(Comic item) {
        ((MainActivity)requireActivity()).openComic(item, _adapter.getFragmentCategory(_viewPager.getCurrentItem()));
    }

}