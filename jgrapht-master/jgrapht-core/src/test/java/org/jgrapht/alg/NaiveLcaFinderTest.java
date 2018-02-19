/*
 * (C) Copyright 2016-2018, by Barak Naveh, Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class NaiveLcaFinderTest
{
    private static <V, E> void checkLcas(NaiveLcaFinder<V, E> finder, V a, V b, Collection<V> expectedSet){
        Set<V> lcaSet = finder.findLcas(a, b);
        Assert.assertTrue(lcaSet.containsAll(expectedSet));
        Assert.assertEquals(lcaSet.size(), expectedSet.size());
    }


    @Test
    public void testNormalCases()
    {
        SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("f", "e");
        g.addEdge("e", "h");

        NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<>(g);

        Assert.assertEquals("f", finder.findLca("f", "h"));
        Assert.assertEquals("f", finder.findLca("h", "f"));
        Assert.assertEquals("b", finder.findLca("g", "h"));
        Assert.assertEquals("c", finder.findLca("c", "c"));
        Assert.assertEquals("a", finder.findLca("a", "e")); // tests one path not descending

        checkLcas(finder, "f", "h", Arrays.asList("f"));
        checkLcas(finder, "h", "f", Arrays.asList("f"));
        checkLcas(finder, "g", "h", Arrays.asList("b"));
        checkLcas(finder, "c", "c", Arrays.asList("c"));
        checkLcas(finder, "a", "e", Arrays.asList("a"));
    }

    @Test
    public void testNoLca()
    {
        SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("f", "g");
        g.addEdge("f", "h");
        g.addEdge("g", "i");
        g.addEdge("h", "i");

        NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<>(g);

        Assert.assertEquals(null, finder.findLca("i", "e"));
        Assert.assertTrue(finder.findLcas("i", "e").isEmpty());
    }

    @Test
    public void testLoops()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("f", "e");
        g.addEdge("e", "h");
        g.addEdge("h", "e");
        g.addEdge("h", "h");
        g.addEdge("i", "i");
        NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<>(g);

        Assert.assertEquals("f", finder.findLca("h", "f"));
        Assert.assertEquals(null, finder.findLca("a", "i"));

        checkLcas(finder, "h", "f", Arrays.asList("f"));
        Assert.assertTrue(finder.findLcas("a", "i").isEmpty());
    }

    @Test
    public void testArrivalOrder()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("g");
        g.addVertex("e");
        g.addVertex("h");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("a", "g");
        g.addEdge("b", "g");
        g.addEdge("g", "e");
        g.addEdge("e", "h");
        NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<>(g);

        Assert.assertEquals("b", finder.findLca("b", "h"));
        Assert.assertEquals("b", finder.findLca("c", "e"));

        checkLcas(finder, "b", "h", Arrays.asList("b"));
        checkLcas(finder, "c", "e", Arrays.asList("b"));
    }

    @Test
    public void testTwoLcas(){

        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("a", "c");
        g.addEdge("a", "d");
        g.addEdge("b", "c");
        g.addEdge("b", "d");

        NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<>(g);

        checkLcas(finder, "c", "d", Arrays.asList("a", "b"));
    }

}
