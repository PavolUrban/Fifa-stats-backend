package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.model.GeneralFilterRequest;
import com.javasampleapproach.springrest.mysql.model.PlayerStats;
import com.javasampleapproach.springrest.mysql.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/playerStats")
public class PlayersController {

	@Autowired
	PlayerService playerService;

	@PostMapping("/getGlobalStats")
	public Map<String, PlayerStats> getGlobalStats(@RequestBody GeneralFilterRequest request) {
		return playerService.getGlobalStats(request);
	}
}
