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
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.R;
import com.xayris.donalduck.adapters.ArchivePagerAdapter;
import com.xayris.donalduck.adapters.ComicsArchiveAdapter;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentArchiveBinding;
import com.xayris.donalduck.ui.detail.ComicDetailFragment;

import java.util.Objects;

public class ArchiveFragment extends Fragment implements  View.OnClickListener, ComicsArchiveAdapter.OnItemClickListener {

    private FragmentArchiveBinding _binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity)requireActivity()).showMenu();
        ViewPager2 viewPager = view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        ArchivePagerAdapter _adapter = new ArchivePagerAdapter(getChildFragmentManager(), getLifecycle());
        _adapter.addFragment(new ArchiveTabFragment(ArchiveType.All));
        if(ComicsRepository.getInstance().getUnstartedComics().size() > 0)
            _adapter.addFragment(new ArchiveTabFragment(ArchiveType.Unstarted));
        if(ComicsRepository.getInstance().getCompletedComics().size() > 0)
            _adapter.addFragment(new ArchiveTabFragment(ArchiveType.Completed));

        viewPager.setAdapter(_adapter);
        TabLayout _tabLayout = view.findViewById(R.id.tablayout);

        new TabLayoutMediator(_tabLayout, viewPager, (tab, position) -> {
            Context context = requireContext();
            ArchiveType archiveType = ((ArchivePagerAdapter) Objects.requireNonNull(viewPager.getAdapter())).getFragmentType(position);
            @SuppressLint("DiscouragedApi") int resIdentifier = context.getResources().getIdentifier(archiveType.toString().toLowerCase() + "_comics", "string", context.getPackageName());
            tab.setText(getString(resIdentifier, ComicsRepository.getInstance().getComicsByArchiveType(archiveType).size()));
        }).attach();
        _binding.addComicBtn.setOnClickListener(ArchiveFragment.this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    private void addComic() {
        requireActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.nav_host_fragment_activity_main, ComicDetailFragment.newInstance(null), ComicDetailFragment.class.getName())
                .addToBackStack(null).commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.addComicBtn) {
            addComic();
        }
    }


    @Override
    public void onItemClick(Comic item) {
        ((MainActivity)requireActivity()).openComic(item);
    }

    public enum ArchiveType {
        Unstarted,
        Completed,
        All
    }
}