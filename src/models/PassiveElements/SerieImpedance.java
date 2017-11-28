package models.PassiveElements;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.FieldLUDecomposition;

public class SerieImpedance extends SerieElement{
    
    private Complex Impedance;
    
    public SerieImpedance(Complex _Z){
        super();

        this.Impedance = new Complex(_Z.getReal(),_Z.getImaginary());
    }
    public SerieImpedance(SerieImpedance _cpy){
        super();
        
        this.Impedance = new Complex(_cpy.Impedance.getReal(),_cpy.Impedance.getImaginary());
    }
    
    @Override
    public Complex ISource(Complex _VLoad, Complex _ILoad){
        return new Complex(_ILoad.getReal(),_ILoad.getImaginary());
    }
    @Override
    public Complex VLoad(Complex _VSource, Complex _ILoad){
        return _VSource.subtract(this.Impedance.multiply(_ILoad));
    }
    @Override
    public Complex Losses(Complex _ISource, Complex _ILoad){
        return this.Impedance.multiply(_ISource.multiply(_ISource.conjugate()));
    }
    @Override
    public SerieElement clone(){
        SerieElement cloneObj = new SerieImpedance(this);
        
        return cloneObj;
    }
    @Override
    public String toString(){
        return "Linha: "+this.Impedance.getReal()+" + "+this.Impedance.getImaginary()+"j";
    }
}