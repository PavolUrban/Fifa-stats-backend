package com.javasampleapproach.springrest.mysql.model.seasons;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupStage {
    List<SingleGroup> groupsList;

    public GroupStage() {
        this.groupsList = new ArrayList<>();
    }
}
