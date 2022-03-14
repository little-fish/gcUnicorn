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

import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import cz.babi.gcunicorn.android.databinding.ActivitySettingsBinding

/**
 * Settings activity.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.1
 * @since 1.0.0
 */
class SettingsActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onAfterCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
