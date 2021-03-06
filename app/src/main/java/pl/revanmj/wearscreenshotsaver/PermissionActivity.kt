package pl.revanmj.wearscreenshotsaver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionActivity : AppCompatActivity() {
    private var uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = intent.data
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val saveIntent = Intent(this, SaveActivity::class.java)
                    saveIntent.putExtra(SaveActivity.KEY_MOVE, true)
                    startActivity(saveIntent)
                } else {
                    Toast.makeText(this,
                            getString(R.string.error_permission), Toast.LENGTH_SHORT)
                            .show()
                }
                finish()
                return
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST = 1
    }
}
