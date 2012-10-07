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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugins.standards.panelperspective.breadcrumb.PanelPluginBreadCrumbPanel;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.forge.cms.groovy.plugin.domain.GroovyScript;
import org.onehippo.forge.cms.groovy.plugin.provider.GroovyScriptsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management plugin for Groovy scripts stored in the repository
 *
 * @author Jeroen Reijn
 */
public class GroovyScriptManagementPanel extends PanelPluginBreadCrumbPanel {

    private final static Logger logger = LoggerFactory.getLogger(GroovyScriptManagementPanel.class);

    private final FileUploadField fileUpload;
    private final GroovyScriptsDataProvider groovyScriptsDataProvider = new GroovyScriptsDataProvider();

    private GroovyScript groovyScript;
    private final Form form;

    public GroovyScriptManagementPanel(String id, IBreadCrumbModel breadCrumbModel) {
        super(id, breadCrumbModel);

        form = new Form("form");

        List<IColumn<?>> columns = new ArrayList<IColumn<?>>();

        columns.add(new PropertyColumn(new Model<String>("Name"), "name","name"));
        columns.add(new PropertyColumn(new Model<String>("Path"), "path"));
        columns.add(new AbstractColumn(new Model<String>("Actions")) {
            public void populateItem(Item cellItem,
                                     String componentId, IModel rowModel) {
                GroovyScript groovyScript = ((GroovyScript)
                        rowModel.getObject());
                cellItem.add(new
                        ActionPanel(componentId, groovyScript));
            }
        });

        DataTable table = new DefaultDataTable("datatable", columns, groovyScriptsDataProvider, 10);

        form.add(new AjaxButton("ajax-upload", form) {

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                final FileUpload uploadedFile = fileUpload.getFileUpload();
                if (uploadedFile != null) {
                    storeUploadedGroovyScript(uploadedFile);
                    target.addComponent(form);
                }
            }
        });

        form.setMultiPart(true);
        form.setOutputMarkupId(true);
        fileUpload = new FileUploadField("fileUpload");
        form.add(fileUpload);
        table.setOutputMarkupId(true);
        form.add(table);
        form.setOutputMarkupId(true);
        add(form);
    }

    private void storeUploadedGroovyScript(final FileUpload uploadedFile) {
        String clientFileName = uploadedFile.getClientFileName();
        GroovyScript groovyScript = new GroovyScript(clientFileName);
        try {
            InputStream inputStream = uploadedFile.getInputStream();
            String script = IOUtils.toString(inputStream, "UTF-8");
            groovyScript.setScript(script);
            groovyScript.create();
            groovyScriptsDataProvider.setDirty();
        } catch (IOException ioe) {
            logger.error("An exception occurred while trying to get script with name: {}", clientFileName);
        } catch (RepositoryException e) {
            logger.error("An exception occurred while trying to store the script with name: {}", clientFileName);
        }
    }

    @Override
    public IModel<String> getTitle(final Component component) {
        return new ResourceModel("groovy-script-management-panel-title");
    }

    class ActionPanel extends Panel {
        public ActionPanel(final String componentId, final GroovyScript groovyScript) {
            super(componentId);

            add(new AjaxLink("delete") {

                @Override
                public void onClick(final AjaxRequestTarget ajaxRequestTarget) {
                    UserSession userSession = (UserSession) Session.get();
                    javax.jcr.Session jcrSession = userSession.getJcrSession();
                    Node node = null;
                    try {
                        node = jcrSession.getRootNode().getNode(groovyScript.getPath());
                        node.remove();
                        jcrSession.save();
                        groovyScriptsDataProvider.setDirty();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    ajaxRequestTarget.addComponent(form);
                }
            });

        }
    }



}
