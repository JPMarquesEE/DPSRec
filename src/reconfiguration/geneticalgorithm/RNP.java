package reconfiguration.geneticalgorithm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;

import models.DistributionSystem.DistributionSystem;

public class RNP {
    private Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> Florest;
    private Hashtable<Pair<Integer,Integer>,Boolean> State;
    private ArrayList<Pair<Integer,Integer>> OpenSwitches;
    
    private Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> Binary2RNP(DistributionSystem sys, Hashtable<Pair<Integer,Integer>,Boolean> state){
        Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> rnp = new Hashtable();
        Hashtable<Integer,Integer> feeders = sys.getFeeders();
        Multigraph<Integer,DefaultEdge> zoneTopology = sys.getZoneGraph();
        
        Iterator<Map.Entry<Integer,Integer>> itFeeders = feeders.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itStates = state.entrySet().iterator();
        
        while(itStates.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entryStates = itStates.next();
            
            if(!entryStates.getValue()){
                Integer zoneFrom = sys.getZone(entryStates.getKey().getFirst()),
                        zoneTo = sys.getZone(entryStates.getKey().getSecond());

                    zoneTopology.removeEdge(zoneTo, zoneFrom);
            }
        }
        
        while(itFeeders.hasNext()){
            Map.Entry<Integer,Integer> entryFeeders = itFeeders.next();
            
            ArrayList<Pair<Integer,Integer>> feederRNP = new ArrayList();
            ArrayList<Integer> visitedNodes = new ArrayList();
            NeighborIndex<Integer,DefaultEdge> Neighbors = new NeighborIndex(zoneTopology);
            Stack<Pair<Integer,Integer>> nextZones = new Stack();
            List<Integer> neighborsList = Neighbors.neighborListOf(entryFeeders.getValue());
            
            visitedNodes.add(entryFeeders.getValue());
            for(int i=0;i<neighborsList.size();++i)
                nextZones.push(new Pair(neighborsList.get(i),1));
            
            while(!nextZones.isEmpty()){
                neighborsList.clear();
                Pair<Integer,Integer> NodeDepth = nextZones.pop();
                Integer Node = NodeDepth.getFirst(),
                        Depth = NodeDepth.getSecond();
                
                visitedNodes.add(Node);
                feederRNP.add(NodeDepth);
                
                neighborsList = Neighbors.neighborListOf(Node);
                for(int i=0;i<neighborsList.size();++i){
                    Integer neighborNode = neighborsList.get(i);
                    if(!visitedNodes.contains(neighborNode))
                        nextZones.push(new Pair(neighborNode,Depth+1));
                }
            }
            
            rnp.put(entryFeeders.getValue(), feederRNP);
        }
        
        return rnp;
    }
    private Hashtable<Pair<Integer,Integer>,Boolean>  RNP2Binary(DistributionSystem sys){
        Hashtable<Pair<Integer,Integer>,Boolean> breakersState = sys.getBreakers();
        Hashtable<Pair<Integer,Integer>,Pair<Integer,Integer>> auxBreakers = sys.getZonesBranches();
        ArrayList<Pair<Integer,Integer>> ActiveBranches = new ArrayList();
        Iterator<Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>>> itTree = this.Florest.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itStates = breakersState.entrySet().iterator();
        
        while(itStates.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entryStates = itStates.next();
            
            breakersState.replace(entryStates.getKey(), Boolean.FALSE);
        }
        
        while(itTree.hasNext()){
            Stack<Pair<Integer,Integer>> NodeStack = new Stack();
            Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>> entryTree = itTree.next();
            ArrayList<Pair<Integer,Integer>> RNP = entryTree.getValue();
            
            NodeStack.push(new Pair(entryTree.getKey(),0));
            for(int i=0;i<RNP.size();++i){
                if(RNP.get(i).getValue()>NodeStack.peek().getValue()){
                    ActiveBranches.add(new Pair(NodeStack.peek().getKey(),RNP.get(i).getKey()));
                    NodeStack.push(new Pair(RNP.get(i).getKey(),RNP.get(i).getValue()));
                }
                else{
                    while(!(RNP.get(i).getValue()>NodeStack.peek().getValue()))
                        NodeStack.pop();
                    
                    ActiveBranches.add(new Pair(NodeStack.peek().getKey(),RNP.get(i).getKey()));
                    NodeStack.push(new Pair(RNP.get(i).getKey(),RNP.get(i).getValue()));
                }
            }
        }
        
        for(int i=0;i<ActiveBranches.size();++i){
            Pair<Integer,Integer> pairOne = new Pair(ActiveBranches.get(i).getKey(),ActiveBranches.get(i).getValue());
            Pair<Integer,Integer> pairTwo = new Pair(ActiveBranches.get(i).getValue(),ActiveBranches.get(i).getKey());
            
            if(auxBreakers.containsKey(pairOne))
                breakersState.replace(auxBreakers.get(pairOne), Boolean.TRUE);
            else if(auxBreakers.containsKey(pairTwo))
                breakersState.replace(auxBreakers.get(pairTwo), Boolean.TRUE);
        }
        
        return breakersState;
    }
    
    public RNP (DistributionSystem sys){
        this.State = sys.getBreakers();
        this.Florest = Binary2RNP(sys,this.State);
        
        this.OpenSwitches = new ArrayList();
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itStates = this.State.entrySet().iterator();
        
        while(itStates.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entryStates = itStates.next();
            
            if(!entryStates.getValue())
                OpenSwitches.add(new Pair(entryStates.getKey()));
        }
    }
    public RNP (Hashtable<Pair<Integer,Integer>,Boolean> state, DistributionSystem sys){
        this.Florest = this.Binary2RNP(sys,state);
        this.State = RNP2Binary(sys);
        
        this.OpenSwitches = new ArrayList();
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itStates = this.State.entrySet().iterator();
        
        while(itStates.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entryStates = itStates.next();
            
            if(!entryStates.getValue())
                OpenSwitches.add(new Pair(entryStates.getKey()));
        }
    }
    public RNP (RNP _cpy){
        this.Florest = _cpy.getFlorest();
        this.OpenSwitches = _cpy.getOpenSwitches();
        this.State = _cpy.getState();
    }
    
    public Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> getFlorest(){
        Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> out = new Hashtable();
        Iterator<Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>>> it = this.Florest.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>> entry = it.next();
            ArrayList<Pair<Integer,Integer>> aux = new ArrayList();
            
            for(int i=0;i<entry.getValue().size();++i)
                aux.add(new Pair(new Integer(entry.getValue().get(i).getFirst()),new Integer(entry.getValue().get(i).getSecond())));
            
            out.put(entry.getKey(), aux);
        }
        
        return out;
    }
    public Hashtable<Pair<Integer,Integer>,Boolean> getState(){
        Hashtable<Pair<Integer,Integer>,Boolean> out = new Hashtable();
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> it = this.State.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entry = it.next();
            
            out.put(new Pair(entry.getKey().getFirst(),entry.getKey().getSecond()),new Boolean(entry.getValue()));
        }
        
        return out;
    }
    public ArrayList<Pair<Integer,Integer>> getOpenSwitches(){
        ArrayList<Pair<Integer,Integer>> out = new ArrayList();
        
        for(int i=0;i<this.OpenSwitches.size();++i)
            out.add(new Pair(this.OpenSwitches.get(i)));
        
        return out;
    }
    
    // OPERADORES PAO E CAO E FUNÇÕES AUXILIARES
    public RNP operatorPAO(DistributionSystem sys){
        Hashtable<Pair<Integer,Integer>,Boolean> newState = this.getState();
        Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest = this.getFlorest();
        
        if(!this.OpenSwitches.isEmpty()){
            Integer indxSwitch = ThreadLocalRandom.current().nextInt(0, this.OpenSwitches.size());
            Pair<Integer,Integer> CloseSwitch = this.OpenSwitches.get(indxSwitch);
            
            int NodeOne = sys.getZone(CloseSwitch.getFirst()),
                NodeTwo = sys.getZone(CloseSwitch.getSecond());
            int FeederOne = this.getFeeder(florest,NodeOne),
                FeederTwo = this.getFeeder(florest,NodeTwo);
            int indxOne = this.getIndex(florest, FeederOne, NodeOne),
                indxTwo = this.getIndex(florest, FeederTwo, NodeTwo);
            ArrayList<Integer> RootsOne = this.getRoots(florest, FeederOne, indxOne),
                               RootsTwo = this.getRoots(florest, FeederTwo, indxTwo);
            Pair<Integer,Integer> OpenSwitch;
            
            if((RootsOne.contains(NodeTwo))||(NodeTwo==FeederTwo)){
                int Root = this.getRoot(florest, FeederOne, indxOne);
                OpenSwitch = sys.getBranch(NodeOne, Root);
            }
            else if(RootsTwo.contains(NodeOne)||(NodeOne==FeederOne)){
                int Root = this.getRoot(florest, FeederTwo, indxTwo);
                 OpenSwitch = sys.getBranch(NodeTwo, Root);
            }
            else{
                int RootOne = this.getRoot(florest, FeederOne, indxOne),
                    RootTwo = this.getRoot(florest, FeederTwo, indxTwo);
                
                Pair<Integer,Integer> PairOne = sys.getBranch(NodeOne, RootOne),
                                      PairTwo = sys.getBranch(NodeTwo, RootTwo);
                    
                if(ThreadLocalRandom.current().nextBoolean())
                    OpenSwitch = PairOne;
                else
                    OpenSwitch = PairTwo;
            }
            
            newState.replace(CloseSwitch, true);
            newState.replace(OpenSwitch, false);
        }
        
        return new RNP(newState, sys);
    }
    public RNP operatorCAO(DistributionSystem sys){
        Hashtable<Pair<Integer,Integer>,Boolean> newState = this.getState();
        Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest = this.getFlorest();
        
        if(!this.OpenSwitches.isEmpty()){
            // Escolha da chave a ser fechada e definição das variáveis das chaves;
            Integer indxSwitch = ThreadLocalRandom.current().nextInt(0, this.OpenSwitches.size());
            Pair<Integer,Integer> CloseSwitch = this.OpenSwitches.get(indxSwitch),
                                  OpenSwitch;
            
            // Decomposição das chaves em nós de zona e coleta dos dados auxiliares
            int NodeOne = sys.getZone(CloseSwitch.getFirst()),
                NodeTwo = sys.getZone(CloseSwitch.getSecond());
            int FeederOne = this.getFeeder(florest,NodeOne),
                FeederTwo = this.getFeeder(florest,NodeTwo);
            int indxOne = this.getIndex(florest, FeederOne, NodeOne),
                indxTwo = this.getIndex(florest, FeederTwo, NodeTwo);
            
            // Criação da lista das raízes dos dois nós de zona adjacentes a chave de abertura
            ArrayList<Integer> OneRoots = this.getRoots(florest, FeederOne, indxOne),
                           auxOne = new ArrayList(),
                           TwoRoots = this.getRoots(florest, FeederTwo, indxTwo),
                           auxTwo = new ArrayList();
            
            // Processo para retirar as raízes comuns, que são inelegíveis para abertura da próxima chave
            // pois acarretaria em uma condição de ilhamento.
            auxOne.addAll(OneRoots);
            auxOne.removeAll(TwoRoots);
            auxTwo.addAll(TwoRoots);
            auxTwo.removeAll(OneRoots);
            
            if(auxOne.size()!=OneRoots.size()){
                int tam = OneRoots.size(),
                    MutualIndx = auxOne.size();
                for(int i=1;i<(tam-MutualIndx);++i)
                    OneRoots.remove(MutualIndx+1);
            }
            
            if(auxTwo.size()!=TwoRoots.size()){
                int tam = TwoRoots.size(),
                    MutualIndx = auxTwo.size();
                for(int i=1;i<(tam-MutualIndx);++i)
                    TwoRoots.remove(MutualIndx+1);
            }
            
            int indx, RootOpen;
            if(OneRoots.size()<=1){
                indx = ThreadLocalRandom.current().nextInt(1, TwoRoots.size());
                RootOpen = TwoRoots.get(indx);
                    
                OpenSwitch = sys.getBranch(RootOpen, TwoRoots.get(indx-1));
            }
            else if(TwoRoots.size()<=1){
                indx = ThreadLocalRandom.current().nextInt(1, OneRoots.size());
                RootOpen = OneRoots.get(indx);
                    
                OpenSwitch = sys.getBranch(RootOpen, OneRoots.get(indx-1));
            }
            else{
                if(ThreadLocalRandom.current().nextBoolean()){
                    indx = ThreadLocalRandom.current().nextInt(1, OneRoots.size());
                    RootOpen = OneRoots.get(indx);
                    
                    OpenSwitch = sys.getBranch(RootOpen, OneRoots.get(indx-1));
                }
                else{
                    indx = ThreadLocalRandom.current().nextInt(1, TwoRoots.size());
                    RootOpen = TwoRoots.get(indx);
                    
                    OpenSwitch = sys.getBranch(RootOpen, TwoRoots.get(indx-1));
                }
            }
            
            newState.replace(CloseSwitch, true);
            newState.replace(OpenSwitch, false);
        }
        
        return new RNP(newState, sys);
    }
    
    private int getRoot(Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest, int Feeder, int indx){
        if(indx==-1)
            return -1;
        else{
            int Root = Feeder,
                depthNode = this.getDepth(florest, Feeder, indx);
                ArrayList<Pair<Integer,Integer>> tree = florest.get(Feeder);
        
            for(int i=indx;i>=0;--i){
                int auxDepth = this.getDepth(florest, Feeder, i);
                if(depthNode>auxDepth){
                    Root = tree.get(i).getKey();
                    break;
                }
            }
        
            return Root;
        }
    }
    private ArrayList<Integer> getRoots(Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest, int Feeder, int indx){
        ArrayList<Integer> out = new ArrayList();
        
        if(indx!=-1){
            int ActualRoot = florest.get(Feeder).get(indx).getKey(),    
                ActualRootDepth = this.getDepth(florest,Feeder,indx),
                index = indx,
                auxNode = ActualRoot,
                auxNodeDepth;
                                
            --index;
            
            out.add(ActualRoot);
            while(index>=0){
                auxNode = florest.get(Feeder).get(index).getKey();
                auxNodeDepth = florest.get(Feeder).get(index).getValue();
                if(auxNodeDepth<ActualRootDepth){
                    ActualRootDepth = auxNodeDepth;
                    out.add(auxNode);
                }
                --index;
            }
        }
        out.add(Feeder);
        
        return out;
    }
    private Boolean containNode(ArrayList<Pair<Integer,Integer>> RNP, int Node){
        Boolean ans = false;
        
        for(int i=0;i<RNP.size();++i){
            if(RNP.get(i).getKey()==Node){
                ans = true;
                break;
            }
        }
        
        return ans;
    }
    private int getFeeder(Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest, int Node){
        int Feeder = -1;
        Iterator<Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>>> itFlorest = florest.entrySet().iterator();
        Boolean run = true;
        
        while(itFlorest.hasNext()&&(run)){
            Map.Entry<Integer,ArrayList<Pair<Integer,Integer>>> entryFlorest = itFlorest.next();
            if(entryFlorest.getKey()==Node){
                run = false;
                Feeder = entryFlorest.getKey();
            }
            else if(this.containNode(entryFlorest.getValue(),Node)){
                run = false;
                Feeder = entryFlorest.getKey();
            }
        }
        
        return Feeder;
    }
    private int getIndex(Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest, int Feeder, int Node){
        int out = -1;
        ArrayList<Pair<Integer,Integer>> Tree = florest.get(Feeder);
            
        for(int i=0;i<Tree.size();++i){
            if(Tree.get(i).getKey()==Node){
                out = i;
                break;
            }
        }
            
        return out;
    }
    private int getDepth(Hashtable<Integer,ArrayList<Pair<Integer,Integer>>> florest, int Feeder, int indx){
        if(indx==-1)
            return 0;
        else
            return florest.get(Feeder).get(indx).getValue();
    }
}
