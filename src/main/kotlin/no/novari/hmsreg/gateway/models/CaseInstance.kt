package no.novari.hmsreg.gateway.models

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import no.novari.hmsreg.gateway.validation.ExactlyOneMainDocument
import java.time.LocalDateTime

data class CaseInstance(
    @field:NotBlank val organizationName: String,
    @field:NotBlank val instanceId: String,
    @field:NotBlank val organizationNumber: String,
    @field:NotBlank val projectName: String,
    @field:NotBlank val mainSupplier: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PROCESSED_DATE_TIME_PATTERN)
    val processed: LocalDateTime,
    @field:NotBlank val processedByEmail: String,
    @field:NotBlank val status: String,
    @field:NotBlank val type: String,
    @field:NotBlank val template: String,
    @field:NotBlank val deviationCode: String,
    @field:NotBlank val deviationCodeFU: String,
    @field:NotBlank val projectId: String,
    @field:NotBlank val department: String,
    @field:Valid
    @field:NotEmpty
    @field:ExactlyOneMainDocument
    val documents: List<@Valid Document>,
) {
    companion object {
        const val PROCESSED_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"
    }
}
