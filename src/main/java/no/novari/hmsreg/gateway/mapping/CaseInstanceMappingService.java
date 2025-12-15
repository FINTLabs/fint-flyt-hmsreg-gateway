package no.novari.hmsreg.gateway.mapping;

import no.novari.hmsreg.gateway.models.CaseInstance;
import no.novari.hmsreg.gateway.models.Document;
import no.novari.flyt.instance.gateway.InstanceMapper;
import no.novari.flyt.instance.gateway.model.File;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Service
public class CaseInstanceMappingService implements InstanceMapper<CaseInstance> {

    @Override
    public Mono<InstanceObject> map(
            Long sourceApplicationId,
            CaseInstance caseInstance,
            Function<File, Mono<UUID>> persistFile
    ) {
        Document mainDocument = caseInstance
                .getDocuments()
                .stream()
                .filter(Document::getIsMainDocument)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No main document found"));

        List<Document> attachments = caseInstance
                .getDocuments()
                .stream()
                .filter(document -> !document.getIsMainDocument())
                .toList();

        Mono<Map<String, String>> mainDocumentInstanceValuePerKeyMono = mapMainDocumentToInstanceValuePerKey(
                persistFile,
                sourceApplicationId,
                caseInstance.getInstanceId(),
                mainDocument
        );

        Mono<List<InstanceObject>> attachmentInstanceObjectsMono = mapAttachmentDocumentsToInstanceObjects(
                persistFile,
                sourceApplicationId,
                caseInstance.getInstanceId(),
                attachments
        );

        HashMap<String, String> valuePerKey = getStringStringHashMap(caseInstance);

        return Mono.zip(
                        mainDocumentInstanceValuePerKeyMono,
                        attachmentInstanceObjectsMono
                )
                .map((Tuple2<Map<String, String>, List<InstanceObject>> mainDocumentValuePerKeyAndAttachmentsInstanceObjects) -> {

                            valuePerKey.putAll(mainDocumentValuePerKeyAndAttachmentsInstanceObjects.getT1());
                            return InstanceObject.builder()
                                    .valuePerKey(valuePerKey)
                                    .objectCollectionPerKey(
                                            Map.of(
                                                    "vedlegg", mainDocumentValuePerKeyAndAttachmentsInstanceObjects.getT2()
                                            ))
                                    .build();
                        }

                );
    }

    private static HashMap<String, String> getStringStringHashMap(CaseInstance caseInstance) {
        HashMap<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("organisasjonsNavn", caseInstance.getOrganizationName());
        valuePerKey.put("instansId", caseInstance.getInstanceId());
        valuePerKey.put("organisasjonsNummer", caseInstance.getOrganizationNumber());
        valuePerKey.put("prosjektNavn", caseInstance.getProjectName());
        valuePerKey.put("hovedLeverandor", caseInstance.getMainSupplier());
        valuePerKey.put("behandlet", caseInstance.getProcessed().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")));
        valuePerKey.put("behandletEpost", caseInstance.getProcessedByEmail());
        valuePerKey.put("status", caseInstance.getStatus());
        valuePerKey.put("type", caseInstance.getType());
        valuePerKey.put("template", caseInstance.getTemplate());
        valuePerKey.put("deviationCode", caseInstance.getDeviationCode());
        valuePerKey.put("deviationCodeFU", caseInstance.getDeviationCodeFU());
        valuePerKey.put("prosjektId", caseInstance.getProjectId());
        valuePerKey.put("avdeling", caseInstance.getDepartment());
        return valuePerKey;
    }

    private Mono<Map<String, String>> mapMainDocumentToInstanceValuePerKey(
            Function<File, Mono<UUID>> persistFile,
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            Document document
    ) {
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                document
        );

        return persistFile.apply(file)
                .map(fileId -> mapMainDocumentAndFileIdToInstanceValuePerKey(document, fileId));
    }

    private Map<String, String> mapMainDocumentAndFileIdToInstanceValuePerKey(
            Document document,
            UUID fileId
    ) {
        return Map.of(
                "hovedDokumentTittel", Optional.ofNullable(document.getTitle()).orElse(""),
                "hovedDokumentFilnavn", Optional.ofNullable(document.getFilename()).orElse(""),
                "hovedDokumentdato", Optional.ofNullable(document.getDocumentDatetime()).orElse(""),
                "hovedDokumentFil", fileId.toString(),
                "hovedDokumentMediatype", document.getMediatype().toString()
        );
    }

    private Mono<List<InstanceObject>> mapAttachmentDocumentsToInstanceObjects(
            Function<File, Mono<UUID>> persistFile,
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            List<Document> attachmentDocuments
    ) {
        return Flux.fromIterable(attachmentDocuments)
                .flatMap(attachmentDocument -> mapAttachmentDocumentToInstanceObject(
                        persistFile,
                        sourceApplicationId,
                        sourceApplicationInstanceId,
                        attachmentDocument
                ))
                .collectList();
    }

    private Mono<InstanceObject> mapAttachmentDocumentToInstanceObject(
            Function<File, Mono<UUID>> persistFile,
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            Document attachmentDocument
    ) {
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                attachmentDocument
        );
        return persistFile.apply(file)
                .map(fileId -> mapAttachmentDocumentAndFileIdToInstanceObject(
                        attachmentDocument,
                        fileId
                ));
    }

    private InstanceObject mapAttachmentDocumentAndFileIdToInstanceObject(
            Document attachmentDocument,
            UUID fileId
    ) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel", Optional.ofNullable(attachmentDocument.getTitle()).orElse(""),
                        "filnavn", Optional.ofNullable(attachmentDocument.getFilename()).orElse(""),
                        "fildato", Optional.ofNullable(attachmentDocument.getDocumentDatetime()).orElse(""),
                        "fil", fileId.toString(),
                        "mediatype", attachmentDocument.getMediatype().toString()
                ))
                .build();
    }

    private File toFile(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            Document document
    ) {
        return File
                .builder()
                .name(document.getFilename())
                .type(document.getMediatype())
                .sourceApplicationId(sourceApplicationId)
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .encoding("UTF-8")
                .base64Contents(document.getDocumentBase64())
                .build();
    }

}
