package nanodegree.vardaan.moviefinder.fragments;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.StringTokenizer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nanodegree.vardaan.moviefinder.R;
import nanodegree.vardaan.moviefinder.api.MovieResponse;
import nanodegree.vardaan.moviefinder.api.MovieResponse.Movie;
import nanodegree.vardaan.moviefinder.api.RetrofitAdapter;
import nanodegree.vardaan.moviefinder.api.TmdbService;
import nanodegree.vardaan.moviefinder.async.MovieListLoader;
import nanodegree.vardaan.moviefinder.async.MovieStoreAsyncTask;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MoviesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

    private final static String IS_DUAL_PANE = "is_dual_pane";

    private final static String ARG_ITEMS = "items";

    private final static String ARG_SELECTED_ITEM = "selected_item";

    private final static String ARG_SORT_ORDER = "sort_order";

    private final static String ARG_VIEW_STATE = "view_state";

    private final static int VIEW_STATE_LOADING = 0;

    private final static int VIEW_STATE_ERROR = 1;

    private final static int VIEW_STATE_EMPTY = 2;

    private final static int VIEW_STATE_RESULTS = 3;

    private final static int LOADER_ID = 1;

    @InjectView(R.id.empty_text_view)
    TextView mEmptyTextView;

    @InjectView(R.id.error_text_view)
    TextView mErrorTextView;

    @InjectView(R.id.retry_button)
    Button mRetryButton;

    @InjectView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private MoviesAdapter mAdapter;

    private String mSortOrder;

    private ListActionListener mActionListener;

    private boolean isDualPane;

    public static MoviesFragment getInstance(boolean isDualPane) {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_DUAL_PANE, isDualPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActionListener = (ListActionListener) activity;
        } catch (ClassCastException e) {
            Log.e(this.getClass().getName(),
                    "Activity must implement " + ListActionListener.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
        if (!isDualPane && mSortOrder.equals(getString(R.string.sort_order_favorites))
                && mAdapter.getItemCount() > 0) {
            // update favorites list
            populateData();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDualPane = getArguments().getBoolean(IS_DUAL_PANE, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.inject(this, v);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getSpanCount()));
        mAdapter = new MoviesAdapter(getActivity(), isDualPane, mActionListener);
        mRecyclerView.setAdapter(mAdapter);
        mSortOrder = getSortParam();
        if (savedInstanceState == null) {
            populateData();
        } else {
            mSortOrder = savedInstanceState.getString(ARG_SORT_ORDER);
            if (mSortOrder != null && !mSortOrder.equalsIgnoreCase(getSortParam())) {
                populateData();
            }
            int state = savedInstanceState.getInt(ARG_VIEW_STATE, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorViews();
                    break;
                case VIEW_STATE_RESULTS:
                    int selectedPosition = savedInstanceState.getInt(ARG_SELECTED_ITEM, 0);
                    ArrayList<Movie> items = savedInstanceState.getParcelableArrayList(ARG_ITEMS);
                    mAdapter.mSelectedPosition = selectedPosition;
                    mAdapter.setItems(items);
                    showResultViews();
                    if (isDualPane) {
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(mAdapter.mSelectedPosition);
                            }
                        });
                    }
                    break;
                case VIEW_STATE_EMPTY:
                    showEmptyViews();
                    break;
                default:
                    showLoadingViews();
                    break;
            }
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int state = VIEW_STATE_RESULTS;
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_LOADING;
        } else if (mErrorTextView.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_ERROR;
        } else if (mEmptyTextView.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_EMPTY;
        }
        outState.putInt(ARG_VIEW_STATE, state);
        outState.putParcelableArrayList(ARG_ITEMS, mAdapter.getItems());
        outState.putInt(ARG_SELECTED_ITEM, mAdapter.mSelectedPosition);
        outState.putString(ARG_SORT_ORDER, mSortOrder);
        super.onSaveInstanceState(outState);
    }

    /**
     * Changes sort order, stores selected param to SharedPreferences, reloads fragment data.
     *
     * @param sortOrder sortBy param
     */
    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
        mAdapter.mSelectedPosition = 0;
        populateData();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prefs_sort_order), mSortOrder);
        editor.apply();
    }

    public void setDualPane(boolean dualPane) {
        isDualPane = dualPane;
    }

    public void favoriteListChanged(long movieId) {
        loadFavorites();
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            // TODO: remove/insert by one item
            populateData();
        }
    }

    /** Returns favorites list. */
    private ArrayList<Long> loadFavorites() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favoritesString = prefs.getString(getString(R.string.prefs_favorites), "");
        ArrayList<Long> list = new ArrayList<>();
        if (favoritesString.length() > 0) {
            StringTokenizer st = new StringTokenizer(favoritesString, ",");
            while (st.hasMoreTokens()) {
                list.add(Long.parseLong(st.nextToken()));
            }
        }
        if (mAdapter != null) {
            mAdapter.setFavorites(list);
        }
        return list;
    }

    /** Loads data from server or from db. */
    private void populateData() {
        // load from db or from server
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            showLoadingViews();
            getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
            loadFavorites();
            loadData();
        }
    }

    /** Loads movie list. */
    private void loadData() {
        showLoadingViews();
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        service.getMovieList(mSortOrder, new Callback<MovieResponse>() {
            @Override
            public void success(MovieResponse movieResponse, Response response) {
                showResultViews();
                mAdapter.setItems(movieResponse.results);
                mRecyclerView.scrollToPosition(0);
                storeMovies(movieResponse.results);
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorViews();
            }
        });
    }

    /** Retries to reload movie list from server. */
    private void reload() {
        loadData();
    }

    /**
     * Returns default (popularity) or set by user sortBy param.
     *
     * @return sortBy param
     */
    private String getSortParam() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getString(R.string.sort_order_popularity);
        return prefs.getString(getString(R.string.prefs_sort_order), defaultValue);
    }

    /** Calculates grid span count */
    private int getSpanCount() {
        int orientation = getResources().getConfiguration().orientation;
        int sw = getResources().getConfiguration().smallestScreenWidthDp;
        boolean landscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (sw < 600) {
            return (landscape) ? 4 : 2;
        } else if (sw < 720) {
            return (landscape) ? 2 : 3;
        } else {
            return (landscape) ? 3 : 2;
        }
    }

    /** Helper method to hide all elements, except progress bar. */
    private void showLoadingViews() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
        mActionListener.onEmptyMovieList();
    }

    /** Helper method to hide all elements, except error views. */
    private void showErrorViews() {
        mErrorTextView.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
        mActionListener.onEmptyMovieList();
    }

    /** Helper method to hide all elements, except empty view. */
    private void showEmptyViews() {
        mEmptyTextView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mActionListener.onEmptyMovieList();
    }


    /** Helper method to hide all elements, except recycler view. */
    private void showResultViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @SuppressWarnings("unchecked")
    /** Stores movie list to db. */
    private void storeMovies(ArrayList<Movie> movieList) {
        new MovieStoreAsyncTask(getActivity()).execute(movieList);
    }

    /**
     * Loader callbacks
     */
    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        return new MovieListLoader(getActivity(), loadFavorites());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
        if (data == null) {
            showErrorViews();
        } else if (data.size() > 0) {
            mAdapter.setItems(data);
            mRecyclerView.scrollToPosition(0);
            showResultViews();
        } else {
            showEmptyViews();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {

    }

    /** Movie list action listener */
    public interface ListActionListener {

        void onMovieSelected(Movie movie, boolean isFavorite);

        void onEmptyMovieList();
    }

    /** Movies RecyclerView adapter class. */
    private static class MoviesAdapter extends RecyclerView.Adapter<MovieViewHolder> {

        final private Context mContext;

        final private ListActionListener mActionListener;

        final private boolean isDualPane;

        private ArrayList<Movie> mItems;

        private ArrayList<Long> mFavorites;

        // keep track of selected item
        private int mSelectedPosition;

        public MoviesAdapter(Context context, boolean dualPane, ListActionListener listener) {
            mContext = context;
            isDualPane = dualPane;
            mActionListener = listener;
            mItems = new ArrayList<>();
        }

        @SuppressWarnings("deprecation")
        @Override
        public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.movie_item, parent, false);
            MovieViewHolder holder = new MovieViewHolder(v);
            if (isDualPane) {
                Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.movie_placeholder);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.itemView.setBackground(drawable);
                } else {
                    holder.itemView.setBackgroundDrawable(drawable);
                }
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(MovieViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mItems.get(position).getPosterUrl())
                    .centerCrop()
                    .placeholder(R.drawable.movie_placeholder)
                    .crossFade()
                    .into(holder.mPosterView);
            holder.mPosterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(position);
                }
            });
            if (isDualPane) {
                holder.itemView.setSelected(mSelectedPosition == position);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        public void setFavorites(ArrayList<Long> favorites) {
            mFavorites = favorites;
        }

        public ArrayList<Movie> getItems() {
            return mItems;
        }

        public void setItems(ArrayList<Movie> items) {
            mItems = items;
            notifyDataSetChanged();
            if (isDualPane) {
                int position = (mSelectedPosition < mItems.size()) ? mSelectedPosition
                        : mItems.size() - 1;
                selectItem(position);
            }
        }

        private void selectItem(int position) {
            int prevPosition = mSelectedPosition;
            mSelectedPosition = position;
            notifyItemChanged(position);
            notifyItemChanged(prevPosition);
            Movie movie = mItems.get(position);
            boolean isFavorite = (mFavorites != null && mFavorites.contains(movie.id));
            mActionListener.onMovieSelected(movie, isFavorite);
        }
    }

    /** Movie view holder class. */
    static class MovieViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.poster_image_view)
        public ImageView mPosterView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setClickable(true);
        }
    }

}
