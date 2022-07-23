package com.armdev19.inappstemplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*
import com.armdev19.inappstemplate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        billingClient = BillingClient.newBuilder(this@MainActivity)
            .enablePendingPurchases()
            .setListener { billingResult, listPurchase ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && listPurchase != null) {
                    for (purchase in listPurchase) {
                        verifySubPurchase(purchase)
                    }
                }
            }
            .build()
        establishConnection()
    }

    override fun onResume() {
        super.onResume()
        checkPurchaseOnResume()
        checkSubscribeOnResume()
    }

    private fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
                establishConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                   showProductsPurchase()
                    showProductsSubscribe()
                }
            }

        })

    }

    private fun showProductsPurchase() {
        val skuList = ArrayList<String>()
        skuList.add("test.id.purchase_two")

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build(), object : SkuDetailsResponseListener{
            override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
                Log.d(TAG, "responseP: ${billingResult.responseCode} listP:$skuDetailsList ")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    for (skuDetail in skuDetailsList) {
                        if (skuDetail.sku == "test.id.purchase_two") {
                            binding.purchaseButton.setOnClickListener {
                                launchPurchaseFlow(buyItem = skuDetail)
                            }
                            Toast.makeText(this@MainActivity, "test.id.purchase_one", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        })
    }

    private fun showProductsSubscribe() {
        val skuList = ArrayList<String>()
        skuList.add("test.id.purchase_ones")
        skuList.add("test.id.subscribe_two")
        skuList.add("test.id.subscribe_three")

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)


        billingClient.querySkuDetailsAsync(params.build(), object : SkuDetailsResponseListener{
            override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    Log.d(TAG, "response: ${billingResult.responseCode} list:$skuDetailsList ")
                    for (skuDetail in skuDetailsList) {
                         if (skuDetail.sku == "com.one.subscribe") {
                            binding.subscribeOneButton.setOnClickListener {
                                launchPurchaseFlow(buyItem = skuDetail)
                            }
                        } else if (skuDetail.sku == "test.id.subscribe_two") {
                            binding.subscribeTwoButton.setOnClickListener {
                                launchPurchaseFlow(buyItem = skuDetail)
                            }
                        } else if (skuDetail.sku == "test.id.subscribe_three") {
                            binding.subscribeThreeButton.setOnClickListener {
                                launchPurchaseFlow(buyItem = skuDetail)
                            }
                        }
                    }
                }
            }

        })
    }

    private fun launchPurchaseFlow(buyItem: SkuDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(buyItem)
            .build()

        billingClient.launchBillingFlow(this@MainActivity, billingFlowParams)
    }

    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, object : AcknowledgePurchaseResponseListener{
            override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    binding.subscribeOneButton.visibility = View.GONE
                    binding.subscribeTwoButton.visibility = View.GONE
                    binding.subscribeThreeButton.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "You are premium user now", Toast.LENGTH_SHORT).show()

                }
            }
        })
    }

    private fun checkPurchaseOnResume() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, object : PurchasesResponseListener{
            override fun onQueryPurchasesResponse(billingResult: BillingResult, purchaseList: MutableList<Purchase>) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in purchaseList) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifySubPurchase(purchase)
                        }
                    }
                }
            }

        })
    }

    private fun checkSubscribeOnResume() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, object : PurchasesResponseListener{
            override fun onQueryPurchasesResponse(billingResult: BillingResult, purchaseList: MutableList<Purchase>) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in purchaseList) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifySubPurchase(purchase)
                        }
                    }
                }
            }

        })
    }
}