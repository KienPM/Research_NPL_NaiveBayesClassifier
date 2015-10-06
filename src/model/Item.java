/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import control.Controler;

/**
 *
 * @author Ken
 */
public class Item {

   private double[] pInTypes; // Stores probability a word in a document of each type

    public Item() {
        pInTypes = new double[Controler.NUMBER_OF_TYPE];
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            pInTypes[i] = 0;
        }
    }

    public Item(double[] pInType) {
        this.pInTypes = pInType;
    }
   
    public double getPInType(int index) {
        return pInTypes[index];
    }
    
    public void setPInType(int index, double value) {
        pInTypes[index] = value;
    }
}
