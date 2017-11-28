package models.Conceptual;

import org.apache.commons.math3.complex.Complex;
import java.util.ArrayList;
import models.PassiveElements.*;

public class Slack extends Bus{
    private Complex Vbus;
    private double mismatch;
    private ArrayList<ShuntElement> elements;
    
    public Slack(Complex _Vbus){
        this.Vbus = new Complex(_Vbus.getReal(),_Vbus.getImaginary());
        this.mismatch = 0;
        this.elements = new ArrayList();
    }
    public Slack(Slack _cpy){
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
        
        if(this.elements.isEmpty()&&(this.Vbus.abs()!=0)){
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
        
        this.mismatch = 0.0;
    }
    @Override
    public void setVbus(Complex _Vbus){}
    @Override
    public Complex Vbus(){
        return new Complex(this.Vbus.getReal(),this.Vbus.getImaginary());
    }
    @Override
    public Bus clone(){
        return new Slack(this);
    }
    @Override
    public String toString(){
        String newline = System.getProperty("line.separator");
        String out = new String("Barra Slack"+newline+"Elementos: "+newline);
        
        for(int i=0;i<this.elements.size();++i){
            out = out+this.elements.get(i).toString()+newline;
        }
        
        return out;
    }
}