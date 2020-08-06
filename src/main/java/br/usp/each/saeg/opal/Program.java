/**
 * OPAL - Open-source Program Analysis Library
 * Copyright (c) 2014, 2016 University of Sao Paulo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.usp.each.saeg.opal;

import br.usp.each.saeg.subsumption.graphdua.Flowgraph;

import java.util.HashMap;
import java.util.Map;


public class Program {

    private final Flowgraph<Block> graph = new Flowgraph<Block>();
    private final Flowgraph<Block> invgraph = new Flowgraph<Block>();

    private final Map<Integer, String> ids2variables = new HashMap<Integer, String>();


    public Flowgraph<Block> getGraph() {
        return this.graph;
    }

    public String variable(int id) {return ids2variables.get(id); }

    public void addVariable(final String name, final int id) {

        if (!ids2variables.containsKey(id)) {
            ids2variables.put(id, name);
        }
    }

    public int numberOfVars() {
        return ids2variables.size();
    }

    public Flowgraph<Block> getInvGraph() {
        return this.invgraph;
    }

//    public void createInvGraph(Flowgraph<Block> graph) {
//
//        invgraph = new Flowgraph<>();
//        Iterator<Block> it = graph.iterator();
//
//        while (it.hasNext()) {
//            Block node = it.next();
//            invgraph.add(node);
//        }
//
//        it = graph.iterator();
//
//        while (it.hasNext()) {
//            Block from = it.next();
//            for (final Block to : graph.neighbors(from.id())) {
//                invgraph.addEdge(from.id(),to.id());
//            }
//        }
//    }
}
