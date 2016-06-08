package nanodegree.vardaan.moviefinder.async;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import nanodegree.vardaan.moviefinder.api.TrailersResponse.Trailer;
import nanodegree.vardaan.moviefinder.provider.MovieProvider;
import nanodegree.vardaan.moviefinder.provider.MovieProvider.TrailerContract;


public class TrailerListLoader extends AsyncTaskLoader<ArrayList<Trailer>> {

    private final long mMovieId;

    private ArrayList<Trailer> mTrailers;

    public TrailerListLoader(Context context, long movieId) {
        super(context);
        mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mTrailers != null) {
            deliverResult(mTrailers);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<Trailer> loadInBackground() {
        String selection = MovieProvider.COL_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(mMovieId)};
        Cursor cursor = getContext().getContentResolver()
                .query(TrailerContract.CONTENT_URI, null, selection, selectionArgs, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        } else {
            mTrailers = new ArrayList<>();
            int key = cursor.getColumnIndex(TrailerContract.KEY);
            int name = cursor.getColumnIndex(TrailerContract.NAME);
            while (cursor.moveToNext()) {
                Trailer trailer = new Trailer();
                trailer.key = cursor.getString(key);
                trailer.name = cursor.getString(name);
                mTrailers.add(trailer);
            }
            cursor.close();
            return mTrailers;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
