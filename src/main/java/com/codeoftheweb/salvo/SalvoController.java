package com.codeoftheweb.salvo;
import com.codeoftheweb.salvo.Clases.*;
import com.codeoftheweb.salvo.Repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {


    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SalvoRepository salvoRepository;


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @PostMapping(path = "/games")
    public ResponseEntity<Map> findGamePlayer(Authentication authentication) {


        LocalDateTime Tiempo = LocalDateTime.now();

        Game newgame = gameRepository.save(new Game(Tiempo));
        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(
                newgame, this.playerRepository.findByuserName(authentication.getName()), LocalDateTime.now()
        ));


        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.ACCEPTED);
    }


    @RequestMapping("/games")
    public Map<String, Object> makeGame(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();


        if (isGuest(authentication)) {
            dto.put("player", "Guest");
        } else {
            dto.put("player", playerRepository.findByuserName(authentication.getName()).makePlayerDTO());

        }

        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping(value = "/games/players/{gameplayerid}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map> PlaceShips(@PathVariable Long gameplayerid
            , @RequestBody List<Ship> lugarbarco
            , Authentication authentication) {
        if (playerRepository.findByuserName(authentication.getName()) == null) {
            return new ResponseEntity<>(makeMap("No disponiblez", 0), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayerRepository.findAll().stream().noneMatch(a -> a.getId().equals(gameplayerid))) {
            return new ResponseEntity<>(makeMap("No disponiblea", 0), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByuserName(authentication.getName()).getGamePlayer().stream().noneMatch(a -> a.getId().equals(gameplayerid))) {
            return new ResponseEntity<>(makeMap("No disponibleb", 0), HttpStatus.FORBIDDEN);
        }

        lugarbarco.forEach(b -> b.setGamePlayerID(gamePlayerRepository.getById(gameplayerid)));

        lugarbarco.forEach(b -> shipRepository.save(b));


        for (Ship i : lugarbarco) {
            if (i.gettype().equals("carrier") && i.getshipLocations().size() != 5) {
                System.out.println("no anda el 1");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos!"), HttpStatus.FORBIDDEN);
            }
            if (i.gettype().equals("submarine") && i.getshipLocations().size() != 3) {
                System.out.println("no anda el 2");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos!"), HttpStatus.FORBIDDEN);
            }
            if (i.gettype().equals("battleship") && i.getshipLocations().size() != 4) {
                System.out.println("no anda el 3");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos!"), HttpStatus.FORBIDDEN);
            }
            if (i.gettype().equals("destroyer") && i.getshipLocations().size() != 3) {
                System.out.println("no anda el 4");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos!"), HttpStatus.FORBIDDEN);
            }
            if (i.gettype().equals("patrolboat") && i.getshipLocations().size() != 2) {
                System.out.println("no anda el 5");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos!"), HttpStatus.FORBIDDEN);
            }
            if (!i.gettype().equals("patrolboat") &&
                    !i.gettype().equals("destroyer") &&
                    !i.gettype().equals("battleship") &&
                    !i.gettype().equals("submarine") &&
                    !i.gettype().equals("carrier")) {
                System.out.println("no anda el 6");
                return new ResponseEntity<>(makeMap("error", "Error en el nombre de barcos!"), HttpStatus.FORBIDDEN);


            }
        }


        return new ResponseEntity<>(makeMap("OK", "barcos colocados"), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/games/players/{gameplayerid}/ships", method = RequestMethod.GET)
    public ResponseEntity<Map> PlaceShips(@PathVariable Long gameplayerid
            , Authentication authentication) {
        if (gamePlayerRepository.findById(gameplayerid).isPresent()) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("ship", gamePlayerRepository.findById(gameplayerid).get().getShip().stream().map(b -> b.makeShipDTO()).collect(Collectors.toList()));

            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        }


        return new ResponseEntity<>(makeMap("Este gameplayer es como el estado no existe", 0), HttpStatus.FORBIDDEN);
    }


    @RequestMapping(value = "/game/{gameid}/players", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map> joinGameButton(@PathVariable Long gameid, Authentication authentication) {

        if (playerRepository.findByuserName(authentication.getName()) == null) {


            return new ResponseEntity<>(makeMap("Sin permisos", 0), HttpStatus.UNAUTHORIZED);
        }
        if (gameRepository.findAll().stream().noneMatch(b -> b.getId().equals(gameid))) {


            return new ResponseEntity<>(makeMap("No game", 0), HttpStatus.FORBIDDEN);

        }
        if (gameRepository.getById(gameid).getGamePlayer().size() >= 2) {

            return new ResponseEntity<>(makeMap("Lleno", 0), HttpStatus.FORBIDDEN);

        }

        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(
                gameRepository.getById(gameid), this.playerRepository.findByuserName(authentication.getName()), LocalDateTime.now()
        ));
        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

    }


    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map> findGamePlayer(@PathVariable Long nn, Authentication authentication) {

        GamePlayer gamePlayerID = gamePlayerRepository.findById(nn).get();
        if(gamePlayerID.getGameId().getGamePlayer().stream().filter(b -> !b.getId().equals(gamePlayerID.getId())).findFirst().isPresent()){
        GamePlayer enemigo = gamePlayerID.getGameId().getGamePlayer().stream().filter(b -> !b.getId().equals(gamePlayerID.getId())).findFirst().get();



        if(playerRepository.findByuserName(authentication.getName()).getGamePlayer().stream()
                .anyMatch(b->b.getId().equals(nn))
        ){
            if (enemigo.gameState().equals("WON")) {
                Score Score5= new Score(LocalDateTime.now(),0F,gamePlayerID.getGameId(),gamePlayerID.getPlayerId());
                scoreRepository.save(Score5);
                Score Score6= new Score(LocalDateTime.now(),1F,enemigo.getGameId(),enemigo.getPlayerId());
                scoreRepository.save(Score6);
            }
            if (gamePlayerID.gameState().equals("WON")) {
                Score Score3= new Score(LocalDateTime.now(),1F,gamePlayerID.getGameId(),gamePlayerID.getPlayerId());
                scoreRepository.save(Score3);
                Score Score4= new Score(LocalDateTime.now(),0F,enemigo.getGameId(),enemigo.getPlayerId());
                scoreRepository.save(Score4);
            }
            if (gamePlayerID.gameState().equals("TIE")) {
                Score Score1= new Score(LocalDateTime.now(),0.5F,enemigo.getGameId(),enemigo.getPlayerId());
                scoreRepository.save(Score1);
                Score Score2= new Score(LocalDateTime.now(),0.5F,gamePlayerID.getGameId(),gamePlayerID.getPlayerId());
                scoreRepository.save(Score2);
            }
            return new ResponseEntity<>(gamePlayerID.makeGameViewDTO(),HttpStatus.ACCEPTED);
        }
        else{ return new ResponseEntity<>(makeMap("No hagas trampa",0),HttpStatus.UNAUTHORIZED);}
        }else{return new ResponseEntity<>(makeMap("No hay oponentee",0),HttpStatus.UNAUTHORIZED);

        }
    }










    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email, @RequestParam String password) {
        if (email.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByuserName(email);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> guardatiritos(@PathVariable Long gamePlayerId, @RequestBody Salvo salvoLocations, Authentication authentication) {
        List<Integer> gato = new ArrayList<>();
        if (playerRepository.findByuserName(authentication.getName()) == null) {


            return new ResponseEntity<>(makeMap("Sin permisos", 0), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayerRepository.findAll().stream().noneMatch(a -> a.getId().equals(gamePlayerId))) {

            return new ResponseEntity<>(makeMap("No disponiblea", 0), HttpStatus.FORBIDDEN);

        }

        if (playerRepository.findByuserName(authentication.getName()).getGamePlayer().stream().noneMatch(a -> a.getId().equals(gamePlayerId))) {
            return new ResponseEntity<>(makeMap("No disponibleb", 0), HttpStatus.UNAUTHORIZED);
        }
        Player authenticatedPlayer = playerRepository.findByuserName(authentication.getName());

        if (gamePlayerRepository.getById(gamePlayerId).getPlayerId().getId() != authenticatedPlayer.getId()) {

            return new ResponseEntity<>(makeMap("error", "naciste en argentina"), HttpStatus.UNAUTHORIZED);

        }
        if (gamePlayerRepository.getById(gamePlayerId).getGameId().getPlayer().size() < 2) {

            return new ResponseEntity<>(makeMap("Error", "Falta oponente"), HttpStatus.UNAUTHORIZED);

        }// revisar


// borrar

//        if(gamePlayerRepository.getById(gamePlayerId).getSalvo().size()<=0){
//
//            salvoRepository.save( new Salvo(1, tiritos.getGamePlayerID(), tiritos.getSalvoLocation()));
//
//            return new ResponseEntity<>(makeMap("OK","tiro hecho"), HttpStatus.CREATED);
//
//        }else {
//             gato = gamePlayerRepository.getById(gamePlayerId).getSalvo()
//                    .stream()
//                    .map(b -> b.getTurn())
//                    .collect(Collectors.toList());
//
//
//
//
//        }




        GamePlayer ene=gamePlayerRepository.getById(gamePlayerId).getGameId()
                .getGamePlayer().stream().filter(b->!b.getId().equals(gamePlayerId)).findFirst().get();
        GamePlayer mi=gamePlayerRepository.getById(gamePlayerId);
        int enetiro= ene.getSalvo().size();
        int mitiro= mi.getSalvo().size();



        //  List <String> a= salvoLocations.getSalvoLocations();//como guardar la variable con una letra directamente


        salvoLocations.setGamePlayerID(gamePlayerRepository.getById(gamePlayerId));// verifico si esta en 0 y lo seteo en 1
        if (gamePlayerRepository.getById(gamePlayerId).getSalvo().size() <= 0) {
            salvoLocations.setTurn(1);
        } else {

            salvoLocations.setTurn(gamePlayerRepository.getById(gamePlayerId).getSalvo().size() + 1);// le doy al valor que tiene uno mas osea el siguiente turno

        }


        if(enetiro==mitiro){

            salvoRepository.save(salvoLocations);
            return new ResponseEntity<>(makeMap("OK", "Tiro hecho"), HttpStatus.CREATED);

        }else if(enetiro>mitiro){

            salvoRepository.save(salvoLocations);
            return new ResponseEntity<>(makeMap("OK", "Tiro hecho"), HttpStatus.CREATED);


        }else{

            return new ResponseEntity<>(makeMap("error","No es tu turno"), HttpStatus.FORBIDDEN);


        }
    }




}









