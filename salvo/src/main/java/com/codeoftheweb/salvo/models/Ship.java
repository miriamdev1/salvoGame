package com.codeoftheweb.salvo.models;



import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String shipType;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="gameplayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection  // Ship locations list of strings
    @Column(name="shipLocations")
    private List<String> shipLocations = new ArrayList<>();


    public Ship() { }

    public Ship(String shipType, GamePlayer gamePlayer, List<String> shipLocations) {
        this.shipType = shipType;
        this.gamePlayer = gamePlayer;
        this.shipLocations = shipLocations;
    }

    //methods

    public Long getId() {
        return this.id;
    }

    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public String getshipType() {
        return this.shipType;
    }

    public List<String> getShipLocations() {
        return this.shipLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setType(String shipType) {
        this.shipType = shipType;
    }


    // Return List<Object> Ship
    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
         dto.put("shipLocations", this.getShipLocations());
         dto.put("shipType", this.getshipType());
        return dto;
    }



}
