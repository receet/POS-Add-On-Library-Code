package com.example.posaddon

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.receet_pos_add_on.managers.PosManager
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() , View.OnClickListener {
    private lateinit var posManager: PosManager
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sendOrder -> {
                posManager.createOrder(createOrder(),callback = {qrCode ->
                    //handle the QR code here
                    Log.d("qrCodeValue",qrCode.toString())
                })
            }
            R.id.resetAuthKey -> {
                posManager.resetAuthKey()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        posManager = PosManager.getInstance(this)
        isEnabled.isChecked = posManager.isEnabled
        sendOrder.isEnabled = posManager.isEnabled
        resetAuthKey.isEnabled = posManager.isEnabled
        isEnabled.setOnCheckedChangeListener { _, isChecked ->
            posManager.isEnabled = isChecked
            resetAuthKey.isEnabled = isChecked
            sendOrder.isEnabled = isChecked
        }

    }

    private fun createOrder(): JSONObject {
        val order = JSONObject()
        order.put("media", "digital")
        order.put("languageId", 1)
        order.put("action", 4028)
        val orderDetails = JSONObject()
        orderDetails.put("externalId", "T004-126572")
        orderDetails.put("totalProduct", 1383.00)
        orderDetails.put("subTotalProduct", 1449.00)
        orderDetails.put("order_product_subtotal", 1383.00)
        orderDetails.put("description", "Test")
        orderDetails.put("adjustmentDescription", "0")
        orderDetails.put("currency", "NIS")
        orderDetails.put("timePlaced", Date())
        orderDetails.put("createdBy", "12344")
        order.put("order", orderDetails)
        val orderItemsArray = JSONArray()
        val orderItem = JSONObject()
        orderItem.put("price", 1320.00)
        orderItem.put("quantity", "1")
        orderItem.put("description", "Breville Toaster Oven BOV650")
        orderItem.put("totalProduct", "1254.00")
        orderItem.put("taxAmount", "5.00")
        orderItem.put("shipCharge", "1.00")
        orderItem.put("shipTaxAmount", "1.00")
        orderItem.put("totalAdjustment", "1.00")
        orderItem.put("adjustmentDescription", "5% Discount")
        orderItem.put("itemNumber", "17172")
        orderItemsArray.put(orderItem)
        order.put("order_items", orderItemsArray)
        return order

    }
}
