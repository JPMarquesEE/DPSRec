package models.PassiveElements;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;

public class ShuntImpedance extends ShuntElement {
    private final Complex Admittance;
    
    public ShuntImpedance(Complex _Y){
        super();
        
        this.Admittance = new Complex(_Y.getReal(),_Y.getImaginary());
    }
    public ShuntImpedance(ShuntImpedance _cpy){
        super(_cpy);
        
        this.Admittance = new Complex(_cpy.Admittance.getReal(),_cpy.Admittance.getImaginary());
    }

    @Override
    public Complex getAdmittance(){
        return new Complex(this.Admittance.getReal(),this.Admittance.getImaginary());
    }
    @Override
    public Complex getRatedPower(){
        return null;
    }
    @Override
    public boolean isDynamic(){
        return false;
    }
    @Override
    public Complex NodalCurrent (Complex Vbus){
        return this.Admittance.multiply(Vbus);
    }
    
    @Override
    public ShuntElement clone(){
        ShuntElement cloneObj = new ShuntImpedance(this);
        
        return cloneObj;
    }   
    @Override
    public String toString(){
        return "Impedância Shunt: "+this.Admittance.getReal()+"+"+this.Admittance.getImaginary()+"j";
    }
}
