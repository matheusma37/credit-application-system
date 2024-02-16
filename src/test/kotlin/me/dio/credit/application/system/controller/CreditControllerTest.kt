package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDTO
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditControllerTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `should create a credit and return 201 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(5000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailCustomer").value("matheus.dev@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.incomeCustomer").value(1000.0))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create a credit without customer and return 400 status`() {
        val creditDto: CreditDto = buildCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation..."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a credit with more than 48 installments and return 400 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, numberOfInstallments = 49)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation..."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find all credits by customer id and return 200 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        creditRepository.save(buildCreditDto(customerId = customer.id!!, creditValue = BigDecimal.valueOf(2000)).toEntity())
        creditRepository.save(buildCreditDto(customerId = customer.id!!, creditValue = BigDecimal.valueOf(1000)).toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditCode").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditValue").value(2000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfInstallments").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].creditCode").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].creditValue").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfInstallments").value(4))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find any credit for invalid customer id and return status 200`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL?customerId=1")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by credit code and customer id and return 200 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val credit: Credit = creditRepository.save(buildCreditDto(customerId = customer.id!!).toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${credit.creditCode}?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(5000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(4))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find any credit for valid credit code but invalid customer id and return status 400`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val credit: Credit = creditRepository.save(buildCreditDto(customerId = customer.id!!).toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${credit.creditCode}?customerId=2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation..."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class java.lang.IllegalArgumentException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find any credit for invalid credit code but valid customer id and return status 400`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${UUID.randomUUID()}?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation..."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class java.lang.IllegalArgumentException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    private fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(5000.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusMonths(1),
        numberOfInstallments: Int = 4,
        customerId: Long = 1L,
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId,
    )

    private fun buildCustomerDto(
        firstName: String = "Matheus",
        lastName: String = "Dev",
        cpf: String = "32632437059",
        email: String = "matheus.dev@email.com",
        password: String = "senha123",
        zipCode: String = "12435000",
        street: String = "Matheus Street",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
    ) = CustomerDTO(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        zipCode = zipCode,
        street = street,
        income = income,
    )
}