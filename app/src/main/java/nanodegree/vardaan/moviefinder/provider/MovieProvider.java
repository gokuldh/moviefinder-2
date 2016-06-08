package nanodegree.vardaan.moviefinder.provider;

import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.ProviGenOpenHelper;
import com.tjeannin.provigen.ProviGenProvider;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;


public class MovieProvider extends ProviGenProvider {

    public static final String COL_MOVIE_ID = "movie_id";

    private static final String AUTHORITY = "content://nanodegree.vardaan.moviefinder/";

    private static Class[] contracts = new Class[]{
            MovieContract.class, TrailerContract.class, ReviewContract.class};

    @Override
    public SQLiteOpenHelper openHelper(Context context) {
        return new ProviGenOpenHelper(getContext(), "MovieDatabase", null, 1, contracts);
    }

    @Override
    public Class[] contractClasses() {
        return contracts;
    }

    /**
     * Movie data model contract.
     */
    public interface MovieContract extends ProviGenBaseContract {

        @Column(Column.Type.TEXT)
        String TITLE = "title";

        @Column(Column.Type.TEXT)
        String BACKDROP_PATH = "backdrop_path";

        @Column(Column.Type.TEXT)
        String POSTER_PATH = "poster_path";

        @Column(Column.Type.TEXT)
        String OVERVIEW = "overview";

        @Column(Column.Type.REAL)
        String RATING = "rating";

        @Column(Column.Type.TEXT)
        String RELEASE_DATE = "release_date";

        @ContentUri
        Uri CONTENT_URI = Uri.parse(AUTHORITY + "movie");
    }

    /**
     * Movie trailer data model contract.
     */
    public interface TrailerContract extends ProviGenBaseContract {

        @Column(Column.Type.TEXT)
        String MOVIE_ID = COL_MOVIE_ID;

        @Column(Column.Type.TEXT)
        String KEY = "key";

        @Column(Column.Type.TEXT)
        String NAME = "name";

        @ContentUri
        Uri CONTENT_URI = Uri.parse(AUTHORITY + "trailer");

    }

    /**
     * Movie review data model contract.
     */
    public interface ReviewContract extends ProviGenBaseContract {

        @Column(Column.Type.TEXT)
        String MOVIE_ID = COL_MOVIE_ID;

        @Column(Column.Type.TEXT)
        String AUTHOR = "author";

        @Column(Column.Type.TEXT)
        String CONTENT = "content";

        @ContentUri
        Uri CONTENT_URI = Uri.parse(AUTHORITY + "review");

    }
}
