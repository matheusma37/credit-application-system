package me.dio.credit.application.system.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal

data class CustomerUpdateDto(
    @field:NotEmpty(message = "First name must to be present") val firstName: String,
    @field:NotEmpty(message = "Last name must to be present") val lastName: String,
    @field:NotNull(message = "Income must to be present") val income: BigDecimal,
    @field:NotEmpty(message = "Zip code must to be present") val zipCode: String,
    @field:NotEmpty(message = "Street must to be present") val street: String,
) {
    fun toEntity(customer: Customer): Customer = customer.also {
        it.firstName = this.firstName
        it.lastName = this.lastName
        it.income = this.income
        it.address.zipCode = this.zipCode
        it.address.street = this.street
    }
}
