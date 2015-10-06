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
public class WordCounter {

    private int[] nInTypes; // Stores number of a word in each type

    public WordCounter() {
        nInTypes = new int[Controler.NUMBER_OF_TYPE];
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            nInTypes[i] = 0;
        }
    }

    public WordCounter(int[] nInType) {
        this.nInTypes = nInType;
    }
    
    public int getNInType(int index) {
        return nInTypes[index];
    }
    
    public void setNInType(int index, int value) {
        nInTypes[index] = value;
    }
    
    public void incNInType(int index) {
        ++nInTypes[index];
    }
}
