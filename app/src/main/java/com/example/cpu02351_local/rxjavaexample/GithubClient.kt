package com.example.cpu02351_local.retrofitdemo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface GithubClient {
    @GET("/users/{user}/repos")
    fun getRepos(@Path("user") user: String): Call<List<GithubRepo>>
}