import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.*

private const val FILE_PATH = "/tmp/test.text"
private const val FILE_FLAGS = O_RDONLY
private const val FILE_MODE = S_IRUSR or S_IWUSR

private typealias Task = () -> Unit

private val pendingTasks : MutableList<Task> = mutableListOf()

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

        val bytesRead = read(fileDescriptor, buffer, bytesToRead.toULong())

        if(bytesRead == -1L) {
            if(errno == EWOULDBLOCK) {
                println("We would have blocked but instead lets do something and then block until ready")
                pendingTasks.forEach { task ->
                    task()
                }
                epoll_wait(epollFd, event.ptr, 1, -1) // Block only when we have nothing to do 🤩
            } else return
        } else {
            // read data when ready
        }
    }
}