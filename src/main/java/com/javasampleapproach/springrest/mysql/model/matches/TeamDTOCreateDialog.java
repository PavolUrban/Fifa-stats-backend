package com.javasampleapproach.springrest.mysql.model.matches;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamDTOCreateDialog {
    private String teamName;
    private Long id;
}
