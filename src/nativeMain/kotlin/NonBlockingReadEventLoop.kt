import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.O_RDONLY
import platform.posix.open

typealias FileDescriptor = Int
typealias Callback = () -> Unit

class EventLoop {

    private val epoll = epoll_create1(0)

    private val io = mutableMapOf<FileDescriptor, Callback>()

    fun registerIO(fd: FileDescriptor, callback: Callback) {

        val event = nativeHeap.alloc<epoll_event>()
        event.events = EPOLLIN.toUInt()
        event.data.fd = fd
        epoll_ctl(epoll, EPOLL_CTL_ADD, fd, event.ptr)

        io[fd] = callback
    }

    fun start() {

        while(true) {

            val pendingIo = io.size
            val events = nativeHeap.allocArray<epoll_event>(pendingIo) // potentially all are ready

            val amountOfReadyFileDescriptors = epoll_wait(epoll, events, pendingIo, -1)

            for (i in 0..< amountOfReadyFileDescriptors) {
                val fd = events[i].data.fd
                io[fd]?.invoke()
            }
        }
    }
}

fun EventLoop.registerHttpHandler(callback: Callback) {
    val httpSocketFileDescriptor = open("/tmp/http_mock", O_RDONLY)
    registerIO(httpSocketFileDescriptor, callback)
}

fun EventLoop.databaseQuery(callback: Callback) {
    val httpSocketFileDescriptor = open("/tmp/db_mock", O_RDONLY)
    registerIO(httpSocketFileDescriptor, callback)
}

fun EventLoop.writeResponse(callback: Callback) {
    val httpSocketFileDescriptor = open("/tmp/response_mock", O_RDONLY)
    registerIO(httpSocketFileDescriptor, callback)
}

fun main() {

    val server = EventLoop()

    server.registerHttpHandler {

        server.databaseQuery {

            server.writeResponse {

            }

        }

    }

    server.start()
}
