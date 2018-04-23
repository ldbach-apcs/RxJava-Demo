package com.example.cpu02351_local.rxjavaexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.example.cpu02351_local.retrofitdemo.GithubClient
import com.example.cpu02351_local.retrofitdemo.GithubRepo
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadDataAndFillList()
    }

    private fun loadDataAndFillList() {
        val call = getApiCallable()
        val callGithubApi = createRxSingle(call)
        setupAndSubscribeToRxSingle(callGithubApi)
    }

    private fun setupAndSubscribeToRxSingle(callGithubApi: Single<List<GithubRepo>>?) {
        callGithubApi!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<GithubRepo>> {
                    var d: Disposable? = null

                    override fun onSuccess(list: List<GithubRepo>) {
                        // Inflate ListView
                        val adapter =
                                ArrayAdapter<GithubRepo>(
                                        this@MainActivity,
                                        R.layout.list_item,
                                        list)
                        list_item.adapter = adapter

                        if (d != null && !d!!.isDisposed)
                            d!!.dispose()
                    }

                    override fun onSubscribe(d: Disposable) {
                        // Meh?
                        this.d = d
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
    }

    private fun createRxSingle(call: Call<List<GithubRepo>>): Single<List<GithubRepo>>? {
        return Single.create { emitter ->

            val response = call.execute()
            response.body()?.let { emitter.onSuccess(it) }
                    ?: emitter.onError(Exception("Fail to get result"))

            /*
            call.enqueue(object : Callback<List<GithubRepo>> {
                override fun onResponse(call: Call<List<GithubRepo>>, response: Response<List<GithubRepo>>) {
                    // response.body()
                    response.body()?.let { emitter.onSuccess(it) }
                            ?: emitter.onError(Exception("Fail to get result"))
                }

                override fun onFailure(call: Call<List<GithubRepo>>, t: Throwable) {
                    emitter.onError(t)
                }
            })
            */
        }
    }

    private fun getApiCallable(): Call<List<GithubRepo>> {
        val baseUrl = "https://api.github.com/"
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(
                        GsonConverterFactory.create()
                )
        val retrofit = builder
                .client(
                        httpClient.build()
                )
                .build()
        val client = retrofit.create(GithubClient::class.java)
        return client.getRepos("ldbach-apcs")
    }
}
