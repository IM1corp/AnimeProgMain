package com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.LoadImageEvents;
import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.MainActivity.fragments.Adapters.MenuAnimeItem;
import com.imcorp.animeprog.MainActivity.fragments.Adapters.Separator;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime.OneAnimeWithId;

import java.util.ArrayList;
import java.util.List;

public class AnimePreviewItem extends RecyclerView.Adapter<AnimePreviewItem.FavoriteItemModel> implements onItemEdit {
    private final int layoutId;
    private final ArrayList<OneAnimeWithId> items = new ArrayList<>();
    private final MyApp mContext;
    private Events events = null;
    private OnBind eventOnBind = null;
    private ItemTouchHelper itemTouchHelper;
    public boolean fullyLoaded=false;
    private boolean showProgress,showDownloadCount,showMenuBnt,enableDeleting,enableDeletingFromMenu, showHostImageView;

    public AnimePreviewItem(MyApp context, final int params, final RecyclerView recyclerView, final NestedScrollView scrollView) {
        this.layoutId = R.layout.fragment_anime_preview_item;
        this.mContext = context;
        this.parseParams(params,recyclerView);
        this.init(scrollView,recyclerView);
    }
    class OnScroll extends RecyclerView.OnScrollListener implements NestedScrollView.OnScrollChangeListener {
        private boolean loading=false;

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            onScroll(scrollY,v);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            onScroll(recyclerView.getScrollY(),recyclerView);
        }
        private void onScroll(int scrollY, View v){
            if(!fullyLoaded&&!loading&&v.getHeight()-scrollY<100){
                synchronized (this) {
                    loading = true;
                    final int c = getItemCount();
                    if (events != null) {
                        new Thread(() -> {
                            final boolean b = events.loadMore(c);
                            synchronized (this) {
                                loading = false;
                                fullyLoaded = b;
                            }
                        }).start();
                    }
                    else fullyLoaded = true;
                }
            }
        }
    }
    private void init(final NestedScrollView scrollView,final RecyclerView recyclerView){
        final LinearLayoutManager manager = (LinearLayoutManager)recyclerView.getLayoutManager();
        assert manager!=null : "Manager not defined";
        manager.setAutoMeasureEnabled(true);
        if(scrollView!=null) scrollView.setOnScrollChangeListener(new OnScroll());
        else recyclerView.addOnScrollListener(new OnScroll());

        DividerItemDecoration decor = new Separator(mContext,mContext.getResources().getDrawable(R.drawable.recycle_view_seperator));
        recyclerView.addItemDecoration(decor);
    }
    private void setOnItemDrag(RecyclerView recyclerView, final boolean enableModify,final boolean enableDeleting){
        ItemTouchHelper.Callback callback = new OnItemTouchHelper(this,enableDeleting,enableModify);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    private void parseParams(final int params,final RecyclerView recyclerView) {
        final boolean enable_modify = (params & ENABLE_REORDER) != 0;
        enableDeleting = (params & ENABLE_SWIPE_DELETING) != 0;
        enableDeletingFromMenu = (params & ENABLE_DELETING_FROM_MENU) != 0;
        showProgress = (params & SHOW_PROGRESS) != 0;
        showDownloadCount = (params & SHOW_DOWNLOAD_COUNT) != 0;
        showMenuBnt = (params & SHOW_MENU_BUTTON) != 0;
        showHostImageView = (params & SHOW_HOST_IMAGE_VIEW) != 0;
        if (enable_modify || enableDeleting)
            this.setOnItemDrag(recyclerView, enable_modify, enableDeleting);
    }

    @NonNull
    @Override public FavoriteItemModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new FavoriteItemModel(view);
    }

    @Override public void onBindViewHolder(@NonNull FavoriteItemModel holder, int position) {
        holder.bind(items.get(position),position);
    }
    @Override public int getItemCount() {
        return items.size();
    }
    public AnimePreviewItem setEvents(Events events) {
        this.events = events;
        return this;
    }
    public AnimePreviewItem setOnBind(OnBind event){
        eventOnBind=event;
        return this;
    }
    public AnimePreviewItem addItems(@Nullable List<OneAnimeWithId> items) {
        if (items != null&&items.size()>0) {
            this.items.addAll(items);
            this.notifyDataSetChanged();//TODO:(from,this.items.size());
        }
        return this;
    }
    public ArrayList<OneAnimeWithId> getItems() {
        return this.items;
    }
    public AnimePreviewItem clear() {
        this.items.clear();
        this.notifyDataSetChanged();
        return this;
    }
    @Override public void onItemMove(int fromPosition, int toPosition) {
        OneAnimeWithId obj = this.items.get(fromPosition);
        final int c = fromPosition>toPosition?-1:+1,
                id_to = this.items.get(toPosition).id,
                g=obj.id>id_to?1:-1;
        if(this.events!=null)events.onItemMove(obj.id,id_to);
        for(int i=fromPosition+c;i!=toPosition+c;i+=c) items.get(i).id+=g;
        obj.id = id_to;
        this.items.add(toPosition,items.remove(fromPosition));
        this.notifyItemMoved(fromPosition,toPosition);
    }
    @Override public void onItemDelete(int position) {
        if(this.events!=null)events.onItemDelete(position);
        this.items.remove(position);
        this.notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }
    public class FavoriteItemModel extends RecyclerView.ViewHolder implements LoadImageEvents, GestureDetector.OnGestureListener,
            View.OnTouchListener,View.OnClickListener {
        private int index;
        private final TextView titleTextView, descriptionImageView;
        private final androidx.appcompat.widget.AppCompatImageView imageView, hostImageView;
        private final ImageButton menuButton;
        private final View view;
        private View progressView;

        public OneAnimeWithId getAnime(){return items.get(index);}

        FavoriteItemModel(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.imageView);
            this.titleTextView = itemView.findViewById(R.id.titleTextView);
            this.descriptionImageView = itemView.findViewById(R.id.descriptionTextView);
            this.menuButton = itemView.findViewById(R.id.menuButton);
            this.hostImageView = itemView.findViewById(R.id.hostImageView);
            this.view = itemView;

            this.mGestureDetector = new GestureDetector(mContext,this);
        }

        void bind(final OneAnimeWithId anime,final int index) {
            this.index=index;
            this.imageView.setImageResource(0);
            this.imageView.setBackgroundColor(mContext.getResources().getColor(R.color.noImageColor));
            anime.anime.loadCover(mContext, mContext.request, this);
            this.titleTextView.setText(anime.anime.title);
            this.descriptionImageView.setText(anime.anime.description != null &&!anime.anime.description.isEmpty()?
                    anime.anime.description :
                    anime.anime.year + " " + mContext.getString(R.string.year));
            this.titleTextView.post(() -> {
                final double lines = (double)titleTextView.getHeight() / titleTextView.getLineHeight();
                descriptionImageView.setMaxLines(7 - (int) Math.round(lines));
            });
            if(showMenuBnt) {
                this.menuButton.setVisibility(View.VISIBLE);
                this.menuButton.setOnClickListener(btn ->
                        new MenuAnimeItem(mContext, R.style.BottomSheetMenu, anime, enableDeletingFromMenu)
                                .setOnDelete(v -> onItemDelete(index))
                                .setOnButtonClick(this)
                                .setEvents().show()
                );
            }
            if(showDownloadCount)view.findViewById(R.id.downloadsCount).setVisibility(View.VISIBLE);
            if(eventOnBind!=null)eventOnBind.bind(this);
            this.view.setOnClickListener(this);
            this.updateProgress(anime.anime);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.updateHostImageView();
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void updateHostImageView(){
            if(showHostImageView){
                hostImageView.setVisibility(View.VISIBLE);
//                final int icon = Config.getIconByHost(this.anime.anime.HOST);
//                final Drawable dr = ResourcesCompat.getDrawable(mContext.getResources(),icon,null);
//                Bitmap bitmap;
//                if(dr instanceof  VectorDrawable) {
//                    VectorDrawable vectorDrawable = (VectorDrawable) dr;
//                    bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
//                            vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(bitmap);
//                    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//                    vectorDrawable.draw(canvas);
//                }else {
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable) dr;
//                    bitmap = bitmapDrawable.getBitmap();
//                }
//                hostImageView.setImageBitmap(bitmap);
                hostImageView.setImageResource(Config.getIconByHost(this.getAnime().anime.HOST));
            } else {
                hostImageView.setVisibility(View.GONE);
            }
        }
        private void updateProgress(@Nullable OneAnime anime) {
            if(!showProgress)return;
            if(anime==null)anime=this.getAnime().anime;
            if(progressView==null) progressView = view.findViewById(R.id.progress);
            final ConstraintLayout.LayoutParams l = (ConstraintLayout.LayoutParams)progressView.getLayoutParams();
            l.matchConstraintPercentWidth = anime.getProgress(mContext.dataBase).progress;
            progressView.setLayoutParams(l);
            progressView.requestLayout();
        }

        @Override public void onClick(View v) {
            if(events!=null) events.onItemClick(this.getAnime(), FavoriteItemModel.this.index);
        }
        @Override public void onSuccess(Bitmap bitmap) {
            this.imageView.setImageBitmap(bitmap);
        }
        @Override public boolean onFail(Exception exception) {
            if (events != null) events.onErrorLoadingImage(exception);
            return false;
        }
        private final GestureDetector mGestureDetector;
        @Override public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
        @Override public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override public void onShowPress(MotionEvent e) {

        }
        @Override public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }
        @Override public void onLongPress(MotionEvent e) {
            if(itemTouchHelper!=null) itemTouchHelper.startDrag(this);
        }
        @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
    public interface OnBind{
        void bind(FavoriteItemModel model);
    }

    public final static int ENABLE_REORDER = 1<<0;
    public final static int SHOW_PROGRESS = 1<<1;
    public final static int ENABLE_SWIPE_DELETING = 1<<2;
    public final static int SHOW_DOWNLOAD_COUNT = 1<<3;
    public final static int SHOW_MENU_BUTTON = 1<<4;
    public final static int ENABLE_DELETING_FROM_MENU = 1<<5;
    public final static int SHOW_HOST_IMAGE_VIEW = 1<<6;
}
