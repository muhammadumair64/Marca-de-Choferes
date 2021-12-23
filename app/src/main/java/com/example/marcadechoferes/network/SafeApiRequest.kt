package com.example.marcadechoferes.network

import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.myApplication.MyApplication
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.io.Reader

abstract class SafeApiRequest {
    lateinit var tinyDB: TinyDB

    suspend fun<T: Any> apiRequest(call: suspend () -> Response<T>) : T{
        val response = call.invoke()
        var Token=""
        response.headers()
        tinyDB= TinyDB(MyApplication.appContext)
         Token = tinyDB.getString("Cookie").toString()

        when {
            response.isSuccessful -> {
                val Cookielist = response.headers().values("Set-Cookie")
                if(Token=="" && Cookielist.isNotEmpty() ) {
                    val jsessionid = Cookielist[0].split(";").toTypedArray()[0]
                    val separator = jsessionid.split("=").toTypedArray()[0]
                    println("Headers $jsessionid")
                    println("After Split $separator")
                    if (separator == "choferes") {
                        tinyDB.putString("Cookie", jsessionid)
                    }
                }
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