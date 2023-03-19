package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.model.seasons.SeasonWrapper;
import com.javasampleapproach.springrest.mysql.model.seasons.SeasonsRequest;
import com.javasampleapproach.springrest.mysql.services.SeasonsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/seasons")
public class SeasonsController {

	@Autowired
	SeasonsService seasonsService;

	@PostMapping("/getAllPhases")
	public SeasonWrapper getAllPhases(@RequestBody SeasonsRequest seasonsRequest) {
		return seasonsService.getAllPhasesForSeasonAndCompetitionV2(seasonsRequest.getSeason(), seasonsRequest.getCompetition());
	}

}
