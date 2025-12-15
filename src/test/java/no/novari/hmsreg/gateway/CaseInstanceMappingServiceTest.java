package no.novari.hmsreg.gateway;

import no.novari.flyt.instance.gateway.model.File;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import no.novari.hmsreg.gateway.mapping.CaseInstanceMappingService;
import no.novari.hmsreg.gateway.models.CaseInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseInstanceMappingServiceTest {
    public static final long sourceApplicationId = 6;

    @Mock
    CaseInstanceMappingService caseInstanceMappingService;

    TestCaseUtils testCaseUtils = new TestCaseUtils();

    ArgumentMatcher<File> argumentMatcherHoveddokument;
    ArgumentMatcher<File> argumentMatcherVedlegg1;
    ArgumentMatcher<File> argumentMatcherVedlegg2;

    @Mock
    Function<File, Mono<UUID>> persistFile;

    private CaseInstance createIncomingCaseInstance() {
        return CaseInstance
                .builder()
                .organizationName("testOrgNavn")
                .instanceId("testInstansId")
                .organizationNumber("testOrgNr")
                .projectName("testProsjektNavn")
                .mainSupplier("testHovedleverandør")
                .processed(LocalDateTime.parse("2024-09-04T08:39:43.0200000"))
                .processedByEmail("testEpost")
                .status("testStatus")
                .type("testType")
                .template("testTemplate")
                .deviationCode("testDeviationCode")
                .deviationCodeFU("testDeviationCodeFU")
                .projectId("testProsjektId")
                .department("testAvdeling")
                .documents(testCaseUtils.createDocuments())
                .build();
    }

    private InstanceObject createExpectedInstanceObject() {
        HashMap<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("organisasjonsNavn", "testOrgNavn");
        valuePerKey.put("instansId", "testInstansId");
        valuePerKey.put("organisasjonsNummer", "testOrgNr");
        valuePerKey.put("prosjektNavn", "testProsjektNavn");
        valuePerKey.put("hovedLeverandor", "testHovedleverandør");
        valuePerKey.put("behandlet", "2024-09-04T08:39:43.0200000");
        valuePerKey.put("behandletEpost", "testEpost");
        valuePerKey.put("status", "testStatus");
        valuePerKey.put("type", "testType");
        valuePerKey.put("template", "testTemplate");
        valuePerKey.put("deviationCode", "testDeviationCode");
        valuePerKey.put("deviationCodeFU", "testDeviationCodeFU");
        valuePerKey.put("prosjektId", "testProsjektId");
        valuePerKey.put("avdeling", "testAvdeling");
        valuePerKey.put("hovedDokumentTittel", "testHoveddokumentTittel");
        valuePerKey.put("hovedDokumentFilnavn", "testHoveddokumentFilnavn.pdf");
        valuePerKey.put("hovedDokumentdato", "testHoveddokumentDato");
        valuePerKey.put("hovedDokumentFil", "40b1417d-f4dd-4be6-ae59-e36490957565");
        valuePerKey.put("hovedDokumentMediatype", "application/pdf");

        return InstanceObject
                .builder()
                .valuePerKey(valuePerKey)
                .objectCollectionPerKey(
                        Map.of(
                                "vedlegg", List.of(
                                        InstanceObject.builder().valuePerKey(
                                                Map.of(
                                                        "tittel", "testVedlegg1Tittel",
                                                        "filnavn", "testVedlegg1Filnavn.pdf",
                                                        "fildato", "testVedlegg1Dato",
                                                        "fil", "68bf4daf-a0af-4df5-a1ef-3a1409aef4dc",
                                                        "mediatype", "application/pdf"
                                                )
                                        ).build(),
                                        InstanceObject.builder().valuePerKey(
                                                Map.of(
                                                        "tittel", "testVedlegg2Tittel",
                                                        "filnavn", "testVedlegg2Filnavn.docx",
                                                        "fildato", "testVedlegg2Dato",
                                                        "fil", "e4127b11-6c71-4570-b362-d4aae28b7193",
                                                        "mediatype", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                                )
                                        ).build()
                                )
                        )
                )
                .build();
    }

    public void setUpFileFunctionMock() {
        argumentMatcherHoveddokument = file ->
                "testHoveddokumentFilnavn.pdf".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/pdf".equals(String.valueOf(file.getType())) &&
                        "6".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testInstansId".equals(file.getSourceApplicationInstanceId()) &&
                        "SG92ZWRkb2t1bWVudA==".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("40b1417d-f4dd-4be6-ae59-e36490957565")))
                .when(persistFile).apply(argThat(argumentMatcherHoveddokument));

        argumentMatcherVedlegg1 = file ->
                "testVedlegg1Filnavn.pdf".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/pdf".equals(String.valueOf(file.getType())) &&
                        "6".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testInstansId".equals(file.getSourceApplicationInstanceId()) &&
                        "SG92ZWRkb2t1bWVudA==".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("68bf4daf-a0af-4df5-a1ef-3a1409aef4dc")))
                .when(persistFile).apply(argThat(argumentMatcherVedlegg1));

        argumentMatcherVedlegg2 = file ->
                "testVedlegg2Filnavn.docx".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(String.valueOf(file.getType())) &&
                        "6".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testInstansId".equals(file.getSourceApplicationInstanceId()) &&
                        "UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("e4127b11-6c71-4570-b362-d4aae28b7193")))
                .when(persistFile).apply(argThat(argumentMatcherVedlegg2));
    }

    @Test
    public void givenCaseInstanceWithMainDocumentAndTwoAttachmentsShouldReturnMappedExpectedInstance() {
        CaseInstance incomingCase = createIncomingCaseInstance();
        InstanceObject expectedInstance = createExpectedInstanceObject();

        setUpFileFunctionMock();

        caseInstanceMappingService = new CaseInstanceMappingService();

        InstanceObject instanceObject = caseInstanceMappingService.map(
                sourceApplicationId,
                incomingCase,
                persistFile
        ).block();
        assertThat(instanceObject).isEqualTo(expectedInstance);

        verify(persistFile).apply(argThat(argumentMatcherHoveddokument));
        verify(persistFile).apply(argThat(argumentMatcherVedlegg1));
        verify(persistFile).apply(argThat(argumentMatcherVedlegg2));
        verifyNoMoreInteractions(persistFile);
    }

}