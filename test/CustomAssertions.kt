import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import io.ktor.http.*

fun Assert<HttpStatusCode>.isSuccess() = given { actual ->
    if (actual.isSuccess()) return
    expected("success but was statusCode:${show(actual)}")
}

fun Assert<HttpStatusCode>.isNotSuccess() = given { actual ->
    if (!actual.isSuccess()) return
    expected("not success but was statusCode:${show(actual)}")
}