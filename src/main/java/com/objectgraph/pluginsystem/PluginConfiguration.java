/*
 * Copyright 2013 Emanuele Tamponi
 *
 * This file is part of object-graph.
 *
 * object-graph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * object-graph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.objectgraph.pluginsystem;

import com.objectgraph.core.ListNode;
import com.objectgraph.core.ObjectNode;

import java.io.File;

public class PluginConfiguration extends ObjectNode {

    @Property
    protected ListNode<File> libraries = new ListNode<>(File.class);
    @Property
    protected ListNode<String> packages = new ListNode<>(String.class);

    public PluginConfiguration(String... packages) {
        for (String p : packages)
            this.packages.add(p);

        initialiseNode();
    }

    public ListNode<File> getLibraries() {
        return libraries;
    }

    public ListNode<String> getPackages() {
        return packages;
    }

}
