/*
 * gcUnicorn
 * Copyright (C) 2023  Martin Misiarz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cz.babi.gcunicorn.android.ui.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import com.google.android.libraries.places.api.Places
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants
import com.sucho.placepicker.MapType
import com.sucho.placepicker.PlacePicker
import cz.babi.gcunicorn.android.BuildConfig
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.`fun`.*
import cz.babi.gcunicorn.android.databinding.ActivitySearchBinding
import cz.babi.gcunicorn.android.preference.PreferenceKey
import cz.babi.gcunicorn.android.service.UnicornService
import cz.babi.gcunicorn.android.storage.useNewStorageApi
import cz.babi.gcunicorn.android.ui.dialog.Error
import cz.babi.gcunicorn.android.ui.dialog.LocationAcquiringDialogFragment
import cz.babi.gcunicorn.android.ui.dialog.OnError
import cz.babi.gcunicorn.android.ui.dialog.OnLocationAcquired
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheType
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location
import java.security.SecureRandom


/**
 * Search activity.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.1
 * @since 1.0.0
 */
class SearchActivity : BaseAppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val TAG = "SEARCH_ACTIVITY"
    }

    private lateinit var binding: ActivitySearchBinding

    private val maxCountEditText: AppCompatEditText by lazy {
        binding.layoutSearch.searchMaxCount
    }

    private val maxCountLayoutWrapper: TextInputLayout by lazy {
        binding.layoutSearch.searchMaxCountWrapper
    }

    private val latitudeEditText: TextInputEditText by lazy {
        binding.layoutSearch.searchLat
    }

    private val latitudeLayoutWrapper: TextInputLayout by lazy {
        binding.layoutSearch.searchLatWrapper
    }

    private val longitudeEditText: AppCompatEditText by lazy {
        binding.layoutSearch.searchLon
    }

    private val longitudeLayoutWrapper: TextInputLayout by lazy {
        binding.layoutSearch.searchLonWrapper
    }

    private val maxCountTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidMaxCount()) {
                        maxCountLayoutWrapper.isErrorEnabled = true
                        maxCountLayoutWrapper.error = resources.getString(R.string.validator_error_max_count)
                    } else {
                        maxCountLayoutWrapper.error = null
                        maxCountLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val latitudeTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidLatitude()) {
                        latitudeLayoutWrapper.isErrorEnabled = true
                        latitudeLayoutWrapper.error = resources.getString(R.string.validator_error_latitude)
                    } else {
                        latitudeLayoutWrapper.error = null
                        latitudeLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val longitudeTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidLongitude()) {
                        longitudeLayoutWrapper.isErrorEnabled = true
                        longitudeLayoutWrapper.error = resources.getString(R.string.validator_error_longitude)
                    } else {
                        longitudeLayoutWrapper.error = null
                        longitudeLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationAcquiringDialog: LocationAcquiringDialogFragment by lazy {
        LocationAcquiringDialogFragment().apply {
            onLocationAcquired = object : OnLocationAcquired {
                override fun onLocationAcquired(location: android.location.Location) {
                    latitudeEditText.text = location.latitude.toEditable()
                    longitudeEditText.text = location.longitude.toEditable()
                }
            }

            onError = object : OnError {
                override fun onError(error: Error) {
                    val message = when (error) {
                        Error.NO_COARSE_PERMISSION -> R.string.text_no_permission_coarse_location
                        Error.NO_FINE_PERMISSION -> R.string.text_no_permission_fine_location
                        Error.NO_PROVIDER_AVAILABLE -> R.string.text_no_location_provider
                        Error.INTERNAL_ERROR -> R.string.text_can_not_obtain_location
                    }

                    rootView.snack(message)
                }
            }
        }
    }

    private val notificationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra(UnicornService.NOTIFICATION_MESSAGE)?.let {
                rootView.snack(it)
            }
        }
    }

    private val notificationIntentFilter by lazy {
        IntentFilter(ACTION_NOTIFY)
    }

    override fun onAfterCreate(savedInstanceState: Bundle?) {
        bindView()
        prepareNotificationChannel()
        checkFirstRun()
        setDefaultValuesFromLocus()
    }

    override fun onResume() {
        super.onResume()

        latitudeEditText.addTextChangedListener(latitudeTextWatcherListener)
        longitudeEditText.addTextChangedListener(longitudeTextWatcherListener)
        maxCountEditText.addTextChangedListener(maxCountTextWatcherListener)

        localBroadcastManager.registerReceiver(notificationBroadcastReceiver, notificationIntentFilter)

        validateInputs()
    }

    override fun onPause() {
        super.onPause()

        latitudeEditText.removeTextChangedListener(latitudeTextWatcherListener)
        longitudeEditText.removeTextChangedListener(longitudeTextWatcherListener)
        maxCountEditText.removeTextChangedListener(maxCountTextWatcherListener)

        localBroadcastManager.unregisterReceiver(notificationBroadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ACTIVITY_RESULT_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getParcelableExtra<AddressData>(Constants.ADDRESS_INTENT)?.let {
                        latitudeEditText.text = it.latitude.toEditable()
                        longitudeEditText.text = it.longitude.toEditable()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_WAKE_LOCK, PERMISSION_READ_EXTERNAL_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    buildNeutralDialog(R.string.dialog_no_required_permission_title, R.string.dialog_no_required_permission_message, R.string.text_bye_bye) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        finish()
                    }.show()
                }
            }
        }
    }

    /**
     * Binds view to this activity and set-up widgets.
     */
    private fun bindView() {
        binding = ActivitySearchBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.searchToolbar)

        intent?.let {
            if (it.action == LocusConst.INTENT_ITEM_POINT_TOOLS || it.action == LocusConst.INTENT_ITEM_MAIN_FUNCTION_GC) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }

        // Fill the spinner with available cache types.
        binding.layoutSearch.searchCacheType.adapter = ArrayAdapter(this, R.layout.item_spinner, CacheType.values().map { it.pattern })
                .apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

        binding.layoutSearch.searchGps.setOnClickListener {
            acquireLocationFromGpsOrNetwork()
        }

        binding.layoutSearch.searchMap.setOnClickListener {
            acquireLocationFromMaps()
        }

        binding.layoutSearch.searchDownload.setOnClickListener {
            startDownloadProcess()
        }
    }

    /**
     * It is necessary for newer version of Android to set-up notification channel.
     */
    private fun prepareNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.notification_channel_desc)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether the application runs for the first time. If so, it creates secret key property.
     *
     * It also checks whether the application has necessary permissions.
     *
     * @see [cz.babi.gcunicorn.android.security.Security]
     */
    private fun checkFirstRun() {
        // Check if we already have secure key generated.
        if (!privateSharedPreferences.contains(PreferenceKey.SECURE_KEY.key)) {
            val key = ByteArray(16)
            SecureRandom().nextBytes(key)

            privateSharedPreferences
                    .edit()
                    .putString(PreferenceKey.SECURE_KEY.key, Base64.encodeToString(key, Base64.DEFAULT))
                    .apply()

            key.fill(0)
        }

        // Request write permission for SDK < 21.
        // For SDK >= 21 we will store data in the application's folder.
        if (!useNewStorageApi()) {
            if (!this.hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_EXTERNAL_STORAGE)
            }
        }

        // Request wake lock permission.
        if (!this.hasPermissions(Manifest.permission.WAKE_LOCK)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WAKE_LOCK), PERMISSION_WAKE_LOCK)
        }
    }

    /**
     * Checks whether the activity has been started from Locus. If so, it will try to obtain location passed from Locus and fills text widgets.
     */
    private fun setDefaultValuesFromLocus() {
        if (LocusUtils.isLocusAvailable(this)) {
            // Obtain location from Locus.
            when {
                IntentHelper.isIntentMainFunctionGc(intent) -> obtainLocusLocationFromIntent(intent)
                IntentHelper.isIntentPointTools(intent) -> IntentHelper.getPointFromIntent(this, intent)?.location
                else -> null
            }?.also {
                latitudeEditText.text = it.latitude.toEditable()
                longitudeEditText.text = it.longitude.toEditable()
            }
        }
    }

    /**
     * Activity could be started from different Locus locations. Therefore, we need to obtain location coming from different [Intent]s a different way.
     * @param intent [Intent] sent from Locus.
     * @return Location if exists, otherwise returns null.
     */
    private fun obtainLocusLocationFromIntent(intent: Intent): Location? = when {
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_GPS) -> IntentHelper.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS)
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER) -> IntentHelper.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
        else -> null
    }

    /**
     * Acquires location from GPS or network if providers are available.
     *
     * First it tries to obtain location from GPS and if no GPS provider is available (GPS is turned off) then it tries to obtain location from network.
     */
    private fun acquireLocationFromGpsOrNetwork() {
        val gpsProviderEnabled: Boolean = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: IllegalArgumentException) {
            false
        }

        val networkProviderEnabled: Boolean = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: IllegalArgumentException) {
            false
        }

        when {
            gpsProviderEnabled -> requestGpsLocation()
            networkProviderEnabled -> requestNetworkLocation()
            else -> {
                rootView.snack(R.string.text_no_location_provider)
            }
        }
    }

    /**
     * Starts new activity to pick a location.
     */
    private fun acquireLocationFromMaps() {
        try {
            startActivityForResult(PlacePicker.IntentBuilder()
                    .setLatLong(49.6279094, 18.6274503)  // Initial Latitude and Longitude the Map will load into
                    .showLatLong(true)  // Show Coordinates in the Activity
                    .setMapZoom(12.0f)  // Map Zoom Level. Default: 14.0
                    .setAddressRequired(false) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                    .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
//                    .setMarkerDrawable(R.drawable.ic_map_marker) // Change the default Marker Image
                    .setMarkerImageImageColor(R.color.colorPrimary)
                    .setFabColor(R.color.colorPrimary)
                    .setPrimaryTextColor(R.color.colorPrimaryText) // Change text color of Shortened Address
                    .setSecondaryTextColor(R.color.colorSecondaryText) // Change text color of full Address
                    .setBottomViewColor(R.color.colorBackground) // Change Address View Background Color (Default: White)
                    .setMapRawResourceStyle(R.raw.map_style)  //Set Map Style (https://mapstyle.withgoogle.com/)
                    .setMapType(MapType.HYBRID)
                    .setPlaceSearchBar(false, BuildConfig.API_KEY) //Activate GooglePlace Search Bar. Default is false/not activated. SearchBar is a chargeable feature by Google
                    .onlyCoordinates(true)  //Get only Coordinates from Place Picker
                    .hideLocationButton(true)   //Hide Location Button (Default: false)
                    .disableMarkerAnimation(true)   //Disable Marker Animation (Default: false)
                    .build(this),
                ACTIVITY_RESULT_PLACE_PICKER)
        } catch (e: ActivityNotFoundException) {
            rootView.snack(getString(R.string.error_can_not_start_place_picker), 3000)
            Log.e(TAG, "Can not create place picker.", e)
        }
    }

    /**
     * Requests GPS location.
     *
     * If the application has no permission to access to GPS, it asks for that permission.
     */
    private fun requestGpsLocation() {
        if (this.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationAcquiringDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_FINE)
        }
    }

    /**
     * Requests network location.
     *
     * If the application has no permission to access to network location, it asks for that permission.
     */
    private fun requestNetworkLocation() {
        if (this.hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showLocationAcquiringDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_LOCATION_COARSE)
        }
    }

    /**
     * Displays new dialog displaying location acquiring.
     */
    private fun showLocationAcquiringDialog() {
        locationAcquiringDialog.show(supportFragmentManager, "location")
    }

    /**
     * If GC credentials has been provided before, it starts new [JobIntentService] which will try to download geocaches.
     */
    private fun startDownloadProcess() {
        if (sharedPreferences.getString(PreferenceKey.GC_USERNAME.key, "").isNullOrEmpty() ||
                security.decryptWithBase64(
                        privateSharedPreferences.getString(PreferenceKey.SECURE_KEY.key, "")!!,
                        sharedPreferences.getString(PreferenceKey.GC_PASSWORD.key, "")!!).isEmpty()) {
            rootView.snack(R.string.text_invalid_gc_credentials, 2500)
        } else {
            JobIntentService.enqueueWork(this, UnicornService::class.java, SERVICE_JOB_ID, Intent().apply {
                putExtra(UnicornService.COORDINATION_LAT, latitudeEditText.text.toString())
                putExtra(UnicornService.COORDINATION_LON, longitudeEditText.text.toString())
                putExtra(UnicornService.FILTER_CACHE_TYPE, binding.layoutSearch.searchCacheType.selectedItem.toString())
                putExtra(UnicornService.FILTER_MAX_CACHE_COUNT, maxCountEditText.text.toIntOrNull() ?: FILTER_DEFAULT_CACHE_COUNT)
                putExtra(UnicornService.FILTER_MAX_DISTANCE, binding.layoutSearch.searchMaxDistance.text.toDoubleOrNull() ?: FILTER_MAX_DISTANCE)
                putExtra(UnicornService.FILTER_ALLOW_DISABLED, binding.layoutSearch.searchAllowDisabled.isChecked)
                putExtra(UnicornService.FILTER_EXCLUDE_OWN, !binding.layoutSearch.searchIncludeOwn.isChecked)
                putExtra(UnicornService.FILTER_EXCLUDE_FOUND, !binding.layoutSearch.searchIncludeFound.isChecked)
                putExtra(UnicornService.FILTER_SKIP_PREMIUM, binding.layoutSearch.searchSkipPremium.isChecked)
            })
        }
    }

    /**
     * Validates all inputs and set enable/disable download button accordingly.
     */
    private fun validateInputs() {
        binding.layoutSearch.searchDownload.isEnabled = isValidMaxCount() && isValidLatitude() && isValidLongitude()
    }

    /**
     * Checks whether latitude text view contains correct value.
     * @return True if latitude is correct, otherwise returns false.
     */
    private fun isValidLatitude(): Boolean {
        val latitude = latitudeEditText.text.toFloatOrNull()

        return latitude != null && latitude in -85.0..85.0
    }

    /**
     * Checks whether longitude text view contains correct value.
     * @return True if longitude is correct, otherwise returns false.
     */
    private fun isValidLongitude(): Boolean {
        val longitude = longitudeEditText.text.toFloatOrNull()

        return longitude != null && longitude in -180.0..180.0
    }

    /**
     * Checks whether max count text view contains correct value.
     * @return True if max count is correct, otherwise returns false.
     */
    private fun isValidMaxCount(): Boolean {
        if (maxCountEditText.text?.isEmpty() == true) return true

        val maxCount = maxCountEditText.text.toIntOrNull()

        return maxCount != null && maxCount <= FILTER_MAX_CACHE_COUNT
    }

    /**
     * Builds a dialog with single neutral button.
     * @param title Title.
     * @param message Message.
     * @param button Button text.
     * @param action An action to perform on the button click.
     */
    private fun buildNeutralDialog(title: Int, message: Int, button: Int, action: (DialogInterface, Int) -> Unit): AlertDialog {
        return AlertDialog.Builder(this).also {
            it.setTitle(title)
            it.setMessage(message)
            it.setNeutralButton(button, action)
        }.create()
    }
}
