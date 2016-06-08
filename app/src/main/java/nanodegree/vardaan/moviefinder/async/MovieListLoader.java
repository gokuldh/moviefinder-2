package nanodegree.vardaan.moviefinder.async;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import nanodegree.vardaan.moviefinder.api.MovieResponse.Movie;
import nanodegree.vardaan.moviefinder.provider.MovieProvider;


public class MovieListLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private ArrayList<Long> mFavoriteIds;

    public MovieListLoader(Context context, ArrayList<Long> favoriteIds) {
        super(context);
        mFavoriteIds = favoriteIds;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mFavoriteIds == null || mFavoriteIds.size() == 0) {
            deliverResult(new ArrayList<Movie>());
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<Movie> loadInBackground() {
        String selectionMarks = "";
        String[] favorites = new String[mFavoriteIds.size()];
        for (int i = 0; i < favorites.length; i++) {
            selectionMarks += "?,";
            favorites[i] = Long.toString(mFavoriteIds.get(i));
        }
        selectionMarks = selectionMarks.substring(0, selectionMarks.length() - 1);
        String selection = MovieProvider.MovieContract._ID + " in (" + selectionMarks + ")";
        Cursor cursor = getContext().getContentResolver()
                .query(MovieProvider.MovieContract.CONTENT_URI, null,
                        selection, favorites, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return new ArrayList<>();
        } else {
            int id = cursor.getColumnIndex(MovieProvider.MovieContract._ID);
            int title = cursor.getColumnIndex(MovieProvider.MovieContract.TITLE);
            int backdropPath = cursor.getColumnIndex(MovieProvider.MovieContract.BACKDROP_PATH);
            int posterPath = cursor.getColumnIndex(MovieProvider.MovieContract.POSTER_PATH);
            int overview = cursor.getColumnIndex(MovieProvider.MovieContract.OVERVIEW);
            int rating = cursor.getColumnIndex(MovieProvider.MovieContract.RATING);
            int releaseDate = cursor.getColumnIndex(MovieProvider.MovieContract.RELEASE_DATE);
            ArrayList<Movie> movies = new ArrayList<>();
            while (cursor.moveToNext()) {
                Movie movie = new Movie();
                movie.id = cursor.getLong(id);
                movie.title = cursor.getString(title);
                movie.backdropPath = cursor.getString(backdropPath);
                movie.posterPath = cursor.getString(posterPath);
                movie.overview = cursor.getString(overview);
                movie.rating = cursor.getFloat(rating);
                movie.releaseDate = cursor.getString(releaseDate);
                movies.add(movie);
            }
            cursor.close();
            return movies;
        }
    }

    @Override
    public void deliverResult(ArrayList<Movie> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
