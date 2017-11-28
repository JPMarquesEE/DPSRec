package reconfiguration.geneticalgorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.DistributionSystem.DistributionSystem;
import org.apache.commons.math3.util.Pair;

public class OutputNSNP {
    private ArrayList<CromossomeRNP> ParettoFront;
    private CromossomeRNP bestLosses;
    private CromossomeRNP bestVA;
    private Hashtable<Integer,Pair<Double,Double>> dtaBest;
    private int numIt;
    private double time;
    
    public OutputNSNP(ArrayList<CromossomeRNP> _ParettoFront, Hashtable<Integer,Pair<Double,Double>> _dtaBest, int _numIt, double _time){
        this.numIt = _numIt;
        this.time = _time;
        this.ParettoFront = new ArrayList();
        this.dtaBest = new Hashtable();
        
        this.ParettoFront.addAll(_ParettoFront);
        this.dtaBest.putAll(_dtaBest);
        
        this.ParettoFront.sort(new ComparatorImpl(1,true));
        this.bestLosses = new CromossomeRNP(this.ParettoFront.get(0));
        
        this.ParettoFront.sort(new ComparatorImpl(2,true));
        this.bestVA = new CromossomeRNP(this.ParettoFront.get(this.ParettoFront.size()-1));
    }
    
    public void PrintReport(String _dir, int _lengthPop, int _maxIt, int _maxCont){
        try {
            Writer report = new FileWriter(_dir+"_ga_report.txt");
            String newline = System.getProperty("line.separator");
            Iterator<Map.Entry<Integer,Pair<Double,Double>>> itData = this.dtaBest.entrySet().iterator();
            
            // Cabeçalho do Relatório
            report.write("Tempo: "+this.time/(1e9)+newline+"Num. de Individuos: "+_lengthPop+newline+"Iterações: "+this.numIt+newline+"Max. Iteração: "+_maxIt+newline+"Max. Contador: "+_maxCont+newline+newline);
            report.flush();
            
            // Melhores por Iteração
            report.write("Melhores por Iteração"+newline);
            report.flush();
            while(itData.hasNext()){
                Map.Entry<Integer,Pair<Double,Double>> entryData = itData.next();
                
                report.write(entryData.getKey()+"\t"+entryData.getValue().getFirst()+"\t"+entryData.getValue().getSecond()+newline);
                report.flush();
            }
            
            // Fronteiras de Pareto 
            report.write(newline+"Fronteiras de Pareto"+newline);
            report.flush();
            for(int i=0;i<this.ParettoFront.size();++i){
                report.write(this.ParettoFront.get(i).getLosses()+"\t"+this.ParettoFront.get(i).getVoltageAggregation()+newline);
                report.flush();
            }
            report.close();
        }
        catch (IOException ex) {
            Logger.getLogger(DistributionSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void PrintGeneralReport(String _dir, int _lengthPop, int _maxIt, int _maxCont, int _numExec, double _avTime, int _avIt, double _freq){
        try {
            Writer report = new FileWriter(_dir+"_general_ga_report.txt");
            String newline = System.getProperty("line.separator");
            Iterator<Map.Entry<Integer,Pair<Double,Double>>> itData = this.dtaBest.entrySet().iterator();
            
            // Cabeçalho do Relatório
            report.write("Num. Execuções: "+_numExec+newline+"Tempo Médio: "+_avTime+newline+"Num. Iterações Médias: "+_avIt+newline+"Frequencia [%]: "+_freq+newline+"Tempo: "+this.time+newline+"Num. de Individuos: "+_lengthPop+newline+"Iterações: "+this.numIt+newline+"Max. Iteração: "+_maxIt+newline+"Max. Contador: "+_maxCont+newline+newline);
            report.flush();
            
            // Melhores por Iteração
            report.write("Melhores por Iteração"+newline);
            report.flush();
            while(itData.hasNext()){
                Map.Entry<Integer,Pair<Double,Double>> entryData = itData.next();
                
                report.write(entryData.getKey()+"\t"+entryData.getValue().getFirst()+"\t"+entryData.getValue().getSecond()+newline);
                report.flush();
            }
            
            // Fronteiras de Pareto 
            report.write(newline+"Fronteiras de Pareto"+newline);
            report.flush();
            for(int i=0;i<this.ParettoFront.size();++i){
                report.write(this.ParettoFront.get(i).getLosses()+"\t"+this.ParettoFront.get(i).getVoltageAggregation()+newline);
                report.flush();
            }
            report.close();
        }
        catch (IOException ex) {
            Logger.getLogger(DistributionSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public CromossomeRNP bestLosses(){
        return new CromossomeRNP(this.bestLosses);
    }
    public double bestValLosses(){
        return this.bestLosses.getLosses();
    }
    public CromossomeRNP bestVA(){
        return new CromossomeRNP(this.bestVA);
    }
    public double bestValVA(){
        return this.bestVA.getVoltageAggregation();
    }
    public double time(){
        return this.time;
    }
    public int numIt(){
        return this.numIt;
    }
    
    private class ComparatorImpl implements Comparator<CromossomeRNP> {
        private int indx;
        private boolean ascending;

        public ComparatorImpl(int _indx, boolean _asc){
            this.indx = _indx;
            this.ascending = _asc;
        }

        @Override
        public int compare(CromossomeRNP x, CromossomeRNP y) {
            switch(indx){
                case 1:
                    // Losses
                    Double xLosses = x.getLosses();
                    Double yLosses = y.getLosses();
                    int sComp = xLosses.compareTo(yLosses);

                    if (sComp != 0) {
                        return this.ascending? sComp:-sComp;
                    }
                    else{
                        Double xVA = x.getVoltageAggregation();
                        Double yVA = y.getVoltageAggregation();
                        return this.ascending? xVA.compareTo(yVA):-(xVA.compareTo(yVA));
                    }
                default:
                    // Voltage Aggregation
                    Double xVA_1 = x.getVoltageAggregation();
                    Double yVA_1 = y.getVoltageAggregation();
                    int sComp_1 = xVA_1.compareTo(yVA_1);

                    if (sComp_1 != 0) {
                        return this.ascending? sComp_1:-sComp_1;
                    }
                    else{
                        Double xLosses_1 = x.getLosses();
                        Double yLosses_1 = y.getLosses();
                        return this.ascending? xLosses_1.compareTo(yLosses_1):-(xLosses_1.compareTo(yLosses_1));
                    }
            }
        }
    }
}
