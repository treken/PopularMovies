package com.jcMobile.android.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jcMobile.android.popularmovies.Adapters.MovieAdapter;
import com.jcMobile.android.popularmovies.DataList.FeedItem;
import com.jcMobile.android.popularmovies.DataList.JSONResultList;
import com.jcMobile.android.popularmovies.utilities.EndlessRecyclerViewScrollListener;
import com.jcMobile.android.popularmovies.utilities.SetMarginOfGridlayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TopRatedListFragment extends Fragment implements MovieAdapter.ListItemClickListener {

    @BindView(com.jcMobile.android.popularmovies.R.id.mainPage_CoordinatorLayout)
    CoordinatorLayout mLayout;

    @BindView(com.jcMobile.android.popularmovies.R.id.rv_recycleview_PosterImage)
     RecyclerView mRecycleView;

    @BindView(com.jcMobile.android.popularmovies.R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    private final String key = BuildConfig.THE_MOVIE_DB_API_TOKEN;

    private GridLayoutManager mLayoutManager;

    private MainActivity mMainActivity;

    private boolean snakeBarAppear = false;

    private Gson mGson;

    private RequestQueue mRequestQueue;

    private JSONResultList ResultList = new JSONResultList();

    private List<FeedItem> TopRatedMoviesList = new ArrayList<>();

    private MovieAdapter mMovieAdapter;

    private final String KEY_INSTANCE_STATE_RV_POSITION = "recycleViewKey";

    private final String MOVIE_RATE_URL =
            "http://api.themoviedb.org/3/movie/top_rated?api_key=" + key + "&language=en-US&page=";



    public TopRatedListFragment() {

    }

    public TopRatedListFragment newInstance() {
        TopRatedListFragment fragment = new TopRatedListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        if (internet_connection()) {
            mRequestQueue = Volley.newRequestQueue(MainActivity.getmContext());
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            mGson = gsonBuilder.create();

            StringRequest requestForTopRatedMovies = new StringRequest(Request.Method.GET, MOVIE_RATE_URL + "1", onPostsLoadedTopRated, onPostsError);

            mRequestQueue.add(requestForTopRatedMovies);

            snakeBarAppear = false;

        }else{
            snakeBarAppear = true;
        }


        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(com.jcMobile.android.popularmovies.R.layout.fragment_mainpage, container, false);

        ButterKnife.bind(this, view);

        SetMarginOfGridlayout setMarginOfGridlayout = new SetMarginOfGridlayout(0);

        mLayoutManager = new GridLayoutManager(getActivity(), numberOfColumns());

        mRecycleView.setLayoutManager(mLayoutManager);

        mRecycleView.addItemDecoration(setMarginOfGridlayout);

        mRecycleView.setHasFixedSize(true);

        if(snakeBarAppear) showSnakeBar();

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener
                = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {


                StringRequest requestForTopRatedMovies = new StringRequest(Request.Method.GET, MOVIE_RATE_URL + String.valueOf(page), onPostsLoadedTopRated, onPostsError);

                mRequestQueue.add(requestForTopRatedMovies);


                final int curSize = mMovieAdapter.getItemCount();
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        // Notify adapter with appropriate notify methods
                        mMovieAdapter.notifyItemRangeInserted(curSize, TopRatedMoviesList.size() - 1);
                    }
                });

            }
        };
        mRecycleView.addOnScrollListener(endlessRecyclerViewScrollListener);

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
            mRecycleView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mRecycleView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onClick(int clickedItemIndex, ImageView sharedImageView) {

        String textTitle;

        String textReleaseDate;

        String textOverview;

        Double textVoteAverage;

        String urlThumbnail;

        int numberMovieIDInTMDB;

        Class destinationActivity = ChildActivity.class;

        Intent startChildActivityIntent = new Intent(getContext(), destinationActivity);

        textTitle = TopRatedMoviesList.get(clickedItemIndex).getTitle();

        textReleaseDate = TopRatedMoviesList.get(clickedItemIndex).getReleaseDate();

        textOverview = TopRatedMoviesList.get(clickedItemIndex).getOverview();

        textVoteAverage = TopRatedMoviesList.get(clickedItemIndex).getVoteAverage();

        urlThumbnail = TopRatedMoviesList.get(clickedItemIndex).getPosterPath();

        numberMovieIDInTMDB = TopRatedMoviesList.get(clickedItemIndex).getId();


        Bundle extras = new Bundle();

        extras.putString("title", textTitle);
        extras.putString("releaseDate", textReleaseDate);
        extras.putString("overview", textOverview);
        extras.putDouble("voteAverage", textVoteAverage);
        extras.putString("Thumbnail", urlThumbnail);
        extras.putInt("id", numberMovieIDInTMDB);
        extras.putString("transitionName", ViewCompat.getTransitionName(sharedImageView));

        String transitionName = getString(R.string.transition_name);

        startChildActivityIntent.putExtras(extras);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                sharedImageView,   // Starting view
                transitionName    // The String
        );
        //Start the Intent
        ActivityCompat.startActivity(getActivity(), startChildActivityIntent, options.toBundle());
    }


    private final Response.Listener<String> onPostsLoadedTopRated = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            ResultList = mGson.fromJson(response, JSONResultList.class);

            if (TopRatedMoviesList == null || TopRatedMoviesList.size() == 0) {

                TopRatedMoviesList = ResultList.getResults();

                mMovieAdapter = new MovieAdapter(MainActivity.getmContext(), TopRatedMoviesList, TopRatedListFragment.this);

                mRecycleView.setAdapter(mMovieAdapter);

            } else {

                TopRatedMoviesList.addAll(ResultList.getResults());
            }


        }
    };


    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showSnakeBar();
        }
    };

    private  boolean internet_connection() {

        ConnectivityManager cm =
                (ConnectivityManager) MainActivity.getmContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    private int numberOfColumns() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;

    }

    private void showSnakeBar(){
        String message = getString(com.jcMobile.android.popularmovies.R.string.no_internet_connection);
        Snackbar snackbar = Snackbar
                .make(mLayout, message, Snackbar.LENGTH_LONG);
        View SnakeView = snackbar.getView();
        SnakeView.setBackgroundColor(Color.WHITE);
        snackbar.show();
    }

}
