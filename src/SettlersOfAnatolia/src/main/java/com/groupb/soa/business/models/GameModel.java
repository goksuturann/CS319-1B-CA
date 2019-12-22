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
    private int freeRoads;
    private boolean firstTurn;
    private boolean secondTurn;
    private boolean firstTurnSettBuilt;
    private boolean firstTurnRoadBuilt;
    private int robberMoves;

    private TradeWithBank currentTwB;
    private List<DomesticTrade> domesticTrades;
     private boolean isFlood; //new
    private boolean isWolfAttacked; //new
    private boolean isCybeleMonth; //new
    private boolean isEarthquake;

    public GameModel(Color[] playerColors) {
        tile = new GameTile();
        playerList = new PlayerList(playerColors);
        bank = new Bank();
        dice = new Dice(0.0,0.0);
        dice2 = new Dice(0.0,0.0);
        turn = 0;
        queue = 0;
        freeRoads = 0;
        firstTurn = true;
        secondTurn = false;
        firstTurnSettBuilt = false;
        firstTurnRoadBuilt = false;
        robberMoves = 0;

        currentTwB = null;
        domesticTrades = new ArrayList<>();
        isFlood = false;
        isWolfAttacked = false;
        isCybeleMonth = false;
        isEarthquake = false;

    }
    // player rolls the dice and sources are distributed
    public boolean produceResources(){
            return tile.produceResources(dice.getValue() + dice2.getValue() , playerList);
    }
    
    public boolean sendRobberToHexagon( int index)
    {
       if( robberMoves > 0)
       {
            boolean result = tile.sendRobberToHexagon(index, playerList);
            if(result)
                robberMoves--;
            return result;
       }
       return false;
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
        boolean result = tile.buildRoad(index, playerList.getCurrentPlayer().getColor(),  playerList,  firstTurn || secondTurn || freeRoads > 0);
        if(result){
            System.out.println("GameModel: Building Road by " + playerList.getCurrentPlayer().getColor().toString() + " on the Edge " + index);
            // if it is the set-up turns, notify that the player built their free road.
            if( firstTurn || secondTurn)
                firstTurnRoadBuilt = true;
            // if it is not the set-up turns and the player built a free road, subtract 1 free road privilege.
            else if( !firstTurn && !secondTurn && freeRoads > 0)
                freeRoads--;
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
        
        // if player has the right to build free roads, stop.
        if( freeRoads > 0)
            return;
        
        // if player has the right to move the robber, stop.
        if( robberMoves > 0)
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
    
    public boolean playCard( String cardName, String sourceName, String sourceName2)
    {
        // first, we check if the player has the card.
        if( playerList.getCurrentPlayer().getCardNo(cardName) <= 0)
        {
            return false;
        }
        
        // if the player has the card...
        // play it
        DevCard curCard = playerList.getCurrentPlayer().getCard(cardName);
        // if there is no such card, return false.
        if( curCard == null)
            return false;
        
        boolean isPlayed = false;
        if( cardName.equals("Knight"))
        {
            System.out.println( "Knight checkpoint");
            Knight knight = (Knight) curCard;
            isPlayed = knight.play(this);
        }
        
        else if( cardName.equals("Road Building"))
        {
            System.out.println( "Road Building checkpoint");
            RoadBuilding rb = (RoadBuilding) curCard;
            // To do
            isPlayed = rb.play(this);
        }
        
        else if( cardName.equals("Monopoly"))
        {
            System.out.println( "Monopoly checkpoint");
            Monopoly mono = (Monopoly) curCard;
            mono.setSelectedSource(sourceName);
            isPlayed = mono.play( this);
        }
        
        else if( cardName.equals("Year of Plenty"))
        {
            YearOfPlenty yop = (YearOfPlenty) curCard;
            yop.setSelectedSource1(sourceName);
            yop.setSelectedSource2(sourceName2);
            isPlayed = yop.play( this);
        }
        if( isPlayed)
        {
            playerList.getCurrentPlayer().removeCard(curCard);
            return true;
        }
        return false;
    }
    
    protected void addFreeRoads( int i)
    {
        System.out.println( "i = " + i);
        System.out.println( playerList.getCurrentPlayer().getRemRoads() + " xd");
        freeRoads += (int) Math.min(i, playerList.getCurrentPlayer().getRemRoads());
    }
    
    protected void addRobberMove()
    {
        robberMoves += 1;
    }
    
    public int getPlayerCardNo( String cardName)
    {
        return playerList.getCurrentPlayer().getCardNo( cardName);
    }
    
    public int getPlayerPlayableCardNo( String cardName)
    {
        return playerList.getCurrentPlayer().getPlayableCardNo( cardName);
    }
    
    public int[] getSources(){
        return tile.getResources();
    }
    
    public int [] getNumberOfHexagons(){
        return tile.getNumbersofHexagons();
    }
    
    public boolean buyCard()
    {
        return playerList.getCurrentPlayer().buyDevCard(bank);
    }

    
    public boolean startTradeWithBank()
    {
        if ( currentTwB != null)
            return false;
        currentTwB = new TradeWithBank( playerList.getCurrentPlayer(), bank);
        return true;
    }
    
    public boolean cancelTradeWithBank()
    {
        if( currentTwB == null)
            return false;
        currentTwB = null;
        return true;
    }
    
    public boolean addSourceToSelf( int sourceNo, int amount)
    {
        return currentTwB.playerAddSource(sourceNo, amount);
    }
    
    public boolean subSourceFromSelf( int sourceNo, int amount)
    {
        return currentTwB.playerSubSource(sourceNo, amount);
    }
    
    public boolean addSourceToBank( int sourceNo, int amount)
    {
        return currentTwB.bankAddSource(sourceNo, amount);
    }
    
    public boolean subSourceFromBank( int sourceNo, int amount)
    {
        return currentTwB.bankSubSource( sourceNo, amount);
    }
    
    public boolean isTwBValid()
    {
        return currentTwB.isAValidTrade();
    }
    
    public int TwBgetBankSourceNo( int sourceNo)
    {
        return currentTwB.getBankSourceNo(sourceNo);
    }
    
    public int TwBgetPlayerSourceNo( int sourceNo)
    {
        return currentTwB.getPlayerSourceNo( sourceNo);
    }
    
    public int getTwBSourceRights()
    {
        return currentTwB.calculateSourceRights();
    }
    
    public int getTwBUsedSourceRights()
    {
        return currentTwB.calculateUsedSourceRights();
    }
    
    public boolean finalizeTwB()
    {
        boolean result = currentTwB.finalizeTrade();
        if( result)
            currentTwB = null;
        return result;
    }
    
    public boolean addDomesticTrade(int[] offers, int[] inReturn)
    {
        boolean canMakeTrade = true;
        boolean leftAllZeroes = true;
        boolean rightAllZeroes = true;
        for( int i = 0; i < offers.length; i++)
        {
            canMakeTrade &= (playerList.getCurrentPlayer().getSourceNo(i) >= offers[i]);
            leftAllZeroes &= offers[i] == 0;
            rightAllZeroes &= inReturn[i] == 0;
        }
        if( !canMakeTrade || leftAllZeroes || rightAllZeroes)
            return false;
        
        domesticTrades.add( new DomesticTrade(playerList.getCurrentPlayer(), offers, inReturn));
        return true;
    }
    
    public int getDomesticTradeNo()
    {
        return domesticTrades.size();
    }
    
    public void getDomesticTradesInfo(List<String> offers, List<String> inReturn)
    {
        for( DomesticTrade dt : domesticTrades)
        {
            if( !dt.isCreator(playerList.getCurrentPlayer()))
            {
                StringBuffer curOffer, curInReturn;
                curOffer = new StringBuffer();
                curInReturn = new StringBuffer();
                dt.getTradeInfo(curOffer, curInReturn);
                offers.add(curOffer.toString());
                inReturn.add(curInReturn.toString());
            }
        }
    }
    
    public boolean finalizeDomesticTrade( int index)
    {
        boolean result = domesticTrades.get(index).finalizeTrade(playerList.getCurrentPlayer());
        if(result)
            domesticTrades.remove(index);
        return result;
    }
    
    public boolean isDomesticTradeValid(int index)
    {
        return domesticTrades.get(index).isTradeValid(playerList.getCurrentPlayer());
    }
    public boolean getWolfAttacked(){ //new
        return isWolfAttacked;
   }
   public void setWolfAttacked(boolean wolfAttacked){ //new
        isWolfAttacked= wolfAttacked;
   }

    public boolean getFlood(){ //new
        return isFlood;
    }
    public void setFlood(boolean flood){ //new
        isFlood= flood;
    }


    public boolean getCybeleMonth(){ //new
        return isCybeleMonth;
    }
    public void setCybeleMonth(boolean cybele){ //new
        isCybeleMonth= cybele;
    }
    //new added
    public void setEarthquake(boolean earthquake){
        isEarthquake = earthquake;
    }
    public boolean getEarthquake(){
        return  isEarthquake;
    }

}



