package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetail {
    private int matchId;
    private String goalscorers; //todo improve this + yc and rc
    private String yellowcards;
    private String redcards;
}
