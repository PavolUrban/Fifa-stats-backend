package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;

import java.util.List;

@Data
public class GeneralFilterRequest {
    private String competition;
    private List<String> competitionPhases;
    private List<String> seasons;
}
