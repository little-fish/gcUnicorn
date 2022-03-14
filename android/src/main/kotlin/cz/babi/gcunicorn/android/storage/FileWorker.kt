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

package cz.babi.gcunicorn.android.storage

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Utility class handling files.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class FileWorker() {

    companion object {
        private const val HOME_FOLDER = "gcUnicorn"
        private const val GPX_FOLDER = "gpx"
        private const val TAG = "FILE_WORKER"
    }

    /**
     * Create and file of given name with given content on external storage under [HOME_FOLDER] directory.
     * @param fileName File name.
     * @param content Content to be written into given file name.
     * @return [File] instance of created file or null if an error occurred.
     */
    fun writeToExternalStorage(context: Context, fileName: String, content: String): File? {
        return if (isExternalStorageWritable()) {
            getInternalStorageDirectoryHomePath(context)?.let {
                val homeDir = File(it.absolutePath + File.separator + HOME_FOLDER + File.separator + GPX_FOLDER)
                if (createDirectoriesIfNecessary(homeDir)) {
                    val outputFile = File(homeDir.absolutePath + File.separator + fileName)
                    FileOutputStream(outputFile).apply {
                        try {
                            write(content.toByteArray())
                            flush()

                            return outputFile
                        } catch (e: IOException) {
                            Log.e(TAG, "Can not write given content to file '$fileName'.", e)
                        } finally {
                            try {
                                close()
                            } catch (e: IOException) { /* Does not bother us. */}
                        }
                    }
                } else {
                    Log.e(TAG, "Can not create home directory in external storage.")
                }
            }

            null
        } else {
            Log.e(TAG, "External storage is not writable.")
            null
        }
    }

    /**
     * Checks whether external storage is writable or not.
     * @return True if external storage is writable, otherwise returns false.
     */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Creates directories of given file if necessary.
     * @param file [File] to be created directories for.
     * @return True if directory already exists or directories has been created, otherwise returns false.
     */
    private fun createDirectoriesIfNecessary(file: File) = if (!file.exists()) {
        file.mkdirs()
    } else {
        true
    }

    /**
     * Obtains home directory where GPX files should be stored.
     * @param context Application context.
     * @return Home directory.
     */
    private fun getInternalStorageDirectoryHomePath(context: Context): File? {
        return if (useNewStorageApi()) {
            context.getExternalFilesDir(null)
        } else {
            Environment.getExternalStorageDirectory()
        }
    }
}

/**
 * Determines whether new APi should be used for storing files.
 * @return True if current android version is greater than [Build.VERSION_CODES.LOLLIPOP]
 */
fun useNewStorageApi() = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP
