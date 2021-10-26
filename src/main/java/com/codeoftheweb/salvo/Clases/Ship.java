package com.codeoftheweb.salvo.Clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class  Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayerID")
    private GamePlayer gamePlayerID;

    @ElementCollection
    @Column(name="shipLocation")
    private List<String> shipLocations = new ArrayList<>();



    public Ship() { }

    public Ship(String type, GamePlayer gamePlayerID, List<String>shipLocations) {
        this.type = type;
        this.gamePlayerID = gamePlayerID;
        this.shipLocations = shipLocations;
    }


    public Map<String,Object> makeShipDTO(){

        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("type",this.gettype());
        dto.put("locations",this.getshipLocations());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String gettype() {
        return type;
    }

    public void settype(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(GamePlayer gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public List<String> getshipLocations() {
        return shipLocations;
    }

    public void setshipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }
}