package com.aevrontech.finevo.domain.calculator

/**
 * Malaysian financial calculators for EPF, PCB, SOCSO, and EIS. Based on Malaysian government rates
 * (2024).
 */
object MalaysianCalculators {

    // ============================================
    // EPF (KWSP) CALCULATOR
    // ============================================

    /**
     * Calculate EPF contributions.
     *
     * @param grossSalary Monthly gross salary in MYR
     * @param age Employee age
     * @return EPFResult with employee and employer contributions
     */
    fun calculateEPF(grossSalary: Double, age: Int): EPFResult {
        // Employee rate: 11% for age ≤60, 0% for age >60 (can opt for 5.5%)
        val employeeRate =
                when {
                    age > 60 -> 0.0 // Optional contribution
                    else -> 0.11 // 11%
                }

        // Employer rate: 13% if salary > RM5000, 12% if salary ≤ RM5000
        val employerRate = if (grossSalary > 5000) 0.13 else 0.12

        val employeeContribution = grossSalary * employeeRate
        val employerContribution = grossSalary * employerRate
        val totalContribution = employeeContribution + employerContribution
        val netSalaryAfterEPF = grossSalary - employeeContribution

        return EPFResult(
                grossSalary = grossSalary,
                employeeRate = employeeRate,
                employerRate = employerRate,
                employeeContribution = employeeContribution,
                employerContribution = employerContribution,
                totalContribution = totalContribution,
                netSalaryAfterEPF = netSalaryAfterEPF
        )
    }

    // ============================================
    // SOCSO (PERKESO) CALCULATOR
    // ============================================

    /**
     * Calculate SOCSO contributions. Based on Categories 1 (Employment Injury + Invalidity) and 2
     * (Injury only for 60+).
     *
     * @param grossSalary Monthly gross salary in MYR
     * @param age Employee age
     * @return SOCSOResult with employee and employer contributions
     */
    fun calculateSOCSO(grossSalary: Double, age: Int): SOCSOResult {
        // SOCSO category: Category 1 for under 60, Category 2 for 60+
        val isCategory1 = age < 60

        // SOCSO contribution brackets (simplified)
        // Actual SOCSO uses detailed wage brackets, this is an approximation
        val employeeRate = if (isCategory1) 0.005 else 0.0 // 0.5% for Cat 1, 0% for Cat 2
        val employerRate = if (isCategory1) 0.0175 else 0.0125 // 1.75% Cat 1, 1.25% Cat 2

        // Maximum wage subject to SOCSO: RM5000
        val cappedSalary = minOf(grossSalary, 5000.0)

        val employeeContribution = cappedSalary * employeeRate
        val employerContribution = cappedSalary * employerRate
        val totalContribution = employeeContribution + employerContribution

        return SOCSOResult(
                grossSalary = grossSalary,
                category = if (isCategory1) 1 else 2,
                employeeContribution = employeeContribution,
                employerContribution = employerContribution,
                totalContribution = totalContribution
        )
    }

    // ============================================
    // EIS (SIP) CALCULATOR
    // ============================================

    /**
     * Calculate EIS (Employment Insurance System) contributions.
     *
     * @param grossSalary Monthly gross salary in MYR
     * @return EISResult with employee and employer contributions
     */
    fun calculateEIS(grossSalary: Double): EISResult {
        // EIS rate: 0.2% each for employee and employer
        // Maximum insured salary: RM5000
        val cappedSalary = minOf(grossSalary, 5000.0)

        val rate = 0.002 // 0.2%
        val employeeContribution = cappedSalary * rate
        val employerContribution = cappedSalary * rate
        val totalContribution = employeeContribution + employerContribution

        return EISResult(
                grossSalary = grossSalary,
                cappedSalary = cappedSalary,
                rate = rate,
                employeeContribution = employeeContribution,
                employerContribution = employerContribution,
                totalContribution = totalContribution
        )
    }

    // ============================================
    // PCB (MTD) CALCULATOR - Simplified
    // ============================================

    /**
     * Calculate PCB (Monthly Tax Deduction) using simplified method. For accurate calculation, use
     * LHDN MTD calculator.
     *
     * @param grossSalary Monthly gross salary in MYR
     * @param epfDeduction Monthly EPF employee contribution
     * @param maritalStatus Single, Married, or Married with spouse working
     * @param childrenCount Number of children for relief
     * @return PCBResult with estimated tax
     */
    fun calculatePCB(
            grossSalary: Double,
            epfDeduction: Double,
            maritalStatus: MaritalStatus = MaritalStatus.SINGLE,
            childrenCount: Int = 0
    ): PCBResult {
        // Calculate annual income
        val annualGross = grossSalary * 12

        // Apply reliefs
        val personalRelief = 9000.0 // Individual relief
        val spouseRelief =
                when (maritalStatus) {
                    MaritalStatus.MARRIED_SPOUSE_NOT_WORKING -> 4000.0
                    else -> 0.0
                }
        val childRelief = childrenCount * 2000.0 // RM2000 per child (under 18)
        val epfRelief = minOf(epfDeduction * 12, 4000.0) // Max RM4000
        val socsoRelief = minOf(grossSalary * 0.005 * 12, 350.0) // Max RM350

        val totalRelief = personalRelief + spouseRelief + childRelief + epfRelief + socsoRelief
        val chargeableIncome = maxOf(0.0, annualGross - totalRelief)

        // Calculate annual tax using Malaysian tax brackets (2024)
        val annualTax = calculateAnnualTax(chargeableIncome)

        // Apply rebates
        val rebate = if (chargeableIncome <= 35000) 400.0 else 0.0
        val netAnnualTax = maxOf(0.0, annualTax - rebate)

        // Monthly PCB
        val monthlyPCB = netAnnualTax / 12

        return PCBResult(
                grossSalary = grossSalary,
                annualGross = annualGross,
                totalRelief = totalRelief,
                chargeableIncome = chargeableIncome,
                annualTax = annualTax,
                rebate = rebate,
                netAnnualTax = netAnnualTax,
                monthlyPCB = monthlyPCB
        )
    }

    /** Calculate annual tax based on Malaysian progressive tax rates. */
    private fun calculateAnnualTax(chargeableIncome: Double): Double {
        // Malaysian tax brackets 2024
        val brackets =
                listOf(
                        TaxBracket(0.0, 5000.0, 0.0), // 0%
                        TaxBracket(5001.0, 20000.0, 0.01), // 1%
                        TaxBracket(20001.0, 35000.0, 0.03), // 3%
                        TaxBracket(35001.0, 50000.0, 0.06), // 6% (was 8% in some older versions)
                        TaxBracket(50001.0, 70000.0, 0.11), // 11%
                        TaxBracket(70001.0, 100000.0, 0.19), // 19%
                        TaxBracket(100001.0, 400000.0, 0.25), // 25%
                        TaxBracket(400001.0, 600000.0, 0.26), // 26%
                        TaxBracket(600001.0, 2000000.0, 0.28), // 28%
                        TaxBracket(2000001.0, Double.MAX_VALUE, 0.30) // 30%
                )

        var tax = 0.0
        var remainingIncome = chargeableIncome

        for (bracket in brackets) {
            if (remainingIncome <= 0) break

            val bracketWidth = bracket.max - bracket.min + 1
            val taxableInBracket = minOf(remainingIncome, bracketWidth)
            tax += taxableInBracket * bracket.rate
            remainingIncome -= taxableInBracket
        }

        return tax
    }

    // ============================================
    // ZAKAT CALCULATOR (Optional)
    // ============================================

    /**
     * Calculate Zakat on income (simplified nisab method).
     *
     * @param annualIncome Annual income in MYR
     * @param goldPricePerGram Current gold price per gram (for nisab calculation)
     * @return ZakatResult with amount payable
     */
    fun calculateZakat(annualIncome: Double, goldPricePerGram: Double = 280.0): ZakatResult {
        // Nisab = 85 grams of gold
        val nisab = 85 * goldPricePerGram

        // Zakat rate: 2.5%
        val zakatRate = 0.025

        val isEligible = annualIncome >= nisab
        val zakatAmount = if (isEligible) annualIncome * zakatRate else 0.0

        return ZakatResult(
                annualIncome = annualIncome,
                nisab = nisab,
                isEligible = isEligible,
                zakatRate = zakatRate,
                zakatAmount = zakatAmount
        )
    }

    // ============================================
    // COMBINED SALARY BREAKDOWN
    // ============================================

    /** Calculate complete salary breakdown with all deductions. */
    fun calculateSalaryBreakdown(
            grossSalary: Double,
            age: Int = 30,
            maritalStatus: MaritalStatus = MaritalStatus.SINGLE,
            childrenCount: Int = 0
    ): SalaryBreakdown {
        val epf = calculateEPF(grossSalary, age)
        val socso = calculateSOCSO(grossSalary, age)
        val eis = calculateEIS(grossSalary)
        val pcb = calculatePCB(grossSalary, epf.employeeContribution, maritalStatus, childrenCount)

        val totalDeductions =
                epf.employeeContribution +
                        socso.employeeContribution +
                        eis.employeeContribution +
                        pcb.monthlyPCB
        val netSalary = grossSalary - totalDeductions

        return SalaryBreakdown(
                grossSalary = grossSalary,
                epf = epf,
                socso = socso,
                eis = eis,
                pcb = pcb,
                totalDeductions = totalDeductions,
                netSalary = netSalary
        )
    }
}

// ============================================
// DATA CLASSES
// ============================================

data class EPFResult(
        val grossSalary: Double,
        val employeeRate: Double,
        val employerRate: Double,
        val employeeContribution: Double,
        val employerContribution: Double,
        val totalContribution: Double,
        val netSalaryAfterEPF: Double
)

data class SOCSOResult(
        val grossSalary: Double,
        val category: Int,
        val employeeContribution: Double,
        val employerContribution: Double,
        val totalContribution: Double
)

data class EISResult(
        val grossSalary: Double,
        val cappedSalary: Double,
        val rate: Double,
        val employeeContribution: Double,
        val employerContribution: Double,
        val totalContribution: Double
)

data class PCBResult(
        val grossSalary: Double,
        val annualGross: Double,
        val totalRelief: Double,
        val chargeableIncome: Double,
        val annualTax: Double,
        val rebate: Double,
        val netAnnualTax: Double,
        val monthlyPCB: Double
)

data class ZakatResult(
        val annualIncome: Double,
        val nisab: Double,
        val isEligible: Boolean,
        val zakatRate: Double,
        val zakatAmount: Double
)

data class SalaryBreakdown(
        val grossSalary: Double,
        val epf: EPFResult,
        val socso: SOCSOResult,
        val eis: EISResult,
        val pcb: PCBResult,
        val totalDeductions: Double,
        val netSalary: Double
)

enum class MaritalStatus {
    SINGLE,
    MARRIED_SPOUSE_WORKING,
    MARRIED_SPOUSE_NOT_WORKING
}

private data class TaxBracket(val min: Double, val max: Double, val rate: Double)
