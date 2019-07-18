package findik.mustafa.monogram.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.classes.Comment;
import findik.mustafa.monogram.classes.GetTimeAgo;
import findik.mustafa.monogram.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private List<Comment> commentList;

    // initalzie.
    private FirebaseAuth mAuth;
    private DatabaseReference mCommentDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userPhoto;
        public TextView username, userComment, postTime ;

        MyViewHolder(View view) {
            super(view);
            userPhoto = view.findViewById(R.id.profile_user_image);
            username = view.findViewById(R.id.user_Name);
            userComment = view.findViewById(R.id.user_comment_txt);
            postTime = view.findViewById(R.id.comment_date);
        }

    }


    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList; // commentlistesi.


    }

    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.comment_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CommentAdapter.MyViewHolder holder, int position) {
        final Comment comment = commentList.get(position);
        final Context context = holder.userPhoto.getContext(); // Context.

        mAuth = FirebaseAuth.getInstance();

        // Resmi TimeStrmp olarak kaydettik  GetTimeAgo classı ile ne kadar zaman önce olduğunu alabliyoruz.
        Long lastTime = Long.parseLong(comment.getPostDate());
        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, context); // Ne kadar zaman önce.

        final Picasso picassoUserImage = Picasso.with(context);
        picassoUserImage.setIndicatorsEnabled(false); // Sol Üsteki Renkli üçgenlerden kurtulmak için.

        //  Resimler Uri olduğundan Picasso yardımıysa eklenitoz resimleri
        // NetworkPolicy OFFLINE ile resimleri hafızada cache de tutuyoruz.
        picassoUserImage.load(comment.getUserImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userPhoto, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                picassoUserImage.load(comment.getUserImage()).into(holder.userPhoto);
            }
        });
        holder.username.setText(comment.getUserName());
        holder.userComment.setText(comment.getUserComment());
        holder.postTime.setText(lastSeenTime);

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
