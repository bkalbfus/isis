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
package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.ui.util.LocalResourceUtil;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.header.HeaderUiModelProvider;

import lombok.val;

/**
 * top-level view
 */
@Route("main")
@RouteAlias("")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@PWA(name = "Example Project", shortName = "Example Project")
//@Theme(value = Material.class, variant = Material.DARK)
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class MainViewVaa extends AppLayout 
implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;
    
    private final transient IsisAppCommonContext commonContext;
    private final transient UiActionHandler uiActionHandler;
    private final transient HeaderUiModelProvider headerUiModelProvider;
    private Div pageContent = new Div();
    
    /**
     * Constructs the main view of the web-application, with the menu-bar and page content. 
     */
    @Inject
    public MainViewVaa(
            final MetaModelContext metaModelContext,
            final UiActionHandler uiActionHandler,
            final HeaderUiModelProvider headerUiModelProvider) {

        this.commonContext = IsisAppCommonContext.of(metaModelContext);
        this.uiActionHandler = uiActionHandler;
        this.headerUiModelProvider = headerUiModelProvider;
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        val faStyleSheet = LocalResourceUtil.ResourceDescriptor.webjars(FontAwesomeDecorator.FONTAWESOME_RESOURCE);
        LocalResourceUtil.addStyleSheet(faStyleSheet);
        
        setPrimarySection(Section.NAVBAR);

        val menuBarContainer = MainView_createHeader.createHeader(
                commonContext, 
                headerUiModelProvider.getHeader(), 
                this::onActionLinkClicked);
        
        addToNavbar(menuBarContainer);
        setContent(pageContent = new Div());
        setDrawerOpened(false);
    }

    private void onActionLinkClicked(ManagedAction managedAction) {
        uiActionHandler.handleActionLinkClicked(managedAction, this::replaceContent);
    }
    
    private void replaceContent(Component component) {
        pageContent.removeAll();
        pageContent.add(component);
    }



    
}
