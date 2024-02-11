package org.example

import kotlin.math.ln
import kotlin.random.Random

object Probability {
    private var alpha = 0L

    private var beta = 0L

    private var lambda = 0.0

    fun setProbability(alpha: Long, beta: Long, lambda: Double) {
        this.alpha = alpha
        this.beta = beta
        this.lambda = lambda
    }

    fun wait() : Long = beta

    fun uniformDistribution(): Long = alpha + ((beta - alpha) * Random.nextDouble(0.0, 1.0)).toLong()

    fun expDistribution(): Long = (-1 / lambda * ln(1 - Random.nextDouble(0.0, 1.0))).toLong()
}