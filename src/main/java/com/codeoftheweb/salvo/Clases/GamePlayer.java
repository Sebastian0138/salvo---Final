package com.codeoftheweb.salvo.Clases;

import com.codeoftheweb.salvo.Repos.GamePlayerRepository;
import com.codeoftheweb.salvo.Repos.ShipRepository;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowired;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

    @Entity
    public class GamePlayer {


        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
        @GenericGenerator(name = "native", strategy = "native")
        private Long id;
        private LocalDateTime joinDate;

        @ElementCollection
        @Column(name = "selfHits")
        private List<String> self = new ArrayList<>();

        @ElementCollection
        @Column(name = "opponentHits")
        private List<String> opponent = new ArrayList<>();

        @OneToMany(mappedBy = "gamePlayerID", fetch = FetchType.EAGER)
        private Set<Ship> ships;

        @OneToMany(mappedBy = "gamePlayerID", fetch = FetchType.EAGER)
        private Set<Salvo> salvo;


        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "gameId")
        private Game gameId;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "playerId")
        private Player playerId;

        public GamePlayer() {
        }


        public Optional<Score> getScoreGP() {

            return this.
                    getPlayerId().getScorePl(this.gameId);

        }


        public GamePlayer(Game gameId, Player playerId, LocalDateTime joinDate) {
            this.joinDate = joinDate;
            this.gameId = gameId;
            this.playerId = playerId;
        }

        public Map<String, Object> makeGamePlayerDTO() {
            Map<String, Object> dto = new LinkedHashMap<>();

            dto.put("id", this.getId());
            dto.put("player", this.getPlayerId().makePlayerDTO());

            return dto;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public LocalDateTime getJoinDate() {
            return joinDate;
        }

        public void setJoinDate(LocalDateTime joinDate) {
            this.joinDate = joinDate;
        }

        public Game getGameId() {
            return gameId;
        }

        public void setGameId(Game gameId) {
            this.gameId = gameId;
        }


        public Player getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Player playerId) {
            this.playerId = playerId;
        }

        public Set<Ship> getShip() {
            return ships;
        }

        public void setShip(Set<Ship> ship) {
            this.ships = ship;
        }

        public Set<Salvo> getSalvo() {
            return salvo;
        }

        public void setSalvo(Set<Salvo> salvo) {
            this.salvo = salvo;
        }

        public Map<String, Object> makeGameViewDTO() {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", this.getGameId().getId());
            dto.put("created", this.getGameId().getCreationDate());
            dto.put("gameState", gameState());
            dto.put("gamePlayers", this.getGameId().getGamePlayer()
                    .stream()
                    .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())
                    .collect(Collectors.toList()));
            dto.put("ships", this.getShip()
                    .stream()
                    .map(ship -> ship.makeShipDTO())
                    .collect(Collectors.toList()));


            dto.put("salvoes", this
                    .getGameId()
                    .getGamePlayer()
                    .stream()
                    .flatMap(gamePlayer -> gamePlayer.getSalvo()
                            .stream()
                            .map(b -> b.makeSalvoDTO())
                    ));
            if (getShip().size() != 0) {
                dto.put("hits", this.makeHitsDTO());


            } else {

                dto.put("hits", this.makefalsoHitsDTO());


            }
            return dto;




        }


        public Map<String, Object> makefalsoHitsDTO() {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("self", new ArrayList<>());
            dto.put("opponent", new ArrayList<>());


            return dto;


        }


        public Map<String, Object> makeHitsDTO() {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("self", this.getSalvo().stream().map(this::selfDTO));
            dto.put("opponent", this.getOpponent().getSalvo().stream().map(sb -> oponentDTO(sb)));


            return dto;


        }


        public Map<String, Object> selfDTO(Salvo salvo) {
            Map<String, Object> dto = new LinkedHashMap<>();
            List<String> eneshiplocations = this.getOpponent().getShip().stream().flatMap(a -> a.getshipLocations().stream()).collect(Collectors.toList());
            List<String> misalvolocations = this.getSalvo().stream().flatMap(a -> a.getSalvoLocations().stream()).collect(Collectors.toList());
            List<String> hitsLocations = misalvolocations.stream().filter(x -> eneshiplocations.contains(x)).collect(Collectors.toList());

            Optional<GamePlayer> enemigoop = this.getGameId().getGamePlayer().stream().filter(b -> !b.getId().equals(this.getId())).findFirst();
            GamePlayer enemigo = new GamePlayer();
            enemigo = enemigoop.orElse(null);

            dto.put("turn", salvo.getTurn());
            dto.put("hitLocations", hitsLocations);
            if (enemigo != null) {
                dto.put("damages", this.makeDamage(salvo));
                dto.put("missed", misalvolocations.size() - hitsLocations.size());
            }
            return dto;
        }

        public Map<String, Object> oponentDTO(Salvo salvo) {
            Map<String, Object> dto = new LinkedHashMap<>();

            List<String> mishiplocations = this.getShip().stream().flatMap(a -> a.getshipLocations().stream()).collect(Collectors.toList());
            List<String> enesalvolocations = this.getOpponent().getSalvo().stream().flatMap(a -> a.getSalvoLocations().stream()).collect(Collectors.toList());
            List<String> hitsLocations = enesalvolocations.stream().filter(x -> mishiplocations.contains(x)).collect(Collectors.toList());
            Optional<GamePlayer> enemigoop = this.getGameId().getGamePlayer().stream().filter(b -> !b.getId().equals(this.getId())).findFirst();
            GamePlayer enemigo = new GamePlayer();
            enemigo = enemigoop.orElse(null);


            dto.put("turn", salvo.getTurn());
            dto.put("hitLocations", hitsLocations);
            if (enemigo != null) {
                dto.put("damages", this.makeDamage(salvo));
                dto.put("missed", enesalvolocations.size() - hitsLocations.size());
            }
            return dto;
        }

        public GamePlayer getOpponent() {
            Game juegoactual = this.getGameId();
            GamePlayer opponent = juegoactual.getGamePlayer().stream().filter(gamePlayer -> gamePlayer.getId() != this.getId()).findFirst().orElse(null);
            return opponent;
        }

        private List<String> locacionesdebarcos(String type) {
            Optional<Ship> locacionesdebarcos = ships.stream()
                    .filter(ship -> ship.gettype() == type).findAny();
            if (locacionesdebarcos.isEmpty()) {
                return new ArrayList<>();
            } else {
                return locacionesdebarcos.get().getshipLocations();
            }
        }

        public Map<String, Object> makeDamage(Salvo salvo) {


            ArrayList<String> hits_pegados = new ArrayList<String>();
            Map<String, Object> DTO_danios = new LinkedHashMap<>();
            int ccarrier = 0;
            int csubmarine = 0;
            int cbattleship = 0;
            int cdestroyer = 0;
            int cpatrolboat = 0;
            int carrier1 = 0;
            int submarine1 = 0;
            int battleship1 = 0;
            int destroyer1 = 0;
            int patrolboat1 = 0;

            GamePlayer oponente1 = this.getOpponent();
            List<String> carrierlocation = new ArrayList<>();
            List<String> submarinelocation = new ArrayList<>();
            List<String> battleshiplocation = new ArrayList<>();
            List<String> destroyerlocation = new ArrayList<>();
            List<String> patrolboarlocation = new ArrayList<>();

            for (Ship ship : oponente1.getShip()) {

                switch (ship.gettype()) {

                    case "carrier":

                        carrierlocation = ship.getshipLocations();
                        break;
                    case "submarine":

                        submarinelocation = ship.getshipLocations();
                        break;
                    case "battleship":

                        battleshiplocation = ship.getshipLocations();
                        break;
                    case "destroyer":

                        destroyerlocation = ship.getshipLocations();
                        break;
                    case "patrolboat":

                        patrolboarlocation = ship.getshipLocations();
                        break;


                }
            }


            DTO_danios.put("patrolboatHits", patrolboarlocation.stream().filter(location -> salvo.getSalvoLocations().contains(location)).count());
            DTO_danios.put("destroyerHits", destroyerlocation.stream().filter(location -> salvo.getSalvoLocations().contains(location)).count());
            DTO_danios.put("carrierHits", carrierlocation.stream().filter(location -> salvo.getSalvoLocations().contains(location)).count());
            DTO_danios.put("submarineHits", submarinelocation.stream().filter(location -> salvo.getSalvoLocations().contains(location)).count());
            DTO_danios.put("battleshipHits", battleshiplocation.stream().filter(location -> salvo.getSalvoLocations().contains(location)).count());


            List<String> allsalvos = new ArrayList<>();
            allsalvos = salvo.getGamePlayerID().getSalvo().stream().filter(b -> b.getTurn() <= salvo.getTurn()).flatMap(a -> a.getSalvoLocations().stream()).collect(Collectors.toList());

            List<String> finalAllsalvos = allsalvos;
            DTO_danios.put("carrier", carrierlocation.stream().filter(finalAllsalvos::contains).count());
            List<String> finalAllsalvos1 = allsalvos;
            DTO_danios.put("submarine", submarinelocation.stream().filter(finalAllsalvos1::contains).count());
            List<String> finalAllsalvos2 = allsalvos;
            DTO_danios.put("battleship", battleshiplocation.stream().filter(finalAllsalvos2::contains).count());
            List<String> finalAllsalvos3 = allsalvos;
            DTO_danios.put("destroyer", destroyerlocation.stream().filter(finalAllsalvos3::contains).count());
            List<String> finalAllsalvos4 = allsalvos;
            DTO_danios.put("patrolboat", patrolboarlocation.stream().filter(finalAllsalvos4::contains).count());


            return DTO_danios;
        }

        public String gameState() {

            if (this.getGameId().getGamePlayer().size() != 2) {
                return "WAITINGFOROPP";
            }
            GamePlayer enemigo = this.getGameId().getGamePlayer().stream().filter(b -> !b.getId().equals(this.getId())).findFirst().get();

            if (this.getShip().size() != 5) {
                return "PLACESHIPS";
            }

            if (enemigo.getShip().size() != 5) {
                return "WAITINGFOROPP";
            }

            if ((this.getSalvo().size()>2 && enemigo.getSalvo().size()>2 )&&(this.getSalvo().size()== enemigo.getSalvo().size() )){
                String jug_gana="";
                String ene_gana="";


                if (barcosHundidos(this,enemigo)) {
                    ene_gana = "Ganoelplayer";
                }
                if (barcosHundidos(enemigo,this)) {
                    jug_gana = "Ganoelplayer";
                }
                if (jug_gana.equals("Ganoelplayer") && ene_gana.equals("Ganoelplayer")) {
                    return "TIE";
                }
                if (jug_gana.equals("Ganoelplayer")) {
                    return "WON";
                }
                if (ene_gana.equals("Ganoelplayer")) {
                    return "LOST";
                }
            }
            if(enemigo.getId()<this.getId()){
                if(enemigo.getSalvo().size()<=this.getSalvo().size()){return "WAIT";}
                else {return "PLAY";}
            }
            if(enemigo.getId()>this.getId()){
                if(enemigo.getSalvo().size()>=this.getSalvo().size()){return "PLAY";}
                else {return "WAIT";}
            }

            return "";
        }
        private boolean barcosHundidos(GamePlayer gpBarcos, GamePlayer gpSalvos) {

            GamePlayer opponent = this.getOpponent();

            if (!gpBarcos.getShip().isEmpty() && !gpSalvos.getSalvo().isEmpty()) {
                return  gpSalvos.getSalvo()
                        .stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList())
                        .containsAll(gpBarcos.getShip()
                                .stream().flatMap(ship -> ship.getshipLocations().stream())
                                .collect(Collectors.toList()));
            }
            return false;
        }
    }







//public Map<String,Object>makeHitsSelfDTO(){
//
//         Map<String,Object>dto= new LinkedHashMap<>();
//         dto.put("turn",this.getSalvo().size());
//         List<String>eneshiplocations=this.getOpponent().getShip().stream().flatMap(a->a.getshipLocations().stream()).collect(Collectors.toList());
//         List<String>misalvolocations=this.getSalvo().stream().flatMap(a->a.getSalvoLocations().stream()).collect(Collectors.toList());
//         List<String>hitsLocations = misalvolocations.stream().filter(x->eneshiplocations.contains(x)).collect(Collectors.toList());
//         dto.put("hitLocation",hitsLocations);
//         dto.put("damages",1);
//         dto.put("missed",1);
//
//
//
//
//
//      return dto;  }


//public Map<String,Object>makeHitsOpponentDTO(){
//
//
//    Map<String,Object>dto= new LinkedHashMap<>();
//    dto.put("turn",this.getOpponent().getSalvo().size());
//    List<String>mishiplocations=this.getShip().stream().flatMap(a->a.getshipLocations().stream()).collect(Collectors.toList());
//    List<String>enesalvolocations=this.getOpponent().getSalvo().stream().flatMap(a->a.getSalvoLocations().stream()).collect(Collectors.toList());
//    List<String>hitsLocations = enesalvolocations.stream().filter(x->mishiplocations.contains(x)).collect(Collectors.toList());
//    dto.put("hitLocation",hitsLocations);
//    dto.put("damages",1);
//    dto.put("missed",1);
//
//
//return dto;
//}




//
//        public Map<String,Object>makedamagesSelfDTO() {
//
//
//            Map<String, Object> dto = new LinkedHashMap<>();
//
//            dto.put("turn",this.getSalvo().size());
//            dto.put("damages",1);
//            dto.put("missed",1);
//
//
//
//
//
//
//            return dto;
//
//
//        }

//        public Map<String,Object> makeDamagesselfDTO(int turno1, GamePlayer opponent){
//            ArrayList<String> hits_pegados = new ArrayList<String>();
//            Map<String,Object> dto= new LinkedHashMap<>();
//            int ccarrier=0;
//            int csubmarine=0;
//            int cbattleship=0;
//            int cdestroyer=0;
//            int cpatrolboat=0;
//            int carrier1=0;
//            int submarine1=0;
//            int battleship1=0;
//            int destroyer1=0;
//            int patrolboat1=0;
//            Map<String, Object> DTO_turno= new LinkedHashMap<>();
//            Map<String, Object> DTO_danios  = new LinkedHashMap<>();
//
//
//            int finalTurnitos = turno1;
//            Salvo Salvos_importen = opponent.getSalvo().stream().filter(b->b.getTurn() == finalTurnitos).findFirst().get();
//
//
//            List<String> Salvos_turno= Salvos_importen.getSalvoLocations();
//
//            for (String c : getShip().stream().map(Ship::gettype).collect(Collectors.toList())) {
//                carrier1=0;
//                submarine1=0;
//                battleship1=0;
//                destroyer1=0;
//                patrolboat1=0;
//
//                List<String> loca_barcos = (opponent.getShip().stream().filter(b -> b.gettype().equals(c)).findFirst().get()).getshipLocations();
//                for (String b : loca_barcos) {
//
//                    if (Salvos_turno.contains(b)) {
//                        hits_pegados.add(b);
//                        if (c.equals("carrier")) {
//                            ccarrier++;
//                            carrier1++;
//                        }
//                        if (c.equals("submarine")) {
//                            submarine1++;
//                            csubmarine++;
//                        }
//                        if (c.equals("battleship")) {
//                            cbattleship++;
//                            battleship1++;
//                        }
//                        if (c.equals("destroyer")) {
//                            destroyer1++;
//                            cdestroyer++;
//
//
//                        }
//                        if (c.equals("patrolboat")) {
//                            patrolboat1++;
//                            cpatrolboat++;
//
//
//
//                        }
//
//                        DTO_danios.put("patrolboatHits",patrolboat1);
//                        DTO_danios.put("destroyerHits",destroyer1);
//                        DTO_danios.put("carrierHits",carrier1);
//                        DTO_danios.put("submarineHits",submarine1);
//                        DTO_danios.put("battleshipHits",battleship1);
//
//                        DTO_danios.put("carrier",ccarrier);
//                        DTO_danios.put("submarine",csubmarine);
//                        DTO_danios.put("battleship",cbattleship);
//                        DTO_danios.put("destroyer",cdestroyer);
//                        DTO_danios.put("patrolboat",cpatrolboat);
//                       // DTO_turno.put("damages",DTO_danios);
//
//
//                    }
//
//
//                }
//
//
//            }
//            Ship carrier = this.getShip().stream().filter(sh -> sh.gettype().equals("carrier")).findFirst().get();
//            List <String> carrierLocations = carrier.getshipLocations();
//
//            return DTO_danios;
//        }
//
//
//
//
//        public Map<String,Object> makeDamagesopponentDTO(int turno1, GamePlayer opponent){
//            ArrayList<String> hits_pegados = new ArrayList<String>();
//            Map<String,Object> dto= new LinkedHashMap<>();
//            int ccarrier=0;
//            int csubmarine=0;
//            int cbattleship=0;
//            int cdestroyer=0;
//            int cpatrolboat=0;
//            int carrier1=0;
//            int submarine1=0;
//            int battleship1=0;
//            int destroyer1=0;
//            int patrolboat1=0;
//            Map<String, Object> DTO_turno= new LinkedHashMap<>();
//            Map<String, Object> DTO_danios  = new LinkedHashMap<>();
//
//
//            int finalTurnitos = turno1;
//            Salvo Salvos_importen = this.getSalvo().stream().filter(b->b.getTurn() == finalTurnitos).findFirst().get();
//
//
//            List<String> Salvos_turno= Salvos_importen.getSalvoLocations();
//
//            for (String c : opponent.getShip().stream().map(Ship::gettype).collect(Collectors.toList())) {
//                carrier1=0;
//                submarine1=0;
//                battleship1=0;
//                destroyer1=0;
//                patrolboat1=0;
//
//                List<String> loca_barcos = (this.getShip().stream().filter(b -> b.gettype().equals(c)).findFirst().get()).getshipLocations();
//                for (String b : loca_barcos) {
//
//                    if (Salvos_turno.contains(b)) {
//                        hits_pegados.add(b);
//                        if (c.equals("carrier")) {
//                            ccarrier++;
//                            carrier1++;
//                        }
//                        if (c.equals("submarine")) {
//                            submarine1++;
//                            csubmarine++;
//                        }
//                        if (c.equals("battleship")) {
//                            cbattleship++;
//                            battleship1++;
//                        }
//                        if (c.equals("destroyer")) {
//                            destroyer1++;
//                            cdestroyer++;
//
//
//                        }
//                        if (c.equals("patrolboat")) {
//                            patrolboat1++;
//                            cpatrolboat++;
//
//
//
//                        }
//
//                        DTO_danios.put("patrolboatHits",patrolboat1);
//                        DTO_danios.put("destroyerHits",destroyer1);
//                        DTO_danios.put("carrierHits",carrier1);
//                        DTO_danios.put("submarineHits",submarine1);
//                        DTO_danios.put("battleshipHits",battleship1);
//
//                        DTO_danios.put("carrier",ccarrier);
//                        DTO_danios.put("submarine",csubmarine);
//                        DTO_danios.put("battleship",cbattleship);
//                        DTO_danios.put("destroyer",cdestroyer);
//                        DTO_danios.put("patrolboat",cpatrolboat);
//                        // DTO_turno.put("damages",DTO_danios);
//
//
//                    }
//
//
//                }
//
//
//            }
//            Ship carrier = this.getShip().stream().filter(sh -> sh.gettype().equals("carrier")).findFirst().get();
//            List <String> carrierLocations = carrier.getshipLocations();
//
//            return DTO_danios;
//        }
//
//
//
//
//
//
//
//
//
//    }
//



//public Map<String,Object> makeHitsSelfOppontDTO(){
//            int ccarrier=0;
//
//            int csubmarine=0;
//            int cbattleship=0;
//            int cdestroyer=0;
//            int cpatrolboat=0;
//          int  carrier1=0;
//          int  submarine1=0;
//          int  battleship1=0;
//          int  destroyer1=0;
//          int  patrolboat1=0;
//
//
//
//            Map<String,Object>dto= new LinkedHashMap<>();
//           int turnos=0;
//    for (turnos=0;turnos!=this.getSalvo().size();turnos++){
//
//        int Turnos=turnos;
//        List<String> Salvoturno= this.getSalvo().stream().filter(a->a.getTurn()==Turnos).findFirst().get().getSalvoLocations();
//        System.out.println("esto funciona?"+Turnos);
//
//        for (String d : this.getShip().stream().map(Ship::gettype).collect(Collectors.toList())){
//
//            carrier1=0;
//            submarine1=0;
//            battleship1=0;
//            destroyer1=0
//                    };
////            patrolboat1=0;
////
////
////
////            List<String> localizacionbarcos = (this.getShip().stream().filter(b -> b.gettype().equals(d)).findFirst().get()).getshipLocations();
////            for (String b : localizacionbarcos) {
////                dto.put("turn",Turnos);
////                if (Salvoturno.contains(b)) {
////                    if (b.equals("carrier")) {
////                        ccarrier++;
////                        carrier1++;
////                        dto.put("carrier",carrier1);
////
//                    if(b.equals("submarine")){
//                    submarine1++;
//                    csubmarine++;
//                    dto.put("submarine",submarine1);
//
//                    }
//                    if(b.equals("battleship")){
//                    battleship1++;
//                    cbattleship++;
//                    dto.put("battleship",battleship1);
//
//
//                    }
//                    if(b.equals("destroyer")){
//
//                        destroyer1++;
//                        cdestroyer++;
//                        dto.put("destroyer",destroyer1);
//
//
//                    }
//                    if(b.equals("patroalboat")){
//
//                        patrolboat1++;
//                        cpatrolboat++;
//                        dto.put("patrolboat",patrolboat1);
//
//                    }
//                }
//                }
//
//        }
//
//
//    }
//
//
//    return dto;  }}






//        public Map<String,Object> damages(){
//            Map<String, Object> dto= new LinkedHashMap<>();
//
//            List<String> Salvos_turno= this.getSalvo().stream().filter(b->b.getTurn()==this.getSalvo().size()).findFirst().get().getSalvoLocations();
//            for (String a : this.getShip().stream().map(Ship::gettype).collect(Collectors.toList())){
//
//                List<String> locacionbarcos=(this.getShip().stream().filter(b-> b.gettype().equals(a)).findFirst().get()).getshipLocations();
//                for (String b:locacionbarcos){
//                    if( Salvos_turno.contains(b)){
//                        dto.put(a,b);
//
//          //          }


          //      }


          //  }

//            return dto;
//
//

