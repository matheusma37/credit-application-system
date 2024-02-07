package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CustomerDTO
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
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

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerControllerTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/customers"
    }

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should create a customer and return 201 status`() {
        val customerDTO: CustomerDTO = buildCustomerDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Matheus"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Dev"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("32632437059"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("matheus.dev@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12435000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Matheus Street"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with previously used CPF and return 409 status`() {
        customerRepository.save(buildCustomerDto().toEntity())
        val customerDTO: CustomerDTO = buildCustomerDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict! Consult the documentation..."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.dao.DataIntegrityViolationException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with firstName empty and return 400 status`() {
        val customerDTO: CustomerDTO = buildCustomerDto(firstName = "")
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)

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
    fun `should find customer by id and return 200 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Matheus"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Dev"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("32632437059"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("matheus.dev@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12435000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Matheus Street"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customer.id))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find customer with invalid id and return 400 status`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/1")
                .accept(MediaType.APPLICATION_JSON)
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
    fun `should delete customer by id and return 204 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not delete customer with invalid id and return 400 status`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("$URL/1")
                .accept(MediaType.APPLICATION_JSON)
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
    fun `should update a customer and return 200 status`() {
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val customerUpdateDto: CustomerUpdateDto = buildCustomerUpdateDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)

        mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$URL?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("MatheusUpdate"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("DevUpdate"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("32632437059"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("matheus.dev@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(5000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("45656000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Matheus Street Updated"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customer.id))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not update a customer with invalid customer id and return 400 status`() {
        val customerUpdateDto: CustomerUpdateDto = buildCustomerUpdateDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)

        mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$URL?customerId=1")
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

    private fun buildCustomerUpdateDto(
        firstName: String = "MatheusUpdate",
        lastName: String = "DevUpdate",
        income: BigDecimal = BigDecimal.valueOf(5000.0),
        zipCode: String = "45656000",
        street: String = "Matheus Street Updated"
    ): CustomerUpdateDto = CustomerUpdateDto(
        firstName = firstName,
        lastName = lastName,
        income = income,
        zipCode = zipCode,
        street = street
    )
}