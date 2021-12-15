package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FifaPlayer {

    private String name;
    private int cardsTotal = 0;
    private int redCards = 0;
    private int yellowCards = 0;
}
