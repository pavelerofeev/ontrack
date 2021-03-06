package net.nemerosa.ontrack.extension.indicators.ui

import net.nemerosa.ontrack.extension.indicators.acl.IndicatorTypeManagement
import net.nemerosa.ontrack.ui.resource.*
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on

@Component
class ProjectIndicatorTypeResourceDecorator : AbstractLinkResourceDecorator<ProjectIndicatorType>(ProjectIndicatorType::class.java) {

    override fun getLinkDefinitions(): Iterable<LinkDefinition<ProjectIndicatorType>> = listOf(

            Link.UPDATE linkTo { t: ProjectIndicatorType ->
                on(IndicatorTypeController::class.java).getUpdateForm(t.id)
            } linkIf { t: ProjectIndicatorType, rc: ResourceContext ->
                t.source == null && rc.isGlobalFunctionGranted(IndicatorTypeManagement::class.java)
            },

            Link.DELETE linkTo { t: ProjectIndicatorType ->
                on(IndicatorTypeController::class.java).deleteType(t.id)
            } linkIf { t: ProjectIndicatorType, rc: ResourceContext ->
                t.source == null && rc.isGlobalFunctionGranted(IndicatorTypeManagement::class.java)
            }

    )

}