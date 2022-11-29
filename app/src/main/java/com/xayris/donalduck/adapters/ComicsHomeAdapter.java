package com.xayris.donalduck.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.xayris.donalduck.R;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.data.entities.Story;
import com.xayris.donalduck.databinding.ComicHomeListItemBinding;
import com.xayris.donalduck.utils.Utility;

import java.util.List;

public class ComicsHomeAdapter extends RecyclerView.Adapter<ComicsHomeAdapter.ComicViewHolder> {

    private List<Comic> _comics;
    private final ComicActionListener _clickListener;
    private final Size _coverSize;
    public ComicsHomeAdapter(Context context, List<Comic> comics, ComicActionListener clickListener) {
        _comics = comics;
        _clickListener = clickListener;
        DisplayMetrics metrics = Utility.getDisplayMetrics(context);
        TypedValue outValue = new TypedValue();
        context.getResources().getValue(R.dimen.home_item_width_downscale_factor, outValue, true);
        float itemWidthDownscaleFactor = outValue.getFloat();
        outValue = new TypedValue();
        context.getResources().getValue(R.dimen.home_item_height_downscale_factor, outValue, true);
        float itemHeightDownscaleFactor = outValue.getFloat();
        _coverSize = new Size((int)(metrics.widthPixels / itemWidthDownscaleFactor), (int)(metrics.widthPixels / itemHeightDownscaleFactor));
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_home_list_item, parent, false);

        return new ComicViewHolder(view, _coverSize);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        Comic story = _comics.get(position);
        holder.update(story, _clickListener, position);
    }

    @Override
    public int getItemCount() {
        return _comics.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Comic> comics) {
        _comics = comics;
        notifyDataSetChanged();
    }

    public static class ComicViewHolder extends RecyclerView.ViewHolder {
        Comic _comic;
        final ComicHomeListItemBinding _binding;
        ComicActionListener _listener;
        int _position;
        final Size _coverSize;
        public ComicViewHolder(@NonNull View itemView, Size coverSize) {
            super(itemView);
            _coverSize = coverSize;
            _binding = ComicHomeListItemBinding.bind(itemView);
            _binding.coverImg.getLayoutParams().width = _coverSize.getWidth();
            _binding.coverImg.getLayoutParams().height = _coverSize.getHeight();
        }

        public void update(Comic comic, ComicActionListener actionListener, int position) {
            _listener = actionListener;
            _comic = comic;
            _position = position;
            Glide.with(itemView.getContext().getApplicationContext()).load(comic.getCoverUrl()).transition(DrawableTransitionOptions.withCrossFade()).apply(new RequestOptions().override(_coverSize.getWidth(),_coverSize.getHeight()).placeholder(R.drawable.cover_placeholder)).into(_binding.coverImg);
            _binding.issueDateTxt.setText(comic.getIssueDateFormatted());
            _binding.issueTxt.setText(itemView.getContext().getString(R.string.issue_number,comic.getIssue().toUpperCase()));
            _binding.storiesProgressTxt.setText(comic.getStoriesProgressFormatted());
            _binding.comicProgressBar.setMax(comic.getStoriesCount());
            _binding.comicProgressBar.setProgress(comic.getReadStoriesCount());
            itemView.setOnClickListener(v -> actionListener.onItemClick(comic));
            Story nextUnreadStory = comic.getNextUnreadStory();
            _binding.nextUnreadStoryTxt.setVisibility(nextUnreadStory != null ? View.VISIBLE : View.GONE);
            _binding.nextUnreadStoryTxt.setText(nextUnreadStory != null ? nextUnreadStory.getTitle() : null);
        }

        public void setNextStoryRead() {
            _listener.onSetNextStoryRead(_comic, _position);
        }
    }

    public static class SwipeCallback extends ItemTouchHelper.Callback {
        private final Paint mClearPaint;
        private final ColorDrawable mBackground;
        private final int backgroundColor;

        public SwipeCallback(Context context) {
            mBackground = new ColorDrawable();
            backgroundColor = context.getResources().getColor(R.color.yellow, context.getTheme());
            mClearPaint = new Paint();
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;

            boolean isCancelled = dX == 0 && !isCurrentlyActive;

            if (isCancelled) {
                clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
                return;
            }

            mBackground.setColor(backgroundColor);
            mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            mBackground.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
        private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
            c.drawRect(left, top, right, bottom, mClearPaint);

        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Utility.performHapticFeedback(viewHolder.itemView.getContext());
            ((ComicViewHolder)viewHolder).setNextStoryRead();
        }
    }

    public interface ComicActionListener {
        void onItemClick(Comic item);
        void onSetNextStoryRead(Comic item, int listPosition);
    }
}
