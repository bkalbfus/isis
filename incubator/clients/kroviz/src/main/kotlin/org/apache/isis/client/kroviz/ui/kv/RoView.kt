/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.utils.IconManager
import io.kvision.core.BsBorder
import io.kvision.core.Component
import io.kvision.core.addBsBorder
import io.kvision.panel.SimplePanel

/**
 * Area between menu bar at the top and the status bar at the bottom.
 * Contains:
 * @Item TabPanel with Tabs
 */
object RoView {
    val tabPanel = RoTabPanel()
    private var tabCount = 0

    fun addTab(
            title: String,
            panel: Component) {
        panel.addBsBorder(BsBorder.BORDER)
        val index = tabPanel.findTab(title)
        if (index != null) {
            val tabs = tabPanel.getChildren()
            val tab = tabs.get(index) as SimplePanel
            removeTab(tab)
            tabPanel.removeTab(index)
        }

        val icon = IconManager.find(title)

        tabPanel.addTab(
                title,
                panel,
                icon,
                image = null,
                closable = true)
        tabPanel.activeIndex = tabCount
        tabCount += 1
    }

    fun removeTab(tab: SimplePanel) {
        tabCount--
        UiManager.closeView(tab)
    }

    fun findActive(): SimplePanel? {
        val index = tabPanel.activeIndex
        if (index > 0) {
            val tabs = tabPanel.getChildren()
            val tab = tabs.get(index) as SimplePanel
            return (tab)
        }
        return null
    }

}
