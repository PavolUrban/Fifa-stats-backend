package com.javasampleapproach.springrest.mysql.services;

import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeasonsService {

    @Autowired
    SeasonsRepository seasonsRepository;

    public List<String> getAvailableSeasonsList() {
        return seasonsRepository.getAvailableSeasonsList();
    }

}
