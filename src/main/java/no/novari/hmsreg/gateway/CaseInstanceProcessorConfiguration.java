package no.novari.hmsreg.gateway;

import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.flyt.instance.gateway.InstanceProcessorFactoryService;
import no.novari.hmsreg.gateway.mapping.CaseInstanceMappingService;
import no.novari.hmsreg.gateway.models.CaseInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class CaseInstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<CaseInstance> caseInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            CaseInstanceMappingService caseInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "sak",
                caseInstance -> Optional.ofNullable(
                        caseInstance.getInstanceId()
                ),
                caseInstanceMappingService
        );
    }

}
