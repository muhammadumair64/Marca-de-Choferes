package com.example.marcadechoferes.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.io.Reader

abstract class SafeApiRequest {


    suspend fun<T: Any> apiRequest(call: suspend () -> Response<T>) : T{
        val response = call.invoke()
        when {
            response.isSuccessful -> {
                return response.body()!!
            }
            (response.code() >= 400||response.code() <= 500) -> {

                throw ResponseException(response.errorBody()?.charStream())
            }
            else -> {
                val error = response.errorBody()?.string()
                val message = StringBuilder()
                error?.let{
                    try{
                        message.append(JSONObject(it).getString("message"))
                    }catch(e: JSONException){ }
                    message.append("\n")
                }
                message.append("Error Code: ${response.code()}")
                throw ApiException(message.toString())

            }
        }
    }

}
data class ResponseException(val response: Reader?) : IOException()
class ApiException(message: String) : IOException(message)
class NoInternetException(message: String) : IOException(message)