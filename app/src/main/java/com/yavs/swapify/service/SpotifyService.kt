package com.yavs.swapify.service

import com.yavs.swapify.data.model.Playlist
import com.yavs.swapify.data.model.Track
import com.yavs.swapify.data.model.User
import com.yavs.swapify.data.model.spotify.SpotifyPlaylist
import com.yavs.swapify.data.model.spotify.SpotifyTrack
import com.yavs.swapify.data.model.spotify.SpotifyUser
import com.yavs.swapify.service.authService.AuthService
import com.yavs.swapify.service.authService.SpotifyAuthService
import com.yavs.swapify.utils.Constants
import com.yavs.swapify.utils.Platform
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import javax.inject.Inject

class SpotifyService  @Inject constructor() : PlatformService {

    private val spotifyAuthService: AuthService = SpotifyAuthService()

    data class Wrapper<T>(val items: T)
    data class TrackWrapper<T>(val track: T?)
    data class Search<T>(val tracks: Wrapper<T>)

    interface SpotifyApi{
        @GET("v1/me")
        suspend fun getUser(
            @Header("Authorization") accessToken: String
        ): Response<SpotifyUser>

        @GET("/v1/me/playlists")
        suspend fun getPlaylists(
            @Header("Authorization") authorization: String
        ): Response<Wrapper<List<SpotifyPlaylist>>>

        @GET("v1/search?")
        suspend fun searchTrack(
            @Header("Authorization")authorization:String,
            @Query("q") title: String,
            @Query("type")type:String,
            @Query("limit")limit:Int,
            @Query("include_external")includeExternal:String
        ): Response<Search<List<SpotifyTrack>>>

        @Headers("Content-Type: application/json")
        @POST("v1/me/playlists")
        suspend fun createPlaylistSwap(
            @Header("Authorization") authorization : String,
            @Body name:RequestBody
        ): Response<SpotifyPlaylist>

        @GET("v1/playlists/{id}/tracks")
        suspend fun getPlaylistTracks(
            @Path("id") id:String,
            @Header("Authorization")authorization:String,
        ): Response<Wrapper<List<TrackWrapper<SpotifyTrack>>>>

        @Headers("Content-Type: application/json")
        @POST("v1/playlists/{id}/tracks")
        suspend fun addTracks(
            @Path("id") id:String,
            @Header("Authorization")authorization:String,
            @Body tracks:RequestBody
        ): Response<Any>
    }
    private val spotifyApi = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.Spotify.BASE_URL).build().create(SpotifyApi:: class.java)


    override suspend fun getUser(token: String): User {
        val response = spotifyApi.getUser("Bearer $token")
        return if (response.isSuccessful) response.body()!!
            .toUser().also { it.isInit = true } else User(platform = Platform.Spotify)
    }

    override suspend fun getPlaylists(token: String): List<Playlist> {
        val response = spotifyApi.getPlaylists("Bearer $token")
        return (if(response.isSuccessful) response.body()!!.items.map { it.toPlaylist() } else emptyList())

    }

    override suspend fun getPlaylistTracks(token: String, playlistId: String): List<Track> {
        val response = spotifyApi.getPlaylistTracks(playlistId,"Bearer $token")
        return (if(response.isSuccessful) response.body()!!.items.filter { it.track!=null }.map { it.track?.toTrack()?: Track() } else emptyList())
    }

    override suspend fun searchTrack(title: String, artist: String,token:String): Track? {
        val response = spotifyApi.searchTrack(
            "Bearer $token",
            "$title $artist",
            "track",
            1,
            "audio"
        )
        return if(response.isSuccessful) response.body()!!.tracks.items.map { it.toTrack() }.getOrNull(0) else null
    }

    override suspend fun createPlaylistSwap(token: String, name:String, tracks: List<Track>): Boolean {
        val requestBody = "{\"name\": \"$name\"}".toRequestBody("application/json; charset=utf-8".toMediaType())
        val response = spotifyApi.createPlaylistSwap("Bearer $token",requestBody)
        val id =  if(response.isSuccessful) response.body()?.id else return false
        val ok = "{\"uris\":[${tracks.joinToString{ "\"${it.uri}\"" }}],\"position\": 0}"
        val tracksBody = ok.toRequestBody("application/json; charset=utf-8".toMediaType())
        val add = spotifyApi.addTracks(id?:"","Bearer $token", tracksBody)
        return add.isSuccessful
    }
    override fun getOAuthUrl(): String {
        return spotifyAuthService.getOAuthUrl()
    }

    override suspend fun getOAuthToken(code: String): String {
        return spotifyAuthService.getOAuthToken(code)
    }
}