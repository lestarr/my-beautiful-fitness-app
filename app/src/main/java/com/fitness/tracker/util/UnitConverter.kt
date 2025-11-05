package com.fitness.tracker.util

object UnitConverter {
    private const val KG_TO_LBS = 2.20462

    fun kgToLbs(kg: Double): Double = kg * KG_TO_LBS

    fun lbsToKg(lbs: Double): Double = lbs / KG_TO_LBS

    fun formatWeight(weight: Double, useKg: Boolean): String {
        val converted = if (useKg) weight else kgToLbs(weight)
        val unit = if (useKg) "kg" else "lbs"
        return "%.1f %s".format(converted, unit)
    }
}
