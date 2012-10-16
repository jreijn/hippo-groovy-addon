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
package org.onehippo.forge.cms.groovy.plugin.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.forge.cms.groovy.plugin.domain.DetachableGroovyScriptModel;
import org.onehippo.forge.cms.groovy.plugin.domain.GroovyScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data provider that gets all available scripts from the scripts library
 * @author Jeroen Reijn
 */
public class GroovyScriptsDataProvider extends SortableDataProvider<GroovyScript> {

    private final static Logger logger = LoggerFactory.getLogger(GroovyScriptsDataProvider.class);
    private final static String QUERY_SCRIPTS_LIST = "content/scripts/groovy//element(*)[@script] order by @jcr:score";
    private static transient List<GroovyScript> scriptList = new ArrayList<GroovyScript>();
    private static volatile boolean dirty = true;
    private static String sessionId = "none";

    public GroovyScriptsDataProvider() {

    }

    @Override
    public Iterator iterator(final int first, final int count) {
        populateScriptList();
        return scriptList.iterator();
    }

    @Override
    public int size() {
        populateScriptList();
        return scriptList.size();
    }

    @Override
    public IModel<GroovyScript> model(final GroovyScript groovyScript) {
        return new DetachableGroovyScriptModel(groovyScript);
    }

    /**
     * Actively invalidate cached list
     */
    public static void setDirty() {
        dirty = true;
    }


    /**
     * Populate list
     */
    private void populateScriptList() {
        synchronized (GroovyScriptsDataProvider.class) {
            if (!dirty && sessionId.equals(Session.get().getId())) {
                return;
            }

            scriptList.clear();
            NodeIterator iter;
            try {
                Query listQuery = ((UserSession) Session.get()).getQueryManager().createQuery(QUERY_SCRIPTS_LIST, Query.XPATH);
                iter = listQuery.execute().getNodes();
                while (iter.hasNext()) {
                    Node node = iter.nextNode();
                    if (node != null) {
                        try {
                            scriptList.add(new GroovyScript(node));
                        } catch (RepositoryException e) {
                            logger.warn("Unable to instantiate new Groovy script.", e);
                        }
                    }
                }
                Collections.sort(scriptList);
                sessionId = Session.get().getId();
                dirty = false;
            } catch (RepositoryException e) {
                logger.error("Error while trying to query groovy script nodes.", e);
            }
        }
    }

}
