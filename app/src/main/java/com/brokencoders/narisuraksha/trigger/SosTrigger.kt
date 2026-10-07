package com.brokencoders.narisuraksha.trigger

import kotlinx.coroutines.flow.Flow

interface SosTrigger {
    val events: Flow<SosEvent>
}
