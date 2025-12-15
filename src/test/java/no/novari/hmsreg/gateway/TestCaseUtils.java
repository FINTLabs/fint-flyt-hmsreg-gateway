package no.novari.hmsreg.gateway;

import no.novari.hmsreg.gateway.models.Document;
import org.springframework.http.MediaType;

import java.util.List;

public class TestCaseUtils {

    public List<Document> createDocuments() {
        return List.of(
                createMainDocument(),
                createOtherDocument1(),
                createOtherDocument2()
        );
    }

    public Document createMainDocument() {
        return Document
                .builder()
                .filename("testHoveddokumentFilnavn.pdf")
                .isMainDocument(true)
                .title("testHoveddokumentTittel")
                .documentDatetime("testHoveddokumentDato")
                .mediatype(MediaType.valueOf("application/pdf"))
                .documentBase64("SG92ZWRkb2t1bWVudA==")
                .build();
    }

    public Document createOtherDocument1() {
        return Document
                .builder()
                .filename("testVedlegg1Filnavn.pdf")
                .isMainDocument(false)
                .title("testVedlegg1Tittel")
                .documentDatetime("testVedlegg1Dato")
                .mediatype(MediaType.valueOf("application/pdf"))
                .documentBase64("SG92ZWRkb2t1bWVudA==")
                .build();
    }

    public Document createOtherDocument2() {
        return Document
                .builder()
                .filename("testVedlegg2Filnavn.docx")
                .isMainDocument(false)
                .title("testVedlegg2Tittel")
                .documentDatetime("testVedlegg2Dato")
                .mediatype(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .documentBase64("UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==")
                .build();
    }

}
