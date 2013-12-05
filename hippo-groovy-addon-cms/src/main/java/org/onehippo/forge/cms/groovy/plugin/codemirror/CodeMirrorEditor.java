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
package org.onehippo.forge.cms.groovy.plugin.codemirror;

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 * Component that displays a CodeMirror panel which gives a nice syntax highlighting for Groovy.
 * @author Jeroen Reijn
 */
public class CodeMirrorEditor extends TextArea {

    private String markupId;

    public CodeMirrorEditor(final String id, final IModel iModel) {
        super(id, iModel);
        setOutputMarkupId(true);
        markupId = getMarkupId();
        add(JavascriptPackageResource.getHeaderContribution(CodeMirrorEditor.class, "v3_20/lib/codemirror.js"));
        add(CSSPackageResource.getHeaderContribution(CodeMirrorEditor.class, "v3_20/lib/codemirror.css"));
        add(CSSPackageResource.getHeaderContribution(CodeMirrorEditor.class, "v3_20/theme/eclipse.css"));
        add(JavascriptPackageResource.getHeaderContribution(CodeMirrorEditor.class, "v3_20/mode/groovy/groovy.js"));

        add(new AbstractBehavior() {
            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderOnLoadJavascript(getJavaScriptForEditor());
            }

        });
    }

    private String getJavaScriptForEditor() {
        StringBuffer jsInit = new StringBuffer();
        jsInit.append("var cm = CodeMirror.fromTextArea(document.getElementById('"+markupId+"'), {lineNumbers: true, matchBrackets: true, mode: \"text/x-groovy\", onChange: function(cm) { cm.save(); }});");
        return jsInit.toString();
    }
}
