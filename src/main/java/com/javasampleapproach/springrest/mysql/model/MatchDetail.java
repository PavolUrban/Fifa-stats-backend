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
    List<MatchEventDetail> events = new ArrayList<>();
}
