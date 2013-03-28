package com.objectgraph.utils;

import java.util.ArrayList;

public class StringList extends ArrayList<String> {

    private static final long serialVersionUID = 1L;

    public StringList(String... strings) {
        for (String s : strings)
            add(s);
    }

}
