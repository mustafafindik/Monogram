package findik.mustafa.monogram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.adapters.MessageAdapter;
import findik.mustafa.monogram.classes.Message;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    private String chatUserId;
    private String chatUserName;
    private Toolbar mToolbar;
    private TextView mUserTitle;
    private CircleImageView mUserPhoto;
    private DatabaseReference mRootRef;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private ImageButton mAddBtn, mSendBtn;
    private EditText mMessage;

    private RecyclerView mMessageListRyc;

    final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager mLiner;
    private MessageAdapter messageAdapter;

    // Storage Firebase
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mAddBtn = findViewById(R.id.chat_add_btn);
        mSendBtn = findViewById(R.id.chat_send_btn);
        mMessage = findViewById(R.id.chat_message_view);
        mMessageListRyc = findViewById(R.id.messages_list);
        messageAdapter = new MessageAdapter(messageList, this);

        mLiner = new LinearLayoutManager(this);
        mMessageListRyc.setHasFixedSize(true);
        mMessageListRyc.setLayoutManager(mLiner);
        mMessageListRyc.setAdapter(messageAdapter);

        chatUserId = getIntent().getStringExtra("Userid");
        chatUserName = getIntent().getStringExtra("userName");
        currentUserId = mAuth.getCurrentUser().getUid();

        // Toolbar.
        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mUserTitle = findViewById(R.id.bar_user_title);
        mUserPhoto = findViewById(R.id.bar_user_image);
        mUserTitle.setText(chatUserName);


        mRootRef.child("Users").child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String UserImage = dataSnapshot.child("profilePhoto").getValue().toString();
                // Resimler Uri şeklinde geldiği için Picasso yardımı ile set edilir. NetworkPolicy Ofline demek cache tut demek.
                Picasso.with(ChatActivity.this).load(UserImage).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.defaultImage).into(mUserPhoto, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ChatActivity.this).load(UserImage)
                                .placeholder(R.color.defaultImage).into(mUserPhoto);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUserId)) {

                    Long tsLong = System.currentTimeMillis();
                    long negativeTS = -1 * tsLong; // Eklenen Postları Son Eklenen En ÜSTTE çıksın diye eklediğimiz Negatif TIME.

                    Map chatMap = new HashMap();
                    chatMap.put("time", ServerValue.TIMESTAMP);
                    chatMap.put("Negativedate", negativeTS);

                    mRootRef.child("Chat").child(currentUserId).child(chatUserId).setValue(chatMap);
                    mRootRef.child("Chat").child(chatUserId).child(currentUserId).setValue(chatMap);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Resimleri Erişim izni var mı kontrol edilir.
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0); // Yoksa izin Alınır.
                } else {
                    //izin varsa cropImage ile resim seçilir ve kesilir.
                    CropImage.activity()
                            .setAspectRatio(1, 1) //ORAN
                            .setMinCropWindowSize(500, 500) //Boyut.
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(ChatActivity.this);
                }

            }
        });


        loadMessages();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ChatActivity.this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                final byte[] thumb_byte = baos.toByteArray(); //resim Byte çevrilir.

                DatabaseReference user_message_push = mRootRef.child("messages").child(currentUserId).child(chatUserId).push();
                final String push_id = user_message_push.getKey();
                String imageName = "MessageImages/" + push_id + ".jpg";

                StorageReference reference = mImageStorage.child(imageName);

                reference.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            String download_url = task.getResult().getDownloadUrl().toString();
                            final Map messageMap = new HashMap();
                            messageMap.put("message", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("date", ServerValue.TIMESTAMP);
                            messageMap.put("from", currentUserId);
                            mMessage.setText("");

                            mRootRef.child("Messages").child(currentUserId).child(chatUserId).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mMessage.setText("");
                                        mRootRef.child("Messages").child(chatUserId).child(currentUserId).child(push_id).setValue(messageMap);
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }
                });

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMessages() {
        mRootRef.child("Messages").child(currentUserId).child(chatUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = new Message();
                String date = dataSnapshot.child("date").getValue().toString();
                String msg = dataSnapshot.child("message").getValue().toString();
                String seen = dataSnapshot.child("seen").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();
                String from = dataSnapshot.child("from").getValue().toString();

                message.setMessage(msg);
                message.setSeen(Boolean.parseBoolean(seen));
                message.setTime(Long.parseLong(date));
                message.setType(type);
                message.setFrom(from);

                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                mMessageListRyc.scrollToPosition(messageList.size() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage() {
        String Message = mMessage.getText().toString();
        if (!TextUtils.isEmpty(Message)) {
            final Map messageMap = new HashMap();
            messageMap.put("message", Message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("date", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);

            DatabaseReference user_message_push = mRootRef.child("messages").child(currentUserId).child(chatUserId).push();
            final String push_id = user_message_push.getKey();

            mRootRef.child("Messages").child(currentUserId).child(chatUserId).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mMessage.setText("");
                        mRootRef.child("Messages").child(chatUserId).child(currentUserId).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Long tsLong = System.currentTimeMillis();
                                    long negativeTS = -1 * tsLong; // Eklenen Postları Son Eklenen En ÜSTTE çıksın diye eklediğimiz Negatif TIME.
                                    Map chatMap = new HashMap();
                                    chatMap.put("time", ServerValue.TIMESTAMP);
                                    chatMap.put("Negativedate", negativeTS);
                                    mRootRef.child("Chat").child(currentUserId).child(chatUserId).updateChildren(chatMap);
                                    mRootRef.child("Chat").child(chatUserId).child(currentUserId).updateChildren(chatMap);

                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChatActivity.this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(this, "Herhangi Bir Mesaj Yazmadınız.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        mRootRef.child("Messages").child(currentUserId).child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( !dataSnapshot.hasChildren()){
                    mRootRef.child("Chat").child(currentUserId).child(chatUserId).removeValue();
                    mRootRef.child("Chat").child(chatUserId).child(currentUserId).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        super.onDestroy();
    }

}
