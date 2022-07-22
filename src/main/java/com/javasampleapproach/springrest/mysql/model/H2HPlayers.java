package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class H2HPlayers {

    // checked - fully used in Overall Stats to get PavolJay vs Kotlik stats per season
    private int pavolJay = 0;
    private int draws = 0;
    private int kotlik = 0;
}
