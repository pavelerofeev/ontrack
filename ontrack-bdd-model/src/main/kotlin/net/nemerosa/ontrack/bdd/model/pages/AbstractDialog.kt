package net.nemerosa.ontrack.bdd.model.pages

import net.serenitybdd.core.pages.WebElementFacade

abstract class AbstractDialog<D : AbstractDialog<D>>(parent: AbstractPage) : AbstractModule(parent) {

    fun waitFor(): D {
        parent.element<WebElementFacade>(".ot-dialog-ok").waitUntilVisible<WebElementFacade>()
        @Suppress("UNCHECKED_CAST")
        return this as D
    }

    fun ok() {
//        assert(okButton.isEnabled)
//        okButton.click()
    }

}