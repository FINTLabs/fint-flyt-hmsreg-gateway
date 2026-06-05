package no.novari.hmsreg.gateway

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinInvalidNullException
import jakarta.servlet.http.HttpServletRequest
import no.novari.hmsreg.gateway.models.CaseInstance
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [InstanceController::class])
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        badRequest(
            request = request,
            message = "Ugyldig forespørsel. Se fieldErrors for detaljer.",
            fieldErrors =
                exception.bindingResult.fieldErrors.map(::toFieldError) +
                    exception.bindingResult.globalErrors.map(::toFieldError),
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        badRequest(
            request = request,
            message = "Ugyldig forespørsel. Mangler påkrevde felter eller ugyldig JSON.",
            fieldErrors = listOf(toRequestBodyError(exception.cause)),
        )

    private fun badRequest(
        request: HttpServletRequest,
        message: String,
        fieldErrors: List<ApiFieldError>,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                timestamp = OffsetDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = message,
                path = request.requestURI,
                fieldErrors = fieldErrors,
            ),
        )

    private fun toFieldError(error: FieldError): ApiFieldError =
        ApiFieldError(
            field = error.field,
            message =
                when (error.code) {
                    "NotBlank" -> "Feltet er påkrevd og kan ikke være tomt."
                    "NotEmpty" -> "Feltet må inneholde minst én verdi."
                    "NotNull" -> "Feltet er påkrevd."
                    "ExactlyOneMainDocument" -> "Det må være nøyaktig ett hoveddokument."
                    else -> error.defaultMessage ?: "Ugyldig verdi."
                },
        )

    private fun toFieldError(error: ObjectError): ApiFieldError =
        ApiFieldError(
            field = error.objectName,
            message =
                when (error.code) {
                    "ExactlyOneMainDocument" -> "Det må være nøyaktig ett hoveddokument."
                    else -> error.defaultMessage ?: "Ugyldig verdi."
                },
        )

    private fun toRequestBodyError(cause: Throwable?): ApiFieldError =
        when (cause) {
            is InvalidFormatException -> {
                invalidFormatError(cause)
            }

            is KotlinInvalidNullException -> {
                ApiFieldError(
                    field = jacksonPath(cause) ?: cause.kotlinPropertyName,
                    message = "Feltet er påkrevd og kan ikke være null.",
                )
            }

            is MismatchedInputException -> {
                ApiFieldError(
                    field = jacksonPath(cause) ?: "requestBody",
                    message = "Ugyldig datatype eller manglende påkrevd felt.",
                )
            }

            is JsonParseException -> {
                ApiFieldError(
                    field = "requestBody",
                    message = "Ugyldig JSON-syntaks.",
                )
            }

            is JsonMappingException -> {
                ApiFieldError(
                    field = jacksonPath(cause) ?: "requestBody",
                    message = "Ugyldig JSON-verdi.",
                )
            }

            else -> {
                ApiFieldError(
                    field = "requestBody",
                    message = "Ugyldig JSON eller datatype.",
                )
            }
        }

    private fun invalidFormatError(exception: InvalidFormatException): ApiFieldError {
        val field = jacksonPath(exception) ?: "requestBody"

        return ApiFieldError(
            field = field,
            message =
                when (exception.targetType) {
                    LocalDateTime::class.java -> {
                        "Ugyldig datoformat. Forventet format er " +
                            "${CaseInstance.PROCESSED_DATE_TIME_PATTERN}, for eksempel " +
                            "2026-06-03T10:13:15.0000000."
                    }

                    MediaType::class.java -> {
                        "Ugyldig mediatype. Bruk for eksempel application/pdf."
                    }

                    else -> {
                        "Ugyldig verdi eller datatype."
                    }
                },
        )
    }

    private fun jacksonPath(exception: JsonMappingException): String? {
        if (exception.path.isEmpty()) {
            return null
        }

        return buildString {
            exception.path.forEach { reference ->
                val fieldName = reference.fieldName

                if (fieldName != null) {
                    if (isNotEmpty()) {
                        append(".")
                    }
                    append(fieldName)
                } else if (reference.index >= 0) {
                    append("[")
                    append(reference.index)
                    append("]")
                }
            }
        }.takeIf(String::isNotEmpty)
    }
}

data class ApiErrorResponse(
    val timestamp: OffsetDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<ApiFieldError>,
)

data class ApiFieldError(
    val field: String,
    val message: String,
)
