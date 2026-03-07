package com.javasampleapproach.springrest.mysql.model.v2DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInfoV2 {
    public int finalMatchesCLCount;
    public int finalMatchesELCount;
    public int titlesCLCount;
    public int titlesELCount;
    public String teamName;
    public String country;
}
