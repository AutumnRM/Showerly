package com.showerly.app.data.remote

import com.showerly.app.data.remote.dto.CrowdApiResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface SchoolApiService {
    @GET
    suspend fun getCrowd(@Url url: String): CrowdApiResponse
}
