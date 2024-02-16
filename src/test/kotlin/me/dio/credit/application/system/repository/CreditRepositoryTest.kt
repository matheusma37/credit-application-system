package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enum.Status
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreditRepositoryTest {
    @Autowired lateinit var creditRepository: CreditRepository
    @Autowired lateinit var testEntityManager: TestEntityManager

    private lateinit var customer: Customer
    private lateinit var credit1: Credit
    private lateinit var credit2: Credit
    
    @BeforeEach fun setup() {
        customer = testEntityManager.persist(buildCustomer())
        credit1 = testEntityManager.persist(buildCredit(customer = customer))
        credit2 = testEntityManager.persist(buildCredit(customer = customer))
    }

    @Test
    fun `should find credit by credit code`() {
        val creditCode1 = UUID.fromString("87cbc7bd-add2-4e4a-8863-dfde984d546c")
        val creditCode2 = UUID.fromString("87cbc7bd-add2-4e4a-8863-dfde984d546d")
        credit1.creditCode = creditCode1
        credit2.creditCode = creditCode2

        val fakeCredit1: Credit = creditRepository.findByCreditCode(creditCode1)!!
        val fakeCredit2: Credit = creditRepository.findByCreditCode(creditCode2)!!

        Assertions.assertThat(fakeCredit1).isNotNull
        Assertions.assertThat(fakeCredit2).isNotNull
        Assertions.assertThat(fakeCredit1).isSameAs(credit1)
        Assertions.assertThat(fakeCredit2).isSameAs(credit2)
        Assertions.assertThat(fakeCredit1.customer).isSameAs(customer)
        Assertions.assertThat(fakeCredit2.customer).isSameAs(customer)
    }

    @Test
    fun `should find all credits by customer id`() {
        val customerId = 1L

        val creditList = creditRepository.findAllByCustomerId(customerId)

        Assertions.assertThat(creditList).isNotEmpty
        Assertions.assertThat(creditList.size).isEqualTo(2)
        Assertions.assertThat(creditList).contains(credit1)
        Assertions.assertThat(creditList).contains(credit2)
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(1500.0),
        status: Status = Status.IN_PROGRESS,
        dayFirstInstallment: LocalDate = LocalDate.of(2024, Month.FEBRUARY, 3),
        numberOfInstallments: Int = 3,
        customer: Customer,
    ) = Credit(
        creditValue = creditValue,
        status = status,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )

    private fun buildCustomer(
        firstName: String = "Matheus",
        lastName: String = "Dev",
        cpf: String = "32632437059",
        email: String = "matheus.dev@email.com",
        password: String = "senha123",
        zipCode: String = "124356000",
        street: String = "Matheus Street",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
    ): Customer = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        income = income,
        address = Address(
            zipCode = zipCode,
            street = street,
        ),
    )
}