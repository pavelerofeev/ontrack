package net.nemerosa.ontrack.extension.indicators.model

import com.fasterxml.jackson.databind.JsonNode
import net.nemerosa.ontrack.model.extension.Extension
import net.nemerosa.ontrack.model.form.Form
import net.nemerosa.ontrack.model.structure.NameDescription

interface IndicatorValueType<T, C> : Extension {

    fun form(nameDescription: NameDescription, config: C, value: T?): Form

    fun status(config: C, value: T): IndicatorCompliance

    fun toClientJson(config: C, value: T): JsonNode
    fun fromClientJson(config: C, value: JsonNode): T?

    fun fromStoredJson(valueConfig: C, value: JsonNode): T?
    fun toStoredJson(config: C, value: T): JsonNode

}
