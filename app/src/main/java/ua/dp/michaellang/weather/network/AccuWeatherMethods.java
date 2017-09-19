package ua.dp.michaellang.weather.network;

import android.accounts.AuthenticatorException;
import android.support.annotation.Nullable;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import ua.dp.michaellang.weather.network.model.Forecast.DailyForecastResponse;
import ua.dp.michaellang.weather.network.model.Forecast.HourlyForecast;
import ua.dp.michaellang.weather.network.model.Location.City;
import ua.dp.michaellang.weather.network.model.Location.Region;
import ua.dp.michaellang.weather.utils.KeyValuePair;
import ua.dp.michaellang.weather.utils.RetrofitUtils;

import java.util.ArrayList;
import java.util.List;

import static ua.dp.michaellang.weather.Contants.API_KEY;

/**
 * Date: 13.09.2017
 *
 * @author Michael Lang
 */
public class AccuWeatherMethods {
    private AccuWeatherService mService;

    private static class Nested {
        static AccuWeatherMethods instance = new AccuWeatherMethods();
    }

    private AccuWeatherMethods() {
        mService = RetrofitUtils.createService(AccuWeatherService.class);
    }

    public static AccuWeatherMethods getInstance() {
        return Nested.instance;
    }

    public void getCurrentCitiesWeather(Observer<KeyValuePair<String, HourlyForecast>> observer,
            final String[] locationKeys, @Nullable String language, @Nullable Boolean details) {
        //записываем все Observable в List
        List<Observable<KeyValuePair<String, HourlyForecast>>> lst = new ArrayList<>();
        for (final String locationKey : locationKeys) {
            //сохраняем результат в виде ключ-значение
            Observable<KeyValuePair<String, HourlyForecast>> observable
                    = mService.getOneHourForecast(locationKey, API_KEY, language, details)
                    .map(new Func1<Response<List<HourlyForecast>>, KeyValuePair<String, HourlyForecast>>() {
                        @Override
                        public KeyValuePair<String, HourlyForecast> call(Response<List<HourlyForecast>> listResponse) {
                            HourlyForecast hourlyForecast = listResponse.body().get(0);
                            return new KeyValuePair<>(locationKey, hourlyForecast);
                        }
                    });

            lst.add(observable);
        }

        //собираем все KeyValuePair в Map, выполняем subscribe
        Observable.merge(lst)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void getCitiesByCountry(final Observer<List<City>> observer,
            String countryId, @Nullable String language, @Nullable Boolean details) {
        mService.getCitiesByCountry(countryId, API_KEY, language, details)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Response<List<City>>, List<City>>() {
                    @Override
                    public List<City> call(Response<List<City>> listResponse) {
                        if (!listResponse.isSuccessful()) {
                            observer.onError(new AuthenticatorException());
                        }
                        return listResponse.body();
                    }
                })
                .subscribe(observer);
    }

    public void get24HoursForecast(Observer<Response<List<HourlyForecast>>> observer,
            String locationKey, @Nullable String language, @Nullable Boolean details) {
        mService.get24HoursForecast(locationKey, API_KEY, language, details)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void getTenDaysForecast(Observer<Response<DailyForecastResponse>> observer,
            String locationKey, @Nullable String language, @Nullable Boolean details) {
        mService.getTenDaysForecast(locationKey, API_KEY, language, details)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public Observable<Response<City>> getCityInfo(String locationKey, @Nullable String language) {
        return mService.getCityInfo(locationKey, API_KEY, language);
    }

    public Observable<Response<List<Region>>> getCountryList(@Nullable String language) {
        return mService.getCountryList(API_KEY, language);
    }


}