package com.codeoftheweb.salvo.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;



@Entity
public class Game {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;


  //Relacion con la tabla GamePlayer
  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
  Set<GamePlayer> gamePlayers;

  //Relacion con la tabla Score
  @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
  private Set<Score> scores;


  private Date creationDate;


  //constructor

  //public Game(long offset) {
  //  Date now = new Date();
  //  this.creationDate = Date.from((now.toInstant().plusSeconds(offset)));
  //}
  public Game() {
    this.creationDate = new Date();
  }


  //Metodos

  public Long getId() {
    return id;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Set<GamePlayer> getGamePlayers(){
    return this.gamePlayers
            .stream()
            .sorted((gp1,gp2) -> (int)(gp1.getGpId() - gp2.getGpId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
  }



    //DTO
    /*DTO methods*/
    //GAME DTO - brings the id, created from the Game class and gamePlayers from the gamePlayer DTO.
    public Map<String, Object> gamesDTO() {
      Map<String, Object> dto = new LinkedHashMap<String, Object>();
      dto.put("id", this.getId());
      dto.put("created",this.getCreationDate());
      dto.put("gamePlayers", this.getGamePlayers().stream().map(gamePlayer -> gamePlayer.gamePlayersDTO()));
      return dto;
    }

  @JsonIgnore
  public List<Player> getPlayers() {
    return gamePlayers.stream()
            .map(gp -> gp.getPlayer())
            .collect(Collectors.toList());
  }

}
