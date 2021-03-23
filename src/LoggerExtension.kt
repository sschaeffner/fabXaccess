package cloud.fabx

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun Logger.domainEvent(msg: String) {
    warn(msg)
}

fun Logger.domainEvent(msg: String, arg: Any) {
    warn(msg, arg)
}

fun Logger.domainEvent(msg: String, vararg arguments: Any) {
    warn(msg, *arguments)
}