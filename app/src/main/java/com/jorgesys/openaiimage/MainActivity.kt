package com.jorgesys.openaiimage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var imageImgV: ImageView
    lateinit var questionTxtV: TextView
    lateinit var queryEdt: TextInputEditText
    //Get Open AI API Key from: https://platform.openai.com/account/api-keys
    var url = "https://api.openai.com/v1/images/generations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageImgV = findViewById(R.id.idIVImage)
        questionTxtV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)

        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (queryEdt.text.toString().length > 0) {
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query to get image...", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun getResponse(query: String) {
        questionTxtV.text = query
        queryEdt.setText("")
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject: JSONObject? = JSONObject()
        jsonObject?.put("prompt", query)
        jsonObject?.put("n", 1)
        jsonObject?.put("size", "256x256")

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    var imageURL: String =
                        response.getJSONArray("data").getJSONObject(0).getString("url")
                    imageURL = imageURL.replace("\\", "");
                    Glide.with(applicationContext).load(imageURL).into(imageImgV)
                },
                Response.ErrorListener { error ->
                    Log.e("OPENAIJorgesys", "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer ${getString(R.string.openai_api_key)}"
                    Log.i("OPENAIJorgesys", "Request headers : $params")
                    return params;
                }
            }

         postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 100000
            }

            override fun getCurrentRetryCount(): Int {
                return 100000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
                Log.e("OPENAIJorgesys", "Request error: ${error.message}")
            }
        })

        queue.add(postRequest)
    }
}