package no.novari.hmsreg.gateway.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@EqualsAndHashCode
@Jacksonized
public class CaseStatus {
    private final String archiveCaseId;
}
