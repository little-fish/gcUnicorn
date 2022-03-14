/*
 * gcUnicorn
 * Copyright (C) 2018  Martin Misiarz
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

package cz.babi.gcunicorn.android.ui.dialog

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.`fun`.hasPermissions

/**
 * Custom dialog requesting a user location.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class LocationAcquiringDialogFragment : DialogFragment() {

    companion object {
        const val MIN_TIME = 0L
        const val MIN_DISTANCE = 0F
    }

    internal lateinit var onLocationAcquired: OnLocationAcquired
    internal lateinit var onError: OnError

    private val locationManager by lazy {
        context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationListener: LocationListener by lazy {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.accuracy < 100) {
                    internalCancelWithDismiss()
                    onLocationAcquired.onLocationAcquired(location)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    init {
        retainInstance = true
        isCancelable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startAcquiringLocation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(R.layout.layout_acquiring_location_dialog)
                .setNegativeButton(R.string.text_cancel) { _, _ ->
                    internalCancelWithDismiss()
                }
                .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        internalCancel()

        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        if (retainInstance) {
            // Handles https://code.google.com/p/android/issues/detail?id=17423.
            dialog?.setDismissMessage(null)
        }

        super.onDestroyView()
    }

    /**
     * Starts location acquiring.
     */
    private fun startAcquiringLocation() {
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
                internalCancelWithError(Error.NO_PROVIDER_AVAILABLE)
            }
        }
    }

    /**
     * Requests GPS location.
     */
    private fun requestGpsLocation() {
        if (context?.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION) == true) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener)
            } catch (e: Exception) {
                internalCancelWithError(Error.INTERNAL_ERROR)
            }
        } else {
            internalCancelWithError(Error.NO_FINE_PERMISSION)
        }
    }

    /**
     * Requests network location.
     */
    private fun requestNetworkLocation() {
        if (context?.hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION) == true) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener)
            } catch (e: Exception) {
                internalCancelWithError(Error.INTERNAL_ERROR)
            }
        } else {
            internalCancelWithError(Error.NO_COARSE_PERMISSION)
        }
    }

    /**
     * Removes location listener.
     */
    private fun internalCancel() {
        locationManager.removeUpdates(locationListener)
    }

    /**
     * Removes location listener and dismiss the dialog.
     */
    private fun internalCancelWithDismiss() {
        dismiss()
    }

    /**
     * Removes location listener, dismiss the dialog and pass the error occurred.
     * @param error Error occurred during location acquiring.
     */
    private fun internalCancelWithError(error: Error) {
        internalCancelWithDismiss()
        onError.onError(error)
    }
}

/**
 * On location acquired callback.
 */
interface OnLocationAcquired {
    fun onLocationAcquired(location: Location)
}

/**
 * On error callback.
 */
interface OnError {
    fun onError(error: Error)
}

/**
 * Possible errors during location acquiring.
 */
enum class Error {
    NO_PROVIDER_AVAILABLE,
    NO_FINE_PERMISSION,
    NO_COARSE_PERMISSION,
    INTERNAL_ERROR;
}
