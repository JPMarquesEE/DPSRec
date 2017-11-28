package models.Conceptual;

import org.apache.commons.math3.complex.Complex;
import java.util.ArrayList;
import models.PassiveElements.SerieElement;

public class Branch {    
    private boolean isSwitchable;
    private boolean wasSetted;
    private Complex ISource, ILoad;
    private SerieElement element;
    
    public Branch(boolean _switchable){
        this.ILoad = new Complex(0.0,0.0);
        this.ISource = new Complex(0.0,0.0);

        this.element = null;
        
        this.isSwitchable = _switchable;
        this.wasSetted = false;
    }
    public Branch(boolean _switchable, SerieElement _element){
        this.ILoad = new Complex(0.0,0.0);
        this.ISource = new Complex(0.0,0.0);

        this.element = _element.clone();
        
        this.isSwitchable = _switchable;
        this.wasSetted = false;
    }
    public Branch(Branch _cpy){
        this.wasSetted =_cpy.wasSetted;
        if(this.wasSetted)
            this.element = _cpy.element.clone();
        else
            this.element = null;
        
        this.ILoad = new Complex(_cpy.ILoad.getReal(),_cpy.ILoad.getImaginary());
        this.ISource = new Complex(_cpy.ISource.getReal(),_cpy.ISource.getImaginary());
        this.isSwitchable = _cpy.isSwitchable;
    }
    
    public Complex getISource(){
        return new Complex(this.ISource.getReal(),this.ISource.getImaginary());
    }
    public Complex getILoad(){
        return new Complex(this.ILoad.getReal(),this.ILoad.getImaginary());
    }
    public boolean Switchable(){
        return this.isSwitchable;
    }
    public Complex getLosses(){
         return this.element.Losses(this.ISource, this.ILoad);
    }
    
    public void setElement(SerieElement _element){
        if(!this.wasSetted){
            this.element = _element;
            this.wasSetted = true;
        }
    }
    
    public void setCurrents(Bus BLoad, ArrayList<Branch> _branchs){
        Complex _ILoad = new Complex(0.0,0.0);
        
        _ILoad = _ILoad.add(BLoad.NodalCurrent());
        for(int i=0;i<_branchs.size();++i)
            _ILoad = _ILoad.add(_branchs.get(i).ISource);
        
        this.ILoad = _ILoad;
        this.ISource = this.element.ISource(BLoad.Vbus(), _ILoad);
    }
    public Complex VLoad(Bus BSource){
        return this.element.VLoad(BSource.Vbus(),this.ILoad);
    }
    public void Reset(){
        this.ISource = new Complex(0,0);
        this.ILoad = new Complex(0,0);
    }
    @Override
    public Branch clone(){
        Branch cloneObj = new Branch(this);
        
        return cloneObj;
    }
    @Override
    public String toString(){
        String newline = System.getProperty("line.separator");
        String out = new String();
        
        if(this.isSwitchable)
            out = out+"Ramo Seccionavel";
        else
            out = out+"Ramo Não Seccionavel";
        
        out = out + newline+"Elemento: "+newline+this.element.toString();
        
        
        return out;
    }
}
