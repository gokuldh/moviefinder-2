package nanodegree.vardaan.moviefinder.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;


public interface TmdbService {

    // Example: /discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
    @GET("/discover/movie")
    void getMovieList(@Query("sort_by") String sortBy, Callback<MovieResponse> callback);

    @GET("/movie/{id}/videos")
    void getTrailers(@Path("id") long id, Callback<TrailersResponse> callback);

    @GET("/movie/{id}/reviews")
    void getReviews(@Path("id") long id, Callback<ReviewsResponse> callback);
}
