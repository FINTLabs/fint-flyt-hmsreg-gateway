package no.novari.hmsreg.gateway.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ExactlyOneMainDocumentValidator::class])
annotation class ExactlyOneMainDocument(
    val message: String = "contains {numberOfMainDocuments} main documents",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
