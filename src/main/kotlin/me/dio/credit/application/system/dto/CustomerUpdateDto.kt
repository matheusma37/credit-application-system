package me.dio.credit.application.system.dto

import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal

data class CustomerUpdateDto(
    val firstName: String,
    val lastName: String,
    val income: BigDecimal,
    val zipCode: String,
    val street: String,
) {
    fun toEntity(customer: Customer): Customer = customer.also {
        it.firstName = this.firstName
        it.lastName = this.lastName
        it.income = this.income
        it.address.zipCode = this.zipCode
        it.address.street = this.street
    }
}
