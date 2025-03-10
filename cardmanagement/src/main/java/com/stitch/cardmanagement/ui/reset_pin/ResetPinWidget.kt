package com.stitch.cardmanagement.ui.reset_pin

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.stitch.cardmanagement.R
import com.stitch.cardmanagement.WidgetSDK
import com.stitch.cardmanagement.data.model.SDKData
import com.stitch.cardmanagement.data.model.SavedCardSettings
import com.stitch.cardmanagement.data.model.response.Card
import com.stitch.cardmanagement.databinding.WidgetResetPinBinding
import com.stitch.cardmanagement.ui.StitchWidget
import com.stitch.cardmanagement.utilities.Constants
import com.stitch.cardmanagement.utilities.Toast
import com.stitch.cardmanagement.utilities.Utils
import okhttp3.internal.http.HTTP_BAD_REQUEST

open class ResetPinWidget : StitchWidget() {

    private lateinit var binding: WidgetResetPinBinding
    private lateinit var savedCardSettings: SavedCardSettings

    companion object {
        lateinit var networkListener: () -> Boolean
        lateinit var progressBarListener: (isVisible: Boolean) -> Unit
        lateinit var logoutListener: (unAuth: Boolean) -> Unit
        lateinit var reFetchSessionToken: (viewType: String) -> Unit
        lateinit var onResetPinSuccess: () -> Unit
        lateinit var onResetPINError: () -> Unit

        fun newInstance(
            networkListener: () -> Boolean,
            progressBarListener: (isVisible: Boolean) -> Unit,
            logoutListener: (unAuth: Boolean) -> Unit,
            reFetchSessionToken: (viewType: String) -> Unit,
            onResetPinSuccess: () -> Unit,
            onResetPINError: () -> Unit,
        ): ResetPinWidget {
            val resetPinWidget = ResetPinWidget()
            this.networkListener = networkListener
            this.progressBarListener = progressBarListener
            this.logoutListener = logoutListener
            this.reFetchSessionToken = reFetchSessionToken
            this.onResetPinSuccess = onResetPinSuccess
            this.onResetPINError = onResetPINError
            return resetPinWidget
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedCardSettings = WidgetSDK.resetPinSettings
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.widget_reset_pin,
                container,
                false
            )
        config()
        return binding.root
    }

    private fun config() {
        binding.viewModel = viewModel
        binding.layoutOutlined.viewModel = viewModel
        binding.layoutFilled.viewModel = viewModel
        binding.layoutStandard.viewModel = viewModel

        viewModel.networkListener = networkListener
        viewModel.progressBarListener = progressBarListener
        viewModel.logoutListener = logoutListener
        viewModel.reFetchSessionToken = reFetchSessionToken

        viewModel.viewType.set(Constants.ViewType.RESET_CARD_PIN)
        if (!arguments?.getString(Constants.ParcelConstants.CARD_NUMBER).isNullOrEmpty())
            viewModel.cardNumber.set(arguments?.getString(Constants.ParcelConstants.CARD_NUMBER))
        viewModel.showCardResetPin.set(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setSDKData(
                arguments?.getParcelable(Constants.ParcelConstants.SDK_DATA, SDKData::class.java),
                savedCardSettings
            )
        } else {
            setSDKData(
                arguments?.getParcelable(Constants.ParcelConstants.SDK_DATA),
                savedCardSettings
            )
        }
        viewModel.onResetPINClick = {
            viewModel.retryCount.set(0)
            viewModel.getWidgetsSecureSessionKey(requireContext())
        }
        viewModel.onResetPINSuccess = {
            viewModel.oldPin.set("")
            viewModel.newPin.set("")
            viewModel.confirmChangePin.set("")
            Toast.success(getString(R.string.pin_change_successfully))
            onResetPinSuccess.invoke()
        }
        viewModel.onResetPINError = { errorCode, errorMessage ->
            onResetPINError.invoke()
            if (errorCode == HTTP_BAD_REQUEST && errorMessage.isNullOrEmpty())
                Toast.success(getString(R.string.invalid_old_pin_api_response))
        }
    }

    private fun setSDKData(sdkData: SDKData?, savedCardSettings: SavedCardSettings) {
        viewModel.sdkData.set(sdkData)
        viewModel.savedCardSettings.set(savedCardSettings)
        viewModel.card.set(viewModel.sdkData.get()?.card)
        viewModel.tokenType.set(viewModel.sdkData.get()?.tokenType)
        viewModel.apiToken.set(viewModel.sdkData.get()?.apiToken)
        viewModel.authToken.set(viewModel.sdkData.get()?.authToken)
        viewModel.customerNumber.set(viewModel.sdkData.get()?.customerNumber)
        viewModel.programName.set(viewModel.sdkData.get()?.programName)
        viewModel.secureToken.set(viewModel.sdkData.get()?.secureToken)
        viewModel.fingerprint.set(Utils.getDeviceFingerprint(requireContext()))
        setFormStyleProperties()
    }

    fun setCardStyle(
        cardsList: ArrayList<Card>?,
        style: String,
        savedCardSettings: SavedCardSettings
    ) {
        viewModel.card.set(cardsList?.find { card ->
            card.cardNumber == viewModel.cardNumber.get()
        })
        viewModel.sdkData.get()?.card = viewModel.card.get()
        viewModel.savedCardSettings.set(savedCardSettings)
        viewModel.oldPin.set("")
        viewModel.newPin.set("")
        binding.layoutOutlined.root.visibility = View.GONE
        binding.layoutFilled.root.visibility = View.GONE
        binding.layoutStandard.root.visibility = View.GONE
        when (style) {
            getString(R.string.style_outlined) -> {
                binding.layoutOutlined.root.visibility = View.VISIBLE
            }

            getString(R.string.style_filled) -> {
                binding.layoutFilled.root.visibility = View.VISIBLE
            }

            getString(R.string.style_standard) -> {
                binding.layoutStandard.root.visibility = View.VISIBLE
            }
        }
        setFormStyleProperties()
    }

    private fun setFormStyleProperties() {
        if (viewModel.savedCardSettings.get()?.fontFamily != null) {
            viewModel.cardStyleFontFamily.set(viewModel.savedCardSettings.get()?.fontFamily)
        } else {
            viewModel.cardStyleFontFamily.set(R.font.inter_regular)
        }
        if (viewModel.savedCardSettings.get()?.fontColor != null) {
            viewModel.cardStyleFontColor.set(viewModel.savedCardSettings.get()?.fontColor)
        } else {
            viewModel.cardStyleFontColor.set(
                ContextCompat.getColor(requireContext(), R.color.text_color)
            )
        }
        if (viewModel.savedCardSettings.get()?.buttonFontColor != null) {
            viewModel.cardStyleButtonFontColor.set(viewModel.savedCardSettings.get()?.buttonFontColor)
        } else {
            viewModel.cardStyleButtonFontColor.set(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
        if (viewModel.savedCardSettings.get()?.buttonBackgroundColor != null) {
            viewModel.cardStyleButtonBackgroundColor.set(viewModel.savedCardSettings.get()?.buttonBackgroundColor)
        } else {
            viewModel.cardStyleButtonBackgroundColor.set(
                ContextCompat.getColor(requireContext(), R.color.colorBase)
            )
        }
        if (viewModel.savedCardSettings.get()?.fontSize != null &&
            viewModel.savedCardSettings.get()?.fontSize.toString().isNotEmpty()
        ) {
            viewModel.styleFontSize.set(
                viewModel.savedCardSettings.get()?.fontSize.toString()
            )
        } else {
            viewModel.styleFontSize.set("16")
        }
        setCardData()
    }

    private fun setCardData() {
        if (viewModel.card.get() != null) {
            viewModel.showCardResetPin.set(true)
            viewModel.isCardNotActivate.set(viewModel.card.get()?.state != Constants.CardState.ACTIVATED)
            viewModel.cardNumber.set(viewModel.card.get()?.cardNumber)
        }
    }

    fun retryResetPin(sdkData: SDKData?, savedCardSettings: SavedCardSettings) {
        setSDKData(sdkData, savedCardSettings)
        viewModel.getWidgetsSecureSessionKey(requireContext())
    }
}