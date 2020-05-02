# POS-Add-On-Library-Code

ReceetPOSAddOn is an easy to integrate with add-on which can be installed on any Point of Sale system in the world that runs on Android and gives it the ability to send digital receipts to customers with minimum integration efforts.

## Installation

### Quickstart
Add Gradle dependency into your app's build.config
```
 dependencies {
    implementation 'com.receet:receet_pos_add_on:1.0.5'
}
```
## Getting Started
1- Define a variable of PosManager

```kotlin
 private lateinit var posManager: PosManager
``` 
2- You can turn on or turn off the integration by calling the following methods

```kotlin
posManager.isEnabled = true // to turn it on 
posManager.isEnabled = false // to turn it off
```

You can always check the status of Receet Point of Sale manager using 'isEnabled' flag

You can reset your Authorization Code by using the method of PosManager 'resetAuthKey()' like this
```kotlin
posManager.resetAuthKey()
```
## Example Project
You can check the example project in this repository.

## Step-by-step guide to turn on Receet Point of Sale integration
1- to obtain your authorization code contact hello@getreceet.com

2- you turn on the integration by calling the following method
```kotlin
posManager.isEnabled = true// to turn it on
```
3- you are ready to send digital receipts.

## Step-by-step guide to send digital orders
To send a digital receipt you prepare the purchase order as the following format, you can add any supported fields you want. Check the supported fields in the tables below:
1- you prepare the order details with the following parameters as a dictionary :

```kotlin
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
```

| Parameter Name  | Type | Description | Required |
| ------------- | ------------- | ------------- | ------------- |
| externalId  | String  | POS Generated Order ID  | Yes  |
| totalProduct  | Double  | Order Total amount  | Yes  |
| totalTax  | Double  | Order Tax amount  | No  |
| totalShipping  | Double | Total shipping amount  | No  |
| totalTaxShipping  | Double  | Shipping tax amount  | No  |
| subTotalProduct  | Double  | Sub-total amount (before tax and discount)  | Yes |
| totalAdjustment  | Double  | Total amount of discounts applied on order  | No  |
| adjustmentDescription  | String  | Order Level Discout description (e.g. 10% Discount) | No |
| description  | String  | Short description of order, if needed  | No |
| currency  | String  | Currency used (USD, ILS, etc....) see [supported currencies](#Supported-currencies) section for more info| Yes |
| timePlaced  | Date | Time and date of when the order was placed | Yes |
| createdBy  | String  | Identifier for the cashier (name or ID)  | No |
| topTextArea  | String  | Free text area shows at the top of the receipt. Can take multiple values (pipe separated, example below). Each value show on a new line  | No |
| bottomTextArea  | String  | value pair will show at the bottom of the receipt. Can take multiple values (pipe separated, example below). Each value show on a new line  | No |


2- you prepare the order items as an array of the purchased items as an array of dictionaries where each dictionary represents an item: 

```kotlin
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
```
| Parameter Name  | Type | Description | Required |
| ------------- | ------------- | ------------- | ------------- |
| price  | Double  | List item price  | Yes  |
| quantity  | Int  | Quantity of list item | Yes  |
| description  | String  | List item product description, will be shown on digital receipt | Yes  |
| totalProduct  | Double | Total product price (Price X Qty)  | Yes  |
| taxAmount  | Double  | Tax Amount  | No  |
| shipCharge  | Double  | Amount of shipping, if any  | No |
| shipTaxAmount  | Double  | Tax Amount of shipping, if any  | No  |
| totalAdjustment  | Double  | Discount amount of list item | No |
| adjustmentDescription  | String  | Item Level Discout description (e.g. 10% Discount)  | No |
| itemNumber  | String  | Item Number, barcode, etc.. | No |


3- you put all this informatin in a new dictionary in the following format with orderDetails,orderItems ready from the previous section
```kotlin
        val order = JSONObject()
        order.put("media", "digital")
        order.put("languageId", 1)
        order.put("action", 4028)
        order.put("order", orderDetails)
        order.put("order_items", orderItemsArray)
```

| Parameter Name  | Type | Description | Required |
| ------------- | ------------- | ------------- | ------------- |
| media  | String  | The type of the receipt media. Values: Digital, DigitalAndPaper, Paper	  | Yes |
| languageId  | Int  | Language used. 1: English 2: Arabic  | Yes |
| order object (details above)  | Dictionary  | Contains order details  | Yes |
| billing_address ojbect (details below)  | Dictionary | Customer Address Information, will be shown in Billed To section on the receipt  | No |
| order_items object (details above)  | array of item object dictionary  | Array of order items  | Yes |

4- you send this dictionary to the Receet Point of Sale manager 

```kotlin
 // send the digital order To Receet POS and a callback to be exectued once the order is completed with the qr code bitmap
  posManager.createOrder(order,callback = {qrCode -> 
     //handle the QR code here
     Log.d("qrCodeValue",qrCode.toString())
   })
```


### Custom Text

topTextArea show on the top of the receipt. Example:

```
"topTextArea" : "Cash transaction"
```
bottomTextArea show at the bottom of the receipt, below Total. Example:
```
"bottomTextArea": "Paid: $ 100 | Change: $ 60.02 | Points from this sale: 20 | Total points: 100"
```

![](https://www.getreceet.com/docs/images/top-bottom-text-area.png)


### Billing Address Object (Optional)

| Parameter Name  | Type | Description | Required |
| ------------- | ------------- | ------------- | ------------- |
|addressType | String | U: User | Yes |
|memberId	| String |Loyalty Customer Member ID	|No|
|status	| String | Status of address. Value: A |Yes|
|isPrimary| Int	|Value: 1	|Yes|
|address1	| String |Address line 1	|Yes|
|address2	|String| Address line 2|	|No|
|address3	|String|Address line 3	|No|
|city	|String|City	|Yes|
|state	|String|State	|Yes|
|country	|String|Country	|Yes|
|zipCode	|String|Zip Code	|Yes|
|phone1	|String|Phone number 1	|Yes|
|phone2	|String|Phone number 2	|No|
|fax	|String|Fax Number	|No|
|email	|String|Email	|No|


## Supported currencies
The following currencies are supported in Receet system, send the symbol of the wanted currency with the order dictionary as described above.

|Currency|	Currency Symbol	 |Currency full name 
| ------------- | ------------- | ------------- |
|AFN|	؋	|Afghani|
|ALL|	Lek	|Lek|
|ANG|	ƒ	|Netherlands Antillean guilder|
|ARS|	$	|Argentine Peso|
|AUD|	$	|Australian Dollar|
|AWG|	ƒ	|Aruban Florin|
|AZN|	₼	|Azerbaijan Manat|
|BAM|	KM	|Convertible Mark|
|BBD|	$	|Barbados Dollar|
|BGN|	лв	|Bulgarian Lev|
|BMD|	$	|Bermudian Dollar|
|BND|	$	|Brunei Dollar|
|BOB|	$b	|boliviano|
|BRL|	R$	|Brazilian Real|
|BSD|	$	|Bahamian Dollar|
|BTC|	Ƀ	|Bitcoin|
|BWP|	P	|Pula|
|BYN|	Br	|Belarusian Ruble|
|BZD|	BZ$	|Belize Dollar|
|CAD|	$	|Canadian Dollar|
|CHF|	CHF	|Swiss Franc|
|CLP|	$	|Chilean Peso|
|CNY|	¥	|Yuan Renminbi|
|COP|	$	|Colombian Peso|
|CRC|	₡	|Costa Rican Colon|
|CUP|	₱	|Cuban Peso|
|CZK|	Kč	|Czech koruna (pl. koruny)|
|DKK|	kr	|Danish Krone|
|DOP|	RD$	|Dominican peso|
|EGP|	£	|Egyptian Pound|
|EUR|	€	|Euro|
|FJD|	$	|Fiji Dollar|
|FKP|	£	|Falkland Islands pound|
|GBP|	£	|pound sterling|
|GHS|	¢	|Ghana Cedi|
|GIP|	£	|Gibraltar Pound|
|GTQ|	Q	|Quetzal|
|GYD|	$	|Guyana Dollar|
|HKD|	$	|Hong Kong Dollar|
|HNL|	L	|Lempira|
|HRK|	kn	|Kuna|
|HUF|	Ft	|Forint|
|IDR|	Rp	|Rupiah|
|ILS|	₪	|New Israeli Sheqel|
|INR|	₹	|Indian Rupee|
|IRR|	﷼	|Iranian rial|
|ISK|	kr	|Iceland Krona|
|JMD|	J$	|Jamaican Dollar|
|JPY|	¥	|Yen|
|KGS|	лв	|Som|
|KHR|	៛	|Riel|
|KPW|	₩	|North Korean won (inv.)|
|KRW|	₩	|South Korean won (inv.)|
|KYD|	$	|Cayman Islands dollar|
|KZT|	лв	|Tenge|
|LAK|	₭	|kip (inv.)|
|LBP|	£	|Lebanese Pound|
|LKR|	₨	|Sri Lanka Rupee|
|LRD|	$	|Liberian Dollar|
|MKD|	ден	|denar (inv.)|
|MNT|	₮	|Tugrik|
|MUR|	₨	|Mauritius Rupee|
|MXN|	$	|Mexican Peso|
|MYR|	RM	|Malaysian Ringgit|
|MZN|	MT	|Mozambique Metical|
|NAD|	$	|Namibia Dollar|
|NGN|	₦	|Naira|
|NIO|	C$	|Cordoba Oro|
|NOK|	kr	|Norwegian Krone|
|NPR|	₨	|Nepalese Rupee|
|NZD|	$	|New Zealand dollar|
|OMR|	﷼	|Rial Omani|
|PAB|	B/.	|Balboa|
|PEN|	S/.	|Sol|
|PHP|	₱	|Philippine peso|
|PKR|	₨	|Pakistan Rupee|
|PLN|	zł	|Zloty|
|PYG|	Gs	|Guarani|
|QAR|	﷼	|Qatari Rial|
|RON|	lei	|Romanian Leu|
|RSD|	Дин.|Serbian Dinar|
|RUB|	₽	|rouble|
|SAR|	﷼	|Saudi Riyal|
|SBD|	$	|Solomon Islands Dollar|
|SCR|	₨	|Seychelles Rupee|
|SEK|	kr	|Swedish Krona|
|SGD|	$	|Singapore Dollar|
|SHP|	£	|Saint Helena pound|
|SOS|	S	|Somali Shilling|
|SRD|	$	|Surinam Dollar|
|SVC|	$	|El Salvador Colon|
|SYP|	£	|Syrian pound|
|THB|	฿	|Baht|
|TRY|	₺	|Turkish Lira|
|TTD|	TT$	|Trinidad and Tobago Dollar|
|TWD|	NT$	|new Taiwan dollar|
|UAH|	₴	|Hryvnia|
|USD|	$	|US Dollar|
|UYU|	$U	|Peso Uruguayo|
|UZS|	лв	|Uzbekistan Sum|
|VND|	₫	|dong|
|XCD|	$	|East Caribbean Dollar|
|YER|	﷼	|Yemeni Rial|
|ZAR|	R	|Rand|
|NIS|	₪	|Israeli shekel|



## Built With
* [OkHttp](https://github.com/square/okhttp) An HTTP & HTTP/2 client for Android and Java applications.
* [Java WebSockets](https://github.com/TooTallNate/Java-WebSocket) - The web socket library used
* [lottie-android](https://github.com/airbnb/lottie-android) - The animation library

## Authors

* **Walaa Abd AlAziz** - *Initial work* - [Receet](https://github.com/receet)
