package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.core.model.DisplayModel
import org.ro.to.Link
import org.ro.to.TObject

/**
 * An Aggregator:
 * @item is initially created in ResponseHandlers, displayModels, Menus
 * @item is assigned to at least one LogEntry,
 * @item is passed on to related LogEntries (eg. sibblings in a list, Layout),
 * @item is notified about changes to related LogEntries,
 * @item invokes subsequent links, and
 * @item triggers creation a view for an object or a list.
 *
 * @see: https://www.enterpriseintegrationpatterns.com/patterns/messaging/Aggregator.html
 *
 * Could be named collector or assembler as well.
 */
abstract class BaseAggregator {

    open lateinit var dsp: DisplayModel

    open fun update(logEntry: LogEntry, subType: String) {}

    open fun reset(): BaseAggregator {
        /* do nothing and */ return this
    }

    open fun getObject(): TObject? {
        return null
    }

    protected fun log(logEntry: LogEntry) {
        logEntry.setUndefined("no handler found")
        console.log("[BaseAggregator.log] no handler found: ${this::class.simpleName}")
        console.log(logEntry.response)
    }

    @Deprecated("use extension function")
    fun invokeWith(link: Link) {
        RoXmlHttpRequest().invoke(link, this)
    }

    fun TObject.getLayoutLink(): Link? {
        var href: String?
        for (l in links) {
            href = l.href
            if (href.isNotEmpty() && href.contains("layout")) {
                return l
            }
        }
        return null
    }

    fun noop() {
        // save a line break in when formatting
    }

    fun Link.invokeWith(aggregator: BaseAggregator, subType: String = "json") {
        RoXmlHttpRequest().invoke(this, aggregator, subType)
    }

}
