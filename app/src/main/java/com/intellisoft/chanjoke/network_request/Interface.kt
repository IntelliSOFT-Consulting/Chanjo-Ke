package com.intellisoft.chanjoke.network_request

import com.intellisoft.chanjoke.fhir.data.DbResetPassword
import com.intellisoft.chanjoke.fhir.data.DbSetPasswordReq
import com.intellisoft.chanjoke.fhir.data.DbSignIn
import com.intellisoft.chanjoke.fhir.data.DbSignInResponse
import com.intellisoft.chanjoke.fhir.data.DbUserInfoResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface Interface {


    @POST("provider/login")
    suspend fun signInUser(
        @Body dbSignIn: DbSignIn
    ): Response<DbSignInResponse>

    @GET("provider/me")
    suspend fun getUserInfo(
        @Header("Authorization") token: String, // Add this line to pass the Bearer Token
    ): Response<DbUserInfoResponse>

//    @GET("provider/reset-password?idNumber={idNumber}&email={email}")
    @GET("provider/reset-password")
    suspend fun resetPassword(
        @Query("idNumber") idNumber:String,
        @Query("email", encoded = true) email:String,
    ): Response<DbResetPassword>

    @POST("provider/reset-password")
    suspend fun setNewPassword(
        @Body dbSetPasswordReq: DbSetPasswordReq
    ): Response<Any>




}