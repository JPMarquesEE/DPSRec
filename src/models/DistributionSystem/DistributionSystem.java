package models.DistributionSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.DefaultEdge;

import models.PassiveElements.*;

public class DistributionSystem {
    private DynamicData dynamicData;
    private StaticData staticData;
    
    // VARIÁVEIS DE TEMPO
    // ** CONSIDERA TODAS AS ENTRADAS IDEAIS (RELACIONADAS AO TEMPO). DESTE MODO NÃO FAZ VERIFICAÇÕES.
    private Integer lengthTime;
    private Integer lengthStudyTime;
    private Integer ActualTime;
    private Integer Period;
    
    private Double LenghtTimeMinutes;
    
    private double Vref,
                   Sref;
    
    // ESTADO DA REDE
    Hashtable<Integer,Hashtable<Pair<Integer,Integer>,Boolean>> BreakersState;
    Hashtable<Integer,Hashtable<Integer,Complex>> Vbuses;
    Hashtable<Integer,Double> Losses;
    
    public DistributionSystem(String FStatic, String FDyn, Integer _lngthTime, Integer _lngthStudyTime, Double _LengthTimeMinutes){
        this.staticData = new StaticData(FStatic);
        this.dynamicData = new DynamicData(FDyn);
        
        this.Vref = this.staticData.getVref();
        this.Sref = this.staticData.getSref();
        
        this.ActualTime = 0;
        this.Period = 1;
        this.lengthStudyTime = new Integer(_lngthStudyTime);
        this.lengthTime = new Integer(_lngthTime);
        
        this.LenghtTimeMinutes = new Double(_LengthTimeMinutes);
        
        this.BreakersState = new Hashtable();
        this.Vbuses = new Hashtable();
        this.Losses = new Hashtable();
    }
    public DistributionSystem(StaticData _cpyStat, DynamicData _cpyDyn, Integer _lngthTime, Integer _lngthStudyTime, Double _LengthTimeMinutes){
        this.staticData = new StaticData(_cpyStat);
        this.dynamicData = new DynamicData(_cpyDyn);
        
        this.Vref = this.staticData.getVref();
        this.Sref = this.staticData.getSref();
        
        this.ActualTime = 0;
        this.Period = 1;
        this.lengthStudyTime = new Integer(_lngthStudyTime);
        this.lengthTime = new Integer(_lngthTime);
        
        this.LenghtTimeMinutes = new Double(_LengthTimeMinutes);
        
        this.BreakersState = new Hashtable();
        this.Vbuses = new Hashtable();
        this.Losses = new Hashtable();
    }
    public DistributionSystem(DistributionSystem _cpy){
        this.dynamicData = new DynamicData(_cpy.dynamicData);
        this.staticData = new StaticData(_cpy.staticData);
        
        this.Vref = this.staticData.getVref();
        this.Sref = this.staticData.getSref();
        
        this.ActualTime = new Integer(_cpy.ActualTime);
        this.lengthStudyTime = new Integer(_cpy.lengthStudyTime);
        this.lengthTime = new Integer(_cpy.lengthTime);
        this.Period = new Integer(_cpy.Period);
        
        this.LenghtTimeMinutes = new Double(_cpy.LenghtTimeMinutes);
        
        this.BreakersState = new Hashtable();
        this.Vbuses = new Hashtable();
        this.Losses = new Hashtable();
        
        Iterator<Map.Entry<Integer,Hashtable<Integer,Complex>>> itVbus = _cpy.Vbuses.entrySet().iterator();
        Iterator<Map.Entry<Integer,Double>> itLosses = _cpy.Losses.entrySet().iterator();
        Iterator<Map.Entry<Integer,Hashtable<Pair<Integer,Integer>,Boolean>>> itStates = _cpy.BreakersState.entrySet().iterator();
        
        while(itVbus.hasNext()){
            Map.Entry<Integer,Hashtable<Integer,Complex>> entryVbus = itVbus.next();
            Iterator<Map.Entry<Integer,Complex>> itVbusAtTime = entryVbus.getValue().entrySet().iterator();
            Hashtable<Integer,Complex> auxVbus = new Hashtable();
            
            while(itVbusAtTime.hasNext()){
                Map.Entry<Integer,Complex> entryVbusAtTime = itVbusAtTime.next();
                
                auxVbus.put(entryVbusAtTime.getKey(),new Complex(entryVbusAtTime.getValue().getReal(),entryVbusAtTime.getValue().getImaginary()));
            }
            
            this.Vbuses.put(entryVbus.getKey(), auxVbus);
        }
        
        while(itLosses.hasNext()){
            Map.Entry<Integer,Double> entry = itLosses.next();
            
            this.Losses.put(entry.getKey(), entry.getValue());
        }
        
        while(itStates.hasNext()){
            Map.Entry<Integer,Hashtable<Pair<Integer,Integer>,Boolean>> entry = itStates.next();
            Hashtable<Pair<Integer,Integer>,Boolean> auxState = new Hashtable();
            Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itStateList = entry.getValue().entrySet().iterator();
            
            while(itStateList.hasNext()){
                Map.Entry<Pair<Integer,Integer>,Boolean> entryState =  itStateList.next();
                
                auxState.put(entryState.getKey(), entryState.getValue()?Boolean.TRUE:Boolean.FALSE);
            }
        }
    }
    
    public DynamicData getDynamicData(){
        return new DynamicData(this.dynamicData);
    }
    public StaticData getStaticData(){
        return new StaticData(this.staticData);
    }
    
    public Hashtable<Integer,Complex> getVbusesAtTime(Integer time){
        Hashtable<Integer,Complex> voltage = new Hashtable();
        Iterator<Map.Entry<Integer,Complex>> itVoltage = this.Vbuses.get(time).entrySet().iterator();
        
        while(itVoltage.hasNext()){
            Map.Entry<Integer,Complex> entry = itVoltage.next();
            
            voltage.put(new Integer(entry.getKey()),new Complex(entry.getValue().getReal(),entry.getValue().getImaginary()));
        }
                
        return voltage;
    }
    public Complex getVbusAtTime(Integer time, Integer bus){
        Complex Vbus = this.Vbuses.get(time).get(bus);
        return new Complex(Vbus.getReal(),Vbus.getImaginary());
    }
    public Double getLossesAtTime(Integer time){
        return new Double(this.Losses.get(time));
    }
    public double getVref(){
        return this.Vref;
    }
    
    // ELEMENTOS TOPOLOGICOS
    public Multigraph<Integer,DefaultEdge> getZoneGraph(){
        return this.staticData.getZoneGraph();
    }
    public Multigraph<Integer,DefaultEdge> getCompleteGraph(){
        return this.staticData.getCompleteGraph();
    }
    public Hashtable<Pair<Integer,Integer>,Boolean> getBreakers(){
        return this.staticData.getBreakers();
    }
    public Hashtable<Integer,Integer> getFeeders(){
        return this.staticData.getFeeders();
    }
    public Hashtable<Pair<Integer,Integer>,Pair<Integer,Integer>> getZonesBranches(){
        return this.staticData.getZonesBranches();
    }
    public Pair<Integer,Integer> getBranch (int ZOne, int ZTwo){       
        return this.staticData.getBranch(ZOne, ZTwo);
    }
    public Hashtable<Integer,ArrayList<Integer>> getZones(){
        return this.staticData.getZones();
    }
    public Integer getZone(Integer bus){
        return this.staticData.getZone(bus);
    }
    
    public void SetInitialState(){
        Hashtable<Pair<Integer,Integer>,Boolean> initialState = this.staticData.getBreakers();
        
        if(this.BreakersState.containsKey(1))
            this.BreakersState.replace(1, initialState);
        else
            this.BreakersState.put(1, initialState);
    }
    public void SetState(Hashtable<Pair<Integer,Integer>,Boolean> state){        
        if(this.BreakersState.containsKey(this.Period))
            this.BreakersState.replace(this.Period, state);
        else
            this.BreakersState.put(this.Period, state);
    }
    
    public void Reset(){
        // RESET DAS VARIAVEIS DE TEMPO
        this.ActualTime = 0;
        this.Period = 1;
        
        // REMOÇÃO DOS ELEMENTOS DINÂMICOS DO STATIC DATA
        this.staticData.Reset();
        
        // RESETA OS ESTADOS
        this.BreakersState = new Hashtable();
        this.Losses = new Hashtable();
        this.Vbuses = new Hashtable();
    }
    
    // FLUXO DE POTÊNCIA
    // ** OBS.: SERIA INTERESSANTE DESENVOLVER UM CONJUNTO DE CLASSES PARA ANÁLISE DO SISTEMA,
    // ** ASSIM PODERIA DESACOPLAR OS MÉTODOS DE ANÁLISE E A REPRESENTAÇÃO DO SISTEMA.
    public boolean PowerFlowAtTime(Double MaxError, Integer MaxIteration){
        // NÃO VERIFICA O ARRAY DE ESTADOS;
        // NÃO VERIFICA A VARIAVEL TIME;
        // NÃO CONSIDERA CASOS DE ILHAMENTO;
        // CONSIDERA O SISTEMA RADIAL;
        
        // CONFIGURAÇÃO INICIAL
        // ** ALOCAR AS CARGAS
        // ** SETAR AS TENSÕES IGUAL A ZERO
        // ** CONFIGURA A TOPOLOGIA DO SISTEMA EM 
//        try{
        boolean out = false;
        if(this.ActualTime<this.lengthTime){
            this.staticData.Reset();
            
//            Writer report = new FileWriter("ITRep.txt");
//            String newline = System.getProperty("line.separator");
            Hashtable<Integer,YLoad> auxLoads = this.dynamicData.getLoadsAtTime(this.ActualTime);
            Hashtable<Integer,Integer> auxFeeders = this.staticData.getFeeders();
            Hashtable<Pair<Integer,Integer>,Boolean> breakersState = this.BreakersState.get(this.Period);
            boolean flagConvergence = true;
            
            Iterator<Map.Entry<Integer,Integer>> itFeeders = auxFeeders.entrySet().iterator();
            Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itBreakers = breakersState.entrySet().iterator();
            Multigraph<Integer,DefaultEdge> topology = this.staticData.getCompleteGraph();

            // ** ALOCA AS CARGAS
            if(!auxLoads.isEmpty())
                this.staticData.addLoads(auxLoads);
            
            // ** CONFIGUROU A TOPOLOGIA DO SISTEMA
            while(itBreakers.hasNext()){
                Map.Entry<Pair<Integer,Integer>,Boolean> entryBreakers = itBreakers.next();
                
                if(!entryBreakers.getValue())
                    topology.removeEdge(entryBreakers.getKey().getFirst(),entryBreakers.getKey().getSecond());
            }          
            
//            this.staticData.StaticPrinter(report);
//            report.write(newline);
//            report.flush();
            
            Complex VoltageRef = new Complex(1,0);
            
            // ** ELEMENTOS DE ORDENAÇÃO
            NeighborIndex<Integer,DefaultEdge> Neighbors = new NeighborIndex(topology);

            while(itFeeders.hasNext()&&flagConvergence){
                Integer Iteration = 0;
                Double Error = Double.MAX_VALUE;
                
                Integer Feeder = itFeeders.next().getKey();
                
                // ** RNP DO FEEDER - CONSTRUÇÃO DO RNP
                ArrayList<Pair<Integer,Integer>> feederRNP = new ArrayList();
                Stack<Pair<Integer,Integer>> nextNode = new Stack();
                ArrayList<Integer> visitedNodes = new ArrayList();
                Pair<Integer,Integer> NodeDepth;
                Integer node, depth;
                
                nextNode.push(new Pair(Feeder,0));
                while(!nextNode.isEmpty()){
                    NodeDepth = nextNode.pop();
                    
                    node = NodeDepth.getFirst();
                    depth = NodeDepth.getSecond();
                    
                    if(!visitedNodes.contains(node)){
                        List<Integer> NeighborsList = Neighbors.neighborListOf(node);
                        visitedNodes.add(node);
                        feederRNP.add(new Pair(NodeDepth));
             
                        for(int i = 0;i<NeighborsList.size();++i){
                            nextNode.push(new Pair(NeighborsList.get(i),depth+1));
                        }
                    }
                }
                
                // ** ITERAÇÃO (POWER FLOW) DA REDE ALIMENTADA PELO NÓ FEEDER
                
                //** Etapa Flat
                this.staticData.setVbus(Feeder, VoltageRef);
                for(int i=1;i<feederRNP.size();++i)
                    this.staticData.setVbus(feederRNP.get(i).getFirst(), VoltageRef);
                
                //** Iterações - Backward-Forward Sweep
                while(!((Error<MaxError)||(MaxIteration<Iteration))){
                    ++Iteration;
                    Error = 0.0;
                    
                   //test.write(newline+"ITERAÇÃO: "+Iteration+newline);
                   // BACKWARD SWEEP
                   for(int i=feederRNP.size()-1;i>=1;--i){
                        for(int j=i;j>=0;--j){
                            if(feederRNP.get(j).getSecond()<feederRNP.get(i).getSecond()){
                                this.staticData.setCurrentBranch(new Pair(feederRNP.get(j).getFirst(),feederRNP.get(i).getFirst()),topology);
                                j=0;
                            }
                        }
                    }

                    // FORWARD SWEEP
                    for(int i=1;i<feederRNP.size();++i){
                        for(int j=i;j>=0;--j){
                            if(feederRNP.get(j).getSecond()<feederRNP.get(i).getSecond()){
                                this.staticData.setVoltageBTo(feederRNP.get(j).getFirst(),feederRNP.get(i).getFirst());
                                j=0;
                            }
                        }
                    }
                
                    Error = this.staticData.getMaxMismatch();
//                    this.staticData.DynamicPrinter(report, Iteration);
//                    report.write(newline);
//                    report.flush();
                }
                if(MaxIteration<=Iteration)
                    flagConvergence = false;
            }
            
            if(flagConvergence){
                out = true;
                ArrayList<Integer> busList = this.staticData.getBusID();
                Hashtable<Integer,Complex> Voltage = new Hashtable();
        
                for(int i = 0; i<busList.size();++i)
                    Voltage.put(busList.get(i), this.staticData.getVbus(busList.get(i)).multiply(this.Vref));
        
                this.Vbuses.put(this.ActualTime, Voltage);
                this.Losses.put(this.ActualTime, this.staticData.Losses()*this.Sref);
        
//              test.close();
            
                ++this.ActualTime;
            }
            this.staticData.Reset();
//            report.close();
        }
        return out;
//        catch(IOException ex){
//            Logger.getLogger(StaticData.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    public boolean PowerFlowAtPeriod(Double MaxError, Integer MaxIteration){
        boolean out = true;
        while((this.ActualTime<(this.lengthStudyTime*this.Period))&&out)
            out = this.PowerFlowAtTime(MaxError, MaxIteration);
        if(out)
            ++this.Period;
        return out;
    }
    public boolean PowerFlow(Double MaxError, Integer MaxIteration){
        boolean out = true;
        while(this.ActualTime<this.lengthTime)
            out = this.PowerFlowAtPeriod(MaxError, MaxIteration);
        return out;
    }
    
    public DistributionSystem getSystemAtPeriod(){
        return new DistributionSystem(this.staticData,this.dynamicData.subDynamicData((this.Period-1)*this.lengthStudyTime, this.Period*this.lengthStudyTime-1),this.lengthStudyTime, this.lengthStudyTime, this.LenghtTimeMinutes);
    }
    
    public void PrintReport(String NameSys){
        try {
            Writer report = new FileWriter("report_"+NameSys+".txt");
            String newline = System.getProperty("line.separator");
            Iterator<Map.Entry<Integer,Hashtable<Pair<Integer,Integer>,Boolean>>> itBreakers = this.BreakersState.entrySet().iterator();
            Iterator<Map.Entry<Integer,Hashtable<Integer,Complex>>> itVbusAtTime = this.Vbuses.entrySet().iterator();
            Iterator<Map.Entry<Integer,Double>> itLosses = this.Losses.entrySet().iterator();
            
            report.write("ENERGIA DISSIPADA (kWh): "+this.LossesKWh()+newline);
            report.flush();
            
            report.write("TENSÕES MÁXIMAS E MÍNIMAS <MIN, MAX>: "+this.VoltageLimits()+newline+newline);
            report.flush();
            
            report.write("TENSÕES NAS BARRAS"+newline);
            report.flush();
            while(itVbusAtTime.hasNext()){
                Map.Entry<Integer,Hashtable<Integer,Complex>> entryVbusAtTime = itVbusAtTime.next();
                Iterator<Map.Entry<Integer,Complex>> itVbus = entryVbusAtTime.getValue().entrySet().iterator();
                
                report.write("Time: "+entryVbusAtTime.getKey()+newline);
                report.flush();
                while(itVbus.hasNext()){
                    Map.Entry<Integer,Complex> entryVbus = itVbus.next();
                    
                    report.write(entryVbus.getKey()+"\t");
                    report.flush();
                    
                    report.write(entryVbus.getValue().abs()+"\t"+entryVbus.getValue().getArgument()*180.0/Math.PI+newline);
                    report.flush();
                }
            }
            
            report.write(newline+"POTÊNCIA DISSIPADA [kW]"+newline);
            report.flush();
            while(itLosses.hasNext()){
                Map.Entry<Integer,Double> entryLosses = itLosses.next();
                
                report.write(entryLosses.getKey()+"\t"+entryLosses.getValue()/1000+newline);
                report.flush();
            }
            

            report.write(newline+"CHAVES ABERTAS"+newline);
            report.flush();
            while(itBreakers.hasNext()){
                Map.Entry<Integer,Hashtable<Pair<Integer,Integer>,Boolean>> entryBreakers = itBreakers.next();
                Iterator<Map.Entry<Pair<Integer,Integer>,Boolean>> itState = entryBreakers.getValue().entrySet().iterator();
                
                report.write("Period: "+entryBreakers.getKey()+newline);
                report.flush();
                
                while(itState.hasNext()){
                    Map.Entry<Pair<Integer,Integer>,Boolean> entryState = itState.next();
                    
                    if(!entryState.getValue()){
                        report.write(entryState.getKey()+newline);
                        report.flush();
                    }
                }
            }
            
            report.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DistributionSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // METODOS DE OTIMIZAÇÃO - RECONFIGUÇÃO POR ALGORITMO GENETICO
    // FITNESS 
    public double LossesKWh(){
        double lossesKWh = 0;
        Iterator<Map.Entry<Integer,Double>> it = this.Losses.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,Double> entry = it.next();
            
            lossesKWh = lossesKWh+entry.getValue()*60*this.LenghtTimeMinutes/(3.6e+6);
        }
        
        return lossesKWh;
    }
    public Pair<Double,Double> VoltageLimits(){
        Pair<Double,Double> VoltageLimits;
        double min = Double.MAX_VALUE,
               max = 0;
        Iterator<Map.Entry<Integer,Hashtable<Integer,Complex>>> itTime = this.Vbuses.entrySet().iterator();
        
        while(itTime.hasNext()){
            Map.Entry<Integer,Hashtable<Integer,Complex>> entryTime = itTime.next();
            Iterator<Map.Entry<Integer,Complex>> itVbuses = entryTime.getValue().entrySet().iterator();
            
            while(itVbuses.hasNext()){
                Map.Entry<Integer,Complex> entryVbuses = itVbuses.next();
                Complex Vbus = entryVbuses.getValue();
                
                double abs = Vbus.abs();
                if((min>abs)&&(abs!=0))
                    min = abs;
                if(abs>max)
                    max = abs;
            }
        }
        
        VoltageLimits = new Pair(min,max);
        
        return VoltageLimits;
    }
    
    public void rndPrinter(int R, int C, int I, int percVar, String NameSys, String curvesFile){
        this.dynamicData.RandomDynamicDataCreator(R, C, I, percVar, NameSys, curvesFile);
    }
}