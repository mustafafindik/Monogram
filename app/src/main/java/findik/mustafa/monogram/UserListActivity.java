package findik.mustafa.monogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import findik.mustafa.monogram.adapters.ChatAdapter;
import findik.mustafa.monogram.classes.User;

public class UserListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private RecyclerView mChatListRecyclerView;
    final List<User> userList = new ArrayList<>();
    private ChatAdapter mAdapter;
    private DatabaseReference mUserDatabase;

    private String currentUserid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Toolbar.
        Toolbar mToolbar = findViewById(R.id.userList_toolbar);
        mToolbar.setTitle("Yeni Mesaj"); // Toolbar title.
        setSupportActionBar(mToolbar);
        // Toolbar geri butonu. Manifeste Parent Activitiy eklemeyi unutma
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mChatListRecyclerView = findViewById(R.id.userList_recyler);
        mAdapter = new ChatAdapter(userList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChatListRecyclerView.setLayoutManager(mLayoutManager);
        mChatListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChatListRecyclerView.setAdapter(mAdapter); // Adapteri Ekliyoruz. recyclerView'e

        currentUserid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true); // Verileri Cache de tutmak için

        mSearchView = findViewById(R.id.userList_search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.onActionViewExpanded();
        mSearchView.clearFocus();
        mSearchView.setOnQueryTextListener(this);

        LoadChatList(null);
    }

    private void LoadChatList(final String Textquery) {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (final DataSnapshot Usersingle : dataSnapshot.getChildren()){
                    final User chat  = new User();
                    final String Userid = Usersingle.getKey();
                    final String image = Usersingle.child("profilePhoto").getValue().toString();
                    final String userName = Usersingle.child("username").getValue().toString();

                    if (!currentUserid.equals(Userid))
                    {
                        if (Textquery !=null){
                            if (userName.toLowerCase().contains(Textquery.toLowerCase())){
                                chat.setUserId(Userid);
                                chat.setUserImage(image);
                                chat.setUserName(userName);
                                userList.add(chat);
                            }
                        }else{
                            chat.setUserId(Userid);
                            chat.setUserImage(image);
                            chat.setUserName(userName);
                            userList.add(chat);
                        }
                    }
                    mAdapter.notifyDataSetChanged(); //değişiklik oldugunda adapteri güncelle.

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
