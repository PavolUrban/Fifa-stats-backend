package com.javasampleapproach.springrest.mysql.services;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FifaPlayerService {

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    public List<FifaPlayerDB> getPlayersByName(final String nameSubstring) {
        return fifaPlayerDBRepository.findByPlayerNameContainingIgnoreCase(nameSubstring);
    }
}
