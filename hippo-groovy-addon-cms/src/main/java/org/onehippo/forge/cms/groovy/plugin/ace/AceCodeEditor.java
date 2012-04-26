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
package org.onehippo.forge.cms.groovy.plugin.ace;

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 * Code editor based on ACE
 * @author Jeroen Reijn
 */
public class AceCodeEditor extends TextArea {

    private String markupId;

    public AceCodeEditor(String id, IModel iModel) {
        super(id, iModel);
        setOutputMarkupId(true);
        markupId = getMarkupId();
        add(JavascriptPackageResource.getHeaderContribution(AceCodeEditor.class, "ace.js"));
        add(JavascriptPackageResource.getHeaderContribution(AceCodeEditor.class, "theme-eclipse.js"));
        add(JavascriptPackageResource.getHeaderContribution(AceCodeEditor.class, "mode-groovy.js"));

        add(new AbstractBehavior() {

            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderOnLoadJavascript(getJavaScriptForEditor());
            }

        });
    }

    private String getJavaScriptForEditor() {
        String callback = "function(){\n" +
                "// Get the value from the editor and place it into the textarea.\n" +
                "var text = editor.getSession().getValue();\n" +
                "textarea.value = text;\n" +
                "}";
        StringBuffer jsInit = new StringBuffer();
        jsInit
                .append("var textarea = document.getElementById('"+markupId+"');")
                .append("var editor = ace.edit(\"_ace\");\n")
                .append("editor.setTheme(\"ace/theme/eclipse\");\n")
                .append("var Mode = require(\"ace/mode/groovy\").Mode;\n")
                .append("editor.getSession().setMode(new Mode());")
                .append("editor.getSession().on('change', " + callback + ");");

        return jsInit.toString();
    }
}
