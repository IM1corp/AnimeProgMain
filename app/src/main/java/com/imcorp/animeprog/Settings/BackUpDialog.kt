package com.imcorp.animeprog.Settings

import android.Manifest
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imcorp.animeprog.DB.DataBase
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.BackupDialogBinding
//import kotlinx.android.synthetic.main.backup_dialog.view.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackUpDialog(private val context: MyApp, themeId: Int) : MaterialAlertDialogBuilder(context, themeId) {
    private var _view: BackupDialogBinding? = BackupDialogBinding.inflate(context.layoutInflater)//.inflate(R.layout.backup_dialog, null)
    public val view: BackupDialogBinding get()= _view!!
    init {
        this.setView(view.root)
        this.setOnCreateBackup()
        setOnDismissListener { _view = null }
    }

    private fun setOnCreateBackup() {
        view.pathTextView.setText(context.filesDir.toString())
        view.selectPathBtn.setOnClickListener {

        }
        view.createButton.setOnClickListener {
            requestPermission{
                val file0 = context.getDatabasePath(DataBase.MAIN_DB_NAME)
                //zipFileAtPath(context.dataBase.myDB)
            }
        }
    }
    private fun requestPermission(vararg permission: String = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode: Int = 2, onSuccess: Runnable){
        if(if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                    permission.any { context.shouldShowRequestPermissionRationale(it)}else false)
        ActivityCompat.requestPermissions(context, permission, requestCode)
        context.addOnRequestPermissionListener{ success: Boolean, i: Int, strings: Array<String> ->
            if(i==requestCode) {
                if (!success) context.notificator.showNeedPermission(view.root) {
                    requestPermission(*permission, onSuccess = onSuccess)
                } else onSuccess.run()
            }
            true
        }
    }

    fun zipFileAtPath(sourcePath: String, toLocation: String?): Boolean {
        fun getLastPathComponent(filePath: String): String {
            val segments = filePath.split('/').toTypedArray()
            return if (segments.isEmpty()) "" else segments[segments.size - 1]
        }
        val BUFFER = 2048
        val sourceFile = File(sourcePath)
        try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(toLocation)
            val out = ZipOutputStream(BufferedOutputStream(
                    dest))
            if (sourceFile.isDirectory) {
                zipSubFolder(out, sourceFile, sourceFile.parent!!.length)
            } else {
                val data = ByteArray(BUFFER)
                val fi = FileInputStream(sourcePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(getLastPathComponent(sourcePath))
                entry.time = sourceFile.lastModified() // to keep modification time after unzipping
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

/*
 *
 * Zips a subfolder
 *
 */

    /*
 *
 * Zips a subfolder
 *
 */
    @Throws(IOException::class)
    private fun zipSubFolder(out: ZipOutputStream, folder: File,
                             basePathLength: Int) {
        val BUFFER = 2048
        val fileList: Array<File> = folder.listFiles()
        var origin: BufferedInputStream? = null
        for (file in fileList) {
            if (file.isDirectory) {
                zipSubFolder(out, file, basePathLength)
            } else {
                val data = ByteArray(BUFFER)
                val unmodifiedFilePath: String = file.getPath()
                val relativePath = unmodifiedFilePath
                        .substring(basePathLength)
                val fi = FileInputStream(unmodifiedFilePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(relativePath)
                entry.time = file.lastModified() // to keep modification time after unzipping
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
        }
    }


}