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

import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Flowgraph;
import br.usp.each.saeg.subsumption.graphdua.Node;

import java.util.*;


public class Program {

    private final Flowgraph<Block> graph = new Flowgraph<Block>();
    private final Flowgraph<Block> invgraph = new Flowgraph<Block>();

    private final Map<Integer, String> ids2variables = new HashMap<Integer, String>();
    private final Map<String, Integer> variables2Ids = new HashMap<String, Integer>();

    private Node[] dataFlowSets;

    int idcounter = 0;

    public Flowgraph<Block> getGraph() {
        return this.graph;
    }

    public String variable(int id) {
        return ids2variables.get(id);
    }

    public void addVariable(final String name, final int id) {

        if (!ids2variables.containsKey(id)) {
            ids2variables.put(id, name);
            if (!variables2Ids.containsKey(name)) {
                variables2Ids.put(name, id);
            }
        }
    }

    public int getVariableId(final String uname, String name) {
        int id = -1;
        if (!variables2Ids.containsKey(uname)) {
            variables2Ids.put(uname, idcounter);
            if (!ids2variables.containsKey(idcounter)) {
                ids2variables.put(idcounter, name);
                id = idcounter;
            }
            ++idcounter;
        } else
            id = variables2Ids.get(uname);

        return id;
    }

    public int numberOfVars() {
        return variables2Ids.size();
    }

    public Flowgraph<Block> getInvGraph() {
        return this.invgraph;
    }

    public void computeDataFlowSets(List<Dua> duas) {

        dataFlowSets = new Node[getGraph().size()];

        Iterator<Block> itBlock = getGraph().iterator();

        while (itBlock.hasNext()) {
            Block blk = itBlock.next();
            Node nn = new Node(blk, 0);
            nn.initNodeSets(duas.size());
            dataFlowSets[blk.id()] = nn;
        }

        itBlock = getGraph().iterator();

        while (itBlock.hasNext()) {
            Block block = itBlock.next();
            int blkId = block.id();

            Iterator<Dua> itDua = duas.iterator();
            int id = 0;

            while (itDua.hasNext()) {
                Dua dua = itDua.next();

                Node node = dataFlowSets[blkId];
                if (dua.isCUse()) {
                    if (dua.use().id() == blkId) {
                        node.setGen(id);
                    }
                }

                if (!dua.isCUse()) {
                    if (dua.to().id() == blkId)
                        node.setGen(id);
                    else {
                        int from = dua.from().id();
                        if (from != blkId)
                            node.setSleepy(id);
                    }
                }

                if (dua.def().id() == blkId) {
                    node.setBorn(id);
                }

                if (dua.def().id() != blkId && getGraph().get(blkId).isDef(dua.var())) {
                    node.setKill(id);
                }
                ++id;
            }
        }
    }

    public BitSet getGen(Node n) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[n.block().id()].getGen();
    }

    public BitSet getBorn(Node n) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[n.block().id()].getBorn();
    }

    public BitSet getKill(Node n) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[n.block().id()].getKill();
    }

    public BitSet getSleepy(Node n) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[n.block().id()].getSleepy();
    }

    public BitSet getGen(int id) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[id].getGen();
    }

    public BitSet getBorn(int id) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[id].getBorn();
    }

    public BitSet getKill(int id) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[id].getKill();
    }

    public BitSet getSleepy(int id) {
        if (dataFlowSets == null) return null;
        return dataFlowSets[id].getSleepy();
    }

}
