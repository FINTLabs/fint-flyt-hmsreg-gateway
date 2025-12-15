package no.novari.hmsreg.gateway.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.hmsreg.gateway.validation.ExactlyOneMainDocument;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class CaseInstance {
    @NotNull
    private final String organizationName;
    @NotNull
    private final String instanceId;
    @NotNull
    private final String organizationNumber;
    @NotNull
    private final String projectName;
    @NotNull
    private final String mainSupplier;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
    private LocalDateTime processed;
    @NotNull
    private final String processedByEmail;
    @NotNull
    private final String status;
    @NotNull
    private final String type;
    @NotNull
    private final String template;
    @NotNull
    private final String deviationCode;
    @NotNull
    private final String deviationCodeFU;
    @NotNull
    private final String projectId;
    @NotNull
    private final String department;

    @ExactlyOneMainDocument
    private final List<@Valid @NotNull Document> documents;
}
