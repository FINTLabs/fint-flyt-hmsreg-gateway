package no.novari.hmsreg.gateway

import no.novari.flyt.gateway.instance.HistoryClient
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import no.novari.hmsreg.gateway.models.CaseStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class CaseStatusService(
    private val sourceApplicationAuthorizationService: SourceApplicationAuthorizationService,
    private val historyClient: HistoryClient,
) {
    fun getCaseStatus(
        authentication: Authentication,
        sourceApplicationInstanceId: String,
    ): CaseStatus? {
        val sourceApplicationId = sourceApplicationAuthorizationService.getSourceApplicationId(authentication)

        return historyClient
            .getArchiveInstanceId(
                sourceApplicationId,
                sourceApplicationInstanceId,
            )?.let { CaseStatus(archiveCaseId = it) }
    }
}
