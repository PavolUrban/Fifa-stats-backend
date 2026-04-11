package com.javasampleapproach.springrest.mysql.model.lineup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRef {
    private long playerId;
    private String playerName;
}

