package me.dio.credit.application.system.dto.request

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import org.hibernate.validator.constraints.Range
import java.math.BigDecimal
import java.time.LocalDate

data class CreditDto(
    @field:NotNull(message = "Credit value must to be present") val creditValue: BigDecimal,
    @field:Future(message = "Day of first installment must to be in the future") val dayFirstInstallment: LocalDate,
    @field:Range(
        min = 1,
        max = 48,
        message = "The maximum number of installments allowed is between 1 and 48"
    ) val numberOfInstallments: Int,
    @field:NotNull(message = "Customer id must to be present") val customerId: Long,
) {
    fun toEntity(): Credit = Credit(
        creditValue = this.creditValue,
        dayFirstInstallment = this.dayFirstInstallment,
        numberOfInstallments = this.numberOfInstallments,
        customer = Customer(id = this.customerId),
    )
}
