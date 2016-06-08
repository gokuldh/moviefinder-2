package nanodegree.vardaan.moviefinder.async;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import nanodegree.vardaan.moviefinder.api.ReviewsResponse.Review;
import nanodegree.vardaan.moviefinder.provider.MovieProvider;
import nanodegree.vardaan.moviefinder.provider.MovieProvider.ReviewContract;


public class ReviewListLoader extends AsyncTaskLoader<ArrayList<Review>> {

    private final long mMovieId;

    private ArrayList<Review> mReviews;

    public ReviewListLoader(Context context, long movieId) {
        super(context);
        mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mReviews != null) {
            deliverResult(mReviews);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<Review> loadInBackground() {
        String selection = MovieProvider.COL_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(mMovieId)};
        Cursor cursor = getContext().getContentResolver()
                .query(ReviewContract.CONTENT_URI, null, selection, selectionArgs, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        } else {
            mReviews = new ArrayList<>();
            int author = cursor.getColumnIndex(ReviewContract.AUTHOR);
            int content = cursor.getColumnIndex(ReviewContract.CONTENT);
            while (cursor.moveToNext()) {
                Review review = new Review();
                review.author = cursor.getString(author);
                review.content = cursor.getString(content);
                mReviews.add(review);
            }
            cursor.close();
            return mReviews;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
