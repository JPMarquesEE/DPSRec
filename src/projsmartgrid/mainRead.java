package projsmartgrid;

import java.util.ArrayList;

import models.DistributionSystem.*;
import reconfiguration.geneticalgorithm.*;
public class mainRead {
    public static void main(String[] args){
        String FStatic = "C:\\Users\\São Paulo\\Dropbox\\TCC\\projSmartGrid v2.2\\src\\84Chiou_StaticData.txt";
        String FDynamic = "C:\\Users\\São Paulo\\Dropbox\\TCC\\projSmartGrid v2.2\\src\\84Chiou_DynamicData_dv2.txt";
//        String cFile = "C:\\Users\\User\\Dropbox\\TCC\\projSmartGrid v2.1\\src\\profile_curves.txt";
        String NameSys = "Sistema_84bus_dv_NUPSE";
        
        ArrayList<OutputNSNP> States;
        RNP FirstState;
        DistributionSystem test = new DistributionSystem(FStatic,FDynamic,96,96,15.0);
        
//        test.rndPrinter(80, 15, 5, 20, NameSys, cFile);
        
        
//        FirstState = new RNP(test);
//        FinalState = FirstState;
//        for(int i=0;i<100000;++i){
//            System.out.println(i);
//            FinalState = FinalState.operatorPAO(test);
//        }
        //Nb < 84
        // Num Ind.: 50;
        // Max It.: 20;
        // Max Total: 500;
        //Nb > 84
        // Num Ind.: 25;
        // Max It.: 100;
        // Max Total: 500;
        
        FirstState = new RNP(test);
        NSNPAlgorithm GA = new NSNPAlgorithm(15,750,150);
             
//        int k=0;
//        while(k<8){
//            FinalState = GA.runNSNP(test.getSystemAtPeriod(), FirstState);
//            test.SetState(FinalState);
//            test.PowerFlowAtPeriod(0.001, 50);
//            ++k;
//        }
        
        test.SetInitialState();
        test.PowerFlow(1E-6, 50);
        test.PrintReport(NameSys);
        test.Reset();

        States = GA.runNthNSNP(test, FirstState, 1e-6, 50, NameSys, 100);
//        
//        // MENOR DISTÂNCIA
//        if(FinalState.get(0)!=null){
//            test.SetState(FinalState.get(0).getState());
//            test.PowerFlow(1E-6, 50);
//            test.PrintReport(NameSys+"_MenorDist");
//            test.Reset();
//        }
//        
        // MENOR INDICE DE PERDAS
        test.SetState(States.get(0).bestLosses().getFlorest().getState());
        test.PowerFlow(1E-6, 100);
        test.PrintReport(NameSys+"_MenorPerdas");
        test.Reset();
//
//        // MELHOR TENSÃO
        test.SetState(States.get(1).bestVA().getFlorest().getState());
        test.PowerFlow(1E-6, 50);
        test.PrintReport(NameSys+"_MaiorTensão");
    }
}