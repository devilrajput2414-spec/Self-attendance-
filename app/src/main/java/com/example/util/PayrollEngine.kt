package com.example.util

import kotlin.math.max
import kotlin.math.min

object PayrollEngine {

    data class PayrollCalculationResult(
        val totalWorkingDays: Int,
        val daysPresent: Double,
        val paidHolidays: Double = 0.0,
        val basicSalary: Double,
        val grossEarnings: Double,
        val pfDeduction: Double,
        val ptDeduction: Double,
        val messDeduction: Double,
        val advanceRecovery: Double,
        val lwfDeduction: Double = 0.0,
        val esiDeduction: Double = 0.0,
        val coLoanDeduction: Double = 0.0,
        val tdsDeduction: Double = 0.0,
        val perExpDeduction: Double = 0.0,
        val totalDeductions: Double,
        val netPayable: Double
    )

    /**
     * Payroll Engine dynamic salary calculator
     * Formula: Net Payable = (Prorated Gross Salary with Paid Company Holidays) - (Sum of all Deductions)
     */
    fun calculateSalary(
        basicSalary: Double,
        totalWorkingDays: Int = 22,
        daysPresent: Double = 22.0,
        paidHolidays: Double = 0.0,
        pfPercentage: Double = 12.0,
        ptFixed: Double = 200.0,
        messCharges: Double = 0.0,
        advanceBalance: Double = 0.0,
        lwfCharges: Double = 0.0,
        esiCharges: Double = 0.0,
        coLoanInstallment: Double = 0.0,
        tdsAmount: Double = 0.0,
        perExpAmount: Double = 0.0,
        isFullMonth: Boolean = true
    ): PayrollCalculationResult {
        val grossEarnings = if (isFullMonth) {
            basicSalary
        } else {
            val days = max(1, totalWorkingDays)
            // Total Payable Days includes attendance present days + paid non-working company holidays
            val payableDays = min(days.toDouble(), daysPresent + paidHolidays)
            (basicSalary / days) * payableDays
        }

        // Provident Fund (PF): % of prorated gross earnings
        val pf = (grossEarnings * (pfPercentage / 100.0))

        // Professional Tax (PT): State slab fixed charge
        val pt = if (grossEarnings > 15000) ptFixed else 0.0

        // Mess charges
        val mess = messCharges

        // Labour Welfare Fund (LWF)
        val lwf = lwfCharges

        // E.S.I.
        val esi = if (esiCharges > 0) esiCharges else if (grossEarnings in 1.0..21000.0) grossEarnings * 0.0075 else 0.0

        // Co Loan
        val coLoan = coLoanInstallment

        // TDS
        val tds = tdsAmount

        // Personal Expense
        val perExp = perExpAmount

        // Advance Salary recovery
        val priorDeductions = pf + pt + mess + lwf + esi + coLoan + tds + perExp
        val advanceRecovery = min(advanceBalance, max(0.0, grossEarnings - priorDeductions))

        val totalDeductions = priorDeductions + advanceRecovery
        val netPayable = max(0.0, grossEarnings - totalDeductions)

        return PayrollCalculationResult(
            totalWorkingDays = totalWorkingDays,
            daysPresent = daysPresent,
            paidHolidays = paidHolidays,
            basicSalary = basicSalary,
            grossEarnings = grossEarnings,
            pfDeduction = pf,
            ptDeduction = pt,
            messDeduction = mess,
            advanceRecovery = advanceRecovery,
            lwfDeduction = lwf,
            esiDeduction = esi,
            coLoanDeduction = coLoan,
            tdsDeduction = tds,
            perExpDeduction = perExp,
            totalDeductions = totalDeductions,
            netPayable = netPayable
        )
    }

    fun formatCurrency(amount: Double): String {
        return String.format("₹%.0f", amount)
    }

    fun formatAmountToWords(amount: Double): String {
        val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        val num = amount.toLong()
        if (num == 0L) return "Zero Rupees Only"

        fun convertLessThanThousand(n: Int): String {
            var str = ""
            if (n % 100 < 20) {
                str = units[n % 100]
                val rem = n / 100
                if (rem > 0) str = units[rem] + " Hundred " + str
            } else {
                str = tens[(n % 100) / 10] + if (n % 10 != 0) " " + units[n % 10] else ""
                val rem = n / 100
                if (rem > 0) str = units[rem] + " Hundred " + str
            }
            return str.trim()
        }

        var n = num
        var result = ""

        val crore = n / 10000000
        n %= 10000000
        if (crore > 0) result += convertLessThanThousand(crore.toInt()) + " Crore "

        val lakh = n / 100000
        n %= 100000
        if (lakh > 0) result += convertLessThanThousand(lakh.toInt()) + " Lakh "

        val thousand = n / 1000
        n %= 1000
        if (thousand > 0) result += convertLessThanThousand(thousand.toInt()) + " Thousand "

        if (n > 0) result += convertLessThanThousand(n.toInt())

        return (result.trim() + " Rupees Only").replace(Regex("\\s+"), " ")
    }
}
