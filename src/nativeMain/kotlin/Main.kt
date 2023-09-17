import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import platform.posix.open
import platform.posix.read
import platform.posix.*


private const val FILE_PATH = "/tmp/test.text"
private const val FILE_FLAGS = O_RDONLY
private const val FILE_MODE = S_IRUSR or S_IWUSR



fun main() = memScoped {

    blockingRead()
    nonBlockingRead()
    nonBlockingReadEpoll()

}