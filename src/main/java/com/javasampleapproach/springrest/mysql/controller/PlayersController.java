package com.javasampleapproach.springrest.mysql.controller;

import java.util.Map;

import com.javasampleapproach.springrest.mysql.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.PlayerStats;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/playerStats")
public class PlayersController {

	@Autowired
	PlayerService playerService;

	@GetMapping("/getGlobalStats")
	public Map<String, PlayerStats> getGlobalStats() {
		return playerService.getGlobalStats();
	}
}
