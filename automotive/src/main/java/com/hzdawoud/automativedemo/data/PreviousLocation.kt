package com.hzdawoud.automativedemo.data

import android.location.Location
import java.util.Date

data class PreviousLocation(
    val location: Location,
    val timestamp: Date
)
