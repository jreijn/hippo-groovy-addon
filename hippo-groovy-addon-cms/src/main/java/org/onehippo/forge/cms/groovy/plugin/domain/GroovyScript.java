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
import javax.jcr.nodetype.NodeType;

import org.apache.wicket.IClusterable;
import org.apache.wicket.Session;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;

/**
 * Simple domain object representing a Groovy script.
 * @author Jeroen Reijn
 */
public class GroovyScript implements Comparable, IClusterable{

    private final static String STORAGE_LOCATION = "content/scripts/groovy/";

    private String name;
    private String script;
    private String path;
    private transient Node node;

    public GroovyScript(final String name) {
        this.name = name;
    }

    public GroovyScript(Node node) throws RepositoryException {
        this.path = node.getPath().substring(1);
        this.name = NodeNameCodec.decode(node.getName());
        this.script = node.getProperty("script").getString();
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }

    public String toString()
    {
        return "[GroovyScript name=" + name + " script=" + script + "]";
    }

    @Override
    public int compareTo(final Object o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //-------------------- persistence helpers ----------//
    /**
     * Create a new groovyscript
     * @throws RepositoryException
     */
    public void create() throws RepositoryException {
        StringBuilder relPath = new StringBuilder(STORAGE_LOCATION);
        relPath.append(NodeNameCodec.encode(getName(), true));
        node = ((UserSession) Session.get()).getRootNode().addNode(relPath.toString(), NodeType.NT_UNSTRUCTURED);
        node.setProperty("script",getScript());
        // save parent when adding a node
        node.getParent().getSession().save();
    }

    public void delete() throws RepositoryException {
        node.remove();
    }

}
