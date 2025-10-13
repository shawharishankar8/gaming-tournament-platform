package com.tournament.controller;

import com.tournament.model.entity.Player;
import com.tournament.repository.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping
    public ResponseEntity<Player> create(@RequestBody Player player) {
        Player saved = playerRepository.save(player);
        return ResponseEntity.created(URI.create("/api/players/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Player> list() {
        return playerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Player get(@PathVariable Long id) {
        return playerRepository.findById(id).orElseThrow();
    }
}


