package tn.esprit.mybudget.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import tn.esprit.mybudget.data.model.CurrencyResponse;

public interface CurrencyApiService {
    @GET("latest/{base}")
    Call<CurrencyResponse> getLatestRates(@Path("base") String base);
}
