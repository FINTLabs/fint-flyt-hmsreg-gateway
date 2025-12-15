package no.novari.hmsreg.gateway;

import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.hmsreg.gateway.models.CaseInstance;
import no.novari.hmsreg.gateway.models.CaseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static no.novari.flyt.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/hmsreg/instances/sak")
public class InstanceController {

    private final InstanceProcessor<CaseInstance> caseInstanceProcessor;
    private final CaseStatusService caseStatusService;

    public InstanceController(
            InstanceProcessor<CaseInstance> caseInstanceProcessor,
            CaseStatusService caseStatusService
    ) {
        this.caseInstanceProcessor = caseInstanceProcessor;
        this.caseStatusService = caseStatusService;
    }

    @GetMapping("{sourceApplicationInstanceId}/status")
    public Mono<ResponseEntity<CaseStatus>> getInstanceCaseInfo(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String sourceApplicationInstanceId
    ) {
        return authenticationMono.map(authentication -> getCaseStatus(
                authentication,
                sourceApplicationInstanceId
        ));
    }

    public ResponseEntity<CaseStatus> getCaseStatus(
            Authentication authentication,
            String sourceApplicationInstanceId
    ) {
        return caseStatusService.getCaseStatus(authentication, sourceApplicationInstanceId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Case with sourceApplicationInstanceId=%s could not be found", sourceApplicationInstanceId)
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<?>> postIncomingInstanceWithCollectionElements(
            @RequestBody CaseInstance caseInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono.flatMap(
                authentication -> caseInstanceProcessor.processInstance(
                        authentication,
                        caseInstance
                )
        );
    }

}
