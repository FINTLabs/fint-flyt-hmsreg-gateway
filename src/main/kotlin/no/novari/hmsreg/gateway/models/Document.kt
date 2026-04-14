package no.novari.hmsreg.gateway.models

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.MediaType

data class Document(
    @field:NotBlank val filename: String,
    val isMainDocument: Boolean,
    @field:NotBlank val title: String,
    @field:NotBlank val documentDatetime: String,
    @field:NotNull val mediatype: MediaType,
    @field:NotBlank val documentBase64: String,
)
