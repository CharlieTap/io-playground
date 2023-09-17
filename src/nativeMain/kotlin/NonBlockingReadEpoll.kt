import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.nativeHeap
import platform.posix.O_NONBLOCK
import platform.posix.O_RDONLY
import platform.posix.S_IRUSR
import platform.posix.S_IWUSR
import platform.posix.open
import platform.posix.read
import platform.linux.*

private const val FILE_PATH = "/tmp/test.text"
private const val FILE_FLAGS = O_RDONLY
private const val FILE_MODE = S_IRUSR or S_IWUSR

fun nonBlockingReadEpoll() {

    val flags = FILE_FLAGS or O_NONBLOCK

    val fileDescriptor = open(FILE_PATH, flags, FILE_MODE)
    val bytesToRead = 1028
    val buffer = nativeHeap.allocArray<ByteVar>(bytesToRead)

    val epollFd = epoll_create1(0)
    if (epollFd == -1) {
        return
    }

    val event = nativeHeap.alloc<epoll_event>()
    event.events = EPOLLIN.toUInt()

    if (epoll_ctl(epollFd, EPOLL_CTL_ADD, fileDescriptor, event.ptr) == -1) {
        return
    }

    while(true) {
        val ready = epoll_wait(epollFd, event.ptr, 1, -1)
        if (ready == -1) {
            return
        }

        val bytesRead = read(fileDescriptor, buffer, bytesToRead.toULong())

        if (bytesRead == -1L) {
            // Something went wrong 🙃
        } else {
            // handle the data read in `buffer`
            return
        }
    }
}