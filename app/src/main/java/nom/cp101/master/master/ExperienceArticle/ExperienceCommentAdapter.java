package nom.cp101.master.master.ExperienceArticle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nom.cp101.master.master.CourseArticle.ConnectionServer;
import nom.cp101.master.master.R;

//文章連接留言的橋接器
public class ExperienceCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private FragmentManager fm;
    private List<ExperienceComment> commentList;
    private final int normalType = 0;
    private final int footType = 1;

    private boolean hasMore = true;
    private boolean hiddenHint = false;
    private LruCache<String, Bitmap> lruCache;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final String EXPERIENCE_COMMENT_HEAD_CACHE = "experience_comment_head_cache";
    private final String TAG = "ExperienceCommentAdapter";

    private final String DIALOG_TAG = "dialog";
    private int localType;

    public ExperienceCommentAdapter(Context context, FragmentManager fm, List<ExperienceComment> commentList, boolean hasMore, int localType) {
        this.context = context;
        this.fm = fm;
        this.commentList = commentList;
        this.hasMore = hasMore;
        this.localType = localType;
        initMemoryCache();
    }

    //LruCache機制
    public void initMemoryCache() {
        //緩存記憶體空間
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        //override存於緩存中的圖片大小
        lruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public int getItemCount() {
        return commentList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return footType;
        } else {
            return normalType;
        }
    }

    class ExperienceCommentViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivHeadComment;
        private TextView tvNameComment, tvTimeComment, tvComment;

        public ExperienceCommentViewHolder(View itemView) {
            super(itemView);
            ivHeadComment = (CircleImageView) itemView.findViewById(R.id.ivHeadComment);
            tvNameComment = (TextView) itemView.findViewById(R.id.tvNameComment);
            tvTimeComment = (TextView) itemView.findViewById(R.id.tvTimeComment);
            tvComment = (TextView) itemView.findViewById(R.id.tvComment);
        }
    }

    class FootViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llExperience;
        private ProgressBar pbExperience;
        private TextView tvExperience;

        public FootViewHolder(View itemView) {
            super(itemView);
            llExperience = (LinearLayout) itemView.findViewById(R.id.llExperience);
            pbExperience = (ProgressBar) itemView.findViewById(R.id.pbExperience);
            tvExperience = (TextView) itemView.findViewById(R.id.tvExperience);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == normalType) {
            view = LayoutInflater.from(context).inflate(R.layout.experience_comment_item, parent, false);
            return new ExperienceCommentViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.experience_comment_foot_view_item, parent, false);
            return new FootViewHolder(view);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ExperienceCommentViewHolder) {
            final ExperienceComment comment = commentList.get(position);
            Date date = comment.getComment_time();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(date);

            //select cache by user_img
            Bitmap headImgBitmap = lruCache.get(comment.getUser_id() + EXPERIENCE_COMMENT_HEAD_CACHE);
            if (headImgBitmap == null) {
                headImgBitmap = ConnectionServer.getPhotoByUserId(comment.getUser_id());
                if (headImgBitmap != null) {
                    ((ExperienceCommentViewHolder) holder).ivHeadComment.setImageBitmap(headImgBitmap);
                    lruCache.put(comment.getUser_id() + EXPERIENCE_COMMENT_HEAD_CACHE, headImgBitmap);
                    Log.d(TAG, "load user_img by server");
                }

            } else {
                ((ExperienceCommentViewHolder) holder).ivHeadComment.setImageBitmap(headImgBitmap);
                Log.d(TAG, "load user_img by cache");
            }

            ((ExperienceCommentViewHolder) holder).tvNameComment.setText(comment.getUser_name());
            ((ExperienceCommentViewHolder) holder).tvTimeComment.setText(dateStr);
            ((ExperienceCommentViewHolder) holder).tvComment.setText(comment.getComment_content());

            ((ExperienceCommentViewHolder) holder).ivHeadComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment fragment = fm.findFragmentByTag(DIALOG_TAG);
                    if (fragment != null) {
                        ft.remove(fragment);
                    }
                    ft.addToBackStack(null);
                    DialogFragment dialogFragment = new ExperienceDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", comment.getUser_id());
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(ft, DIALOG_TAG);
                }
            });

            //footView
        } else {
            ((ExperienceCommentAdapter.FootViewHolder) holder).llExperience.setVisibility(View.VISIBLE);
            ((ExperienceCommentAdapter.FootViewHolder) holder).pbExperience.setVisibility(View.VISIBLE);
            ((ExperienceCommentAdapter.FootViewHolder) holder).tvExperience.setVisibility(View.VISIBLE);

            if (hasMore == true) {
                hiddenHint = false;

                if (commentList.size() > 0) {
                    ((ExperienceCommentAdapter.FootViewHolder) holder).tvExperience.setText(context.getResources().getString(R.string.refreshing));
                }

            } else {
                if (commentList.size() > 0) {
                    ((ExperienceCommentAdapter.FootViewHolder) holder).tvExperience.setText(context.getResources().getString(R.string.refreshing_finish));

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 隐藏提示条
                            ((ExperienceCommentAdapter.FootViewHolder) holder).llExperience.setVisibility(View.GONE);
                            ((ExperienceCommentAdapter.FootViewHolder) holder).pbExperience.setVisibility(View.GONE);
                            ((ExperienceCommentAdapter.FootViewHolder) holder).tvExperience.setVisibility(View.GONE);
                            if (localType == 1) {
                                ((ExperienceCommentAdapter.FootViewHolder) holder).llExperience.setPadding(0, 0, 0, 0);
                            }
                            hiddenHint = true;
                            hasMore = true;
                        }
                    }, 1000);
                }
            }
        }
    }

    //自訂method,取得數據最後的位置,footView不算上

    public int getLastPosition() {
        return commentList.size();
    }

    //自訂method,回傳footView有無隱藏狀態
    public boolean isHiddenHint() {
        return hiddenHint;
    }

    //自訂method,更新recyclerView並改變hasMore值
    public void updateList(List<ExperienceComment> commentsList, boolean hasMore) {
        if (commentsList != null) {
            this.commentList.addAll(commentsList);
        }
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

}
