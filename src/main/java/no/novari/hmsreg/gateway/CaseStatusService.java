package no.novari.hmsreg.gateway;

import no.novari.flyt.instance.gateway.kafka.ArchiveCaseIdRequestService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import no.novari.hmsreg.gateway.models.CaseStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CaseStatusService {

    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;
    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;

    public CaseStatusService(
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService,
            ArchiveCaseIdRequestService archiveCaseIdRequestService
    ) {
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
    }

    public Optional<CaseStatus> getCaseStatus(
            Authentication authentication,
            String sourceApplicationInstanceId
    ) {
        Long sourceApplicationId = sourceApplicationAuthorizationService.getSourceApplicationId(authentication);
        return archiveCaseIdRequestService.getArchiveCaseId(sourceApplicationId, sourceApplicationInstanceId)
                .map(this::toCaseStatus);
    }

    private CaseStatus toCaseStatus(String archiveCaseId) {
        return CaseStatus.builder().archiveCaseId(archiveCaseId).build();
    }

}
