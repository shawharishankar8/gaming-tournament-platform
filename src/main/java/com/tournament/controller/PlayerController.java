package com.tournament.controller;

import com.tournament.model.entity.Player;
import com.tournament.model.entity.User;
import com.tournament.repository.PlayerRepository;
import com.tournament.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PlayerController(PlayerRepository playerRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    // Create player profile for authenticated user
    @PostMapping
    public ResponseEntity<Player> create(@RequestBody Player player, Principal principal) {
        if (player.getGamerTag() == null || player.getGamerTag().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check if gamer tag is already taken
        if (playerRepository.findByGamerTag(player.getGamerTag()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Get current user and link to player
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has a player profile
        if (user.getPlayer() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(user.getPlayer());
        }

        player.setUser(user);
        Player saved = playerRepository.save(player);

        // Update user's player reference
        user.setPlayer(saved);
        userRepository.save(user);

        return ResponseEntity.created(URI.create("/api/players/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Player> list() {
        return playerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> get(@PathVariable Long id) {
        Optional<Player> player = playerRepository.findById(id);
        return player.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<Player> getCurrentPlayer(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPlayer() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user.getPlayer());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id, @RequestBody Player update, Principal principal) {
        // Verify the player belongs to the current user
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Player existing = playerRepository.findById(id).orElseThrow();

        // Security check: only allow users to update their own player
        if (existing.getUser() == null || !existing.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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