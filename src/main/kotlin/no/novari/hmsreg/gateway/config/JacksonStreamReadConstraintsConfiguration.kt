package no.novari.hmsreg.gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize

@Configuration
class JacksonStreamReadConstraintsConfiguration {
    @Bean
    fun jacksonStreamReadConstraintsCustomizer(
        @Value("\${novari.flyt.web-instance-gateway.max-request-size:100MB}")
        maxRequestSize: DataSize,
    ): Jackson2ObjectMapperBuilderCustomizer {
        val maxStringLength = maxRequestSize.toBytes().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.postConfigurer { objectMapper ->
                objectMapper.factory.setStreamReadConstraints(
                    objectMapper.factory
                        .streamReadConstraints()
                        .rebuild()
                        .maxStringLength(maxStringLength)
                        .build(),
                )
            }
        }
    }
}
