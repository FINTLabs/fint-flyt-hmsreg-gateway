package no.novari.hmsreg.gateway.validation

import jakarta.validation.ConstraintValidatorContext
import no.novari.hmsreg.gateway.TestCaseUtils
import no.novari.hmsreg.gateway.models.Document
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ExactlyOneMainDocumentValidatorTest {
    private lateinit var exactlyOneMainDocumentValidator: ExactlyOneMainDocumentValidator
    private val constraintValidatorContext = mock(ConstraintValidatorContext::class.java)

    @BeforeEach
    fun setup() {
        exactlyOneMainDocumentValidator = ExactlyOneMainDocumentValidator()
    }

    @Test
    fun `given documents with one main document should return true`() {
        val documents = TestCaseUtils.createDocuments()

        val valid = exactlyOneMainDocumentValidator.isValid(documents, constraintValidatorContext)

        assertTrue(valid)
    }

    @Test
    fun `given documents with no main documents should return false`() {
        val documents =
            listOf(
                TestCaseUtils.createOtherDocument1(),
                TestCaseUtils.createOtherDocument2(),
            )

        val valid = exactlyOneMainDocumentValidator.isValid(documents, constraintValidatorContext)

        assertFalse(valid)
    }

    @Test
    fun `given documents with more than one main document should return false`() {
        val documents: List<Document> =
            listOf(
                TestCaseUtils.createMainDocument(),
                TestCaseUtils.createMainDocument(),
                TestCaseUtils.createOtherDocument1(),
            )

        val valid = exactlyOneMainDocumentValidator.isValid(documents, constraintValidatorContext)

        assertFalse(valid)
    }
}
