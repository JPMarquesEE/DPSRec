package models.Conceptual;

import org.apache.commons.math3.complex.Complex;
import models.PassiveElements.*;

public abstract class  Bus{
	Bus() {}
	
	abstract public void addElement(ShuntElement _Element);
	abstract public double getMismatch();
	abstract public Complex NodalCurrent();
	abstract public void removeDynamicElements();
	abstract public void Reset();
	abstract public void setVbus(Complex _Vbus);
	abstract public Complex Vbus();
        @Override
        abstract public Bus clone();
        @Override
        abstract public String toString();
}