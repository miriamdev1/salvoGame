package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController //return JSON
@RequestMapping("/api")

public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRespository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(path ="/games/players/{gamePlayerId}/salvoes",method = RequestMethod.POST)
    public ResponseEntity <Map<String, Object>>addSalvo(Authentication authentication, @PathVariable Long gamePlayerId,
                                                        @RequestBody List<String> salvoes){

        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer currentGP = gamePlayerRepository.getOne(gamePlayerId);

        if (loggedPlayer == null){
            return new ResponseEntity<>(makeMap("error", "please log in"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGP == null){
            return new ResponseEntity<>(makeMap("error", "please join a game"), HttpStatus.UNAUTHORIZED);
        }
        if (!loggedPlayer.getId().equals(currentGP.getPlayer().getId())){
            return new ResponseEntity<>(makeMap("error", "This is not your game"), HttpStatus.UNAUTHORIZED);
        }
        if (salvoes.size() > 5) {
            return new ResponseEntity<>(makeMap("error", "You cannot throw more than 5 salvoes"), HttpStatus.FORBIDDEN);

        }
        if (salvoes.size() != 5){
            return new ResponseEntity<>(makeMap("error","wrong number of salvoes"),HttpStatus.FORBIDDEN);
        } else {
            int turn = currentGP.getSalvoes().size()+1;
            Salvo shot = new Salvo(turn,salvoes);

            currentGP.addSalvoes(shot);
            salvoRepository.save(shot);
            gamePlayerRepository.save(currentGP);
        }

        return new ResponseEntity<>(makeMap("success", "you fired your salvos"), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>>addShips(@PathVariable Long gamePlayerId,
                                                       Authentication authentication,
                                                       @RequestBody List<Ship> ships){

        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());

        if(loggedPlayer == null){
            return new ResponseEntity<>(makeMap("error", "please log in"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer currentGP = gamePlayerRepository.getOne(gamePlayerId);
        if(currentGP == null){
            return new ResponseEntity<>(makeMap("error", "please join a game"), HttpStatus.UNAUTHORIZED);
        }
        if(loggedPlayer.getId() != (currentGP.getPlayer().getId())){
            return new ResponseEntity<>(makeMap("error", "This is not your game"), HttpStatus.UNAUTHORIZED);
        }

        if(currentGP.getShips().size() > 0){
            return new ResponseEntity<>(makeMap("error", "You already placed your ships"), HttpStatus.FORBIDDEN);
        }
        if(ships.size() != 5){
            return new ResponseEntity<>(makeMap("error", "There should be only 5 ships"), HttpStatus.FORBIDDEN);
        }else {
            ships.forEach(ship -> {
                currentGP.addShip(ship);
                shipRespository.save(ship);

            });
            return new ResponseEntity<>(makeMap("success","you got ships"), HttpStatus.CREATED);
        }
    }

    @RequestMapping(path="/games/players/{gamePlayerId}/ships", method = RequestMethod.GET)
    public Object getShips(@PathVariable long gpId,Authentication authentication) {
        Map<String,Object> playerShips = new LinkedHashMap<>();

        GamePlayer currentGP = gamePlayerRepository.getOne(gpId);
        Player  loggedPlayer = getAuthenticatedPlayer();

        if (loggedPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "please log in"), HttpStatus.UNAUTHORIZED);
        }

        if ( currentGP == null) {
            return new ResponseEntity<>(makeMap("error", "please join a game"), HttpStatus.UNAUTHORIZED);
        }

        if ( loggedPlayer.getId() != currentGP.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "This is not your game"), HttpStatus.UNAUTHORIZED);
        }

        playerShips.put("gpid", currentGP.getGpId());
        playerShips.put("ships", currentGP.getShips());

        return playerShips;
    }

    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> map = new HashMap<>();

        if (isGuest(authentication)) {
            map.put("Player", "Guest");
        }else {
            map.put("Player", authentication.getName());
        }
        map.put("Games", gameRepository.findAll()
                                       .stream()
                                       .map(game -> game.gamesDTO())
                                       .collect(Collectors.toList()));
        return map;
    }
    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long gamePlayerId,Authentication authentication ) {
        ResponseEntity<Map<String, Object>> responseEntity;

        GamePlayer gamePlayer = gamePlayerRepository.getOne(gamePlayerId);

        if (gamePlayerRepository.findById(gamePlayerId) == null) {
            responseEntity = new ResponseEntity<>(makeMap("error", "There's no gamePlayer with that id."), HttpStatus.FORBIDDEN);
        }else{

            Player player = playerRepository.findByUserName(authentication.getName());
            if(gamePlayer.getPlayer().getId() == player.getId()){
                responseEntity = new ResponseEntity<>(gamePlayer.gameViewDTO(), HttpStatus.OK);
            } else{
                responseEntity = new ResponseEntity<>(makeMap("error", "This is not your game"), HttpStatus.FORBIDDEN);
            }
        }
        return responseEntity;
    }


    //method that responds to a request to create a new player.
    @RequestMapping(path = "/players", method = RequestMethod.POST)

    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String username, @RequestParam String password) {

        ResponseEntity<Map<String, Object>> response;
        if (username.isEmpty() || password.isEmpty()) {
           response = new ResponseEntity<>(makeMap("error", "Missing data (username or password empty)"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByUserName(username)!= null) {
            response = new ResponseEntity<>(makeMap("error", "Username already in use"), HttpStatus.CONFLICT);
        } else {
            playerRepository.save(new Player(username, passwordEncoder.encode(password)));
            response = new ResponseEntity<>(makeMap("username", username), HttpStatus.CREATED);
        }
        return response;
    }


    //method that responds to a request to create a new Game.
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        Player authenticatedPlayer = this.getAuthenticatedPlayer();

        if (!isGuest(authentication)) {
            Player player = playerRepository.findByUserName(authentication.getName());
            Game newGame = new Game();
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(authenticatedPlayer,newGame); //usuario auth + newGame
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getGpId()), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(makeMap("error", "you are not logged in"), HttpStatus.UNAUTHORIZED);
        }
    }

    //method that responds to a request to Join Game.
    @RequestMapping(path = "/game/{game_id}/players", method = RequestMethod.POST)
    public ResponseEntity <Map<String,Object>> joinGame(@PathVariable ("game_id") Long game_id) {

        Player player = getAuthenticatedPlayer();
        Game game = gameRepository.getOne(game_id);
       // Player loggedPlayer = playerRepository.findByUserName(authentication.getName());

        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "please log in"), HttpStatus.UNAUTHORIZED);
        }
        if (game == null) {
            return new ResponseEntity<>(makeMap("error", "There is no game to play"), HttpStatus.FORBIDDEN);
        }
        if (game.getGamePlayers().size() == 2) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        GamePlayer gamePlayer = new GamePlayer(player, game);
        gamePlayerRepository.save(gamePlayer);
        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getGpId()), HttpStatus.CREATED);

    }


    @RequestMapping("/leaderBoard")
    public List<Map<String,Object>> getScoreData(){
        List<Map<String,Object>> scoreList = new ArrayList<>();
        this.getAllPlayers().forEach(player -> {
            if(!player.getScores().isEmpty()) {
                Map<String, Object> dto = new LinkedHashMap<>();
                Map<String, Object> dtoScore = new LinkedHashMap<>();
                dtoScore.put("total",player.getAllScore(player));
                dtoScore.put("won", player.getAllWins(player));
                dtoScore.put("lost", player.getAllLost(player));
                dtoScore.put("tied", player.getAllTied(player));
                dto.put("name",player.getUserName());
                dto.put("score",dtoScore);
                scoreList.add(dto);
            }
        });
        return scoreList;
    }

    private boolean turnHasSalvoes(Salvo newSalvo, Set<Salvo> playerSalvoes){
        boolean hasSalvoes = false;
        for (Salvo salvo: playerSalvoes) {
            if(salvo.getTurn() == newSalvo.getTurn()){
                hasSalvoes = true;
            }
        }
        return hasSalvoes;
    }

    private List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    //use this method in your app controller methods to see if you have a guest user and respond appropriately.
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private Player getAuthenticatedPlayer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return playerRepository.findByUserName(authentication.getName());
        }
    }
    private ResponseEntity<Map<String, Object>> getCreatedGp(GamePlayer gamePlayer) {
        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getGpId()), HttpStatus.CREATED);
    }
    private Map<String, Object> loggedInToDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    private Player getLoggedPlayer(Authentication authentication) {
        return playerRepository.findByUserName(authentication.getName());
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    /* this returns the players username or 'guest' (if no one logged in) */
    public String getUsername(Authentication authentication) {
        if (!isGuest(authentication)) { //This checks there is not a guest user
            String loggedInUser = playerRepository.findByUserName(authentication.getName()).getUserName();
            return loggedInUser;
        } else {
            String loggedInUser = "guest";
            return loggedInUser;
        }
    }

    private Player currentAuthenticatedUser(Authentication authentication) {
        if (isGuest(authentication)) {
            return null;
        }
        return playerRepository.findByUserName(authentication.getName());
    }

    private String currentAuthenticatedUserName(Authentication authentication) {
        if (isGuest(authentication)) {
            return null;
    }
        return authentication.getName();
    }


}


