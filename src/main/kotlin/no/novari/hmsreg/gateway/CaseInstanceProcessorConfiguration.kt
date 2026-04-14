package no.novari.hmsreg.gateway

import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.gateway.webinstance.InstanceProcessorFactoryService
import no.novari.hmsreg.gateway.mapping.CaseInstanceMappingService
import no.novari.hmsreg.gateway.models.CaseInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CaseInstanceProcessorConfiguration {
    @Bean
    fun caseInstanceProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        caseInstanceMappingService: CaseInstanceMappingService,
    ): InstanceProcessor<CaseInstance> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            "sak",
            { caseInstance -> caseInstance.instanceId },
            caseInstanceMappingService,
        )
    }
}
