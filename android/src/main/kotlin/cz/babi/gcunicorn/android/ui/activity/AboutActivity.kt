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

package cz.babi.gcunicorn.android.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import cz.babi.gcunicorn.android.R
import kotlinx.android.synthetic.main.activity_about.about_toolbar as toolbar
import kotlinx.android.synthetic.main.layout_about.about_app_name as appNameTextView
import kotlinx.android.synthetic.main.layout_about.about_github as githubTextView

/**
 * About activity.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class AboutActivity : BaseAppCompatActivity() {

    override fun onAfterCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val version: String = try {
            " v${packageManager.getPackageInfo(packageName, 0).versionName}"
        } catch (e: PackageManager.NameNotFoundException) { "" }

        appNameTextView.text = getString(R.string.text_app_name) + "-" + getString(R.string.text_android) + version
        githubTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}