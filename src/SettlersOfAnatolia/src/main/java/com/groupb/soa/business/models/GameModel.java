/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupb.soa.business.models;

/**
 *
 * @author Göksu
 */
import java.util.ArrayList;
import javafx.scene.paint.Color;

public class GameModel {
    private GameTile tile;
    private PlayerList playerList;
    private Bank bank;
    private Dice dice;
    private Dice dice2;
    private int queue;
    private int turn;
    private boolean firstTurn;
    private boolean secondTurn;
    private boolean firstTurnSettBuilt;
    private boolean firstTurnRoadBuilt;
    
    public GameModel(Color[] playerColors) {
        tile = new GameTile();
        playerList = new PlayerList(playerColors);
        bank = new Bank();
        dice = new Dice(0.0,0.0);
        dice2 = new Dice(0.0,0.0);
        turn = 0;
        queue = 0;
        firstTurn = true;
        secondTurn = false;
        firstTurnSettBuilt = false;
        firstTurnRoadBuilt = false;
    }
    // player rolls the dice and sources are distributed
    public boolean produceResources(){
            return tile.produceResources(dice.value + dice2.value , playerList);
    }
    
    //gets playerList
    public PlayerList getPlayerList(){
        return playerList;
    }

    public boolean buildRoad(int index ){
        // if it is the first turns, and the player did not build a settlement yet, or built both a settlement and a road, return false.
        if( (firstTurn || secondTurn))
        {
            if( !firstTurnSettBuilt)
                return false;
            else if( firstTurnRoadBuilt)
                return false;
        }
        boolean result = tile.buildRoad(index, playerList.getCurrentPlayer().getColor(),  playerList,  firstTurn || secondTurn);
        if(result){
            System.out.println("GameModel: Building Road by " + playerList.getCurrentPlayer().getColor().toString() + " on the Edge " + index);
            if( firstTurn || secondTurn)
                firstTurnRoadBuilt = true;
        } else {
            System.out.println("GameModel: Building Road by " + playerList.getCurrentPlayer().getColor().toString() + " on the Edge " + index + "FAILED for some reasons");
        }
        return result;
    }
    public boolean buildSettlement(int index){
        // if it is the first turns, and the player already built a settlement, return false.
        if( (firstTurn || secondTurn) && firstTurnSettBuilt)
        {
            return false;
        }
        boolean result = tile.buildVertex( index, playerList.getCurrentPlayer().getColor(),  playerList, firstTurn, secondTurn );
        if(result){
           System.out.println("GameModel: Building Settlement by " + playerList.getCurrentPlayer().getColor().toString() + " on the Vertex " + index);
           playerList.getCurrentPlayer().increaseScore( 1);
           if( firstTurn || secondTurn)
               firstTurnSettBuilt = true;
        } else {
            System.out.println("GameModel: Building Settlement by "+ playerList.getCurrentPlayer().getColor().toString() + " on the Vertex " + index + " FAILED for some reasons");
        }
        return result;

    }
    public boolean buildCity(int index){
        // if it is the first turns, return false.
        if (firstTurn ||secondTurn)
            return false;
        boolean result =  tile.upgradeVertex(  index,playerList.getCurrentPlayer().getColor(), playerList);
        if(result){
            System.out.println("GameModel: Building City by "+ playerList.getCurrentPlayer().getColor().toString() + " on the Vertex " + index);
            playerList.getCurrentPlayer().increaseScore(1); // This will be 1 since theres another score added while building the settlement!
        } else {
            System.out.println("GameModel: Building City by "+ playerList.getCurrentPlayer().getColor().toString() + " on the Vertex " + index + " FAILED for some reasons");
        }

       return result;
    }

    public Player getCurrentPlayer()
    {
        return playerList.getCurrentPlayer();
    }
    public Color getCurrentPlayerColor(){
        return playerList.getCurrentPlayer().getColor();
    }
    
    public Bank getBank(){
        return bank;
    }

    public void moveNextPlayer(){
        
        // if it is the first turns and the player hasnt built a settlement and a road yet,
        // stop.
        if( ( firstTurn || secondTurn) && !(firstTurnSettBuilt && firstTurnRoadBuilt) )
            return;
        
        // playerList.next()'s stay parameter is set to 'true'
        // when queue == 3 or 7. This is because in the first 2 rounds,
        // one player gets to play twice at the end.
        playerList.next( secondTurn, queue == 3 || queue == 7);
        queue++;
        turn = queue / 4;
        firstTurn = (turn == 0);
        secondTurn = (turn == 1);
        firstTurnSettBuilt = false;
        firstTurnRoadBuilt = false;
        System.out.println("Game Turn is increased " + turn);
    }
    
    public int[] rollDice()
    {
        int result[] = new int[2];
        if( firstTurn || secondTurn)
        {
            result[0] = result[1] = 0;
            return result;
        }
        // roll the dice
        int d1 = dice.rollDice();
        int d2 = dice2.rollDice();
        result[0] = d1;
        result[1] = d2;
        return result;
    }
    
    
}



