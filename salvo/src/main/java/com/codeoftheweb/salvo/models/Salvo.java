package com.codeoftheweb.salvo.models;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;



@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private int turn;

    public long getId() {
        return id;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    @ElementCollection
    @Column(name="salvoLocation")
    private List<String> salvoLocations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameplayer_id")
    public GamePlayer gamePlayer;


    public Salvo() {

    }
    public Salvo(int turn, GamePlayer gamePlayer, List<String> salvoLocations) {
        this.turn = turn;
        this.salvoLocations = salvoLocations;
        this.gamePlayer = gamePlayer;
    }


    public Salvo(int turn,List<String> salvoLocations){
        this.turn = turn;
        this.salvoLocations = salvoLocations;

    }

    public long getTurn() {
        return turn;
    }


    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getsalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }



    public List<String> getHits(List<String> myShots, Set<Ship> opponentShips){

        List<String> allEnemyLocs = new ArrayList<>();

        opponentShips.forEach(ship -> allEnemyLocs.addAll(ship.getShipLocations()));

        return myShots.stream()
                      .filter(shot -> allEnemyLocs
                      .stream()
                      .anyMatch(loc -> loc.equals(shot)))
                      .collect(Collectors.toList());
    }

    private List<Ship> getSunkenShips(Set<Salvo> mySalvoes,Set<Ship> opponentShips){

        List<String> allShots = new ArrayList<>();

        mySalvoes.forEach(salvo -> allShots.addAll(salvo.getsalvoLocations()));
     return opponentShips.stream()
                         .filter(ship ->allShots.containsAll((ship.getShipLocations())))
                         .collect(Collectors.toList());


    }

    public List<Map> salvoLocationsList(Set<Salvo> salvos) {
        return salvos.stream()
                .map(salvo -> makeSalvoDTO())
                .collect(Collectors.toList());
    }

    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", this.getTurn());
        dto.put("salvoLocations", this.getsalvoLocations());
        dto.put("player", this.getGamePlayer().getPlayer().getId());

        GamePlayer opponent1 = this.getGamePlayer().getOpponent();

        if(opponent1 != null) {

            Set<Ship> enemyShips = opponent1.getShips();

            dto.put("hits", this.getHits(this.getsalvoLocations(), enemyShips));

            Set<Salvo> mySalvoes = this.getGamePlayer().getSalvoes().stream()
                                                                    .filter(salvo -> salvo.getTurn() <= this.getTurn())
                                                                    .collect(Collectors.toSet());
            dto.put("sunk", this.getSunkenShips(mySalvoes, enemyShips).stream()
                                                                      .map(Ship::makeShipDTO));
        }

        return dto;
    }

}