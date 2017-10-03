package com.jcMobile.android.popularmovies;


import android.app.ActivityOptions;
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


public class PopularListFragment extends Fragment implements MovieAdapter.ListItemClickListener {

    @BindView(com.jcMobile.android.popularmovies.R.id.mainPage_CoordinatorLayout)
    CoordinatorLayout mLayout;

    @BindView(com.jcMobile.android.popularmovies.R.id.rv_recycleview_PosterImage)
     RecyclerView mRecycleView;

    @BindView(com.jcMobile.android.popularmovies.R.id.pb_loading_indicator)
     ProgressBar mLoadingIndicator;

    private final String key = BuildConfig.THE_MOVIE_DB_API_TOKEN;

    private GridLayoutManager mLayoutManager;

    private boolean fragmentLoaded;

    private boolean snakeBarAppear = false;

    private Gson mGson;

    private MainActivity mMainActivity;

    private RequestQueue mRequestQueue;

    private JSONResultList ResultList = new JSONResultList();

    private List<FeedItem> PopMoviesList = new ArrayList<>();

    private MovieAdapter mMovieAdapter;

    private final String KEY_INSTANCE_STATE_RV_POSITION = "recycleViewKey";

    private final String MOVIE_POPULAR_URL =
            "http://api.themoviedb.org/3/movie/popular?api_key=" + key + "&language=en-US&page=";


    public PopularListFragment() {

    }

    public PopularListFragment newInstance() {
        PopularListFragment fragment = new PopularListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        if (internet_connection()) {
            mRequestQueue = Volley.newRequestQueue(MainActivity.getmContext());
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            mGson = gsonBuilder.create();

            StringRequest requestForPopularMovies = new StringRequest(Request.Method.GET, MOVIE_POPULAR_URL + "1", onPostsLoadedPopular, onPostsError);

            mRequestQueue.add(requestForPopularMovies);

            snakeBarAppear = false;

        }else{

            snakeBarAppear = true;
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null && !fragmentLoaded){
            fragmentLoaded = true;
        }

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

                StringRequest requestForPopularMovies = new StringRequest(Request.Method.GET, MOVIE_POPULAR_URL + String.valueOf(page), onPostsLoadedPopular, onPostsError);

                mRequestQueue.add(requestForPopularMovies);

                final int curSize = mMovieAdapter.getItemCount();
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        // Notify adapter with appropriate notify methods
                        mMovieAdapter.notifyItemRangeInserted(curSize, PopMoviesList.size() - 1);
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

        textTitle = PopMoviesList.get(clickedItemIndex).getTitle();

        textReleaseDate = PopMoviesList.get(clickedItemIndex).getReleaseDate();

        textOverview = PopMoviesList.get(clickedItemIndex).getOverview();

        textVoteAverage = PopMoviesList.get(clickedItemIndex).getVoteAverage();

        urlThumbnail = PopMoviesList.get(clickedItemIndex).getPosterPath();

        numberMovieIDInTMDB = PopMoviesList.get(clickedItemIndex).getId();


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


    private final Response.Listener<String> onPostsLoadedPopular = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            ResultList = mGson.fromJson(response, JSONResultList.class);

            if (PopMoviesList == null || PopMoviesList.size() == 0) {

                PopMoviesList = ResultList.getResults();

                mMovieAdapter = new MovieAdapter(mMainActivity.getmContext(), PopMoviesList, PopularListFragment.this);

                mRecycleView.setAdapter(mMovieAdapter);
            } else {

                PopMoviesList.addAll(ResultList.getResults());
            }

        }
    };

    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showSnakeBar();
        }
    };

    private boolean internet_connection() {

        ConnectivityManager cm =
                (ConnectivityManager) MainActivity.getmContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void showSnakeBar(){
        String message = getString(com.jcMobile.android.popularmovies.R.string.no_internet_connection);
        Snackbar snackbar = Snackbar
                .make(mLayout, message, Snackbar.LENGTH_LONG);
        View SnakeView = snackbar.getView();
        SnakeView.setBackgroundColor(Color.WHITE);
        snackbar.show();
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

}
