package nl.evanv.app

import kotlin.math.max
import kotlin.math.min

fun clamp(min: Double, value: Double, max: Double) =
    max(min, min(max, value))
