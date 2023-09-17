import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.nativeHeap
import platform.posix.EWOULDBLOCK
import platform.posix.O_NONBLOCK
import platform.posix.O_RDONLY
import platform.posix.S_IRUSR
import platform.posix.S_IWUSR
import platform.posix.errno
import platform.posix.open
import platform.posix.read

private const val FILE_PATH = "/tmp/test.text"
private const val FILE_FLAGS = O_RDONLY
private const val FILE_MODE = S_IRUSR or S_IWUSR

fun nonBlockingRead() {

    val flags = FILE_FLAGS or O_NONBLOCK

    val fileDescriptor = open(FILE_PATH, flags, FILE_MODE)
    val bytesToRead = 1028
    val buffer = nativeHeap.allocArray<ByteVar>(bytesToRead)

    val bytesRead = read(fileDescriptor, buffer, bytesToRead.toULong())

    if(bytesRead == -1L) {
        if(errno == EWOULDBLOCK) {
           // We would have blocked but rather we return an error
        }
    }
}

