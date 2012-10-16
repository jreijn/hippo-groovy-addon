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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.time.Duration;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.hippoecm.frontend.plugins.standards.panelperspective.breadcrumb.PanelPluginBreadCrumbPanel;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.forge.cms.groovy.plugin.GroovyShellOutput;
import org.onehippo.forge.cms.groovy.plugin.codemirror.CodeMirrorEditor;
import org.onehippo.forge.cms.groovy.plugin.domain.GroovyScript;
import org.onehippo.forge.cms.groovy.plugin.provider.GroovyScriptsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Panel for executing Groovy scripts
 *
 * @author Jeroen Reijn
 */
public class GroovyShellPanel extends PanelPluginBreadCrumbPanel {

    private final static Logger logger = LoggerFactory.getLogger(GroovyShellPanel.class);

    private final TextArea textArea;
    private final FileUploadField fileUpload;
    private GroovyShellOutput output = new GroovyShellOutput();
    private GroovyShell shell;
    private GroovyScript selectedScript = null;

    public GroovyShellPanel(final String componentId, final IBreadCrumbModel breadCrumbModel) {
        super(componentId, breadCrumbModel);

        shell = getMinimalSecuredGroovyShell();

        final CompoundPropertyModel<GroovyShellOutput> compoundPropertyModel = new CompoundPropertyModel<GroovyShellOutput>(output);

        final Label shellFeedback = new Label("output", compoundPropertyModel);
        shellFeedback.setOutputMarkupId(true);

        GroovyScriptsDataProvider groovyScriptsDataProvider = new GroovyScriptsDataProvider();

        final Form form = new Form("shellform");

        form.add(new AjaxButton("ajax-upload", form) {

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                final FileUpload uploadedFile = fileUpload.getFileUpload();
                if (uploadedFile != null) {
                    try {
                        String uploadedScript = IOUtils.toString(uploadedFile.getInputStream());
                        if (!StringUtils.isEmpty(uploadedScript)) {
                            GroovyShellPanel.this.setScript(uploadedScript);
                        }
                    } catch (IOException e) {
                        logger.warn("An exception occurred while trying to parse the uploaded script: {}", e);
                    }
                    target.addComponent(form);
                }
            }
        });

        final List<GroovyScript> groovyScripts = getAvailableGroovyScriptsFromStore(groovyScriptsDataProvider);

        addAvailableScriptsInDropdownToForm(form, groovyScripts);

        // add a button that can be used to submit the form via ajax
        form.add(new AjaxButton("ajax-button", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> currentForm) {

                String scriptAsString = GroovyShellPanel.this.getScript();
                Script groovyScript = shell.parse(scriptAsString);

                if (Session.exists()) {
                    UserSession userSession = (UserSession) Session.get();
                    groovyScript.setProperty("session", userSession.getJcrSession());
                    GroovyShellOutput shellOutput = compoundPropertyModel.getObject();
                    groovyScript.setProperty("out", shellOutput);
                }
                try {
                    groovyScript.run();
                } catch (Exception e) {
                    // catch the exception and make it visible for the end user instead of directing it to the log.
                    output.println(e);
                }
                target.addComponent(shellFeedback);
            }
        });

        form.setMultiPart(true);
        fileUpload = new FileUploadField("fileUpload");
        form.add(fileUpload);

        form.setOutputMarkupId(true);
        output.printVersion();
        textArea = new CodeMirrorEditor("script", new Model(""));
        textArea.setOutputMarkupId(true);
        shellFeedback.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        form.add(shellFeedback);
        form.add(textArea);
        add(form);
    }

    private List<GroovyScript> getAvailableGroovyScriptsFromStore(final GroovyScriptsDataProvider groovyScriptsDataProvider) {
        final List<GroovyScript> groovyScripts = new ArrayList<GroovyScript>();
        Iterator iterator = groovyScriptsDataProvider.iterator(0, 10);
        while(iterator.hasNext()) {
            GroovyScript script = (GroovyScript) iterator.next();
            groovyScripts.add(script);
        }
        return groovyScripts;
    }

    private void addAvailableScriptsInDropdownToForm(final Form form, final List<GroovyScript> groovyScripts) {
        final DropDownChoice<GroovyScript> scriptDropDownChoice = new DropDownChoice<GroovyScript>("scripts",
                new PropertyModel<GroovyScript>(this, "selectedScript") , groovyScripts, new IChoiceRenderer() {

            @Override
            public Object getDisplayValue(final Object object) {
                GroovyScript script = (GroovyScript) object;
                return script.getName();
            }

            @Override
            public String getIdValue(final Object object, final int i) {
                GroovyScript script = (GroovyScript) object;
                return script.getPath();
            }
        }) {
        };

        scriptDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(AjaxRequestTarget target) {

            }
        });

        AjaxButton loadScript = new AjaxButton("load-script", form) {

            @Override
            protected void onSubmit(final AjaxRequestTarget ajaxRequestTarget, final Form<?> form) {
                GroovyScript groovyScript = scriptDropDownChoice.getModelObject();
                if(groovyScript != null) {
                    GroovyShellPanel.this.setScript(groovyScript.getScript());
                }
                ajaxRequestTarget.addComponent(form);
            }
        };
        form.add(loadScript);

        scriptDropDownChoice.setOutputMarkupId(true);
        form.add(scriptDropDownChoice);
        if(groovyScripts.size() == 0) {
            scriptDropDownChoice.setVisible(false);
            loadScript.setVisible(false);
        }
    }

    private GroovyShell getMinimalSecuredGroovyShell() {
        final SecureASTCustomizer customizer = new SecureASTCustomizer();
        customizer.setImportsBlacklist(unmodifiableList(asList(
                "java.lang.System", "groovy.lang.GroovyShell",
                "groovy.lang.GroovyClassLoader")));
        customizer.setIndirectImportCheckEnabled(true);

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(customizer);
        return new GroovyShell(configuration);
    }

    public IModel<String> getTitle(final Component component) {
        return new ResourceModel("groovy-shell-panel-title");
    }

    public String getScript() {
        return (String) textArea.getModelObject();
    }

    public void setScript(String newScript) {
        textArea.setModelObject(newScript);
    }
}
