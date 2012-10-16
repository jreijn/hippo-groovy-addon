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
package org.onehippo.forge.cms.groovy.plugin.domain;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.Session;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detachable version of a GroovyScript model representing a {@link Node} inside the repository
 * @author Jeroen Reijn
 */
public class DetachableGroovyScriptModel extends LoadableDetachableModel{

    private final static Logger logger = LoggerFactory.getLogger(DetachableGroovyScriptModel.class);

    private final String path;

    public DetachableGroovyScriptModel(final GroovyScript script) {
        this(script.getPath());
    }

    public DetachableGroovyScriptModel(final String path) {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.path = path.startsWith("/") ? path.substring(1) : path;
    }

    protected Node getRootNode() throws RepositoryException {
        return ((UserSession) Session.get()).getJcrSession().getRootNode();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
     *
     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof DetachableGroovyScriptModel) {
            DetachableGroovyScriptModel other = (DetachableGroovyScriptModel) obj;
            return path.equals(other.path);
        }
        return false;
    }
    @Override
    protected Object load() {
        try {
            return new GroovyScript(getRootNode().getNode(path));
        } catch (RepositoryException e) {
            logger.error("Unable to load script, returning null", e);
            return null;
        }
    }
}
