package no.novari.hmsreg.gateway

import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import no.novari.hmsreg.gateway.mapping.CaseInstanceMappingService
import no.novari.hmsreg.gateway.models.CaseInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class CaseInstanceMappingServiceTest {
    private val caseInstanceMappingService = CaseInstanceMappingService()
    private val persistedFiles = mutableListOf<File>()

    @Test
    fun `given case instance with main document and two attachments should return mapped expected instance`() {
        val incomingCase = createIncomingCaseInstance()
        val expectedInstance = createExpectedInstanceObject()

        val instanceObject =
            caseInstanceMappingService.map(
                SOURCE_APPLICATION_ID,
                incomingCase,
            ) { file ->
                persistedFiles += file
                FILE_ID_PER_NAME[file.name] ?: error("Unexpected file: ${file.name}")
            }

        assertThat(instanceObject).isEqualTo(expectedInstance)
        assertThat(persistedFiles).containsExactlyElementsOf(createExpectedPersistedFiles())
    }

    private fun createIncomingCaseInstance(): CaseInstance {
        return CaseInstance(
            organizationName = "testOrgNavn",
            instanceId = "testInstansId",
            organizationNumber = "testOrgNr",
            projectName = "testProsjektNavn",
            mainSupplier = "testHovedleverandør",
            processed = LocalDateTime.parse("2024-09-04T08:39:43.0200000"),
            processedByEmail = "Test.epost@test.com",
            status = "testStatus",
            type = "testType",
            template = "testTemplate",
            deviationCode = "testDeviationCode",
            deviationCodeFU = "testDeviationCodeFU",
            projectId = "testProsjektId",
            department = "testAvdeling",
            documents = TestCaseUtils.createDocuments(),
        )
    }

    private fun createExpectedPersistedFiles(): List<File> {
        return listOf(
            File(
                name = "testHoveddokumentFilnavn.pdf",
                sourceApplicationId = SOURCE_APPLICATION_ID,
                sourceApplicationInstanceId = "testInstansId",
                type = org.springframework.http.MediaType.APPLICATION_PDF,
                encoding = "UTF-8",
                base64Contents = "SG92ZWRkb2t1bWVudA==",
            ),
            File(
                name = "testVedlegg1Filnavn.pdf",
                sourceApplicationId = SOURCE_APPLICATION_ID,
                sourceApplicationInstanceId = "testInstansId",
                type = org.springframework.http.MediaType.APPLICATION_PDF,
                encoding = "UTF-8",
                base64Contents = "SG92ZWRkb2t1bWVudA==",
            ),
            File(
                name = "testVedlegg2Filnavn.docx",
                sourceApplicationId = SOURCE_APPLICATION_ID,
                sourceApplicationInstanceId = "testInstansId",
                type =
                    org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    ),
                encoding = "UTF-8",
                base64Contents = "UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==",
            ),
        )
    }

    private fun createExpectedInstanceObject(): InstanceObject {
        return InstanceObject(
            valuePerKey =
                linkedMapOf(
                    "organisasjonsNavn" to "testOrgNavn",
                    "instansId" to "testInstansId",
                    "organisasjonsNummer" to "testOrgNr",
                    "prosjektNavn" to "testProsjektNavn",
                    "hovedLeverandor" to "testHovedleverandør",
                    "behandlet" to "2024-09-04T08:39:43",
                    "behandletEpost" to "test.epost@test.com",
                    "status" to "testStatus",
                    "type" to "testType",
                    "template" to "testTemplate",
                    "deviationCode" to "testDeviationCode",
                    "deviationCodeFU" to "testDeviationCodeFU",
                    "prosjektId" to "testProsjektId",
                    "avdeling" to "testAvdeling",
                    "hovedDokumentTittel" to "testHoveddokumentTittel",
                    "hovedDokumentFilnavn" to "testHoveddokumentFilnavn.pdf",
                    "hovedDokumentdato" to "testHoveddokumentDato",
                    "hovedDokumentFil" to "40b1417d-f4dd-4be6-ae59-e36490957565",
                    "hovedDokumentMediatype" to "application/pdf",
                ),
            objectCollectionPerKey =
                mutableMapOf(
                    "vedlegg" to
                        listOf(
                            InstanceObject(
                                valuePerKey =
                                    mapOf(
                                        "tittel" to "testVedlegg1Tittel",
                                        "filnavn" to "testVedlegg1Filnavn.pdf",
                                        "fildato" to "testVedlegg1Dato",
                                        "fil" to "68bf4daf-a0af-4df5-a1ef-3a1409aef4dc",
                                        "mediatype" to "application/pdf",
                                    ),
                            ),
                            InstanceObject(
                                valuePerKey =
                                    mapOf(
                                        "tittel" to "testVedlegg2Tittel",
                                        "filnavn" to "testVedlegg2Filnavn.docx",
                                        "fildato" to "testVedlegg2Dato",
                                        "fil" to "e4127b11-6c71-4570-b362-d4aae28b7193",
                                        "mediatype" to
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    ),
                            ),
                        ),
                ),
        )
    }

    companion object {
        private const val SOURCE_APPLICATION_ID = 6L

        private val FILE_ID_PER_NAME =
            mapOf(
                "testHoveddokumentFilnavn.pdf" to UUID.fromString("40b1417d-f4dd-4be6-ae59-e36490957565"),
                "testVedlegg1Filnavn.pdf" to UUID.fromString("68bf4daf-a0af-4df5-a1ef-3a1409aef4dc"),
                "testVedlegg2Filnavn.docx" to UUID.fromString("e4127b11-6c71-4570-b362-d4aae28b7193"),
            )
    }
}
