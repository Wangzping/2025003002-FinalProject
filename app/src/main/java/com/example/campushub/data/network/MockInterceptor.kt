package com.example.campushub.data.network

import com.example.campushub.data.entity.ActivityEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {

    private val gson: Gson = GsonBuilder().create()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val path = url.encodedPath
        val method = request.method

        Thread.sleep(300) // 模拟网络延迟

        val responseBody = when {
            method == "GET" && path == "/api/activities" -> {
                val category = url.queryParameter("category")
                val query = url.queryParameter("query")
                val activities = when {
                    !query.isNullOrBlank() -> MockDataSource.searchActivities(query)
                    !category.isNullOrBlank() && category != "all" -> MockDataSource.getActivitiesByCategory(category)
                    else -> MockDataSource.getActivities()
                }
                val dtoList = activities.map { it.toDto() }
                """{"code":200,"message":"success","data":${gson.toJson(dtoList)}}"""
            }
            method == "GET" && path.matches(Regex("/api/activities/\\d+")) -> {
                val id = path.substringAfterLast("/").toIntOrNull()
                if (id == null) {
                    """{"code":400,"message":"Invalid activity id","data":null}"""
                } else {
                    val activity = MockDataSource.getActivityById(id)
                    if (activity != null) {
                        """{"code":200,"message":"success","data":${gson.toJson(activity.toDto())}}"""
                    } else {
                        """{"code":404,"message":"Activity not found","data":null}"""
                    }
                }
            }
            else -> """{"code":404,"message":"Not found","data":null}"""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun ActivityEntity.toDto() = com.example.campushub.data.network.dto.ActivityDto(
        id = id, title = title, description = description, date = date, time = time,
        location = location, organizer = organizer, category = category,
        maxParticipants = maxParticipants, currentParticipants = currentParticipants,
        imageUrl = imageUrl, createdAt = createdAt
    )
}
