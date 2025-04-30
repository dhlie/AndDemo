package com.dhl.base.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 *
 * Author: duanhl
 * Create: 2023/10/11 16:01
 * Description:
 *
 */
class FileUtil {
    companion object {

        fun saveToFile(content: String, filePath: String, gzip: Boolean = false): Long {
            return saveToFile(content, File(filePath), gzip)
        }

        fun saveToFile(content: String, file: File, gzip: Boolean = false): Long {
            return saveToFile(content.toByteArray(Charsets.UTF_8), file, gzip)
        }

        fun saveToFile(content: ByteArray, file: File, gzip: Boolean = false): Long {
            return saveToFile(bytes = content, inputStream = null, file, gzip)
        }

        fun saveToFile(inputStream: InputStream, file: File, gzip: Boolean = false): Long {
            return saveToFile(bytes = null, inputStream, file, gzip)
        }

        private fun saveToFile(bytes: ByteArray?, inputStream: InputStream?, file: File, gzip: Boolean = false): Long {
            var outputStream: OutputStream? = null
            try {
                val parent = file.parentFile ?: return -1L
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                if (file.exists()) {
                    file.delete()
                }

                outputStream = if (gzip) {
                    GZIPOutputStream(FileOutputStream(file))
                } else {
                    FileOutputStream(file)
                }
                val count = if (bytes != null) {
                    writeToStream(bytes, outputStream)
                } else if (inputStream != null) {
                    copy(inputStream, outputStream)
                } else {
                    -1L
                }
                if (outputStream is GZIPOutputStream) {
                    outputStream.finish()
                }
                return count
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return -1L
        }

        fun readTextFile(path: String, gzip: Boolean = false): String? {
            return readTextFile(File(path), gzip)
        }

        fun readTextFile(file: File, gzip: Boolean = false): String? {
            val bytes = readBytes(file, gzip)
            return if (bytes == null) null else String(bytes, Charsets.UTF_8)
        }

        fun readBytes(file: File, gzip: Boolean = false): ByteArray? {
            if (!file.exists()) {
                return null
            }

            var inputStream: InputStream? = null
            try {
                inputStream = if (gzip) {
                    GZIPInputStream(FileInputStream(file))
                } else {
                    FileInputStream(file)
                }
                return readBytes(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }

        fun readBytes(inputStream: InputStream): ByteArray? {
            val out = ByteArrayOutputStream()
            copy(inputStream, out)
            return out.toByteArray()
        }

        fun copy(inputStream: InputStream, outputStream: OutputStream): Long {
            try {
                val buf = ByteArray(4096)
                var length = inputStream.read(buf)
                var count = 0L
                while (length != -1) {
                    outputStream.write(buf, 0, length)
                    count += length
                    length = inputStream.read(buf)
                }
                outputStream.flush()
                return count
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return -1L
        }

        fun writeToStream(bytes: ByteArray, outputStream: OutputStream): Long {
            try {
                outputStream.write(bytes, 0, bytes.size)
                outputStream.flush()
                return bytes.size.toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return -1L
        }
    }
}