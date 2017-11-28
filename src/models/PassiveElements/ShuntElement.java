package models.PassiveElements;

import org.apache.commons.math3.complex.Complex;

public abstract class ShuntElement {
    
    ShuntElement(){}
    ShuntElement(ShuntElement _cpy){}
    
    abstract public Complex getAdmittance();
    abstract public Complex getRatedPower();
    abstract public boolean isDynamic();
    abstract public Complex NodalCurrent (Complex Vbus);
    
    @Override
    abstract public ShuntElement clone ();
    @Override
    abstract public String toString();
}
