/**
 * Copyright (C) 2011 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.cms.groovy.plugin.panels;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.panelperspective.breadcrumb.PanelPluginBreadCrumbPanel;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.forge.cms.groovy.plugin.GroovyShellOutput;
import org.onehippo.forge.cms.groovy.plugin.codemirror.CodeMirrorEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Panel for executing Groovy scripts
 *
 * @author Jeroen Reijn
 */
public class GroovyShellPanel extends PanelPluginBreadCrumbPanel {

    private final Form form;
    private final TextArea script;
    private GroovyShellOutput output = new GroovyShellOutput();

    public GroovyShellPanel(final String componentId, final IBreadCrumbModel breadCrumbModel) {
        super(componentId, breadCrumbModel);

        final CompoundPropertyModel<GroovyShellOutput> compoundPropertyModel = new CompoundPropertyModel<GroovyShellOutput>(output);

        final Label shellFeedback = new Label("output", compoundPropertyModel);
        shellFeedback.setOutputMarkupId(true);

        form = new Form("shellform");

        // add a button that can be used to submit the form via ajax
        form.add(new AjaxButton("ajax-button", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                GroovyShell shell = new GroovyShell();
                Script groovyScript = shell.parse(GroovyShellPanel.this.getScript());
                if (Session.exists()) {
                    UserSession userSession = (UserSession) Session.get();
                    groovyScript.setProperty("session", userSession.getJcrSession());
                    GroovyShellOutput shellOutput = compoundPropertyModel.getObject();
                    groovyScript.setProperty("out", shellOutput);
                    groovyScript.setProperty("err", shellOutput);
                }
                groovyScript.run();
                target.addComponent(shellFeedback);
            }

        });

        form.setOutputMarkupId(true);
        output.printVersion();
        script = new CodeMirrorEditor("script", new Model(""));
        shellFeedback.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        form.add(shellFeedback);
        form.add(script);
        add(form);
    }

    public IModel<String> getTitle(final Component component) {
        return new ResourceModel("groovy-shell-panel-title");
    }

    public String getScript() {
        return (String) script.getModelObject();
    }
}
