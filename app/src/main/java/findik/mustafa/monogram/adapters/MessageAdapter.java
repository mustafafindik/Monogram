package findik.mustafa.monogram.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.R;
import findik.mustafa.monogram.classes.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private Context context;

    public MessageAdapter(List<Message> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = new View(parent.getContext());
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        Message message = mMessageList.get(viewType);

        if (message.getFrom().equals(currentUserId)) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_item_right, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_item_left, parent, false);
        }
        return new MessageViewHolder(v);
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public RelativeLayout mMessageRelavite;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            mMessageRelavite = itemView.findViewById(R.id.chat_Relative);
            messageText = (TextView) itemView.findViewById(R.id.message);
            profileImage = (CircleImageView) itemView.findViewById(R.id.chat_user_image);
            messageImage = (ImageView) itemView.findViewById(R.id.message_image_layout);

        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        Message c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = dataSnapshot.child("profilePhoto").getValue().toString();
                Picasso.with(holder.profileImage.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.defaultImage).into(holder.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(holder.profileImage.getContext()).load(image)
                                .placeholder(R.color.defaultImage).into(holder.profileImage);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(message_type.equals("text")) {

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);


        } else {

            holder.messageText.setVisibility(View.GONE);
            Picasso.with(holder.profileImage.getContext()).load(c.getMessage()).fit().centerCrop().placeholder(R.color.defaultImage).into(holder.messageImage);

        }

        Message message = mMessageList.get(position);
        holder.messageText.setText(message.getMessage());


    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
