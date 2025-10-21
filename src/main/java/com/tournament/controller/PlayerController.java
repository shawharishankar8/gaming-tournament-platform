package com.tournament.controller;

import com.tournament.model.entity.Player;
import com.tournament.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
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
        if (player.getGamerTag() == null || player.getGamerTag().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (playerRepository.findByGamerTag(player.getGamerTag()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
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

    @PutMapping("/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id, @RequestBody Player update) {
        Player existing = playerRepository.findById(id).orElseThrow();
        String newTag = update.getGamerTag();
        if (newTag != null && !newTag.isBlank() && !newTag.equals(existing.getGamerTag())) {
            if (playerRepository.findByGamerTag(newTag).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            existing.setGamerTag(newTag);
        }
        if (update.getEloRating() != 0) existing.setEloRating(update.getEloRating());
        existing.setWins(update.getWins());
        existing.setLosses(update.getLosses());
        Player saved = playerRepository.save(existing);
        return ResponseEntity.ok(saved);
    }
}


