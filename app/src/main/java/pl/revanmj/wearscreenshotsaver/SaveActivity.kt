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

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class SaveActivity : Activity() {

    private val nowDate: String
        get() = DateFormat.format(DATE_FORMAT, Calendar.getInstance()).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri : Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            val permissionRequest = Intent(this, PermissionActivity::class.java)
            startActivity(permissionRequest)
            // File will get lost this time
        } else {
            saveScreenshot(uri)
        }
        finish()
    }

    private fun saveScreenshot(uri: Uri?) {
        if (uri == null) {
            return
        } else {
            try {
                val filePath: String =
                        Environment.getExternalStorageDirectory().toString() + String.format(FILE_NAME, nowDate)
                val file = File(filePath)
                file.parentFile.mkdir()

                val fileOutputStream = FileOutputStream(file, true)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                // Invoke MediaScanner so that screenshot will be visible in gallery apps without the delay
                MediaScannerConnection.scanFile(applicationContext,
                        arrayOf(filePath), null
                ) { path, fileUri -> Log.i(TAG, "Path[$path], Uri[$fileUri]") }

                Toast.makeText(applicationContext, R.string.message_save, Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, R.string.message_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val FILE_NAME = "/Pictures/Screenshots/Screenshot_wear_%s.png"
        private const val DATE_FORMAT = "yyyyMMdd-kkmmss"
        private val TAG = SaveActivity::class.java.simpleName
    }
}
