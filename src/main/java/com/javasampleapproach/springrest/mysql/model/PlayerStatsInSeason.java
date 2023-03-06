package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class  PlayerStatsInSeason {
        private String typeOfRecord;
        private Integer playerTeamId;
        private String season;
        private String teamName;
}
