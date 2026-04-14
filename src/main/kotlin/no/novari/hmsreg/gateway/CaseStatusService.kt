package no.novari.hmsreg.gateway

import no.novari.flyt.gateway.webinstance.kafka.ArchiveCaseIdRequestService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import no.novari.hmsreg.gateway.models.CaseStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class CaseStatusService(
    private val sourceApplicationAuthorizationService: SourceApplicationAuthorizationService,
    private val archiveCaseIdRequestService: ArchiveCaseIdRequestService,
) {
    fun getCaseStatus(
        authentication: Authentication,
        sourceApplicationInstanceId: String,
    ): CaseStatus? {
        val sourceApplicationId = sourceApplicationAuthorizationService.getSourceApplicationId(authentication)

        return archiveCaseIdRequestService
            .getArchiveCaseId(
                sourceApplicationId,
                sourceApplicationInstanceId,
            )?.let { CaseStatus(archiveCaseId = it) }
    }
}
