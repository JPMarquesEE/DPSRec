package models.DistributionSystem;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.Pair;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.PassiveElements.*;

public class DynamicData {
    private double Vref;
    private double Sref;
    
    Hashtable<Integer,Hashtable<Integer,YLoad>> LoadsData;
    
    public DynamicData(String FDynamicData){
        try{
            Scanner inputDynamicData = new Scanner(new File(FDynamicData));
            
            this.LoadsData = new Hashtable();
            
            while(inputDynamicData.hasNext()){
                if(inputDynamicData.hasNext("VREF")){
                    inputDynamicData.nextLine();
                    this.Vref = inputDynamicData.nextDouble();
                }
                else if(inputDynamicData.hasNext("SREF")){
                    inputDynamicData.nextLine();
                    inputDynamicData.nextLine();
                    inputDynamicData.nextLine();
                    
                    this.Sref = inputDynamicData.nextDouble();
                }
                else if(inputDynamicData.hasNext("LOADS")){
                    inputDynamicData.nextLine();
                    inputDynamicData.nextLine();
                    while(inputDynamicData.hasNext("Time")){
                        int time;
                        Hashtable<Integer,YLoad> auxLoads = new Hashtable();
                        
                        inputDynamicData.next();
                        time = inputDynamicData.nextInt();
                        
                        while(inputDynamicData.hasNextInt()){
                            int busID;
                            double active, reactive;
                            
                            YLoad auxLoad;
                            busID = inputDynamicData.nextInt();
                            
                            active = inputDynamicData.nextDouble()*1000/this.Sref;
                            reactive = inputDynamicData.nextDouble()*1000/this.Sref;
                                
                            auxLoad = new YLoad(active,reactive,true);
                            auxLoads.put(busID,auxLoad);
                        }
                        
                        this.LoadsData.put(time, auxLoads);
                    }
                }
                else{
                    inputDynamicData.nextLine();
                }
            }
            
            inputDynamicData.close();
            
        }
        catch (FileNotFoundException e){
            System.err.println("Erro na abertura do arquivo.");
            System.exit(1);
        }
    }
    public DynamicData(Hashtable<Integer,Hashtable<Integer,YLoad>> _loads){
        Iterator<Map.Entry<Integer,Hashtable<Integer,YLoad>>> itLoads = _loads.entrySet().iterator();
        
        this.LoadsData = new Hashtable();
        Integer timeLoads = 0;
        
        while(itLoads.hasNext()){
            Map.Entry<Integer,Hashtable<Integer,YLoad>> entryLoads = itLoads.next();
            Iterator<Map.Entry<Integer,YLoad>> itLoadsBus = entryLoads.getValue().entrySet().iterator();
            Hashtable<Integer,YLoad> loadsAux = new Hashtable();
            
            while(itLoadsBus.hasNext()){
                Map.Entry<Integer,YLoad> entryLoadsBus = itLoadsBus.next();
                loadsAux.put(entryLoadsBus.getKey(),new YLoad(entryLoadsBus.getValue()));
            }
            this.LoadsData.put(timeLoads,loadsAux);
            ++timeLoads;
        }
    }
    public DynamicData(DynamicData _cpy){
        Iterator<Map.Entry<Integer,Hashtable<Integer,YLoad>>> itLoads = _cpy.LoadsData.entrySet().iterator();
      
        this.LoadsData = new Hashtable();
        
        while(itLoads.hasNext()){
            Map.Entry<Integer,Hashtable<Integer,YLoad>> entry = itLoads.next();
            Iterator<Map.Entry<Integer,YLoad>> itLoadsBus = entry.getValue().entrySet().iterator();
            Hashtable<Integer,YLoad> loadsAux = new Hashtable();
            
            while(itLoadsBus.hasNext()){
                Map.Entry<Integer,YLoad> entryLoads = itLoadsBus.next();
                loadsAux.put(entryLoads.getKey(),new YLoad(entryLoads.getValue()));
            }
            this.LoadsData.put(entry.getKey(),loadsAux);
        }
    }
    
    public Hashtable<Integer,YLoad> getLoadsAtTime(Integer time){
        if(this.LoadsData.containsKey(time)){
            Hashtable<Integer,YLoad> loads = new Hashtable();
            Iterator<Map.Entry<Integer,YLoad>> itLoads = this.LoadsData.get(time).entrySet().iterator();
            
            while(itLoads.hasNext()){
                Map.Entry<Integer,YLoad> entry = itLoads.next();
                
                loads.put(new Integer(entry.getKey()), new YLoad(entry.getValue()));
            }
            
            return loads;
        }    
        else
            return new Hashtable();
    }
    
    public Hashtable<Integer,Hashtable<Integer,YLoad>> getLoadsAtInterval(Integer initialTime, Integer finalTime){
        // Considera que o intervalo especificado é válido.
        Hashtable<Integer,Hashtable<Integer,YLoad>> loadsAtInterval = new Hashtable();
        Integer time = initialTime;
        
        while(time<=finalTime){
            Hashtable<Integer,YLoad> auxLoads = this.getLoadsAtTime(time);
            loadsAtInterval.put(time, auxLoads);
            ++time;
        }
        
        return loadsAtInterval;
    }
    
    public DynamicData subDynamicData(Integer initialTime, Integer finalTime){
        return new DynamicData(getLoadsAtInterval(initialTime,finalTime));
    }
    public void RandomDynamicDataCreator( int R, int C, int I, int percVar, String NameSys, String curvesFile){
        // FUNÇÃO IMPROVISADA APENAS PARA GERAR OS DADOS DE DEMANDA VARIÁVEL DO DOCUMENTO DO TCC
        // LEITURA A PARTIR DOS DADOS DE CARGA FIXA (CONSIDERA O TAMANHO (SIZE) DE LoadsData IGUAL A 1)
        try{
            Scanner cFile = new Scanner(new File(curvesFile));
            int t0 = 0, auxT = 0,
                numR = 0, numC = 0, numI = 0;
            Hashtable<Pair<Integer,Integer>,ArrayList<Double>> outCoeff = new Hashtable();
            Hashtable<Integer,Hashtable<Integer,Pair<Double,Double>>> outDD = new Hashtable();
            Iterator<Map.Entry<Integer,YLoad>> it = this.LoadsData.get(t0).entrySet().iterator();
            ArrayList<Double> inR = new ArrayList(),
                              inC = new ArrayList(),
                              inI = new ArrayList();
            
            
            while(cFile.hasNextDouble()){
                inR.add(cFile.nextDouble());
                inC.add(cFile.nextDouble());
                inI.add(cFile.nextDouble());
                ++auxT;
            }
            
            cFile.close();
            
            //Construção dos Coeficientes
            while(it.hasNext()){
                Map.Entry<Integer,YLoad> entry = it.next();
                int auxRnd = ThreadLocalRandom.current().nextInt(0, 101),
                    rangeR = R,
                    rangeC = rangeR+C,
                    rangeI = rangeC+I,
                    type = 0;
                ArrayList<Double> coeff = new ArrayList();
                
                
                
                // 1 - Residencial
                // 2 - Comercial
                // 3 - Industrial
                
                if(auxRnd>=0&&auxRnd<rangeR){
                    type=1;
                    ++numR;
                    
                    for(int i=0;i<inR.size();++i){
                        double var = ThreadLocalRandom.current().nextInt(0, percVar+1)/100.0;
                        
                        if(ThreadLocalRandom.current().nextBoolean())
                            coeff.add(inR.get(i)+var);
                        else
                            coeff.add(inR.get(i)-var);
                    }
                }
                else if(auxRnd>rangeR&&auxRnd<=rangeC){
                    type=2;
                    ++numC;
                    
                    for(int i=0;i<inC.size();++i){
                        double var = ThreadLocalRandom.current().nextInt(0, percVar+1)/100.0;
                        
                        if(ThreadLocalRandom.current().nextBoolean())
                            coeff.add(inC.get(i)+var);
                        else
                            coeff.add(inC.get(i)-var);
                    }
                }
                else{
                    type=3;
                    ++numI;
                    
                    for(int i=0;i<inI.size();++i){
                        double var = ThreadLocalRandom.current().nextInt(0, percVar+1)/100.0;
                        
                        if(ThreadLocalRandom.current().nextBoolean())
                            coeff.add(inI.get(i)+var);
                        else
                            coeff.add(inI.get(i)-var);
                    }
                }
                
                outCoeff.put(new Pair(entry.getKey(),type), coeff);
            }
            
            // Construção das Cargas Dinâmicas
            for(int i =0;i<auxT;++i){
                Hashtable<Integer,Pair<Double,Double>> auxLoad = new Hashtable();
                Iterator<Map.Entry<Pair<Integer,Integer>,ArrayList<Double>>> itCoeff = outCoeff.entrySet().iterator();
                
                while(itCoeff.hasNext()){
                    Map.Entry<Pair<Integer,Integer>,ArrayList<Double>> entryCoeff = itCoeff.next();
                    Integer id = entryCoeff.getKey().getKey();
                    YLoad aux = this.LoadsData.get(0).get(id);
                    Double auxP = aux.getRatedPower().getReal()*this.Sref/1000,
                           auxQ = aux.getRatedPower().getImaginary()*this.Sref/1000,
                           coeff = entryCoeff.getValue().get(i);
                    
                    auxLoad.put(id, new Pair(auxP*coeff,auxQ*coeff));
                }
                
                outDD.put(i, auxLoad);
            }
            
            Writer txtDD = new FileWriter(NameSys+".txt"),
                   txtCoeff = new FileWriter(NameSys+"_coeffs.txt");
            String newline = System.getProperty("line.separator");
            
            // Escreve o arquivo de dados dinâmicos
            Iterator<Map.Entry<Integer,Hashtable<Integer,Pair<Double,Double>>>> itDDOut = outDD.entrySet().iterator();
            
            txtDD.write("VREF"+newline+this.Vref+newline+newline+"SREF"+newline+this.Sref+newline+newline+"LOADS"+newline+
                    "ID\tP[k]\tQ[k]"+newline);
            txtDD.flush();
            
            while(itDDOut.hasNext()){
                Map.Entry<Integer,Hashtable<Integer,Pair<Double,Double>>> entryDDOut = itDDOut.next();
                Iterator<Map.Entry<Integer,Pair<Double,Double>>> itBuses = entryDDOut.getValue().entrySet().iterator();
                
                txtDD.write("Time\t"+entryDDOut.getKey()+newline);
                txtDD.flush();
                
                while(itBuses.hasNext()){
                    Map.Entry<Integer,Pair<Double,Double>> entryBuses = itBuses.next();
                    
                    txtDD.write(entryBuses.getKey()+"\t"+entryBuses.getValue().getFirst()+"\t"+entryBuses.getValue().getSecond()+newline);
                    txtDD.flush();
                }
            }
            
            txtDD.close();
            
            // Escreve arquivo dos coeficientes
            Iterator<Map.Entry<Pair<Integer,Integer>,ArrayList<Double>>> itCoeff = outCoeff.entrySet().iterator();
            txtCoeff.write(numR+"\t"+numC+"\t"+numI+newline);
            txtCoeff.flush();
            
            while(itCoeff.hasNext()){
                Map.Entry<Pair<Integer,Integer>,ArrayList<Double>> entryCoeff = itCoeff.next();
                
                txtCoeff.write(entryCoeff.getKey().getFirst()+"\t"+entryCoeff.getKey().getSecond()+newline);
                txtCoeff.flush();
                
                for(int i=0;i<entryCoeff.getValue().size();++i){
                    txtCoeff.write(entryCoeff.getValue().get(i)+newline);
                    txtCoeff.flush();
                }
                    
            }
        }
        catch (FileNotFoundException e){
            System.err.println("Erro na abertura do arquivo.");
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(DynamicData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
