package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TitlesCount {
    private String playerName;
    private int titlesCountEL = 0;
    private int titlesCountCL = 0;
}
