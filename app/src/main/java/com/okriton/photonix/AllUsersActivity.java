package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView usersListView;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataRef;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mAuth = FirebaseAuth.getInstance();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userDataRef.keepSynced(true);

        mToolbar = findViewById(R.id.all_users_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersListView = findViewById(R.id.users_list_view);
        usersListView.setHasFixedSize(true);
        usersListView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null){

            startListening();

        }
    }

    private void startListening() {

        Query userQuery = userDataRef.orderByChild("name");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(userQuery, Users.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, AllUsersActivity.UsersViewHolder>(options){

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_item, viewGroup, false);
                return new UsersViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                final String list_user_id = getRef(position).getKey();

                final String userName = model.getName();
                String userImage = model.getImage();
                String userRegDate = model.getDate();

                holder.setUserData(userName, userImage);

                holder.setDateText(userRegDate);

                holder.sendReqBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", list_user_id);
                        profileIntent.putExtra("user_name", userName);
                        startActivity(profileIntent);

                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", list_user_id);
                        profileIntent.putExtra("user_name", userName);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        usersListView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private TextView userNameText, dateViewText;
        private CircleImageView userImageView;
        private Button sendReqBtn;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            sendReqBtn = mView.findViewById(R.id.send_req_btn);
        }

        @SuppressLint("CheckResult")
        public void setUserData(String name, String image){

            userNameText = mView.findViewById(R.id.single_list_user_name);
            userNameText.setText(name);

            userImageView = mView.findViewById(R.id.single_list_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(userImageView);

        }

        public void setDateText(String dateText) {

            dateViewText = mView.findViewById(R.id.single_reg_date);
            dateViewText.setText(dateText);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {

        firebaseUserSearch(searchText);

        return true;
    }

    private void firebaseUserSearch(String searchText) {

        Query userQuery = userDataRef.orderByChild("lowercase_name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(userQuery, Users.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, AllUsersActivity.UsersViewHolder>(options){


            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_item, viewGroup, false);
                return new UsersViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                final String list_user_id = getRef(position).getKey();

                final String userName = model.getName();
                String userImage = model.getImage();
                String userRegDate = model.getDate();

                holder.setUserData(userName, userImage);

                holder.setDateText(userRegDate);

                holder.sendReqBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", list_user_id);
                        profileIntent.putExtra("user_name", userName);
                        startActivity(profileIntent);

                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", list_user_id);
                        profileIntent.putExtra("user_name", userName);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        usersListView.setAdapter(adapter);
        adapter.startListening();

    }

}
