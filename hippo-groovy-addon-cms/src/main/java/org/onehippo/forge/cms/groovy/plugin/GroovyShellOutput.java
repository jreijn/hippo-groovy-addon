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
package org.onehippo.forge.cms.groovy.plugin;

import groovy.lang.GroovySystem;

/**
 * Wrapper used for the output of the groovy shell output.
 * @author Jeroen Reijn
 */
public class GroovyShellOutput {

    private static final String GROOVY_VERSION = GroovySystem.getVersion();

    private StringBuffer output;

    public GroovyShellOutput() {
        output = new StringBuffer();
    }

    public void printVersion() {
        output.append("Running Groovy version: " + GROOVY_VERSION);
    }

    public void print() {
        output.append("");
    }

    public void print(Object o) {
        output.append(o.toString());
    }

    public void println() {
        output.append("\n");
    }

    public void println(Object o) {
        output.append("\n");
        output.append(o.toString());
    }

    public void printf(String format, Object o) {
        output.append(o.toString());
    }
    
    public String getOutput() {
        return output.toString();
    }

    @Override
    public String toString() {
        return output.toString();
    }

    public void clear() {
        output = new StringBuffer();
    }
}
