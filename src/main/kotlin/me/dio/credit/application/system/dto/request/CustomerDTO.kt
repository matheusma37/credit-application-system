package me.dio.credit.application.system.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import org.hibernate.validator.constraints.br.CPF
import java.math.BigDecimal

data class CustomerDTO(
    @field:NotEmpty(message = "First name must to be present") val firstName: String,
    @field:NotEmpty(message = "Last name must to be present") val lastName: String,
    @field:NotEmpty(message = "CPF must to be present")
    @field:CPF(message = "CPF invalid")
    val cpf: String,
    @field:NotNull(message = "Income must to be present") val income: BigDecimal,
    @field:NotEmpty(message = "Email must to be present")
    @field:Email(message = "Email invalid")
    val email: String,
    @field:NotEmpty(message = "Password must to be present") val password: String,
    @field:NotEmpty(message = "Zip code must to be present") val zipCode: String,
    @field:NotEmpty(message = "Street must to be present") val street: String,
) {
    fun toEntity(): Customer = Customer(
        firstName = this.firstName,
        lastName = this.lastName,
        cpf = this.cpf,
        income = this.income,
        email = this.email,
        password = this.password,
        address = Address(
            zipCode = this.zipCode,
            street = this.street,
        ),
    )
}
