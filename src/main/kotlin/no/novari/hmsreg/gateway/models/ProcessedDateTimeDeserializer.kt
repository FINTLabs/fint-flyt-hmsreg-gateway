package no.novari.hmsreg.gateway.models

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ProcessedDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): LocalDateTime {
        val value =
            parser.valueAsString
                ?: throw InvalidFormatException.from(
                    parser,
                    "processed must be a string",
                    null,
                    LocalDateTime::class.java,
                )

        FORMATTERS.forEach { formatter ->
            try {
                return LocalDateTime.parse(value, formatter)
            } catch (_: DateTimeParseException) {
            }
        }

        throw InvalidFormatException.from(
            parser,
            "Invalid processed date format",
            value,
            LocalDateTime::class.java,
        )
    }

    companion object {
        private val FORMATTERS =
            listOf(
                CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITH_FRACTION,
                CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITHOUT_FRACTION,
                CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITH_SPACE_AND_FRACTION,
                CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITH_SPACE,
            ).map(DateTimeFormatter::ofPattern)
    }
}
