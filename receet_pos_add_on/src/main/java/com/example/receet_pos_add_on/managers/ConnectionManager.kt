package com.example.receet_pos_add_on.managers

import android.content.Context
import android.util.Log
import com.example.receet_pos_add_on.GlobalKeys
import com.example.receet_pos_add_on.GlobalKeys.Companion.ParameterKeys
import com.example.receet_pos_add_on.GlobalKeys.Companion.UserDefaultsKeys
import com.example.receet_pos_add_on.GlobalKeys.Companion.WebService
import com.example.receet_pos_add_on.SharedPreference
import com.example.receet_pos_add_on.interfaces.ConnectionManagerActionsInterface
import com.example.receet_pos_add_on.SingletonHolder
import com.example.receet_pos_add_on.managers.ConnectionManager.WebSocketResponses.*
import com.example.receet_pos_add_on.managers.ConnectionManager.WebSocketActions.*
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ConnectionManager(context: Context): WebSocketListener() {


    companion object : SingletonHolder<ConnectionManager, Context>(::ConnectionManager)


        private var socket: WebSocket? = null
        private val normalClosureStatus = 1000
    private lateinit var connectionManagerActionsInterface : ConnectionManagerActionsInterface

    private val sharedPreference: SharedPreference = SharedPreference(context)
    private val dateFormat = GlobalKeys.Companion.DateFormats.SERVER_FORMAT //the date format which Receet POS Manager accepts

    fun getConnectionManagerActionsInterface( connectionManagerActionsInterface : ConnectionManagerActionsInterface){
        this.connectionManagerActionsInterface   = connectionManagerActionsInterface
    }
    fun connectToWebSocket() {
        val client = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .url(WebService.DEVELOPMENT_WEB_SOCKET_URL)
            .build()
        val wsListener = this
        socket = client.newWebSocket(request, wsListener)
    }

    fun sendOrderToCloud(order: JSONObject) {
        val orderDetails = order[ParameterKeys.ORDER_DETAILS_KEY] as? JSONObject
        val outputFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val outputStringDate = outputFormatter.format(orderDetails?.opt("timePlaced"))
        orderDetails?.put("timePlaced",outputStringDate)
        orderDetails?.put(ParameterKeys.POS_ID_KEY, sharedPreference.getValueString(UserDefaultsKeys.POS_ID_KEY_USER_DEFAULTS))
        order.put("order", orderDetails)
        socket?.send(order.toString())
        Log.d("lolo",order.toString())
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        connectionManagerActionsInterface.webSocketDidConnect()
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        Log.d("Receiving : ", text!!)
        val receivedJSONObject = JSONObject(text)
        when (receivedJSONObject.optInt(ParameterKeys.WEB_SOCKET_ACTION_KEY)) {
            AuthorizationRequired.code -> {
                val jsonObject = JSONObject()
                jsonObject.put(ParameterKeys.WEB_SOCKET_ACTION_KEY, SendAuthorizationToken.code)
                jsonObject.put(ParameterKeys.WEB_SOCKET_ACCESS_TOKEN_KEY, sharedPreference.getValueString(UserDefaultsKeys.AUTHORIZATION_CODE_KEY))
                webSocket?.send(jsonObject.toString())
            }
            ReceiptDelivered.code  -> {
                connectionManagerActionsInterface.receiptDelivered()
                webSocket?.close(normalClosureStatus,"finished")
            }
            OrderCreatedSuccessfully.code -> {
                connectionManagerActionsInterface.orderDeliveredSuccessfully(receivedJSONObject.optInt(ParameterKeys.ORDER_ID_KEY), receivedJSONObject.optInt(ParameterKeys.RECEIPT_ID_KEY), receivedJSONObject.optString("genericReceiptId"))
            }
            AuthorizationSuccessful.code -> {
                sharedPreference.save(UserDefaultsKeys.BEACON_ID_KEY_USER_DEFAULTS, receivedJSONObject.optString(ParameterKeys.BEACON_ID_KEY))
                sharedPreference.save(UserDefaultsKeys.POS_ID_KEY_USER_DEFAULTS, receivedJSONObject.optString(ParameterKeys.POS_ID_KEY))
                connectionManagerActionsInterface.webSocketDidAuthorize()
            }
            AuthorizationFailed.code -> {
                connectionManagerActionsInterface.webSocketAuthorizeFailed()
                 webSocket?.close(normalClosureStatus,"finished")
            }
            UnkownAction.code  -> TODO()
            TimeOut.code  -> {
                connectionManagerActionsInterface.webSocketDidTimeOut()
                webSocket?.close(normalClosureStatus,"finished")
            }
            OrderCreationError.code  ->  connectionManagerActionsInterface.connectionManagerDidEncounterError( "", "")
        }
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        Log.d("Receiving bytes : ", bytes!!.hex())
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        Log.d("Closing" , code.toString() + reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d("Closing" ,code.toString() + reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d("Error : ", t.message + "")

    }


    enum class WebSocketResponses(val code: Int) {
        AuthorizationRequired(4021),
        AuthorizationFailed(4022),
        AuthorizationSuccessful(4023),
        UnkownAction(4024),
        ReceiptDelivered(4025),
        TimeOut(4026),
        OrderCreatedSuccessfully(4027),
        OrderCreationError(4029)

    }

    enum class WebSocketActions(val code: Int) {
        CloseConnection(4019),
        SendAuthorizationToken(4020),
        CreateOrder(4028)
    }
}