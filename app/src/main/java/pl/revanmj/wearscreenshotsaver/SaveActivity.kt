/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.revanmj.wearscreenshotsaver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast

import androidx.core.content.ContextCompat

import java.io.*
import java.lang.Exception
import java.nio.channels.FileChannel
import java.util.Calendar

class SaveActivity : Activity() {
    companion object {
        private val TAG = SaveActivity::class.java.simpleName
        private const val PICTURES_FILE = "/Pictures/Screenshots/Screenshot_wear_%s.png"
        private const val TEMP_FILE = "/temp.png"
        private const val DATE_FORMAT = "yyyyMMdd-kkmmss"
        const val KEY_MOVE = "moveFile"
    }

    private val nowDate: String
        get() = DateFormat.format(DATE_FORMAT, Calendar.getInstance()).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val move = intent?.extras?.getBoolean(KEY_MOVE, false)
        val uri : Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            val permissionRequest = Intent(this, PermissionActivity::class.java)
            permissionRequest.data = uri
            // File must be saved temporarily in internal folder as access token won't be valid
            // after navigating away from this activity to get permissions for external storage
            saveScreenshot(uri, true)
            startActivity(permissionRequest)
        } else if (move != null && move) {
            // We've got permissions, move temp file to proper location
            moveScreenshot()
        } else {
            // We have permissions from the begging, save in proper location
            saveScreenshot(uri, false)
        }
        finish()
    }

    private fun saveScreenshot(uri: Uri?, saveLocally: Boolean) {
        if (uri == null) {
            return
        } else {
            try {
                val filePath: String =
                        if (saveLocally)
                            cacheDir.toString() + TEMP_FILE
                        else
                            (Environment.getExternalStorageDirectory().toString()
                                    + String.format(PICTURES_FILE, nowDate))

                val file = File(filePath)
                file.parentFile?.mkdir()

                val fileOutputStream = FileOutputStream(file, false)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                // Invoke MediaScanner so that screenshot will be visible in gallery apps without the delay
                MediaScannerConnection.scanFile(applicationContext,
                        arrayOf(filePath), null
                ) { path, fileUri -> Log.i(TAG, "Path[$path], Uri[$fileUri]") }

                if (!saveLocally)
                    Toast.makeText(applicationContext, R.string.message_save, Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, R.string.message_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveScreenshot() {
        val tempFile = File(cacheDir.toString() + TEMP_FILE)
        val newFile = File(Environment.getExternalStorageDirectory().toString()
                + String.format(PICTURES_FILE, nowDate))
        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            outputChannel = FileOutputStream(newFile).channel
            inputChannel = FileInputStream(tempFile).channel
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            inputChannel.close()
            tempFile.delete()
            Toast.makeText(applicationContext, R.string.message_save, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.message_error, Toast.LENGTH_SHORT).show()
        } finally {
            inputChannel?.close()
            outputChannel?.close()
        }
    }
}
