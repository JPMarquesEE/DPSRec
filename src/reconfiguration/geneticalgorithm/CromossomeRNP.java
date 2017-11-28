package reconfiguration.geneticalgorithm;

import org.apache.commons.math3.util.Pair;

import models.DistributionSystem.DistributionSystem;

public class CromossomeRNP {
    private RNP Florest;
    private double VoltageAggregation,
                   LossesKWh;
    
    private int Front;
    private double CrowDist;
    
    CromossomeRNP(DistributionSystem sys, RNP state, double error, int maxIt){
        /*Considera que o objeto "sys" já é uma deep copy, então não há necessidade de copia-lo
          novamente, apenas reseta-lo.*/
        this.Florest = new RNP(state);
        
        sys.Reset();
        sys.SetState(this.Florest.getState());       // SETA O ESTADO APENAS PARA O PRIMEIRO PERÍODO.
        
        // Saída adotada para casos em que o algoritmo de FP não converge
        // Atribuir Perdas infinitas e VA igual a zero.
        if(sys.PowerFlowAtPeriod(error, maxIt)){
            this.LossesKWh = sys.LossesKWh();
        
            this.CrowDist= -1;
            this.Front = -1;
        
            Pair<Double,Double> MinMaxVoltage = sys.VoltageLimits();
            double MinVoltage = MinMaxVoltage.getFirst()/sys.getVref(),
                   MaxVoltage = MinMaxVoltage.getSecond()/sys.getVref();
            this.VoltageAggregation = 100.0*Math.exp(-(Math.pow(MinVoltage-1, 2.0)+Math.pow(MaxVoltage-1, 2.0))/(0.005));
        }
        else{
            this.LossesKWh = Double.POSITIVE_INFINITY;
        
            this.CrowDist= -1;
            this.Front = -1;
            this.VoltageAggregation = 0.0;
        }
    }
    CromossomeRNP(CromossomeRNP _cpy){
        this.Florest = new RNP(_cpy.Florest);
        
        this.CrowDist= -1;
        this.Front = -1;
        
        this.VoltageAggregation = _cpy.VoltageAggregation;
        this.LossesKWh = _cpy.LossesKWh;
    }
    
    public void setFront(int _front){
        this.Front = _front;
    }
    public void setCrowDist(double _cdist){
        this.CrowDist = _cdist;
    }
    
    public double getCrowDist(){
        return this.CrowDist;
    }
    public int getFront(){
        return this.Front;
    }
    public RNP getFlorest(){
        return new RNP(this.Florest);
    }
    public double getLosses(){
        return this.LossesKWh;
    }
    public double getVoltageAggregation(){
        return this.VoltageAggregation;
    }

    public boolean DominatedBy(CromossomeRNP y){
        boolean FirstCondition = (this.LossesKWh>=y.LossesKWh)&&(this.VoltageAggregation<=y.VoltageAggregation),
                SecondCondition = (this.LossesKWh>y.LossesKWh)||(this.VoltageAggregation<y.VoltageAggregation);
        
        return FirstCondition&&SecondCondition;
    }
    public boolean ThisDominates(CromossomeRNP y){
        boolean FirstCondition = (this.LossesKWh<=y.LossesKWh)&&(this.VoltageAggregation>=y.VoltageAggregation),
                SecondCondition = (this.LossesKWh<y.LossesKWh)||(this.VoltageAggregation>y.VoltageAggregation);
        
        return FirstCondition&&SecondCondition;
    }
    
    public CromossomeRNP CAOGenerator(DistributionSystem sys, double error, int maxIt){
        RNP newFlorest = this.Florest.operatorCAO(sys);
        
        return new CromossomeRNP(sys,newFlorest,error,maxIt);
    }
    public CromossomeRNP PAOGenerator(DistributionSystem sys, double error, int maxIt){
        RNP newFlorest = this.Florest.operatorPAO(sys);
        
        return new CromossomeRNP(sys,newFlorest,error,maxIt);
    }
    
    @Override
    public String toString(){
        return "@"+"["+"CrowDist: "+Double.toString(this.CrowDist)+"; Losses: "+Double.toString(this.LossesKWh)
                +"; VoltageAggregation: "+Double.toString(this.VoltageAggregation);
    }
}
