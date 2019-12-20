/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupb.soa.business.models;

/**
 *
 * @author goksuturan
 */
public class RoadBuilding extends DevCard {
    public RoadBuilding(String name,Player owner){
        super(name, owner); 
    }
    
    public boolean play(GameModel model){
        //card is bought in the current turn
        if( recentlyBought )
            return false;
        //edges are not set
        model.addFreeRoads( 2);
        return true;
    }
        
    }
    
    
    
    
    

