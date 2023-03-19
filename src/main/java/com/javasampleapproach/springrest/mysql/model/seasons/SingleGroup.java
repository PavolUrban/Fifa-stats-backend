package com.javasampleapproach.springrest.mysql.model.seasons;

import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SingleGroup {
    private String groupName;
    private List<TableTeam> groupTable;
    private List<Goalscorer> goalscorersList;
    private H2HV2 h2hPlayers;

    public SingleGroup(){
        this.groupTable = new ArrayList<>();
        this.goalscorersList = new ArrayList<>();
    }
}
