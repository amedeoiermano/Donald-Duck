package com.xayris.donalduck;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.material.navigation.NavigationBarView;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.ActivityMainBinding;
import com.xayris.donalduck.ui.archive.ComicsFragment;
import com.xayris.donalduck.ui.detail.ComicDetailFragment;
import com.xayris.donalduck.utils.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static final int PICKFILE_RESULT_CODE = 1978;
    MediaPlayer _bgMusicPlayer;
    int _bgMusicPlayerCurrentPos;
    ActivityMainBinding _binding;
    NavHostFragment _navHostFragment;
    NavController _navController;
    Toolbar _toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComicsRepository.getInstance().createDatabase(this);
        ComicsRepository.getInstance().loadComics();
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        findViewById(R.id.logo).setClickable(true);
        findViewById(R.id.logo).setFocusable(true);
        findViewById(R.id.logo).setOnClickListener(this);

        _toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        _navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if(_navHostFragment != null) {
            _navController = _navHostFragment.getNavController();
        }
        _bgMusicPlayer = MediaPlayer.create(this, R.raw.bg_music);
        _bgMusicPlayer.setOnCompletionListener(this);

    }

    private void startBgMusic() {
        showAnimation();
        _bgMusicPlayer.start();
    }

    private void stopBgMusic() {
        hideAnimation();
        _bgMusicPlayer.stop();
        _bgMusicPlayer.prepareAsync();
    }

    private void resumeBgMusic() {
        if(_bgMusicPlayerCurrentPos > 0) {
            _bgMusicPlayer.seekTo(_bgMusicPlayerCurrentPos);
            _bgMusicPlayer.start();
        }
    }

    private void pauseBgMusic() {
        if(_bgMusicPlayer.isPlaying()) {
            _bgMusicPlayerCurrentPos = _bgMusicPlayer.getCurrentPosition();
            _bgMusicPlayer.pause();
        }
    }

    @SuppressLint("DiscouragedApi")
    private void showAnimation() {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 3 + 1);
        String gifResKey = getString(R.string.animated_gif_key, String.valueOf(randomNum));
        Glide.with(getApplicationContext()).load(getResources().getIdentifier(gifResKey, "drawable", getPackageName())).into(new DrawableImageViewTarget(_binding.animatedImg));
        ValueAnimator animator = ValueAnimator.ofInt(0,255);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> _binding.animatedImg.setImageAlpha((Integer) valueAnimator.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                _binding.animatedImg.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
   }

   private void hideAnimation() {
       ValueAnimator animator = ValueAnimator.ofInt(255,0);
       animator.setDuration(200);
       animator.setInterpolator(new DecelerateInterpolator());
       animator.addUpdateListener(valueAnimator -> _binding.animatedImg.setImageAlpha((Integer) valueAnimator.getAnimatedValue()));
       animator.addListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
               super.onAnimationEnd(animation);
               _binding.animatedImg.setVisibility(View.GONE);
           }
       });
       animator.start();
   }

    @Override
    protected void onResume() {
        super.onResume();
        resumeBgMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseBgMusic();
    }

    @Override
    protected void onDestroy() {
        stopBgMusic();
        _bgMusicPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logo)
        {
            Utility.performHapticFeedback(getApplicationContext());
            if(_bgMusicPlayer.isPlaying())
                stopBgMusic();
            else
                startBgMusic();
        }
    }

    public void openComic(Comic comic, ComicsFragment.ArchiveType archiveType) {
        Bundle bundle = new Bundle();
        bundle.putString(ComicDetailFragment.ARG_ISSUE, comic.getIssue());
        bundle.putString(ComicDetailFragment.ARG_ARCHIVE_TYPE, archiveType.toString());

        _navController.navigate(R.id.navigation_detail, bundle);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        hideAnimation();
    }

    public static File commonDocumentDirPath()
    {
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }
        else
        {
            dir = Environment.getExternalStorageDirectory();
        }

        return dir;
    }
    private void backupData() {
        final Realm realm = Realm.getDefaultInstance();
        try {
            final File dst = new File(commonDocumentDirPath().getPath().concat("/donald_backup.realm"));
            FileInputStream inStream = new FileInputStream(realm.getPath());

            if (!dst.exists()) {
                dst.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
            Utility.showToast(this, R.string.data_backup_success, Toast.LENGTH_SHORT);

        } catch (Exception e) {
            realm.close();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.backupData) {
            backupData();
        }
        if(item.getItemId() == R.id.restoreData) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE) {
            Realm realm = Realm.getDefaultInstance();
            Uri content_describer = data.getData();
            InputStream in = null;
            OutputStream out = null;
            int outputMessageRes = 0;
            try {
                realm.close();
                in = getContentResolver().openInputStream(content_describer);
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
                    Objects.requireNonNull(in).close();
                    Objects.requireNonNull(out).close();
                } catch (Exception ignored) {

                }

            } finally {
                Utility.showToast(getApplicationContext(), outputMessageRes, Toast.LENGTH_SHORT);
            }
        }
    }


    public void showMenu() {
        _toolbar.inflateMenu(R.menu.archive_menu);
    }

    public void hideMenu() {
        _toolbar.getMenu().clear();
    }

    public void addComic() {
        _navController.navigate(R.id.navigation_detail);
    }
}