package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enum.Status
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
class CreditServiceTest {
    @MockK lateinit var customerService: CustomerService
    @MockK lateinit var creditRepository: CreditRepository
    @InjectMockKs lateinit var creditService: CreditService

    @Test
    fun `should create credit`() {
        val fakeCredit: Credit = buildCredit()
        val fakeCustomer: Customer = fakeCredit.customer as Customer
        every { creditRepository.save(any()) } returns fakeCredit
        every { customerService.findById(fakeCustomer.id!!) } returns fakeCustomer

        val current: Credit = creditService.save(fakeCredit)

        Assertions.assertThat(current).isNotNull
        Assertions.assertThat(current).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.save(fakeCredit) }
    }

    @Test
    fun `should find all credits by customer id`() {
        val fakeId: Long = Random().nextLong()
        val fakeCredit: Credit = buildCredit(customerId = fakeId)
        every { creditRepository.findAllByCustomerId(fakeId) } returns listOf(fakeCredit)

        val credits: List<Credit> = creditService.findAllByCustomer(fakeId)

        Assertions.assertThat(credits).isNotEmpty
        Assertions.assertThat(credits.size).isEqualTo(1)
        Assertions.assertThat(credits).contains(fakeCredit)
        verify(exactly = 1) { creditRepository.findAllByCustomerId(fakeId) }
    }

    @Test
    fun `should find credit by customer id and credit code`() {
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCustomerId: Long = Random().nextLong()
        val fakeCredit: Credit = buildCredit(creditCode = fakeCreditCode, customerId = fakeCustomerId)
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns fakeCredit

        val current: Credit = creditService.findByCreditCode(customerId = fakeCustomerId, creditCode = fakeCreditCode)

        Assertions.assertThat(current).isNotNull
        Assertions.assertThat(current).isExactlyInstanceOf(Credit::class.java)
        Assertions.assertThat(current).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    @Test
    fun `should not find credit by invalid credit code and throws an IllegalArgumentException`() {
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCustomerId: Long = Random().nextLong()
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns null

        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { creditService.findByCreditCode(customerId = fakeCustomerId, creditCode = fakeCreditCode) }
            .withMessage("CreditCode $fakeCreditCode not found")
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    @Test
    fun `should not find credit by invalid customer id and throws an IllegalArgumentException`() {
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCredit: Credit = buildCredit(creditCode = fakeCreditCode)
        val fakeCustomerId: Long = Random().nextLong()
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns fakeCredit

        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { creditService.findByCreditCode(customerId = fakeCustomerId, creditCode = fakeCreditCode) }
            .withMessage("Contact admin")
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(1500.0),
        status: Status = Status.IN_PROGRESS,
        dayFirstInstallment: LocalDate = LocalDate.of(2024, 2, 3),
        numberOfInstallments: Int = 3,
        creditCode: UUID = UUID.fromString("87cbc7bd-add2-4e4a-8863-dfde984d546c"),
        id: Long = 1L,
        firstName: String = "Matheus",
        lastName: String = "Dev",
        cpf: String = "32632437059",
        email: String = "matheus.dev@email.com",
        password: String = "senha123",
        zipCode: String = "124356000",
        street: String = "Matheus Street",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        customerId: Long = 1L,
    ) = Credit(
        creditValue = creditValue,
        status = status,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        creditCode = creditCode,
        customer = Customer(
            firstName = firstName,
            lastName = lastName,
            cpf = cpf,
            email = email,
            password = password,
            address = Address(
                zipCode = zipCode,
                street = street,
            ),
            income = income,
            id = customerId,
        ),
        id = id,
    )
}