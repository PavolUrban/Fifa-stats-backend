package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.Seasons;
import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/general")
public class GeneralController {
	
	@Autowired
	SeasonsRepository seasonsRepository;
	
	
	@GetMapping("/getSeasonsList")
	public List<String> getAllCustomers() {

		List<Seasons> seasons = (List<Seasons>) seasonsRepository.findAll();
		List<String> seasonsStringList = new ArrayList<>();
		
		for(Seasons season : seasons)
		{
			seasonsStringList.add(season.getSeason());
		}
		
		return seasonsStringList;
	}

}
