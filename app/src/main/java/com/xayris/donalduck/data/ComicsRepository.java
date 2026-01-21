package com.xayris.donalduck.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xayris.donalduck.Category;
import com.xayris.donalduck.R;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.data.entities.Story;
import com.xayris.donalduck.utils.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class ComicsRepository implements OrderedRealmCollectionChangeListener<RealmResults<Comic>> {
    private static final String DB_NAME = "comics.db";

    private static ComicsRepository _singleton;

    public static ComicsRepository getInstance() {
        if(_singleton == null)
            _singleton = new ComicsRepository();
        return _singleton;
    }

    private RealmResults<Comic> _comics;
    private List<Comic> _comicsInProgress;
    private ComicsArchiveResult _archiveComics;


    public void createDatabase(Context context) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(1)
                .allowQueriesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    public Comic getComic(String issue)
    {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Comic.class).equalTo("issue", issue).findFirst();
    }

    public AbstractMap.SimpleEntry<Comic, Story> getRandomUnreadStory() {
        Random random = new Random();

        List<AbstractMap.SimpleEntry<Comic, Story>> unread = getComicsInProgress().stream()
                .flatMap(comic -> comic.getStories().stream()
                        .filter(story -> !story.getIsRead())
                        .map(story -> new AbstractMap.SimpleEntry<>(comic, story))
                )
                .collect(Collectors.toList());

        if (unread.isEmpty()) return null;

        return unread.get(random.nextInt(unread.size()));
    }

    public void saveComic(Comic comic) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        // checks existing stories
        RealmList<Story> stories = comic.getStories();
        for (Story story :
                stories) {
            // if the story already exists, it keeps the read flag value
            Story existingStory = realm.where(Story.class).equalTo("code", story.getCode()).findFirst();
            if (existingStory != null)
                story.setIsRead(existingStory.getIsRead());
        }
        comic.setStories(stories);
        realm.copyToRealmOrUpdate(comic);
        realm.commitTransaction();
        backupData();
    }


    public void processHomeComics() {
        Realm realm = Realm.getDefaultInstance();
        _comicsInProgress = realm.copyFromRealm(_comics);
        _comicsInProgress = _comicsInProgress.stream().filter(comic -> comic.getReadStoriesCount() > 0 && comic.getReadStoriesCount() < comic.getStoriesCount()).collect(Collectors.toList());
        _comicsInProgress.sort((o1, o2) -> {
            int o1Remaining = o1.getStoriesCount() - o1.getReadStoriesCount();
            int o2Remaining = o2.getStoriesCount() - o2.getReadStoriesCount();
            double o1Value = (float) o1Remaining / o1.getStoriesCount();
            double o2Value = (float) o2Remaining / o2.getStoriesCount();
            return Double.compare(o1Value, o2Value);
        });
    }

    public void loadComics() {
        if(_comicsInProgress != null && _archiveComics != null)
            return;
        Realm realm = Realm.getDefaultInstance();
        _comics = realm.where(Comic.class).sort("issue", Sort.DESCENDING).findAll();
        _comics.addChangeListener(this);
        if(_comicsInProgress == null)
            processHomeComics();
        if(_archiveComics == null)
            processComicsArchive();
    }

    public ComicsArchiveResult getArchive() {
        return _archiveComics;
    }

    public List<Comic> getComicsInProgress() {
        return _comicsInProgress;
    }

    private void processComicsArchive() {
        List<Comic> unstartedComics = new ArrayList<>();
        List<Comic> completedComics = new ArrayList<>();

        for (Comic c : _comics) {
             if (c.getReadStoriesCount() == 0)
                unstartedComics.add(c);
            else if (c.getReadStoriesCount() == c.getStoriesCount())
                completedComics.add(c);
        }
        _archiveComics = new ComicsArchiveResult(unstartedComics, completedComics);
    }

    public String getNextComicIssueByCategory(String currentIssue, Category type) {
        List<Comic> list = getComicsByCategory(type);
        // gets index of current issue
        Optional<Comic> current = list.stream().filter(comic -> Objects.equals(comic.getIssue(), currentIssue)).findFirst();
        if(!current.isPresent())
            return null;
        int currentIndex = list.indexOf(current.get());
        if(currentIndex == 0)
            return null;
        return list.get(currentIndex - 1).getIssue();
    }

    public String getPreviousComicIssueByCategory(String currentIssue, Category type) {
        List<Comic> list = getComicsByCategory(type);
        // gets index of current issue
        Optional<Comic> current = list.stream().filter(comic -> Objects.equals(comic.getIssue(), currentIssue)).findFirst();
        if(!current.isPresent())
            return null;
        int currentIndex = list.indexOf(current.get());
        if(currentIndex >= list.size() -1)
            return null;
        return list.get(currentIndex +1).getIssue();
    }

    @Override
    public void onChange(@NonNull RealmResults<Comic> comics, @NonNull OrderedCollectionChangeSet changeSet) {
        processComicsArchive();
        processHomeComics();
    }

    public List<Comic> getComicsByCategory(Category type) {
        switch (type) {
            case InProgress:
                return getComicsInProgress();
            case Unstarted:
                return  getUnstartedComics();
            case Completed:
                return getCompletedComics();
            case All:
                return getAllComics();
        }
        return null;
    }

    public List<Comic> getUnstartedComics() {
        return getArchive().getUnstartedComics();
    }

    public List<Comic> getCompletedComics() {
        return getArchive().getCompletedComics();
    }

    public List<Comic> getAllComics() {
        List<Comic> comics = new ArrayList<>();
        comics.addAll(getComicsInProgress());
        comics.addAll(getUnstartedComics());
        comics.addAll(getCompletedComics());

        comics.sort((o1, o2) -> o2.getIssue().compareTo(o1.getIssue()));
        return comics;
    }

    public void setStoryRead(Story story) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        story.setIsRead(!story.getIsRead());
        realm.copyToRealmOrUpdate(story);
        realm.commitTransaction();
        backupData();
    }

    public void deleteComic(Comic comic) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Comic toDelete = realm.where(Comic.class).equalTo("issue", comic.getIssue()).findFirst();
        if(toDelete != null)
            toDelete.deleteFromRealm();
        realm.commitTransaction();
        backupData();
    }

    public void updateComicCoverUrl(Comic comic, String coverUrl) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        comic.setCoverUrl(coverUrl);
        realm.copyToRealmOrUpdate(comic);
        realm.commitTransaction();
        backupData();
    }

    public boolean backupData() {
        final Realm realm = Realm.getDefaultInstance();
        try {
            final File dst = new File(Utility.getCommonDocumentDirPath().getPath().concat("/donald_backup.realm"));
            FileInputStream inStream = new FileInputStream(realm.getPath());

            if (!dst.exists()) {
                boolean fileCreateResult = dst.createNewFile();
                if(!fileCreateResult)
                    throw new Exception();
            }
            else {
                boolean fileDeleteResult = dst.delete();
                if(!fileDeleteResult)
                    throw new Exception();
                boolean fileCreateResult = dst.createNewFile();
                if(!fileCreateResult)
                    throw new Exception();
            }
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public void restoreData(Context context, @Nullable Intent data) {
        if(data == null)
            return;
        Realm realm = Realm.getDefaultInstance();
        Uri content_describer = data.getData();
        InputStream in = null;
        OutputStream out = null;
        int outputMessageRes = 0;
        try {
            realm.close();
            in = context.getContentResolver().openInputStream(content_describer);
            out = new FileOutputStream(realm.getPath());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            outputMessageRes = R.string.data_restored_succesfully;
        } catch (Exception e) {
            outputMessageRes = R.string.data_restored_failed;
            try {
                if(in != null)
                    in.close();
                if(out != null)
                    out.close();
            } catch (Exception ignored) {

            }

        } finally {
            Utility.showToast(context, outputMessageRes, Toast.LENGTH_SHORT);
        }
    }

    public void addComicsChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Comic>> listener) {
        _comics.addChangeListener(listener);
    }

    public void removeComicsChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Comic>> listener) {
        _comics.removeChangeListener(listener);
    }
    public int getTotalComicsCount() {
        return getAllComics().size();
    }

    public int getTotalReadStoriesCount() {
        return getAllComics().stream()
                .mapToInt(Comic::getReadStoriesCount)
                .sum();
    }

    public int getTotalUnreadStoriesCount() {
        return getAllComics().stream()
                .mapToInt(c -> c.getStoriesCount() - c.getReadStoriesCount())
                .sum();
    }

    public int getComicsCompletionPercentage() {
        int total = getTotalComicsCount();
        if (total == 0) return 0;

        long completed = getAllComics().stream()
                .filter(c -> c.getReadStoriesCount() == c.getStoriesCount() && c.getStoriesCount() > 0)
                .count();

        return (int) Math.round((completed * 100.0) / total);
    }

    public int getStoriesCompletionPercentage() {
        int totalStories = getAllComics().stream()
                .mapToInt(Comic::getStoriesCount)
                .sum();

        if (totalStories == 0) return 0;

        int readStories = getTotalReadStoriesCount();

        return (int) Math.round((readStories * 100.0) / totalStories);
    }

    public List<Integer> getMissingComicsInRange(int start, int end) {
        List<Comic> allComics = getAllComics();

        List<Integer> ownedIssues = allComics.stream()
                .map(Comic::getIssue)
                .filter(Objects::nonNull)
                .map(issueStr -> {
                    try {
                        return Integer.parseInt(issueStr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Integer> missing = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (!ownedIssues.contains(i)) {
                missing.add(i);
            }
        }

        return missing;
    }

    public static class ComicsArchiveResult {
        private final List<Comic> _unstartedComics;
        private final List<Comic> _completedComics;

        public ComicsArchiveResult(List<Comic> unstartedComics, List<Comic> completedComics)
        {
            _unstartedComics = unstartedComics;
            _completedComics = completedComics;
        }

        public List<Comic> getUnstartedComics() {
            return _unstartedComics;
        }

        public List<Comic> getCompletedComics() {
            return _completedComics;
        }
    }

}
