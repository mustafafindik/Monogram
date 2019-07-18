package findik.mustafa.monogram.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.ChatActivity;
import findik.mustafa.monogram.R;
import findik.mustafa.monogram.classes.User;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<User> chatList;
    private FirebaseAuth mAuth;
    private DatabaseReference mChatDatabase;
    private DatabaseReference mUserDatabase;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userPhoto;
        public TextView username;
        public LinearLayout mChatLinear;

        MyViewHolder(View view) {
            super(view);
            userPhoto = view.findViewById(R.id.chat_user_image);
            username = view.findViewById(R.id.chat_user_Name);
            mChatLinear = view.findViewById(R.id.chat_user_bar);
        }

    }

    public ChatAdapter(List<User> chatList) {
        this.chatList = chatList; // .
    }

    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_single_item, parent, false);
        return new ChatAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ChatAdapter.MyViewHolder holder, int position) {
        final User chat = chatList.get(position);
        final Context context = holder.userPhoto.getContext(); // Context.

        mAuth = FirebaseAuth.getInstance();
        final String from_userid = chat.getUserId();
        final String UserName = chat.getUserName();
        final String image = chat.getUserImage();
        holder.username.setText(UserName);
        Picasso.with(holder.userPhoto.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.color.defaultImage).into(holder.userPhoto, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(holder.userPhoto.getContext()).load(image)
                        .placeholder(R.color.defaultImage).into(holder.userPhoto);
            }
        });

        holder.mChatLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoChat = new Intent(context, ChatActivity.class);
                gotoChat.putExtra("Userid",from_userid);
                gotoChat.putExtra("userName",UserName);
                context.startActivity(gotoChat);
            }
        });

    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }


}
