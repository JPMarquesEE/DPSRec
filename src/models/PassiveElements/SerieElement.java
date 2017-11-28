package models.PassiveElements;

import org.apache.commons.math3.complex.Complex;

public abstract class SerieElement {
    
    SerieElement(){}
    
    abstract public Complex ISource(Complex _VLoad, Complex _ILoad);
    abstract public Complex VLoad(Complex _VSource, Complex _ILoad);
    abstract public Complex Losses(Complex _ISource,Complex _ILoad);
    
    @Override
    abstract public SerieElement clone();
    @Override
    abstract public String toString();
}
