package models.Conceptual;

import org.apache.commons.math3.complex.Complex;
import java.util.ArrayList;
import models.PassiveElements.*;

public class PQBus extends Bus{
    private Complex Vbus;
    private double mismatch;
    private ArrayList<ShuntElement> elements;
    
    public PQBus(){
        this.Vbus = new Complex(0,0);
        this.mismatch = 0;
        this.elements = new ArrayList();
    }
    public PQBus(PQBus _cpy){
        this.Vbus = new Complex(_cpy.Vbus.getReal(),_cpy.Vbus.getImaginary());
        this.mismatch = _cpy.mismatch;
        this.elements = new ArrayList();
        
        for(int i=0;i<_cpy.elements.size();++i)
            this.elements.add(_cpy.elements.get(i));
    }
    
    @Override
    public void addElement(ShuntElement _Element){
        this.elements.add(_Element);
    }
    @Override
    public double getMismatch(){
        return this.mismatch;
    }
    @Override
    public Complex NodalCurrent(){
        Complex current = new Complex(0,0);
        
        if((!this.elements.isEmpty())&&(this.Vbus.abs()!=0)){
            for(int i=0;i<this.elements.size();++i)
                current = current.add(this.elements.get(i).NodalCurrent(this.Vbus));
        }
        
        return current;
    }
    @Override
    public void removeDynamicElements(){
        for(int i=0;i<elements.size();++i){
            ShuntElement aux = this.elements.get(i);
            if(aux.isDynamic())
                elements.remove(i);
        }
    }
    @Override
    public void Reset(){
        this.removeDynamicElements();
        
        this.Vbus = new Complex(0,0);
        this.mismatch = 0.0;
    }
    @Override
    public void setVbus(Complex _Vbus){
        Complex current = this.NodalCurrent(),
                admittance = new Complex(0,0),
                ratedPower = new Complex(0,0);
        
        for(int i=0;i<this.elements.size();++i){
            ShuntElement aux = this.elements.get(i);
            Complex Yaux = aux.getAdmittance();
            Complex Saux = aux.getRatedPower();
            
            if(Yaux!=null)
                admittance = admittance.add(Yaux);
            
            if(Saux!=null)
                ratedPower = ratedPower.add(Saux);
        }
        
        Complex Sk = _Vbus.multiply(current.conjugate());
        Complex Srated = admittance.multiply(_Vbus).add(ratedPower);
        this.mismatch = Sk.subtract(Srated).abs();
        
        this.Vbus = new Complex(_Vbus.getReal(),_Vbus.getImaginary());
    }
    @Override
    public Complex Vbus(){
        return new Complex(this.Vbus.getReal(),this.Vbus.getImaginary());
    }
    @Override
    public Bus clone(){
        return new PQBus(this);
    }
    @Override
    public String toString(){
        String newline = System.getProperty("line.separator");
        String out = new String("Barra PQ"+newline+"Elementos: "+newline);
        
        for(int i=0;i<this.elements.size();++i){
            out = out+this.elements.get(i).toString()+newline;
        }
        
        return out;
    }
}