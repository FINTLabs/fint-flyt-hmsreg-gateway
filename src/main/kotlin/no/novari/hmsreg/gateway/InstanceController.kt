package no.novari.hmsreg.gateway

import jakarta.validation.Valid
import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import no.novari.hmsreg.gateway.models.CaseInstance
import no.novari.hmsreg.gateway.models.CaseStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$EXTERNAL_API/hmsreg/instances/sak")
class InstanceController(
    private val caseInstanceProcessor: InstanceProcessor<CaseInstance>,
    private val caseStatusService: CaseStatusService,
) {
    @GetMapping("{sourceApplicationInstanceId}/status")
    fun getCaseStatus(
        authentication: Authentication,
        @PathVariable sourceApplicationInstanceId: String,
    ): ResponseEntity<CaseStatus> =
        caseStatusService
            .getCaseStatus(authentication, sourceApplicationInstanceId)
            ?.let { ResponseEntity.ok(it) }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Case with sourceApplicationInstanceId=$sourceApplicationInstanceId could not be found",
            )

    @PostMapping
    fun postIncomingInstanceWithCollectionElements(
        @Valid
        @RequestBody caseInstance: CaseInstance,
        authentication: Authentication,
    ): ResponseEntity<Void> = caseInstanceProcessor.processInstance(authentication, caseInstance)
}
