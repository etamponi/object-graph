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

package com.objectgraph.utils;

public final class PathUtils {

    private static final String SPLITTER = "\\.";
    private static final String ANY = "*";

    private PathUtils() {
    }

    public static String appendPath(String leftSide, String rightSide) {
        if (rightSide.isEmpty()) {
            return leftSide;
        } else if (leftSide.isEmpty()) {
            return rightSide;
        } else {
            return leftSide + "." + rightSide;
        }
    }

    public static String toLocalProperty(String path) {
        int lastIndex = path.lastIndexOf('.');
        if (lastIndex < 0 || path.isEmpty()) {
            return path;
        } else {
            return path.substring(lastIndex + 1);
        }
    }

    public static boolean isParent(String prefixPath, String path) {
        if (path.isEmpty()) {
            return false;
        }
        if (prefixPath.isEmpty()) {
            return path.indexOf('.') < 0;
        }

        String[] prefixTokens = prefixPath.split(SPLITTER);
        String[] pathTokens = path.split(SPLITTER);
        if ((prefixTokens.length + 1) != pathTokens.length) {
            return false;
        }
        return standardPathComparison(prefixTokens, pathTokens, prefixTokens.length);
    }

    public static boolean isPrefix(String prefixPath, String fullPath) {
        if (prefixPath.isEmpty()) {
            return true;
        }
        if (fullPath.isEmpty()) {
            return prefixPath.isEmpty();
        }

        String[] prefixTokens = prefixPath.split(SPLITTER);
        String[] pathTokens = fullPath.split(SPLITTER);
        if (prefixTokens.length > pathTokens.length) {
            return false;
        }
        return standardPathComparison(prefixTokens, pathTokens, prefixTokens.length);
    }

    public static boolean samePrefix(String path1, String path2) {
        if (path1.isEmpty() || path2.isEmpty()) {
            return true;
        }
        String[] tokens1 = path1.split(SPLITTER);
        String[] tokens2 = path2.split(SPLITTER);
        return standardPathComparison(tokens1, tokens2, Math.min(tokens1.length, tokens2.length));
   }

    public static boolean samePath(String path1, String path2) {
        if (path1.isEmpty()) {
            return path2.isEmpty();
        }
        String[] tokens1 = path1.split(SPLITTER);
        String[] tokens2 = path2.split(SPLITTER);
        if (tokens1.length != tokens2.length) {
            return false;
        }
        return standardPathComparison(tokens1, tokens2, tokens1.length);
    }

    private static boolean standardPathComparison(String[] path1, String[] path2, int l) {
        for (int i = 0; i < l; i++) {
            if (path1[i].equals(ANY) || path2[i].equals(ANY) || path1[i].equals(path2[i])) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

}
