# Secure card Android SDK

The Stitch Secure Widget Android SDK  is the mobile gateway to the Card solution. The SDK enables your mobile app to:

- Enabling secure viewing of card data
- Setting and changing (resetting) card PINs
- Widget settings for customization
- Simplify interacting with the Stitch API by generating device fingerprints

# SDK installation and setup

## Obtain the SDK

Contact the Stitch support team to receive the StitchSecureWidget Android SDK artifact (e.g., `.aar` file) or Maven/Gradle coordinates.

## Add the SDK to your project

1. Place the `.aar` file in your `libs/` directory.
2. In your module’s `build.gradle`, add:

```kotlin
dependencies {
    implementation 'fi.stitch:stitch-secure-widget-android:<version>'
}
```

3. To use JitPack with private repositories, authorize JitPack and obtain your personal access token. Then, add the token to `$HOME/.gradle/gradle.properties`:

```toml
authToken=AUTHENTICATION_TOKEN
```

Reference the token in your `settings.gradle`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Other repositories...
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
}
```

If required by your organization’s settings, approve the JitPack Application on GitHub.

# Initialization and configuration

## SDK environment configuration

During onboarding, you receive your client credentials and environment-specific base URLs. Configure the SDK by setting the base URL in your Application before using the SDK.

```kotlin
import com.stitch.stitchwidgets.WidgetSDK

class StitchApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure the base URL for the Stitch Widget
        WidgetSDK.baseUrl = "https://base_url.com"
    }
}
```

## Secure token acquisition

All widget functionalities (View Card, Set PIN, Change/Reset PIN) require a secure token, which you retrieve by calling Stitch’s secure token endpoint.

1. **Endpoint:** `widgets/secure/token`
2. **Required Parameters:**

- **device_fingerprint:** A unique identifier for the device, typically derived by combining device attributes (model name, OS version, etc.) and hashing them.

```kotlin
import com.stitch.stitchwidgets.utilities.Utils

Utils.deviceFingerprint(context)
```

- **card_id**: Stitch generated ID for the card.
- **customer_id**: stitch generated id for the customer.
- **page_id**: ex: `VIEW_CARD` (other possible values `SET_PIN`, `RESET_PIN`).

3. **Example cURL:**

```shell
curl --location 'https://<base_url>/widgets/secure/token' \
--header 'x-correlation-id: 70c8b0de-252c-4add-88c6-5de2451884a0' \
--header 'Content-Type: application/json' \
--data '{
    "customer_id": 2139629197364346880,
    "card_id": 2144815671544213504,
    "deviceFingerprint": "6f908594b5b8ec6c1f33f5ac28e2450d",
    "page_id": "VIEW_CARD"
}'
```

### Response Details:

- Success (HTTP 200)  
  The API returns a secure token along with a status of `SUCCESS`. The secure token is a one-time token valid for 3 minutes (180 seconds).

```json
{
  "token": "JDJhJDEyJE94bGdwalpWR1BWZVUzYi9EQzJSR09QLjlkaHc0dG9LOURkeEFYZ2ZQaHhpOUhjcnpjaE1T",
  "status": "SUCCESS"
}
```

### Error Responses:

- Customer Not Found (HTTP 404)  
  If the provided customer_id is incorrect or not found, you receive:

```json
{
  "responseStatus": {
    "constant": "CUSTOMER_NOT_FOUND"
  }
}
```

- Card Not Found (HTTP 404)  
  If the provided card_id is incorrect or not found, you receive:

```json
{
  "responseStatus": {
    "constant": "CARD_NOT_FOUND"
  }
}
```

### Token Expiry:

The generated secure token is a one-time token with a validity period of 3 minutes (180 seconds). Be sure to use it within this timeframe to authorize your widget operations.

Once obtained, pass the secure token to the Stitch widgets to authorize actions such as viewing card details or setting/changing a PIN.

# Card management

## Viewing card details

Use the `CardWidget` to securely display a card’s details (masked card number, expiry date, and CVV).

1. Initialize the widget using the provided `newInstance` method. The widget requires listeners for network events, progress updates, logout events, and secure token handling.

```kotlin
import com.stitch.app.ui.MainActivity
import com.stitch.cardmanagement.ui.view_card.CardWidget

val cardWidget = CardWidget.newInstance(
    networkListener = MainActivity.networkListener, // Handles network-related callbacks
    progressBarListener = { isVisible ->
        MainActivity.progressbarVisible(isVisible)  // Shows or hides the progress indicator
    },
    logoutListener = MainActivity.logoutListener,

    secureToken = args.secureToken // Pass the secure token received from your backend
)
```

2. Pass data to the widget using a `Bundle` to pass any required data (including the secure token) to the widget fragment.

```kotlin
import com.stitch.app.utilities.Constants

val bundle = Bundle().apply {
    putParcelable(
        Constants.ParcelConstants.SDK_DATA,
        getBundleData(args.secureToken)
    )
}
cardWidget.arguments = bundle
```

3. Display the fragment embedding the `CardWidget` within your UI by replacing the appropriate container in your layout.

```kotlin
childFragmentManager.beginTransaction().apply {
    replace(R.id.flDemoCardSDKFragment, cardWidget) // Replace the container with the CardWidget fragment
    commitAllowingStateLoss()
}
```

4. Configure widget settings to customize the widget’s appearance and behavior (masking, fonts, colors, etc.).

```kotlin
import com.stitch.cardmanagement.WidgetSDK
import com.stitch.cardmanagement.data.model.SavedCardSettings

val savedCardSettings = SavedCardSettings().apply {
    widgetType = "VIEW_CARD" // Widget type identifier
    fontFamily = R.font.inconsolata_semi_bold // Font resource (ensure it's bundled in your project)
    fontColor = R.color.white // Font color
    fontSize = 16 // Example font size (adjust as needed)
    cardNumberPaddingTop = 0 // Padding for the card number at the top
    maskCardNumber = true // Enable masking of the card number
    maskCvv = true // Enable masking of the CVV
    showEyeIcon = false  // Hide the "eye" button if not needed
}

// Apply these settings to the widget SDK
WidgetSDK.viewCardSettings = savedCardSettings
```

Widget Settings Properties for View Card may include:

| Field                   | Data type | Description                                                                                                      |
| :---------------------- | :-------- | :--------------------------------------------------------------------------------------------------------------- |
| widgetType              | String    | Specifies the widget type (e.g., `VIEW_CARD`, `SET_PIN`, `CHANGE_PIN`).                                          |
| fontSize                | Int       | Defines the font size for the card widget.                                                                       |
| fontColor               | Int       | Defines the font color for the card widget.                                                                      |
| fontFamily              | Int       | Defines the font family for the card widget, limited to custom fonts available in the SDK                        |
| background              | Any       | Defines the background color of the card widget.                                                                 |
| backgroundImage         | File      | Defines the background image of the card widget.                                                                 |
| cvvPaddingTop           | Int       | Defines the top padding for the CVV field in the card widget.                                                    |
| cvvPaddingRight         | Int       | Defines the right padding for the CVV field in the card widget.                                                  |
| cvvPaddingBottom        | Int       | Defines the bottom padding for the CVV field in the card widget.                                                 |
| cvvPaddingLeft          | Int       | Defines the left padding for the CVV field in the card widget.                                                   |
| expiryPaddingTop        | Int       | Defines the top padding for the expiry date field in the card widget.                                            |
| expiryPaddingRight      | Int       | Defines the right padding for the expiry date field in the card widget.                                          |
| expiryPaddingBottom     | Int       | Defines the bottom padding for the expiry date field in the card widget.                                         |
| expiryPaddingLeft       | Int       | Defines the left padding for the expiry date field in the card widget.                                           |
| cardNumberPaddingTop    | Int       | Defines the top padding for the card number field in the card widget.                                            |
| cardNumberPaddingRight  | Int       | Defines the right padding for the card number field in the card widget.                                          |
| cardNumberPaddingBottom | Int       | Defines the bottom padding for the card number field in the card widget.                                         |
| cardNumberPaddingLeft   | Int       | Defines the left padding for the card number field in the card widget.                                           |
| maskCvv                 | Boolean   | Determines whether the CVV field should be masked (hidden) for security purposes in the card widget.             |
| maskCardNumber          | Boolean   | Determines whether the card number should be masked (partially hidden) for security purposes in the card widget. |
| showEyeIcon             | Boolean   | Determines whether an eye button should be displayed to toggle the visibility of masked text in the card widget  |
| buttonFontColor         | Int       | Defines the font color of the widget's button.                                                                   |
| buttonBackground        | Int       | Defines the background color of the widget's button.                                                             |
| textFieldVariant        | String    | Defines the style for the text fields.(`Outlined`, `Standard`, `Filled`)                                         |

## Setting a card PIN

Use the `SetPinWidget` to set a PIN for the card.

1. Initialize the widget.

```kotlin
import com.stitch.app.ui.MainActivity
import com.stitch.cardmanagement.ui.set_pin.SetPinWidget

val setPinFragment = SetPinWidget.newInstance(
    networkListener = MainActivity.networkListener, // Handles network callbacks
    progressBarListener = { isVisible ->
        MainActivity.progressbarVisible(isVisible) // Shows or hides the progress bar
    },
    logoutListener = MainActivity.logoutListener, // Handles logout events if needed
    onSetPinSuccess = {
		    // Navigate up on successful PIN set
        findNavController().navigateUp()
    }
)
```

2. Pass data to the widget using a `Bundle` to pass any required data (including the secure token) to the widget fragment.

```kotlin
import com.stitch.app.utilities.Constants

val bundle = Bundle()
bundle.putParcelable(
    Constants.ParcelConstants.SDK_DATA,
    getBundleData(secureToken)
)
fragment.arguments = bundle
```

3. Display the fragment embedding the `SetPinWidget` within your UI by replacing the appropriate container in your layout.

```kotlin
parentFragmentManager.beginTransaction().apply {
    replace(R.id.flDemoSDKFragment, setPinFragment)
    commitAllowingStateLoss()
}
```

4. Configure widget settings.

## Changing (resetting) the card PIN

Use the `ResetPinWidget` to allow users to change or reset the PIN for an existing card.

1. Initialize the widget.

```kotlin
import com.stitch.app.ui.MainActivity
import com.stitch.cardmanagement.ui.reset_pin.ResetPinWidget

val resetPinFragment = ResetPinWidget.newInstance(
    networkListener = MainActivity.networkListener,
    progressBarListener = { isVisible ->
        MainActivity.progressbarVisible(isVisible)
    },
    logoutListener = MainActivity.logoutListener,
    onSetPinSuccess = {
        findNavController().navigateUp()
    }
)
```

2. Pass data to the widget using a `Bundle` to pass any required data (including the secure token) to the widget fragment.

```kotlin
import com.stitch.app.utilities.Constants

val bundle = Bundle().apply {
    putParcelable(
        Constants.ParcelConstants.SDK_DATA,
        getBundleData(secureToken)
    )
}
resetPinFragment.arguments = bundle
```

3. Display the fragment embedding the `ResetPinWidget` within your UI by replacing the appropriate container in your layout.

```kotlin
parentFragmentManager.beginTransaction().apply {
    replace(R.id.flDemoSDKFragment, resetPinFragment)
    commitAllowingStateLoss()
}
```

4. Configure widget settings.

# Error handling and security considerations

Stitch’s SDK enforces security checks for rooted devices. If the environment is deemed insecure, the SDK immediately throws an error and prevents further usage. You can implement a custom exception to handle this scenario:

1. Create a custom exception

```kotlin
class CardSDKException(
    message: String,
    val errorCode: Int
) : Exception(message) {

    companion object {
        const val INSECURE_ENVIRONMENT = 1001
        const val INSECURE_ENVIRONMENT_MESSAGE = "Insecure environment detected. Please use a secure device."
    }
}

```

2. Throw the exception if the device is rooted

```kotlin
try {
    if (isDeviceRooted) {
        throw CardSDKException(
            CardSDKException.INSECURE_ENVIRONMENT_MESSAGE,
            CardSDKException.INSECURE_ENVIRONMENT
        )
    }
} catch (e: CardSDKException) {
    e.printStackTrace()
    Toast.makeText(
        context,
        CardSDKException.INSECURE_ENVIRONMENT_MESSAGE,
        Toast.LENGTH_SHORT
    ).show()
}

```

Ensure you provide clear messaging to users about using secure devices and avoiding rooted environments.

# Conclusion

By following these steps, you have:

- Integrated the Stitch Secure Widget Android SDK into your application.
- Configured device fingerprinting and secure token retrieval.
- Enabled card management features (view card details, set a PIN, change/reset a PIN) through Stitch widgets.
- Prepared for secure error handling, including the detection of insecure environments.

For additional help or inquiries, contact the Stitch support team. We can assist with advanced customizations, troubleshooting, or best practices for maintaining a secure card experience in your app.
