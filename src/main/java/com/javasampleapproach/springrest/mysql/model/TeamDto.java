package com.javasampleapproach.springrest.mysql.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamDto {

    private long id;
    private String teamName;
    private long teamId;
    private String firstSeasonCL;
    private String firstSeasonEL;
    private String country;
}
