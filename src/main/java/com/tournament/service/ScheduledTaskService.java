package com.tournament.service;

public interface ScheduledTaskService {
    void runCleanup();
    void startDueTournaments();
    void finalizeCompletedTournaments();
}


