package reconfiguration.geneticalgorithm;

import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.commons.math3.util.Pair;

import models.DistributionSystem.DistributionSystem;

public class NSNPAlgorithm {
    int lengthPopulation,
        maxIteration,
        maxCount;
    
    public NSNPAlgorithm(int _lngthPop, int _maxIt, int _maxCount){
        this.lengthPopulation = _lngthPop;
        this.maxIteration  = _maxIt;
        this.maxCount = _maxCount;
    }
    
    public OutputNSNP runNSNP(DistributionSystem sys, RNP FirstState, double error, int maxIt, boolean printReport, String dir){
        long startTime = System.nanoTime();                            // Calcular o tempo aproximado da simulação;
        Hashtable<Integer,Pair<Double,Double>> data = new Hashtable(); // Estrutura para armazenar os dados do relatório;
        
        Pair<Double,Double> auxBest,    // Variável auxiliar para avaliar convergência
                            Best;       // Variável para avaliar convergência
                                        // PADRÃO - firstkey (Losses) / secondkey (VA) - Def. em Population.bestValues();
        int it = 0,         // Contador de iterações
            auxCount = 0;   // Contador de "convergência"
        
        DistributionSystem cpySys = new DistributionSystem(sys);                            // Cópia do sistema de distribuição;
        CromossomeRNP FirstIndividual = new CromossomeRNP(cpySys,FirstState,error,maxIt);   // Primeira configuração
        PopulationRNP P = new PopulationRNP(cpySys,FirstIndividual,this.lengthPopulation,error,maxIt),  // População P
                      Q = P.OffspringGenerator(cpySys,error,maxIt);                                     // População de Filhos Q
        ArrayList<CromossomeRNP> populationR,   // Lista de individuos
                                 populationP,
                                 populationQ;
        
        //** Inicialização das variáveis para avaliação de convergência e geração do relatório
        Best = P.bestValues();
        auxBest = Best;
        auxCount = 0;
        if(printReport)
            data.put(it, auxBest);
        
        //** Etapa de Formação da população R = P+Q;
        populationP = P.getPopulation();
        populationQ = Q.getPopulation();
        
        //** Processo do Iterativo do NSNP
        while((auxCount<this.maxCount)&&(it<this.maxIteration)){
            ++it;
            
            //** Criação da nova de geração de individuos
            populationR = populationP;
            populationR.addAll(populationQ);
            PopulationRNP R = new PopulationRNP(populationR);
            
            P = R.ParentsGenerator(this.lengthPopulation);
            Q = P.OffspringGenerator(cpySys,error,maxIt);
            
            populationP = P.getPopulation();
            populationQ = Q.getPopulation();
            
            //** Avaliação de convergência e construção do relatório
            ++auxCount;
            auxBest = P.bestValues();
            if((!auxBest.getKey().equals(Best.getKey()))||(!auxBest.getValue().equals(Best.getValue()))){
                auxCount = 0;
                Best = auxBest;
            }
            
            data.put(it, auxBest);
        }
        
        OutputNSNP out = new OutputNSNP(P.ParettoFront(),data,it, (System.nanoTime()-startTime)/(1e9));
        if(printReport)
            out.PrintReport(dir, this.lengthPopulation, this.maxIteration, this.maxCount);
        
        return out;
    }
    public ArrayList<OutputNSNP> runNthNSNP(DistributionSystem sys, RNP FirstState, double error, int maxIt, String dir, int n){
        double refLosses = Double.POSITIVE_INFINITY,
               auxLosses,
               refVA = 0,
               auxVA;
        int freqLosses = 0,
            freqVA = 0,
            itLosses = 0,
            itVA = 0;
        double timeLosses = 0.0,
               timeVA = 0.0;
        OutputNSNP outVA = this.runNSNP(sys, FirstState, error, maxIt, false, dir),
                   outLosses = outVA,
                   auxOutput = outVA;
        
        refLosses = outLosses.bestValLosses();
        refVA = outVA.bestValVA();
        ++freqLosses;
        itLosses = itLosses + auxOutput.numIt();
        timeLosses = timeLosses+auxOutput.time();
        ++freqVA;
        itVA = itVA + auxOutput.numIt();
        timeVA = timeVA+auxOutput.time();
        for(int i=1;i<n;++i){
            System.out.println(i+"/"+n);
            auxOutput = this.runNSNP(sys, FirstState, error, maxIt, false, dir);
            auxLosses = auxOutput.bestValLosses();
            auxVA = auxOutput.bestValVA();
            
            // Substitui pelos melhores
            if(auxLosses<refLosses){
                outLosses = auxOutput;
                refLosses = auxLosses;
                freqLosses = 0;
                itLosses = 0;
                timeLosses = 0;
            }
            if(auxVA>refVA){
                outVA = auxOutput;
                refVA = auxVA;
                freqVA = 0;
                itVA = 0;
                timeVA = 0;
            }
            
            // Atualiza dados
            if(auxLosses==refLosses){
                ++freqLosses;
                itLosses = itLosses + auxOutput.numIt();
                timeLosses = timeLosses+auxOutput.time();
            }
            if(auxVA==refVA){
                ++freqVA;
                itVA = itVA + auxOutput.numIt();
                timeVA = timeVA+auxOutput.time();
            }
        }
        
        outLosses.PrintGeneralReport(dir+"_Losses_", this.lengthPopulation, this.maxIteration , this.maxCount, n, timeLosses/freqLosses, itLosses/freqLosses, freqLosses*100/n);
        outVA.PrintGeneralReport(dir+"_VA_", this.lengthPopulation, this.maxIteration, this.maxCount, n, timeVA/freqVA, itVA/freqVA, freqVA*100/n);
        ArrayList<OutputNSNP> out = new ArrayList();
        out.add(outLosses);
        out.add(outVA);
        
        return out;
    }
}