package com.example.receet_pos_add_on

class GlobalKeys {

    companion object {
        object UserDefaultsKeys {
            const val  AUTHORIZATION_CODE_KEY = "auth_code"
            const val  POS_ID_KEY_USER_DEFAULTS = "pos_id"
            const val  BEACON_ID_KEY_USER_DEFAULTS = "beacon_id"
            const val  RECEET_INTEGRATION_KEY = "is_receet_integration_on"
        }

        object DateFormats {
            const val SERVER_FORMAT = "yyyy-MM-dd HH:mm:ss.S"
        }

        object ParameterKeys {
            const val POS_ID_KEY = "posId"
            const val ORDER_DETAILS_KEY = "order"
            const val BEACON_ID_KEY = "beaconIdentifier"
            const val WEB_SOCKET_ACTION_KEY = "action"
            const val WEB_SOCKET_ACCESS_TOKEN_KEY = "access_token"
            const val ORDER_ID_KEY = "orderId"
            const val RECEIPT_ID_KEY = "receiptId"

        }

        object Messages {
            const val AUTH_PROMPT_TITLE = "Auth code required"
            const val AUTH_PROMPT_MESSAGE = "Go to www.getreceet.com and sign in to get your code"
            const val AUTH_PROMPT_PLACEHOLDER = "Enter code here"
            const val AUTH_PROMPT_OK_BUTTON_TITLE = "Ok"
        }
        object WebService {
            const val PRODUCTION_WEB_SOCKET_URL = "wss://receet.app/order-service/websocket"
            const val DEVELOPMENT_WEB_SOCKET_URL = "wss://dev.receet.app/order-service/websocket"

        }
    }
}