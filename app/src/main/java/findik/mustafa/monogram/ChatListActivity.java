package findik.mustafa.monogram;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import findik.mustafa.monogram.adapters.ChatAdapter;
import findik.mustafa.monogram.classes.User;

public class ChatListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private ImageView mUserList;

    private RecyclerView mChatListRecyclerView;
    final List<User> chatList = new ArrayList<>();
    private ChatAdapter mAdapter;
    private DatabaseReference mUserDatabase;

    private String currentUserid;
    private DatabaseReference mChatDatabase;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Toolbar.
        Toolbar mToolbar = findViewById(R.id.chatList_toolbar);
        mToolbar.setTitle("Direct"); // Toolbar title.
        setSupportActionBar(mToolbar);
        // Toolbar geri butonu. Manifeste Parent Activitiy eklemeyi unutma
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mChatListRecyclerView = findViewById(R.id.messages_list_recyler);
        mAdapter = new ChatAdapter(chatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChatListRecyclerView.setLayoutManager(mLayoutManager);
        mChatListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChatListRecyclerView.setAdapter(mAdapter); // Adapteri Ekliyoruz. recyclerView'e

        currentUserid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUserid);
        query = mChatDatabase.orderByChild("Negativedate");
        query.keepSynced(true); // Verileri Cache de tutmak için

        mSearchView = findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.onActionViewExpanded();
        mSearchView.clearFocus();
        mSearchView.setOnQueryTextListener(this);

        mUserList = findViewById(R.id.chat_userlist);
        mUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gotoUserList = new Intent(ChatListActivity.this,UserListActivity.class);
                startActivity(gotoUserList);
            }
        });



        LoadChatList(null);



    }

    private void LoadChatList(final String Textquery) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatList.clear();
                for (final DataSnapshot chatsingle : dataSnapshot.getChildren()){
                    final User chat  = new User();
                    final String Userid = chatsingle.getKey();

                    mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Userid);
                    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String image = dataSnapshot.child("profilePhoto").getValue().toString();
                            final String userName = dataSnapshot.child("username").getValue().toString();
                            if (Textquery !=null){
                                if (userName.toLowerCase().contains(Textquery.toLowerCase())){
                                    chat.setUserId(Userid);
                                    chat.setUserImage(image);
                                    chat.setUserName(userName);
                                    chatList.add(chat);
                                }
                            }else{
                                chat.setUserId(Userid);
                                chat.setUserImage(image);
                                chat.setUserName(userName);
                                chatList.add(chat);
                            }



                            mAdapter.notifyDataSetChanged(); //değişiklik oldugunda adapteri güncelle.
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        LoadChatList(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LoadChatList(newText);
        return false;
    }
}
