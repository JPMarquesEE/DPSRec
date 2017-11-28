package models.DistributionSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.alg.NeighborIndex;

import models.Conceptual.*;
import models.PassiveElements.*;


public class StaticData {
    private double Vref;
    private double Sref;
    
    private Hashtable<Integer,Bus> BusData;
    private Hashtable<Pair<Integer,Integer>,Branch> BranchData;
    
    private Hashtable<Pair<Integer,Integer>,Pair<Integer,Integer>> ZonesBranchs;
    private Hashtable<Integer,Integer> Feeders;
    private Hashtable<Integer,ArrayList<Integer>> Zones;   
    private Hashtable<Pair<Integer,Integer>,Boolean> Breakers;
    
    private Multigraph<Integer,DefaultEdge> CompleteTopology,
                                            ZoneTopology;
    
    // ROTINAS DE CRIAÇÃO DOS ELEMENTOS AUXILIARES
    private void createCompleteGraph(){
        Iterator<Map.Entry<Integer,Bus>> itBus = this.BusData.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> itBranch = this.BranchData.entrySet().iterator();
            
        while(itBus.hasNext()){
            Map.Entry<Integer,Bus> entry = itBus.next();
            this.CompleteTopology.addVertex(entry.getKey());
        }
        
        while(itBranch.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Branch> entry = itBranch.next();
            this.CompleteTopology.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
        }
    }
    private void defineZones(){
        NeighborIndex<Integer,DefaultEdge> Neighbors = new NeighborIndex(this.CompleteTopology);
        ArrayList<Integer> Visited = new ArrayList();
        Integer ZoneNumber = 0;
        Queue<Integer> NextPoint = new LinkedList(),
                       ZoneList = new LinkedList();
        
        Integer bus = this.CompleteTopology.vertexSet().iterator().next();
        
        ZoneList.add(bus);
        Visited.add(bus);
        
        while(!ZoneList.isEmpty()){
            bus = ZoneList.remove();
            ArrayList<Integer> BusesOfZone = new ArrayList();
            BusesOfZone.add(bus);
            NextPoint.add(bus);
            
            while(!NextPoint.isEmpty()){
                bus = NextPoint.remove();
                List<Integer> NeighborsList = Neighbors.neighborListOf(bus);
                
                if(this.BusData.get(bus).getClass().equals(Slack.class))
                    this.Feeders.put(bus, ZoneNumber);
                    
                for(int j=0;j<NeighborsList.size();++j){
                    DefaultEdge edge = this.CompleteTopology.getEdge(bus,NeighborsList.get(j));
                    Pair<Integer,Integer> key = new Pair(this.CompleteTopology.getEdgeSource(edge),this.CompleteTopology.getEdgeTarget(edge));
                    
                    if(!Visited.contains(NeighborsList.get(j))){
                        Visited.add(NeighborsList.get(j));
                        
                        if(this.BranchData.get(key).Switchable()){
                            ZoneList.add(NeighborsList.get(j));
                        }
                        else{
                            NextPoint.add(NeighborsList.get(j));
                            BusesOfZone.add(NeighborsList.get(j));
                            ZoneList.remove(NeighborsList.get(j));
                        }
                    }
                        
                }
            }
            
            this.Zones.put(ZoneNumber, BusesOfZone);
            ++ZoneNumber;
        }
    }
    public Integer getZone(Integer BusID){
        Integer ZoneNum = null;
        Iterator<Map.Entry<Integer,ArrayList<Integer>>> it = this.Zones.entrySet().iterator();
        boolean fContinue = true;
        
        while(it.hasNext()&&fContinue){
            Map.Entry<Integer,ArrayList<Integer>> entry = it.next();
            if(entry.getValue().contains(BusID)){
                ZoneNum = entry.getKey();
                fContinue = false;
            }
        }
        
        return ZoneNum;
    }
    private void createZoneGraph(){
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itBreakers = this.Breakers.entrySet().iterator();
        
        while(itBreakers.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entryBreakers = itBreakers.next();
            Integer SZone = this.getZone(entryBreakers.getKey().getFirst()),
                    TZone = this.getZone(entryBreakers.getKey().getSecond());
            
            this.ZonesBranchs.put(new Pair(SZone,TZone), entryBreakers.getKey());
        }
        
        Iterator<Map.Entry<Integer,ArrayList<Integer>>> itZone = this.Zones.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>>> itBranch = this.ZonesBranchs.entrySet().iterator();
        
        while(itZone.hasNext()){
            Map.Entry<Integer,ArrayList<Integer>> entryZone = itZone.next();
            this.ZoneTopology.addVertex(entryZone.getKey());
        }
        
        while(itBranch.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>> entryBranch = itBranch.next();
            this.ZoneTopology.addEdge(entryBranch.getKey().getFirst(),entryBranch.getKey().getSecond());
        }
    }
    
    // CONSTRUTORES
    public StaticData(String FStaticData){
        try{
            Scanner inputStaticData = new Scanner(new File(FStaticData));
        
            this.BusData = new Hashtable();
            this.BranchData = new Hashtable();
            this.Feeders = new Hashtable();
            this.Zones = new Hashtable();
            this.ZonesBranchs = new Hashtable();
            
            this.Breakers = new Hashtable();
            
            this.CompleteTopology = new Multigraph(DefaultEdge.class);
            this.ZoneTopology = new Multigraph(DefaultEdge.class);
            
            while(inputStaticData.hasNext()){
                if(inputStaticData.hasNext("VREF")){
                    inputStaticData.nextLine();
                    this.Vref = inputStaticData.nextDouble();
                }
                else if(inputStaticData.hasNext("SREF")){
                    inputStaticData.nextLine();
                    inputStaticData.nextLine();
                    inputStaticData.nextLine();

                    this.Sref = inputStaticData.nextDouble();
                }
                else if(inputStaticData.hasNext("BUSES")){
                    inputStaticData.nextLine();
                    inputStaticData.nextLine();
                    
                    while(inputStaticData.hasNextInt()){
                        int busID, tipo;
                        double aux, Vmag, Vang;
                        busID = inputStaticData.nextInt();
                        tipo = inputStaticData.nextInt();
                        Vmag = inputStaticData.nextDouble();
                        Vang = inputStaticData.nextDouble();
                        
                        Bus auxBus;
                        
                        // Tipo 1 - Slack
                        // Tipo 2 - PQ
                        
                        if(tipo==1){
                            Complex Vbus = new Complex((Vmag/this.Vref)*Math.cos(Vang*Math.PI/180.0),(Vmag/this.Vref)*Math.sin(Vang*Math.PI/180.0));
                            auxBus = new Slack(Vbus);
                        }
                        else{
                            // Caso haja mais barras, deve ser alterado.
                            auxBus = new PQBus();
                        }
                        this.BusData.put(busID, auxBus);
                    }
                }
                else if(inputStaticData.hasNext("BRANCHES")){             
                    inputStaticData.nextLine();
                    inputStaticData.nextLine();
                    
                    while(inputStaticData.hasNextInt()){
                        boolean switchable, state;
                        int BSource, BFrom;
                        
                        BSource = inputStaticData.nextInt();
                        BFrom = inputStaticData.nextInt();
                        switchable = inputStaticData.nextInt()==1;
                        state = inputStaticData.nextInt()==1;
                        
                        Branch auxBranch = new Branch(switchable);
                        Pair branchID = new Pair(BSource,BFrom);
                        
                        if(auxBranch.Switchable())
                            this.Breakers.put(branchID, state);
                        
                        this.BranchData.put(branchID, auxBranch);
                    }
                }
                else if(inputStaticData.hasNext("LINES")){
                    inputStaticData.next();
                    if(inputStaticData.hasNext("SERIE")){  
                        inputStaticData.nextLine();
                        inputStaticData.nextLine();
                        
                        double Zref;
                        Zref = Math.pow(this.Vref, 2)/this.Sref;
                        while(inputStaticData.hasNextInt()){
                            int BSource, BFrom;
                            double R,X;
                            
                            BSource = inputStaticData.nextInt();
                            BFrom = inputStaticData.nextInt();
                            R = inputStaticData.nextDouble();
                            X = inputStaticData.nextDouble();
                            
                            Complex Z = new Complex(R,X).divide(Zref);
                            
                            SerieElement line = new SerieImpedance(Z);
                            Pair key = new Pair(BSource,BFrom);
                            Branch auxBranch = this.BranchData.get(key);
                            auxBranch.setElement(line);
                            this.BranchData.replace(key, auxBranch);
                        }
                    }
                    else{ 
                        inputStaticData.nextLine();
                        inputStaticData.nextLine();
                        
                        double Yref;
                        Yref = Math.pow(this.Vref, 2)/this.Sref;
                        Yref = 1/Yref;
                        while(inputStaticData.hasNextInt()){
                            // Considera que a admitância é multiplada por 0.5 na base de dados;
                            // Não considera a multiplicação de constantes de base 10.
                            int BSource, BFrom;
                            double G,B;
                            
                            BSource = inputStaticData.nextInt();
                            BFrom = inputStaticData.nextInt();
                            G = inputStaticData.nextDouble();
                            B = inputStaticData.nextDouble();
                            
                            Complex Y = new Complex(G,B).divide(Yref);
                            
                            ShuntElement line1 = new ShuntImpedance(Y),
                                         line2 = new ShuntImpedance(Y);
                            Bus bsource = this.BusData.get(BSource),
                                bfrom = this.BusData.get(BFrom);
                            bsource.addElement(line1);
                            bfrom.addElement(line2);
                            this.BusData.replace(BFrom, bfrom);
                            this.BusData.replace(BSource, bsource);
                        }
                    }
                }
                else{
                    System.out.println(inputStaticData.nextLine());
                }
            }
            
            inputStaticData.close();
            
            //////////////////////////////////////
            // CRIAÇAO DOS ELEMENTOS AUXILIARES //
            //////////////////////////////////////
            
            // GRAFO COMPLETO E CHAVES SECCIONADORAS
            this.createCompleteGraph();
            
            // DEFINIÇÃO DAS ZONAS E DOS FEEDERS
            this.defineZones();
            
            // DEFINIÇÃO DOS ZONES BRANCHS E CRIAÇÃO DO GRAFO DE ZONAS
            this.createZoneGraph();
            
        }
        catch(FileNotFoundException e){
            System.err.println("Erro na abertura do arquivo.");
            System.exit(1);
        }
    }
    public StaticData(StaticData _cpy){
        this.Vref = _cpy.Vref;
        this.Sref = _cpy.Sref;
        
        Iterator<Map.Entry<Integer,Bus>> itBus = _cpy.BusData.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> itBranch = _cpy.BranchData.entrySet().iterator();
        
        this.BusData = new Hashtable();
        this.BranchData = new Hashtable();
        this.Feeders = new Hashtable();
        this.Zones = new Hashtable();
        this.ZonesBranchs = new Hashtable();
        this.Breakers = new Hashtable();
            
        this.CompleteTopology = new Multigraph(DefaultEdge.class);
        this.ZoneTopology = new Multigraph(DefaultEdge.class);
        
        while(itBus.hasNext()){
            Map.Entry<Integer,Bus> entry = itBus.next();
            this.BusData.put(entry.getKey(),entry.getValue().clone());
            this.CompleteTopology.addVertex(entry.getKey());
        }
        
        while(itBranch.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Branch> entry = itBranch.next();
            this.BranchData.put(entry.getKey(),entry.getValue().clone());
            this.CompleteTopology.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
                
            if(entry.getValue().Switchable()){
                Pair breakerID = new Pair(entry.getKey().getFirst(), entry.getKey().getSecond());
                boolean state = _cpy.Breakers.get(breakerID);
                
                this.Breakers.put(breakerID, state);
            }
        }
        
        this.defineZones();
        this.createZoneGraph();
    }
    
    // METODOS GET
    public ArrayList<Integer> getBusID(){
        Iterator<Map.Entry<Integer,Bus>> it = this.BusData.entrySet().iterator();
        ArrayList<Integer> idArray = new ArrayList();
        
        while(it.hasNext())
            idArray.add(it.next().getKey());
        
        return idArray;
    }
    public ArrayList<Pair<Integer,Integer>> getBranchID(){
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> it = this.BranchData.entrySet().iterator();
        ArrayList<Pair<Integer,Integer>> idArray = new ArrayList();
        
        while(it.hasNext())
            idArray.add(it.next().getKey());
        
        return idArray;
    }
    public double getVref(){
        return this.Vref;
    }
    public double getSref(){
        return this.Sref;
    }
    public Bus getBus(Integer BusID){
        return this.BusData.get(BusID).clone();
    }
    public Pair<Integer,Integer> getBranch(int ZOne, int ZTwo){
        Pair One = new Pair(ZOne, ZTwo),
             Two = new Pair(ZTwo, ZOne);
        
        if(this.ZonesBranchs.containsKey(One))
            return this.ZonesBranchs.get(One);
        else
            return this.ZonesBranchs.get(Two);
    }
    public Branch getBranch(Integer FromID, Integer ToID){
        return this.BranchData.get(new Pair(FromID,ToID)).clone();
    }
    public Multigraph<Integer,DefaultEdge> getCompleteGraph(){
        Multigraph<Integer,DefaultEdge> compltGraph = new Multigraph(DefaultEdge.class);
        Iterator<Map.Entry<Integer,Bus>> itBus = this.BusData.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> itBranch = this.BranchData.entrySet().iterator();
            
        while(itBus.hasNext()){
            Map.Entry<Integer,Bus> entry = itBus.next();
            compltGraph.addVertex(entry.getKey());
        }
        
        while(itBranch.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Branch> entry = itBranch.next();
            compltGraph.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
        }
        
        return compltGraph;
    }
    public Multigraph<Integer,DefaultEdge> getZoneGraph(){
        Multigraph<Integer,DefaultEdge> zoneGraph = new Multigraph(DefaultEdge.class);
        Iterator<Map.Entry<Integer,ArrayList<Integer>>> itZone = this.Zones.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>>> itBranch = this.ZonesBranchs.entrySet().iterator();
        
        while(itZone.hasNext()){
            Map.Entry<Integer,ArrayList<Integer>> entryZone = itZone.next();
            zoneGraph.addVertex(entryZone.getKey());
        }
        
        while(itBranch.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>> entryBranch = itBranch.next();
            zoneGraph.addEdge(entryBranch.getKey().getFirst(),entryBranch.getKey().getSecond());
        }
        
        return zoneGraph;
    }
    public Hashtable<Pair<Integer,Integer>,Pair<Integer,Integer>> getZonesBranches(){
        Hashtable<Pair<Integer,Integer>,Pair<Integer,Integer>> zoneBranches = new Hashtable();
        Iterator<Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>>> itZones = this.ZonesBranchs.entrySet().iterator();
        
        while(itZones.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Pair<Integer,Integer>> entry = itZones.next();
            
            zoneBranches.put(new Pair(entry.getKey()), new Pair(entry.getValue()));
        }
        
        
        return zoneBranches;
    }
    public Hashtable<Integer,Integer> getFeeders(){
        Hashtable<Integer,Integer> feeders = new Hashtable();
        Iterator<Map.Entry<Integer,Integer>> itFeeders = this.Feeders.entrySet().iterator();
        
        while(itFeeders.hasNext()){
            Map.Entry<Integer,Integer> entry = itFeeders.next();
            
            feeders.put(new Integer(entry.getKey()), new Integer(entry.getValue()));
        }
        
        
        return feeders;
    }
    public Hashtable<Integer,ArrayList<Integer>> getZones(){
        Hashtable<Integer,ArrayList<Integer>> zones = new Hashtable();
        Iterator<Map.Entry<Integer,ArrayList<Integer>>> itZones = this.Zones.entrySet().iterator();
        
        while(itZones.hasNext()){
            Map.Entry<Integer,ArrayList<Integer>> entry = itZones.next();
            
            zones.put(new Integer(entry.getKey()), new ArrayList(entry.getValue()));
        }
        
        return zones;
    }   
    public Hashtable<Pair<Integer,Integer>,Boolean> getBreakers(){
        Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itBreakers = this.Breakers.entrySet().iterator();
        Hashtable<Pair<Integer,Integer>,Boolean> breakers = new Hashtable();
        
        while(itBreakers.hasNext()){
            Map.Entry<Pair<Integer,Integer>,Boolean> entry = itBreakers.next();
            
            breakers.put(new Pair(entry.getKey().getFirst(),entry.getKey().getSecond()), entry.getValue()?Boolean.TRUE:Boolean.FALSE);
        }
        
        return breakers;
    }
    
    // AUXILIARES DO ALGORITMO DE FLUXO DE POTÊNCIA
    public void addLoads(Hashtable<Integer,YLoad> loads){
        Iterator<Map.Entry<Integer,YLoad>> it = loads.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,YLoad> entry = it.next();
            
            if(this.BusData.containsKey(entry.getKey())){
                Bus aux = this.BusData.get(entry.getKey());
                aux.addElement(entry.getValue().clone());
            }
        }
    }
    public void removeDynamicElement(){
        Iterator<Map.Entry<Integer,Bus>> it = this.BusData.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,Bus> entry = it.next();
            
            Bus aux = this.BusData.get(entry.getKey());
            aux.removeDynamicElements();
        }
    }
    
    public void Reset(){
        Iterator<Map.Entry<Integer,Bus>> itBus = this.BusData.entrySet().iterator();
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> itBranch = this.BranchData.entrySet().iterator();
        
        while(itBus.hasNext())
            itBus.next().getValue().Reset();
        
        
        while(itBranch.hasNext())
            itBranch.next().getValue().Reset();
    }
    
    public Complex getVbus(Integer BusID){
        if(this.BusData.containsKey(BusID))
            return this.BusData.get(BusID).Vbus();
        else
            return null;
    }
    public void setCurrentBranch(Pair<Integer,Integer> _Branch, Multigraph<Integer,DefaultEdge> ModifiedTopology){
        Integer source = _Branch.getFirst(),
                load = _Branch.getSecond();
        Pair<Integer,Integer> BranchID;
        
        if(this.BranchData.containsKey(_Branch))
            BranchID = _Branch;
        else
            BranchID = new Pair(load,source);
        
        if(this.BranchData.containsKey(BranchID)){
            NeighborIndex<Integer,DefaultEdge> Neighbors = new NeighborIndex(ModifiedTopology);
            List<Integer> NeighborsList = Neighbors.neighborListOf(load);
            ArrayList<Branch> BranchList = new ArrayList();
            NeighborsList.remove(source);
            Branch aux;
            
            for(int i=0;i<NeighborsList.size();++i){
                Integer BusNeighbor = NeighborsList.get(i);
                Pair<Integer,Integer> aux1 = new Pair(load,BusNeighbor),
                                      aux2 = new Pair(BusNeighbor,load);
                
                if(this.BranchData.containsKey(aux1))
                    BranchList.add(this.BranchData.get(aux1));
                else
                    BranchList.add(this.BranchData.get(aux2));
            }
            
            aux = this.BranchData.get(BranchID);
            aux.setCurrents(this.BusData.get(load),BranchList);
            this.BranchData.replace(BranchID, aux);
        }
    }
    public void setVbus(Integer BusID, Complex _Vbus){
        if(this.BusData.containsKey(BusID)){
            Bus aux = this.BusData.get(BusID);
            aux.setVbus(_Vbus);
            this.BusData.replace(BusID, aux);
        }
    }
    public void setVoltageBTo(Integer FromID, Integer ToID){
        Pair<Integer,Integer> BranchAuxID;
        
        if(this.BranchData.containsKey(new Pair(FromID,ToID)))
            BranchAuxID = new Pair(FromID,ToID);
        else
            BranchAuxID = new Pair(ToID,FromID);
        
        Branch BranchAux = this.BranchData.get(BranchAuxID);
        Bus FromAux = this.BusData.get(FromID);
        Complex VLoad = BranchAux.VLoad(FromAux);
        
        this.setVbus(ToID,VLoad);
    }
    public double Losses(){
        Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> it = this.BranchData.entrySet().iterator();
        double Losses = 0;
        
        while(it.hasNext()){
            Losses = Losses + it.next().getValue().getLosses().getReal();
        }
        
        return Losses;
    }
    public double getMismatch(Integer bus){
        return this.BusData.get(bus).getMismatch();
    }
    public double getMaxMismatch(){
        double maxMismatch = 0;
        Iterator<Map.Entry<Integer,Bus>> itBus = this.BusData.entrySet().iterator();
        
        while(itBus.hasNext()){
            double mismatch = itBus.next().getValue().getMismatch();
            if(mismatch>maxMismatch)
                    maxMismatch = mismatch;
        }
        
        return maxMismatch;
    }
    
    // FUNÇÕES DE IMPRESSÃO DE RELATÓRIOS
    private void VoltagePrinter(Writer report){
        try {
            Iterator<Map.Entry<Integer,Bus>> it = this.BusData.entrySet().iterator();
            String newline = System.getProperty("line.separator");
            report.write("TENSÕES NODAIS"+newline+newline);
            report.flush();
            
            while(it.hasNext()){
                Map.Entry<Integer,Bus> entry = it.next();
                Complex Voltage = entry.getValue().Vbus();
                
                report.write(entry.getKey()+"\t"+Voltage.abs()+"\t"+Voltage.getArgument()*180/Math.PI+newline);
                report.flush();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void NodalCurrentPrinter(Writer report){
        try {
            Iterator<Map.Entry<Integer,Bus>> it = this.BusData.entrySet().iterator();
            String newline = System.getProperty("line.separator");
            report.write("CORRENTES NODAIS"+newline+newline);
            report.flush();
            
            while(it.hasNext()){
                Map.Entry<Integer,Bus> entry = it.next();
                Complex current = entry.getValue().NodalCurrent();
                
                report.write(entry.getKey()+"\t"+current.abs()+"\t"+current.getArgument()*180/Math.PI+newline);
                report.flush();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void BranchCurrentPrinter(Writer report){
        try {
            Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> it = this.BranchData.entrySet().iterator();
            String newline = System.getProperty("line.separator");
            report.write("CORRENTES DOS RAMOS"+newline+newline);
            report.flush();
            
            while(it.hasNext()){
                Map.Entry<Pair<Integer,Integer>,Branch> entry = it.next();
                Complex current = entry.getValue().getISource();
                
                report.write("("+entry.getKey().getFirst()+","+entry.getKey().getSecond()+")\t"+current.abs()+"\t"+current.getArgument()*180/Math.PI+newline);
                report.flush();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public void DynamicPrinter(Writer report, int Iteration){
        try{
            String newline = System.getProperty("line.separator");
            report.write("ITERAÇÃO: "+Iteration+newline+"MISMATCH: "+this.getMaxMismatch()+newline+newline);
            report.flush();
            
            this.NodalCurrentPrinter(report);
            report.write(newline);
            report.flush();
            
            this.BranchCurrentPrinter(report);
            report.write(newline);
            report.flush();
            
            this.VoltagePrinter(report);
        }
        catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void StaticBusesPrinter(Writer report){
        try {
            Iterator<Map.Entry<Integer,Bus>> it = this.BusData.entrySet().iterator();
            String newline = System.getProperty("line.separator");
            report.write("DADOS DAS BARRAS"+newline+newline);
            report.flush();
            
            while(it.hasNext()){
                Map.Entry<Integer,Bus> entry = it.next();
            
                report.write(entry.getKey()+newline+entry.getValue().toString()+newline);
                report.flush();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void StaticBranchesPrinter(Writer report){
        try {
            Iterator<Map.Entry<Pair<Integer,Integer>,Branch>> it = this.BranchData.entrySet().iterator();
            String newline = System.getProperty("line.separator");
            report.write("DADOS DOS RAMOS"+newline+newline);
            report.flush();
            
            while(it.hasNext()){
                Map.Entry<Pair<Integer,Integer>,Branch> entry = it.next();
            
                report.write("("+entry.getKey().getFirst()+","+entry.getKey().getSecond()+")"+newline+entry.getValue().toString()+newline+newline);
                report.flush();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void StaticPrinter(Writer report){
        try {
            String newline = System.getProperty("line.separator");
            this.StaticBusesPrinter(report);
            
            report.write(newline);
            report.flush();
            
            this.StaticBranchesPrinter(report);
        } catch (IOException ex) {
            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}