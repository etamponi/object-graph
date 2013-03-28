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
