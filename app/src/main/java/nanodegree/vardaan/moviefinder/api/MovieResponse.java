package nanodegree.vardaan.moviefinder.api;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class MovieResponse {

    public ArrayList<Movie> results;

    public static class Movie implements Parcelable {

        public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

        private final static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

        public long id;

        @SerializedName("original_title")
        public String title;

        @SerializedName("backdrop_path")
        public String backdropPath;

        @SerializedName("poster_path")
        public String posterPath;

        public String overview;

        @SerializedName("vote_average")
        public float rating;

        @SerializedName("release_date")
        public String releaseDate;

        public Movie() {
        }

        private Movie(Parcel in) {
            id = in.readLong();
            title = in.readString();
            backdropPath = in.readString();
            posterPath = in.readString();
            overview = in.readString();
            rating = in.readFloat();
            releaseDate = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(id);
            out.writeString(title);
            out.writeString(backdropPath);
            out.writeString(posterPath);
            out.writeString(overview);
            out.writeFloat(rating);
            out.writeString(releaseDate);
        }

        /**
         * Helper methods to build poster image url.
         */
        public String getPosterUrl() {
            return IMAGE_BASE_URL + "w185" + this.posterPath;
        }

        public String getBackdropUrl() {
            return IMAGE_BASE_URL + "w780" + this.backdropPath;
        }

    }
}
