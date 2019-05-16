package com.example.pos_add_on.managers

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.View.*
import android.view.Window
import android.widget.Button
import org.json.JSONObject
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.pos_add_on.interfaces.ConnectionManagerActionsInterface
import com.example.pos_add_on.interfaces.PopUpViewActionsInterface
import com.example.pos_add_on.interfaces.VirtualBeaconActionsInterface
import android.net.ConnectivityManager
import com.example.pos_add_on.GlobalKeys.Companion.Messages.AUTH_PROMPT_MESSAGE
import com.example.pos_add_on.GlobalKeys.Companion.Messages.AUTH_PROMPT_OK_BUTTON_TITLE
import com.example.pos_add_on.GlobalKeys.Companion.Messages.AUTH_PROMPT_PLACEHOLDER
import com.example.pos_add_on.GlobalKeys.Companion.Messages.AUTH_PROMPT_TITLE
import com.example.pos_add_on.GlobalKeys.Companion.UserDefaultsKeys.AUTHORIZATION_CODE_KEY
import com.example.pos_add_on.GlobalKeys.Companion.UserDefaultsKeys.RECEET_INTEGRATION_KEY
import com.example.pos_add_on.R
import com.example.pos_add_on.SharedPreference
import com.example.pos_add_on.SingletonHolder


class PosManager(private val context: Context) : ConnectionManagerActionsInterface, VirtualBeaconActionsInterface,
    PopUpViewActionsInterface {

    private val sharedPreference: SharedPreference = SharedPreference(context)
    private var connectionManager : ConnectionManager = ConnectionManager(context)
    private var virtualBeaconManager  = VirtualBeaconManager.getInstance(context)
    private var digitalOrder: JSONObject = JSONObject()
    private var timer : CountDownTimer? = null
    private var popUpDialog : Dialog? = null
    private val requestCodeBluetoothOn = 1313

    companion object : SingletonHolder<PosManager, Context>(::PosManager)

    private var isEnabled: Boolean = false
        set(value) {
            field = value
            sharedPreference.save(RECEET_INTEGRATION_KEY, isEnabled)
            if (value){
                if (sharedPreference.getValueString(AUTHORIZATION_CODE_KEY) == null)
                    showAuthCodeAlert(AUTH_PROMPT_TITLE, AUTH_PROMPT_MESSAGE)
            }}


    init {
        isEnabled = sharedPreference.getValueBoolien(RECEET_INTEGRATION_KEY,false)
        virtualBeaconManager.getVirtualBeaconActionsInterface(this)
        connectionManager.getConnectionManagerActionsInterface(this)
    }

    fun createOrder(order : JSONObject) {
        if(isNetworkAvailable()) {
            if((BluetoothManager().isBluetoothSupported) && (!BluetoothManager().isBluetoothEnabled)) {
                turnOnBluetooth(context as Activity)
            }
            else {
                digitalOrder = order
                connectionManager.connectToWebSocket()
            }
        }
        else {
            connectionManagerDidEncounterError("No internet connection",
                    "Please connect to the internet to continue")
        }
    }

    fun resetAuthKey(){
        if(isNetworkAvailable()) {
            showAuthCodeAlert(AUTH_PROMPT_TITLE, AUTH_PROMPT_MESSAGE)
        }
        else {
            connectionManagerDidEncounterError("No internet connection",
                    "Please connect to the internet to continue")
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun turnOnBluetooth(activity: Activity) {
        // request to turn on Bluetooth
        val requestBluetoothOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        // Set the Bluetooth discoverable.
        requestBluetoothOn.action = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
        // request to turn on Bluetooth
        activity.startActivityForResult(requestBluetoothOn, requestCodeBluetoothOn)
    }

    private fun showAuthCodeAlert(title: String, msg: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
        builder.setTitle(title)
        val input = EditText(context)
        input.hint = AUTH_PROMPT_PLACEHOLDER
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        builder.setView(input)
        builder.setPositiveButton(AUTH_PROMPT_OK_BUTTON_TITLE
        ) { _, _ ->
            if(input.text.toString().trim() != "")
                sharedPreference.save(AUTHORIZATION_CODE_KEY,input.text.toString())
        }
        builder.setNegativeButton("Cancel") {_,_ ->
            return@setNegativeButton
        }
        builder.show()
    }

    private fun resetPosState() {
        virtualBeaconManager.stopTransmitting()
        digitalOrder = JSONObject()
        if(popUpDialog?.isShowing!!) {
            popUpDialog?.dismiss()
        }
    }
    override fun cancelButtonPressed() {
        resetPosState()
    }

    override fun virtualBeaconManagerDidEncounterError(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setTitle(title)
        builder.setNeutralButton(
                AUTH_PROMPT_OK_BUTTON_TITLE
        ) { _, _ ->
            return@setNeutralButton
        }
        builder.show()
    }

    override fun virtualBeaconManagerDidStartAdvertising() {
        popUpDialog = Dialog(context)
        popUpDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popUpDialog?.setCancelable(false)
        popUpDialog?.setContentView(R.layout.dialog_layout)
        popUpDialog?.setOnDismissListener {
            popUpDialog = Dialog(context)
            popUpDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            popUpDialog?.setCancelable(false)
            popUpDialog?.setContentView(R.layout.dialog_layout)
            timer?.cancel()
        }
        val yesBtn = popUpDialog?.findViewById(R.id.cancel) as Button
        yesBtn.setOnClickListener {
            popUpDialog?.dismiss()
        }
        popUpDialog?.show()

    }

    override fun orderDeliveredSuccessfully(orderID: Int, receiptID: Int) {
        virtualBeaconManager.startTransmitting()
    }

    override fun orderNotDelivered() {
    }

    override fun receiptDelivered() {
        virtualBeaconManager.stopTransmitting()
        ( context as Activity).runOnUiThread {
            timer = object: CountDownTimer(10000,1000) {
                override fun onTick(millisUntilFinished: Long) {
                    popUpDialog?.findViewById<LottieAnimationView>(R.id.lottieAnimationView)?.setAnimation("success-animation.json")
                    popUpDialog?.findViewById<LottieAnimationView>(R.id.lottieAnimationView)?.repeatCount = 1
                    val x = millisUntilFinished/1000
                    popUpDialog?.findViewById<TextView>(R.id.textView2)?.visibility = GONE
                    popUpDialog?.findViewById<Button>(R.id.cancel)?.text = context.getString(R.string.next_sale_in, x)
                }

                override fun onFinish() {
                    popUpDialog?.cancel()
                }
            }
            timer?.start()
        }
    }
    override fun webSocketDidConnect() {
    }

    override fun webSocketDidDisconnect() {
        resetPosState()
    }

    override fun connectionManagerDidEncounterError(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setTitle(title)
        builder.setNeutralButton(
                AUTH_PROMPT_OK_BUTTON_TITLE
        ) { _, _ ->
            return@setNeutralButton
        }
        builder.show()
    }

    override fun webSocketDidAuthorize() {
        connectionManager.sendOrderToCloud(digitalOrder)
    }

    override fun webSocketAuthorizeFailed() {
        (context as Activity).runOnUiThread {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Please Check Your Key And Re-enter It Carefully")
            builder.setTitle("Your Key Is Wrong!")
            builder.setNeutralButton(
                    AUTH_PROMPT_OK_BUTTON_TITLE
            ) { _, _ ->
                return@setNeutralButton
            }
            builder.show()
        }
    }

    override fun posDidTimeOut() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Please send the order again")
        builder.setTitle("Something went wrong")
        builder.setNeutralButton(
                AUTH_PROMPT_OK_BUTTON_TITLE
        ) { _, _ ->
            return@setNeutralButton
        }
        builder.show()
    }

    override fun webSocketDidTimeOut() {
        resetPosState()
    }



}