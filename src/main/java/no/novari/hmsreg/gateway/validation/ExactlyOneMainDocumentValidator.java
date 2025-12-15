package no.novari.hmsreg.gateway.validation;

import no.novari.hmsreg.gateway.models.Document;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;

public class ExactlyOneMainDocumentValidator implements ConstraintValidator<ExactlyOneMainDocument, Collection<Document>> {

    @Override
    public boolean isValid(Collection<Document> documents, ConstraintValidatorContext constraintValidatorContext) {
        long numberOfMainDocuments = documents.stream()
                .filter(Document::getIsMainDocument)
                .count();
        if (numberOfMainDocuments == 1) {
            return true;
        }
        if (constraintValidatorContext instanceof HibernateConstraintValidatorContext) {
            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                    .addMessageParameter("numberOfMainDocuments", numberOfMainDocuments);
        }
        return false;
    }

}
