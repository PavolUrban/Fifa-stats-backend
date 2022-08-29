package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetail {
    private int matchId;
    private String typeOfFormat;
    List<MatchEventDetail> eventsFirstHalf = new ArrayList<>();
    List<MatchEventDetail> eventsSecondHalf = new ArrayList<>();
    List<MatchEventDetail> eventsOverTime = new ArrayList<>();
    List<MatchEventDetail> eventsWithoutTime = new ArrayList<>();
}
