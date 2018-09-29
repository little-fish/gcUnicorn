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

package cz.babi.gcunicorn.webapp.desktop

import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.util.Locale

/**
 * Tray icon.
 *
 * @param image Tray image.
 * @param tooltip Tooltip.
 * @param applicationContext Application context used for closing the application properly.
 * @param messageSource Message source for obtaining localized messages.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class Tray(image: Image, tooltip: String, private val applicationContext: ApplicationContext, private val messageSource: MessageSource) : TrayIcon(image, tooltip) {

    init {
        popupMenu = createPopupMenu()
        SystemTray.getSystemTray().add(this)
    }

    private fun createPopupMenu(): PopupMenu = PopupMenu().apply {
        add(MenuItem().apply {
            label = messageSource.getMessage("label.tray.exit.text", null, Locale.getDefault())
            addActionListener { _ ->
                SystemTray.getSystemTray().remove(this@Tray)
                System.exit(SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 }))
            }
        })
    }
}