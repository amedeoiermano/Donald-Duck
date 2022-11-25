package com.xayris.donalduck.ui;

import androidx.fragment.app.Fragment;

import com.xayris.donalduck.ui.archive.ComicsFragment;

public class BaseComicsFragment extends Fragment {
    protected ComicsFragment.ArchiveType _archiveType;

    public BaseComicsFragment(ComicsFragment.ArchiveType category) {
        _archiveType = category;
    }

    public BaseComicsFragment() {
    }

    public ComicsFragment.ArchiveType getArchiveType() {
        return _archiveType;
    }
}
