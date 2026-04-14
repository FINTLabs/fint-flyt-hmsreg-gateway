package no.novari.hmsreg.gateway

import no.novari.hmsreg.gateway.models.Document
import org.springframework.http.MediaType

object TestCaseUtils {
    fun createDocuments(): List<Document> {
        return listOf(
            createMainDocument(),
            createOtherDocument1(),
            createOtherDocument2(),
        )
    }

    fun createMainDocument(): Document {
        return Document(
            filename = "testHoveddokumentFilnavn.pdf",
            isMainDocument = true,
            title = "testHoveddokumentTittel",
            documentDatetime = "testHoveddokumentDato",
            mediatype = MediaType.APPLICATION_PDF,
            documentBase64 = "SG92ZWRkb2t1bWVudA==",
        )
    }

    fun createOtherDocument1(): Document {
        return Document(
            filename = "testVedlegg1Filnavn.pdf",
            isMainDocument = false,
            title = "testVedlegg1Tittel",
            documentDatetime = "testVedlegg1Dato",
            mediatype = MediaType.APPLICATION_PDF,
            documentBase64 = "SG92ZWRkb2t1bWVudA==",
        )
    }

    fun createOtherDocument2(): Document {
        return Document(
            filename = "testVedlegg2Filnavn.docx",
            isMainDocument = false,
            title = "testVedlegg2Tittel",
            documentDatetime = "testVedlegg2Dato",
            mediatype =
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                ),
            documentBase64 = "UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==",
        )
    }
}
