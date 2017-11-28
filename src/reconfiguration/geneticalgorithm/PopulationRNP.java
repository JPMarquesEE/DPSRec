package reconfiguration.geneticalgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.util.Pair;

import models.DistributionSystem.DistributionSystem;

public class PopulationRNP {
    private int numIndividuals;
    
    private ArrayList<CromossomeRNP> Population;
    private Hashtable<Integer,ArrayList<CromossomeRNP>> Fronts;
    
    public PopulationRNP(DistributionSystem sys, CromossomeRNP FirstIndividual, int _numIndividuals, double error, int maxIt){
        this.Population = new ArrayList();
        this.Fronts = new Hashtable();
        
        this.numIndividuals = _numIndividuals;
        
        int rndIndex,
            rndNum,
            choiceOperator = 50;
        
        CromossomeRNP cpy = new CromossomeRNP(FirstIndividual);
        
        this.Population.add(cpy);
        for(int i=0;i<this.numIndividuals-1;++i){
            rndIndex = ThreadLocalRandom.current().nextInt(0, this.Population.size());
            rndNum = ThreadLocalRandom.current().nextInt(0, 101);
            if(rndNum<choiceOperator){
                this.Population.add(this.Population.get(rndIndex).PAOGenerator(sys, error, maxIt));
                if(choiceOperator>0)
                    --choiceOperator;
            }
            else{
                this.Population.add(this.Population.get(rndIndex).CAOGenerator(sys, error, maxIt));
                if(choiceOperator<101)
                    ++choiceOperator;
            }
        }
        
        // Define os Fronts e Seta os CrowDist's
        this.ConstructFront();
        this.SetCrowdist();
    }
    public PopulationRNP(ArrayList<CromossomeRNP> _Population){
        this.numIndividuals = _Population.size();
        this.Fronts = new Hashtable();
        this.Population = new ArrayList();
        
        for(int i=0;i<_Population.size();++i)
            this.Population.add(new CromossomeRNP(_Population.get(i)));
        
        // Define os Fronts e Seta os CrowDist's
        this.ConstructFront();
        this.SetCrowdist();
    }
    public PopulationRNP(PopulationRNP _cpy){
        this.numIndividuals = _cpy.numIndividuals;
        
        this.Population = new ArrayList();
        this.Fronts = new Hashtable();
        
        for(int i=0;i<_cpy.Population.size();++i)
            this.Population.add(new CromossomeRNP(_cpy.Population.get(i)));
        
        // Define os Fronts e Seta os CrowDist's
        this.ConstructFront();
        this.SetCrowdist();
    }
    
    public PopulationRNP ParentsGenerator(int sizeParents){
        int size = 0;
        boolean run = true;
        int Front = 1;
        ArrayList<CromossomeRNP> parentsList = new ArrayList();
        
        while((Front<=this.Fronts.size())&&(run)){
            ArrayList<CromossomeRNP> auxPopulationList = this.Fronts.get(Front);
            int auxSize = auxPopulationList.size();
            
            if((auxSize+size)<=sizeParents){
                for(int i=0;i<auxPopulationList.size();++i)
                    parentsList.add(auxPopulationList.get(i));
                
                size = parentsList.size();
            }
            else{
                auxPopulationList.sort(new Comparator<CromossomeRNP>(){
                    @Override
                    public int compare(CromossomeRNP X, CromossomeRNP Y){
                        Double xCrowdist = X.getCrowDist(),
                               yCrowdist = Y.getCrowDist();
                        
                        return -(xCrowdist.compareTo(yCrowdist));
                    }
                });
                
                int k = sizeParents - size;
                
                for(int i=0;i<k;++i)
                    parentsList.add(auxPopulationList.get(i));
                
                run = false;
            }
            
            ++Front;
        }
        
        return new PopulationRNP(parentsList);
    }
    
    public PopulationRNP OffspringGenerator(DistributionSystem sys, double error, int maxIt){
        ArrayList<CromossomeRNP> offspringPopulation = new ArrayList();
        
        for(int i=0;i<this.numIndividuals;++i){
            int indxOne = ThreadLocalRandom.current().nextInt(0, this.Population.size()),
                indxTwo = ThreadLocalRandom.current().nextInt(0, this.Population.size());
            CromossomeRNP offspringIndividual = tournamentGenerator(this.Population.get(indxOne),this.Population.get(indxTwo),sys,error,maxIt);  
            offspringPopulation.add(offspringIndividual);
        }
        
        return new PopulationRNP(offspringPopulation);
    }
    private CromossomeRNP tournamentGenerator(CromossomeRNP X, CromossomeRNP Y, DistributionSystem sys, double error, int maxIt){
        int xFront = X.getFront(),
            yFront = Y.getFront();
        double xCrowDist = X.getCrowDist(),
               yCrowDist = Y.getCrowDist();
        boolean operator = ThreadLocalRandom.current().nextBoolean();
        
        if(xFront>yFront)   return operator? X.CAOGenerator(sys, error, maxIt):X.PAOGenerator(sys, error, maxIt);
        else if(xFront<yFront)  return operator? Y.CAOGenerator(sys, error, maxIt):Y.PAOGenerator(sys, error, maxIt);
        else{
            if(xCrowDist>yCrowDist) return operator? X.CAOGenerator(sys, error, maxIt):X.PAOGenerator(sys, error, maxIt);
            else if(xCrowDist<yCrowDist)    return operator? Y.CAOGenerator(sys, error, maxIt):Y.PAOGenerator(sys, error, maxIt);
            else{
                boolean choice = ThreadLocalRandom.current().nextBoolean();
                
                return choice? (operator? X.CAOGenerator(sys, error, maxIt):X.PAOGenerator(sys, error, maxIt)):operator? Y.CAOGenerator(sys, error, maxIt):Y.PAOGenerator(sys, error, maxIt);
            }
        }
    }
    
    public ArrayList<CromossomeRNP> getPopulation(){
        ArrayList<CromossomeRNP> auxPopulation = new ArrayList();
        
        for(int i=0;i<this.Population.size();++i)
            auxPopulation.add(new CromossomeRNP(this.Population.get(i)));
        
        return auxPopulation;
    }
    public int PopulationSize(){
        return this.numIndividuals;
    }
    
    private void ConstructFront(){
        int Front = 1;
        ArrayList<CromossomeRNP> FirstFront = new ArrayList();
        ArrayList<Integer> FirstFrontIndexes = new ArrayList();
        Hashtable<Integer,Pair<Integer,ArrayList<Integer>>> auxHash = new Hashtable();
        Hashtable<Integer,ArrayList<Integer>> auxFronts = new Hashtable();
        
        for(int i=0;i<this.Population.size();++i){
            int auxCount = 0;
            ArrayList<Integer> dominated = new ArrayList();
            CromossomeRNP ith = this.Population.get(i);
            
            for(int j=0;j<this.Population.size();++j){
                if(i!=j){
                    CromossomeRNP jth = this.Population.get(j);
                    if(ith.ThisDominates(jth))
                        dominated.add(j);
                    if(ith.DominatedBy(jth))
                        ++auxCount;
                }
            }
            
            auxHash.put(i, new Pair(auxCount,dominated));
            
            if(auxCount==0){
                FirstFront.add(ith);
                ith.setFront(Front);
                FirstFrontIndexes.add(i);
            }
        }
        
        this.Fronts.put(Front, FirstFront);
        auxFronts.put(Front,FirstFrontIndexes);
        
        ArrayList<Integer> ActualFront = FirstFrontIndexes;
        while(!ActualFront.isEmpty()){
            ArrayList<Integer> FrontIndexes = new ArrayList();
            ArrayList<CromossomeRNP> FrontRNP = new ArrayList();
            
            for(int i=0;i<ActualFront.size();++i){
                int actualIndxRNP = ActualFront.get(i);
                ArrayList<Integer> dominatedList = auxHash.get(actualIndxRNP).getValue();
                
                for(int j=0;j<dominatedList.size();++j){
                    int actualDominatedIndx = dominatedList.get(j);
                    Pair<Integer,ArrayList<Integer>> actualDominatedPair = auxHash.get(actualDominatedIndx);
                    int dominatedNumber = actualDominatedPair.getFirst()-1;
                    
                    auxHash.replace(actualDominatedIndx, new Pair(dominatedNumber,actualDominatedPair.getSecond()));
                    if(dominatedNumber==0){
                        CromossomeRNP auxRNP = this.Population.get(actualDominatedIndx);
                        
                        auxRNP.setFront(Front+1);
                        FrontIndexes.add(actualDominatedIndx);
                        FrontRNP.add(auxRNP);
                    }
                }
            }
            
            if(!FrontIndexes.isEmpty()){
                ++Front;
                this.Fronts.put(Front, FrontRNP);
                auxFronts.put(Front,FrontIndexes);
            }
            ActualFront = FrontIndexes;
        }
    }
    private void SetCrowdist(){
        Iterator<Map.Entry<Integer,ArrayList<CromossomeRNP>>> it = this.Fronts.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,ArrayList<CromossomeRNP>> entry = it.next();
            ArrayList<CromossomeRNP> aux = entry.getValue();
            
            for(int i=0;i<aux.size();++i)
                aux.get(i).setCrowDist(0.0);
            
            for(int i=0;i<2;++i){
                this.SortObjectiveFuctions(i, true);
                if(!aux.isEmpty()){
                    Double max, min;
                    
                    if(i==0){
                        min = aux.get(0).getVoltageAggregation();
                        max = aux.get(aux.size()-1).getVoltageAggregation();
                    }
                    else{
                        min = aux.get(0).getLosses();
                        max = aux.get(aux.size()-1).getLosses();
                    }
                    
                    aux.get(0).setCrowDist(Double.POSITIVE_INFINITY);
                    aux.get(aux.size()-1).setCrowDist(Double.POSITIVE_INFINITY);
                    
                    for(int j=1;j<(aux.size()-1);++j){
                        double valueTop, valueDown;
                        
                        if(i==0){
                            valueTop = aux.get(j+1).getVoltageAggregation();
                            valueDown = aux.get(j-1).getVoltageAggregation();
                        }
                        else{
                            valueTop = aux.get(j+1).getLosses();
                            valueDown = aux.get(j-1).getLosses();
                        }
                        double newCrowDist;
                        
                        if(((valueTop-valueDown)==0)&&((max-min)==0))
                            newCrowDist = 0;
                        else
                            newCrowDist = aux.get(j).getCrowDist()+(valueTop-valueDown)/(max-min);
                        
                        aux.get(j).setCrowDist(newCrowDist);
                    }
                }
            }
        }
    }
    private void SortObjectiveFuctions(int indx, boolean ascending){
        Iterator<Map.Entry<Integer,ArrayList<CromossomeRNP>>> it = this.Fronts.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer,ArrayList<CromossomeRNP>> entry = it.next();
            
            entry.getValue().sort(new ComparatorImpl(indx, ascending));
        }
    }

    public ArrayList<CromossomeRNP> ParettoFront(){
        ArrayList<CromossomeRNP> out = new ArrayList(),
                                 aux = this.Fronts.get(1);
        
        for(int i=0;i<aux.size();++i)
            out.add(new CromossomeRNP(aux.get(i)));
        
        return out;
    }
    public Pair<Double,Double> bestValues(){
        Double losses, VA;
        ArrayList<CromossomeRNP> aux1 = new ArrayList();
        ArrayList<CromossomeRNP> aux2 = this.Fronts.get(1); // Primeira Fronteira
        
        for(int i=0;i<aux2.size();++i)
            aux1.add(aux2.get(i));
        
        // Menor indíce de perdas encontrado;
        aux1.sort(new ComparatorImpl(1,true));
        losses = aux1.get(0).getLosses();
        
        // Maior Voltage Aggregation encontrado;
        aux1.sort(new ComparatorImpl(2,true));
        VA = aux1.get(aux2.size()-1).getVoltageAggregation();
        
        return new Pair(losses,VA);
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
