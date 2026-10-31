package com.outofthebox.biblefaithfuelbible.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID_NO_ADS = "bbf_no_ads"
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_IS_AD_FREE = "is_ad_free"
    }

    private val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isAdFree = MutableStateFlow(sp.getBoolean(KEY_IS_AD_FREE, false))
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _adFreePrice = MutableStateFlow<String?>(null)
    val adFreePrice: StateFlow<String?> = _adFreePrice.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var billingClient: BillingClient? = null

    fun initialize() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    queryProductDetails()
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_NO_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = queryProductDetailsResult.productDetailsList.firstOrNull { it.productId == PRODUCT_ID_NO_ADS }
                _productDetails.value = details
                details?.oneTimePurchaseOfferDetails?.formattedPrice?.let { price ->
                    _adFreePrice.value = price
                }
            } else {
                Log.e(TAG, "Query product details failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var foundAdFreePurchase = false
                purchaseList.forEach { purchase ->
                    if (purchase.products.contains(PRODUCT_ID_NO_ADS) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        foundAdFreePurchase = true
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    }
                }
                setAdFreeStatus(foundAdFreePurchase)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val details = _productDetails.value
        if (details == null) {
            if (billingClient?.isReady == true) {
                queryProductDetails()
            } else {
                startConnection()
            }
            _billingStatusMessage.value = "Connecting to Google Play. Please try again in a moment."
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _isLoading.value = true
        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            _isLoading.value = false
            _billingStatusMessage.value = "Could not start purchase flow: ${result?.debugMessage}"
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        _isLoading.value = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingStatusMessage.value = "Purchase cancelled."
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                setAdFreeStatus(true)
                _billingStatusMessage.value = "You already own the No-Ads upgrade! Ads are removed."
            }
            else -> {
                _billingStatusMessage.value = "Purchase status: ${billingResult.debugMessage}"
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(PRODUCT_ID_NO_ADS) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            setAdFreeStatus(true)
            _billingStatusMessage.value = "Thank you! Ads have been permanently removed."
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully.")
            }
        }
    }

    fun restorePurchases() {
        _isLoading.value = true
        if (billingClient?.isReady == true) {
            queryPurchases()
            _isLoading.value = false
            if (_isAdFree.value) {
                _billingStatusMessage.value = "Purchases restored successfully! Ads are removed."
            } else {
                _billingStatusMessage.value = "No previous No-Ads purchase found on this Google account."
            }
        } else {
            startConnection()
            _isLoading.value = false
            _billingStatusMessage.value = "Connecting to Google Play. Please try again in a moment."
        }
    }

    fun setAdFreeStatus(adFree: Boolean) {
        _isAdFree.value = adFree
        sp.edit { putBoolean(KEY_IS_AD_FREE, adFree) }
    }

    fun clearStatusMessage() {
        _billingStatusMessage.value = null
    }

    fun destroy() {
        billingClient?.endConnection()
    }
}
