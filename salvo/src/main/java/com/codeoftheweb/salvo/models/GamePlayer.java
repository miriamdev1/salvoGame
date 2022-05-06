package com.codeoftheweb.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity

public class GamePlayer {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private Long gpId;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "game_id")
  private Game game;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "player_id")
  private Player player;

  @OneToMany(mappedBy="gamePlayer", fetch= FetchType.EAGER)
  private Set<Ship> ships;

  @OneToMany (mappedBy = "gamePlayer", fetch = FetchType.EAGER)
  public Set<Salvo> salvoes;


  private Date joinDate;

  //constructor

  public GamePlayer() {
  }

  public GamePlayer(Game game,Player player){
    this.player = player;
    this.game = game;
    this.joinDate = new Date();
  }

  public GamePlayer(Player player,Game game){
    this.player = player;
    this.game = game;
    this.joinDate = new Date();
  }


  public GamePlayer(Date joinDate) {
    this.joinDate = joinDate;

  }

  //metodos

  public Long getGpId() {
    return gpId;
  }

  public Date getJoinDate() {
    return joinDate;
  }

  public Game getGame() {
    return this.game;
  }

  public Player getPlayer() {
    return this.player;
  }

  public Set<Ship> getShips(){
    return this.ships;
  }

  public Set<Salvo> getSalvoes() {
    return salvoes;
  }

  public GamePlayer getOpponent(){

    return this.getGame().getGamePlayers().stream()
                                          .filter(gp -> gp.getGpId() != this.getGpId())
                                          .findFirst()
                                          .orElse(null);
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void setGame(Game game) {
    this.game = game;
  }


  public void addShip(Ship ship){
    ship.setGamePlayer(this);
    ships.add(ship);
  }
  public void addSalvoes(Salvo salvo){
    salvo.setGamePlayer(this);
    salvoes.add(salvo);
  }
  //Add a method GamePlayer.getScore() that returns a Score that the controller can call to get the score, if any.
  //Null means the game is not finished.
  public Score getScore() {

      return this.player.getScoreByGame(this.game);
    }

  //GAME VIEW DTO -
  public Map<String, Object> gameViewDTO() {

    GamePlayer opponent = GetOpponent().orElse(null);

    Map<String, Object> dto = new LinkedHashMap<String, Object>();
    dto.put("gpId", this.getGame().getId());
    dto.put("created", this.getGame().getCreationDate());
    dto.put("gamePlayers", this.getGame().getGamePlayers().stream().map(gp -> gp.gamePlayersDTO()));
    dto.put("ships", this.getShips().stream().map(ship -> ship.makeShipDTO()));
    dto.put("salvoes", this.getGame().getGamePlayers().stream()
                                                      .flatMap(gp -> gp.getSalvoes()
                                                      .stream()
                                                      .sorted(Comparator.comparing(Salvo::getTurn))
                                                      .map(salvo -> salvo.makeSalvoDTO()))
                                                      .collect(Collectors.toList())
                                                 );
      if(opponent != null) {
          dto.put("hits", Hitsdto(this, opponent));
      }
    return dto;
  }


  public Map<String, Object> gamePlayersDTO() {
    Map<String, Object> dto = new LinkedHashMap<String, Object>();
    dto.put("gpId", this.getGpId());
    dto.put("player", this.getPlayer().makePlayerDTO());

    Score score = this.getPlayer().getScoreByGame(this.getGame());
    if (this.getScore() != null) {
    //  dto.put("score", this.getScore().getScore());
      dto.put("score", score.getScore());
    } else {
      dto.put("score", null);
    }
    //if (this.getScore() != null)
    //  dto.put("score", this.getScore());
    //else
    //  dto.put("score", null);
    return dto;

  }

  private Map<String,Object> Hitsdto(GamePlayer self,
                                     GamePlayer opponent){
                                        Map<String, Object> dto = new LinkedHashMap<>();
                                        dto.put("self", getHits(self,opponent));
                                        dto.put("opponent", getHits(opponent,self));
                                        return dto;
  }

  public Optional<GamePlayer> GetOpponent(){
    return this.getGame().getGamePlayers()
               .stream()
               .filter(opponent -> this.getGpId() != opponent.getGpId())
               .findFirst();
  }

  private List<Map<String, Object>> getSinks (int turn, Set<Ship> ships, Set<Salvo> salvoes){
    List<String> allShots = new ArrayList<>();
    salvoes.stream()
            .filter(salvo -> salvo.getTurn() <= turn)
            .forEach(salvo -> allShots.addAll(salvo.getsalvoLocations()));

    return ships.stream()
            .filter(ship -> allShots.containsAll(ship.getShipLocations()))
            .map(Ship:: makeShipDTO)
            .collect(Collectors.toList());
  }




  //**********************************CALCULA CUANTOS BARCOS DEL OPONENTE ACERTE***********************************
  private List<Map> getHits(GamePlayer self,
                            GamePlayer opponent){
    List<Map> dto = new ArrayList<>();

    int carrierDamage = 0;
    int destroyerDamage = 0;
    int patrolboatDamage = 0;
    int submarineDamage = 0;
    int battleshipDamage = 0;
    List<String> carrierLocations = new ArrayList<>();
    List<String> destroyerLocations = new ArrayList<>();
    List<String> submarineLocations = new ArrayList<>();
    List<String> patrolboatLocations = new ArrayList<>();
    List<String> battleshipLocations = new ArrayList<>();

    for (Ship ship: self.getShips()) {
      switch (ship.getshipType()){
        case "carrier":
          carrierLocations = ship.getShipLocations();
          break ;
        case "battleship" :
          battleshipLocations = ship.getShipLocations();
          break;
        case "destroyer":
          destroyerLocations = ship.getShipLocations();
          break;
        case "submarine":
          submarineLocations = ship.getShipLocations();
          break;
        case "patrolBoat":
          patrolboatLocations = ship.getShipLocations();
          break;
      }
    }
    List<Salvo> opponentSalvo = opponent.getSalvoes().stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());
    for (Salvo salvo : opponentSalvo) {
      Integer carrierHitsInTurn = 0;
      Integer battleshipHitsInTurn = 0;
      Integer submarineHitsInTurn = 0;
      Integer destroyerHitsInTurn = 0;
      Integer patrolboatHitsInTurn = 0;
      Integer missedShots = salvo.getsalvoLocations().size();
      Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
      Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
      List<String> salvoLocationsList = new ArrayList<>();
      List<String> hitCellsList = new ArrayList<>();
      salvoLocationsList.addAll(salvo.getsalvoLocations());
      for (String salvoShot : salvoLocationsList) {
        if (carrierLocations.contains(salvoShot)) {
          carrierDamage++;
          carrierHitsInTurn++;
          hitCellsList.add(salvoShot);
          missedShots--;
        }
        if (battleshipLocations.contains(salvoShot)) {
          battleshipDamage++;
          battleshipHitsInTurn++;
          hitCellsList.add(salvoShot);
          missedShots--;
        }
        if (submarineLocations.contains(salvoShot)) {
          submarineDamage++;
          submarineHitsInTurn++;
          hitCellsList.add(salvoShot);
          missedShots--;
        }
        if (destroyerLocations.contains(salvoShot)) {
          destroyerDamage++;
          destroyerHitsInTurn++;
          hitCellsList.add(salvoShot);
          missedShots--;
        }
        if (patrolboatLocations.contains(salvoShot)) {
          patrolboatDamage++;
          patrolboatHitsInTurn++;
          hitCellsList.add(salvoShot);
          missedShots--;
        }
      }


      damagesPerTurn.put("carrierHits", carrierHitsInTurn);
      damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
      damagesPerTurn.put("submarineHits", submarineHitsInTurn);
      damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
      damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
      damagesPerTurn.put("carrier", carrierDamage);
      damagesPerTurn.put("battleship", battleshipDamage);
      damagesPerTurn.put("submarine", submarineDamage);
      damagesPerTurn.put("destroyer", destroyerDamage);
      damagesPerTurn.put("patrolboat", patrolboatDamage);
      hitsMapPerTurn.put("turn", salvo.getTurn());
      hitsMapPerTurn.put("hitLocations", hitCellsList);
      hitsMapPerTurn.put("damages", damagesPerTurn);
      hitsMapPerTurn.put("missed", missedShots);
      dto.add(hitsMapPerTurn);
    }

    return dto;
  }

}
