package no.novari.hmsreg.gateway.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import no.novari.hmsreg.gateway.models.Document
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class ExactlyOneMainDocumentValidator : ConstraintValidator<ExactlyOneMainDocument, Collection<Document>> {
    override fun isValid(
        documents: Collection<Document>?,
        constraintValidatorContext: ConstraintValidatorContext,
    ): Boolean {
        val numberOfMainDocuments = documents.orEmpty().count(Document::isMainDocument)

        if (numberOfMainDocuments == 1) {
            return true
        }

        if (constraintValidatorContext is HibernateConstraintValidatorContext) {
            constraintValidatorContext
                .unwrap(HibernateConstraintValidatorContext::class.java)
                .addMessageParameter("numberOfMainDocuments", numberOfMainDocuments)
        }

        return false
    }
}
