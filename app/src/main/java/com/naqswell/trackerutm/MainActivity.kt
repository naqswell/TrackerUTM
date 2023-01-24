package com.naqswell.trackerutm

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.naqswell.trackerutm.databinding.ActivityMainBinding

enum class UTMParams(val displayName : String) {
    SOURCE("utm_source"),
    MEDIUM("utm_medium"),
    CAMPAIGN("utm_campaign"),
    CONTENT("utm_content"),
    TERM("utm_term"),
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var referrerClient: InstallReferrerClient
    private var queryParams = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        referrerClient = InstallReferrerClient.newBuilder(this).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        lateinit var response: ReferrerDetails
                        try {
                            response = referrerClient.installReferrer
                            val referrer = response.installReferrer

                            val referrerTokens: List<String> = referrer.split("&")
                            for (i in referrerTokens.indices) {
                                val values = referrerTokens[i].split("=").toTypedArray()
                                when (values[0]) {
                                    UTMParams.SOURCE.displayName -> queryParams[UTMParams.SOURCE.displayName] = values[1]
                                    UTMParams.MEDIUM.displayName -> queryParams[UTMParams.MEDIUM.displayName] = values[1]
                                    UTMParams.CAMPAIGN.displayName -> queryParams[UTMParams.CAMPAIGN.displayName] = values[1]
                                    UTMParams.CONTENT.displayName -> queryParams[UTMParams.CONTENT.displayName] = values[1]
                                    UTMParams.TERM.displayName -> queryParams[UTMParams.TERM.displayName] = values[1]
                                }
                            }
                            Log.i("UTM_QUERY", queryParams.toString())
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        Log.d("UTM_QUERY", "Feature not supported")
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE ->                         // Connection couldn't be established. Log.i("UTM_QUERY", "Feature not supported")
                        Log.d("UTM_QUERY", "Fail to establish connection")
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.d("UTM_QUERY", "Service disconnected")
            }
        })
    }
}