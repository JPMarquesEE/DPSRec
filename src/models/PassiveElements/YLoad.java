package models.PassiveElements;

import org.apache.commons.math3.complex.Complex;

public class YLoad extends ShuntElement{
    private boolean isDynamic;
    private Complex RatedPower;
    
    public YLoad(double ActivePower, double ReactivePower, boolean dynamic){
        super();
        
        this.RatedPower = new Complex(ActivePower,ReactivePower);
        this.isDynamic = dynamic;    
    }
    public YLoad(YLoad _cpy){
        this.RatedPower = new Complex(_cpy.RatedPower.getReal(),_cpy.RatedPower.getImaginary());
        this.isDynamic = _cpy.isDynamic;
    }
    
    @Override
    public Complex getAdmittance(){
        return null;
    }
    @Override
    public Complex getRatedPower(){
        return new Complex(this.RatedPower.getReal(),this.RatedPower.getImaginary());
    }
    @Override
    public boolean isDynamic(){
        return this.isDynamic;
    }
    @Override
    public Complex NodalCurrent (Complex Vbus){
        return this.RatedPower.divide(Vbus).conjugate();
    }
    
    @Override
    public ShuntElement clone(){
        ShuntElement cloneObj = new YLoad(this);
        
        return cloneObj;
    }
    public String toString(){
        return "Carga: "+this.RatedPower.abs()+" "+this.RatedPower.getArgument()*180/Math.PI;
    }
}
