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

package cz.babi.gcunicorn.android.ui.widget

import android.content.Context
import android.os.Build
import android.support.v4.widget.CircularProgressDrawable
import android.util.AttributeSet
import android.widget.ProgressBar
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.`fun`.getPlatformColor


/**
 * Custom progress bar.
 *
 * @author FD_
 * @version 1.0.0
 * @since 1.0.0
 */
class MaterialProgressBar : ProgressBar {

    companion object {
        // Same dimensions as medium-sized native Material progress bar
        private const val RADIUS_DP = 16
        private const val WIDTH_DP = 4
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val screenDensity = resources.displayMetrics.density

            indeterminateDrawable = CircularProgressDrawable(context).apply {
                setColorSchemeColors(resources.getPlatformColor(R.color.colorAccent))

                centerRadius = RADIUS_DP * screenDensity
                strokeWidth = WIDTH_DP * screenDensity
            }
        }
    }
}