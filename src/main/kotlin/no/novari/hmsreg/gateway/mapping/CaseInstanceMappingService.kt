package no.novari.hmsreg.gateway.mapping

import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import no.novari.hmsreg.gateway.models.CaseInstance
import no.novari.hmsreg.gateway.models.Document
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Service
class CaseInstanceMappingService : InstanceMapper<CaseInstance> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: CaseInstance,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        val mainDocument =
            incomingInstance.documents.firstOrNull(Document::isMainDocument)
                ?: throw IllegalArgumentException("No main document found")
        val attachments = incomingInstance.documents.filterNot(Document::isMainDocument)

        val valuePerKey =
            incomingInstance.toValuePerKey() +
                mapMainDocumentToInstanceValuePerKey(
                    persistFile,
                    sourceApplicationId,
                    incomingInstance.instanceId,
                    mainDocument,
                )

        return InstanceObject(
            valuePerKey = valuePerKey,
            objectCollectionPerKey =
                mutableMapOf(
                    "vedlegg" to
                        attachments.map { attachment ->
                            mapAttachmentDocumentToInstanceObject(
                                persistFile,
                                sourceApplicationId,
                                incomingInstance.instanceId,
                                attachment,
                            )
                        },
                ),
        )
    }

    private fun CaseInstance.toValuePerKey(): Map<String, String> {
        return mapOf(
            "organisasjonsNavn" to organizationName,
            "instansId" to instanceId,
            "organisasjonsNummer" to organizationNumber,
            "prosjektNavn" to projectName,
            "hovedLeverandor" to mainSupplier,
            "behandlet" to processed.format(PROCESSED_DATE_TIME_FORMATTER),
            "behandletEpost" to processedByEmail.lowercase(Locale.ROOT),
            "status" to status,
            "type" to type,
            "template" to template,
            "deviationCode" to deviationCode,
            "deviationCodeFU" to deviationCodeFU,
            "prosjektId" to projectId,
            "avdeling" to department,
        )
    }

    private fun mapMainDocumentToInstanceValuePerKey(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        document: Document,
    ): Map<String, String> {
        val fileId =
            persistFile(
                toFile(
                    sourceApplicationId = sourceApplicationId,
                    sourceApplicationInstanceId = sourceApplicationInstanceId,
                    document = document,
                ),
            )

        return mapOf(
            "hovedDokumentTittel" to document.title,
            "hovedDokumentFilnavn" to document.filename,
            "hovedDokumentdato" to document.documentDatetime,
            "hovedDokumentFil" to fileId.toString(),
            "hovedDokumentMediatype" to document.mediatype.toString(),
        )
    }

    private fun mapAttachmentDocumentToInstanceObject(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        attachmentDocument: Document,
    ): InstanceObject {
        val fileId =
            persistFile(
                toFile(
                    sourceApplicationId = sourceApplicationId,
                    sourceApplicationInstanceId = sourceApplicationInstanceId,
                    document = attachmentDocument,
                ),
            )

        return InstanceObject(
            valuePerKey =
                mapOf(
                    "tittel" to attachmentDocument.title,
                    "filnavn" to attachmentDocument.filename,
                    "fildato" to attachmentDocument.documentDatetime,
                    "fil" to fileId.toString(),
                    "mediatype" to attachmentDocument.mediatype.toString(),
                ),
        )
    }

    private fun toFile(
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        document: Document,
    ): File {
        return File(
            name = document.filename,
            sourceApplicationId = sourceApplicationId,
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            type = document.mediatype,
            encoding = "UTF-8",
            base64Contents = document.documentBase64,
        )
    }

    companion object {
        private val PROCESSED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
    }
}
