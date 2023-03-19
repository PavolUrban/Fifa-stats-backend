package com.javasampleapproach.springrest.mysql.mapper;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.builder.ConfigurableConditionExpression;
import org.springframework.stereotype.Component;


// TODO use mapper in the future
@Component
public class MatchesMapper extends ModelMapper {
    public MatchesMapper() {
        matchesToMatchesDTOMapper();
    }

    private void matchesToMatchesDTOMapper() {
        typeMap(Matches.class, MatchesDTO.class)
                .addMappings((ConfigurableConditionExpression<Matches, MatchesDTO> mapper)-> {
                    mapper.map(m -> m.getHomeTeam().getTeamName(), MatchesDTO::setHomeTeam);
                    mapper.map(m -> m.getHomeTeam().getId(), MatchesDTO::setIdHomeTeam);
                    mapper.map(m -> m.getAwayTeam().getTeamName(), MatchesDTO::setAwayTeam);
                    mapper.map(m -> m.getAwayTeam().getId(), MatchesDTO::setIdAwayTeam);
                   // mapper.map(match-> getWinnerPlayer(match), MatchesDTO::setWinnerPlayer);
                });
    }
}
