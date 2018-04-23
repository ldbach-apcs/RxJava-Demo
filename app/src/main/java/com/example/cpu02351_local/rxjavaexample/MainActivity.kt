package com.example.cpu02351_local.rxjavaexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        val callGithubApi = createRxObservable(call)
        setupAndSubscribeToRxObservable(callGithubApi)
    }

    private fun setupAndSubscribeToRxObservable(callGithubApi: Observable<GithubRepo>?) {
        callGithubApi!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<GithubRepo> {
                    var adapter: ArrayAdapter<GithubRepo>? = null
                    var list = ArrayList<GithubRepo>()
                    var d: Disposable? = null


                    override fun onComplete() {
                        if (d != null && !d!!.isDisposed)
                            d!!.dispose()
                    }

                    override fun onNext(t: GithubRepo) {
                        list.add(t)
                        adapter?.notifyDataSetChanged()
                    }

                    override fun onSubscribe(d: Disposable) {
                        // Meh?
                        this.d = d
                        // Inflate ListView
                        adapter =
                                ArrayAdapter(
                                        this@MainActivity,
                                        R.layout.list_item,
                                        list)
                        list_item.adapter = adapter
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
    }

    private fun createRxObservable(call: Call<List<GithubRepo>>):  Observable<GithubRepo>? {
        return Observable.create { emitter ->

            val response = call.execute()
            for (repo in response.body()!!) {
                Thread.sleep(100)
                emitter.onNext(repo)
            }
            emitter.onComplete()

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
