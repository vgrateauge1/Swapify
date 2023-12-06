package com.yavs.swapify.service

import com.yavs.swapify.model.Artist
import com.yavs.swapify.model.Playlist
import com.yavs.swapify.model.Track
import com.yavs.swapify.model.User
import com.yavs.swapify.utils.Platform
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class DeezerService : PlatformService {

    private val http = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    companion object{
        const val APP_ID: Long = 649821
        const val SECRET: String = "48e934960b566e8d508497542ba01205"
        const val REDIRECT_URL: String = "http://yavs.swapify/connect"
        const val BASE_URL = "https://api.deezer.com"
    }

    override fun getUser(token: String?): Result<User> {
         val req = Request.Builder().url("$BASE_URL/user/me?access_token=$token").build()
        val call: Call = http.newCall(req)
        val response: Response = call.execute()
        if (response.code==200){
            val user = json.decodeFromString<User>(response.body?.string()!!)
            user.platform=Platform.Deezer
            return Result.success(user)
        }
        return Result.failure(Exception("No user found"))
    }


    override fun getTrack(trackId: Long): Track {
        val req = Request.Builder().url("$BASE_URL/track?q=$trackId").build()
        val call: Call = http.newCall(req)
        val response: Response = call.execute()
        return json.decodeFromString<Track>(response.body!!.string())
    }


    override fun getArtist(artistId: Long): Artist {
        val req = Request.Builder().url("$BASE_URL/artist?q=$artistId").build()
        val call: Call = http.newCall(req)
        val response: Response = call.execute()
        return json.decodeFromString<Artist>(response.body!!.string())
    }

    override fun getPlaylists(token: String): List<Playlist> {
        val req = Request.Builder().url("$BASE_URL/user/me/playlist?access_token=$token").build()
        val call: Call = http.newCall(req)
        val response: Response = call.execute()
        return json.decodeFromString<List<Playlist>>(response.body!!.string())
    }

    override fun searchTrack(title: String, artist: String): List<Track> {
        val req = Request.Builder().url("$BASE_URL/search?q=artist:\"$artist\" track:\"$title\"").build()
        val call: Call = http.newCall(req)
        val response: Response = call.execute()
        return json.decodeFromString<List<Track>>(response.body!!.string())
    }

    override fun getOAuthUrl(): String {
        return "https://connect.deezer.com/oauth/auth.php?app_id=$APP_ID&redirect_uri=$REDIRECT_URL&perms=basic_access,manage_library,offline_access"
    }

    override fun getOAuthToken(code: String): String {

        try {
            val req = Request.Builder().url("https://connect.deezer.com/oauth/access_token.php?app_id=$APP_ID&secret=$SECRET&code=$code").build()
            val call: Call = http.newCall(req)
            val response: Response = call.execute()
            val r = response.body?.string()
            val token = r?.split("&")?.find { param -> param.startsWith("access_token=") }?.substringAfter("access_token=")
            return token!!
        }catch (e: Exception) {
            throw Exception("Error retrieving token",e.cause)
        }
    }
}