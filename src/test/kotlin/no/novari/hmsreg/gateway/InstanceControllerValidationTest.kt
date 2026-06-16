package no.novari.hmsreg.gateway

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.hmsreg.gateway.models.CaseInstance
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.time.LocalDateTime

class InstanceControllerValidationTest {
    private val validator =
        LocalValidatorFactoryBean().apply {
            afterPropertiesSet()
        }

    private val objectMapper =
        JsonMapper
            .builder()
            .addModule(KotlinModule.Builder().build())
            .addModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()

    @Suppress("UNCHECKED_CAST")
    private val caseInstanceProcessor = mock(InstanceProcessor::class.java) as InstanceProcessor<CaseInstance>
    private val caseStatusService = mock(CaseStatusService::class.java)

    private val mockMvc =
        MockMvcBuilders
            .standaloneSetup(InstanceController(caseInstanceProcessor, caseStatusService))
            .setControllerAdvice(ApiExceptionHandler())
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .setValidator(validator)
            .build()

    @Test
    fun `given processed with allowed date formats should deserialize`() {
        listOf(
            "2026-06-03T10:13:15.0000000",
            "2026-06-03T10:13:15",
            "2026-06-03 10:13:15.0000000",
            "2026-06-03 10:13:15",
        ).forEach { processed ->
            val caseInstance =
                objectMapper.readValue(
                    validRequestBody(processed = processed),
                    CaseInstance::class.java,
                )

            assertThat(caseInstance.processed).isEqualTo(LocalDateTime.parse("2026-06-03T10:13:15"))
        }
    }

    @Test
    fun `given processed with wrong date format should return field error`() {
        mockMvc
            .perform(
                post("/api/hmsreg/instances/sak")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestBody(processed = "2026/06/03 10:13:15")),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("processed"))
            .andExpect(
                jsonPath("$.fieldErrors[0].message")
                    .value(containsString(CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITH_FRACTION)),
            ).andExpect(
                jsonPath("$.fieldErrors[0].message")
                    .value(containsString(CaseInstance.PROCESSED_DATE_TIME_PATTERN_WITH_SPACE)),
            )
    }

    @Test
    fun `given blank documentBase64 should return field error`() {
        mockMvc
            .perform(
                post("/api/hmsreg/instances/sak")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestBody(documentBase64 = "")),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("documents[0].documentBase64"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Feltet er påkrevd og kan ikke være tomt."))
    }

    @Test
    fun `given blank deviationCodeFU should pass validation`() {
        val caseInstance =
            objectMapper.readValue(
                validRequestBody(deviationCodeFU = ""),
                CaseInstance::class.java,
            )

        assertThat(validator.validate(caseInstance)).isEmpty()
    }

    private fun validRequestBody(
        processed: String = "2026-06-03T10:13:15.0000000",
        documentBase64: String = "SG92ZWRkb2t1bWVudA==",
        deviationCodeFU: String = "Ikke relevant",
    ): String =
        """
        {
          "organizationName": "TEST ORGANISASJON AS",
          "instanceId": "aa9d0046-982c-4e7f-b112-c34c25c2076f",
          "organizationNumber": "123456789",
          "projectName": "Testprosjekt",
          "mainSupplier": "TEST LEVERANDØR AS",
          "processed": "$processed",
          "processedByEmail": "test@example.org",
          "status": "Godkjent",
          "type": "Innregistrering",
          "template": "Ikke relevant",
          "deviationCode": "Ikke relevant",
          "deviationCodeFU": "$deviationCodeFU",
          "projectId": "TEST-1",
          "department": "Testavdeling",
          "documents": [
            {
              "documentBase64": "$documentBase64",
              "documentDatetime": "2026-06-05 08:11",
              "filename": "test-dokument.pdf",
              "title": "Testdokument",
              "mediatype": "application/pdf",
              "isMainDocument": true
            }
          ]
        }
        """.trimIndent()
}
