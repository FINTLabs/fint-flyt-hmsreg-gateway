package no.novari.hmsreg.gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.util.unit.DataSize

class JacksonStreamReadConstraintsConfigurationTest {
    @Test
    fun `sets max string length from web instance gateway max request size`() {
        val builder = Jackson2ObjectMapperBuilder.json()

        JacksonStreamReadConstraintsConfiguration()
            .jacksonStreamReadConstraintsCustomizer(DataSize.ofMegabytes(500))
            .customize(builder)

        val objectMapper = builder.build<ObjectMapper>()

        assertThat(objectMapper.factory.streamReadConstraints().maxStringLength)
            .isEqualTo(DataSize.ofMegabytes(500).toBytes().toInt())
    }
}
