package nanodegree.vardaan.moviefinder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import nanodegree.vardaan.moviefinder.api.MovieResponse;
import nanodegree.vardaan.moviefinder.fragments.DetailsFragment;


public class DetailsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 7" landscape
        int orientation = getResources().getConfiguration().orientation;
        int sw = getResources().getConfiguration().smallestScreenWidthDp;
        if (sw == 600 && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
        }
        setContentView(R.layout.activity_details);

        MovieResponse.Movie movie;
        boolean isFavorite;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movie = extras.getParcelable(DetailsFragment.MOVIE);
            isFavorite = extras.getBoolean(DetailsFragment.FAVORITE);
        } else {
            throw new NullPointerException("No movie found in intent extras");
        }

        if (savedInstanceState == null) {
            DetailsFragment fragment = DetailsFragment.getInstance(movie, isFavorite);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
