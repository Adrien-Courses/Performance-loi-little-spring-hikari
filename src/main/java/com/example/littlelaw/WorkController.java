package com.example.littlelaw;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WorkController {

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    /**
     * GET /work?dbMs=5&appMs=35
     *
     * - dbMs  : temps passé en base, connexion Hikari + thread Tomcat occupés (x)
     * - appMs : temps passé dans "la logique métier", seul le thread Tomcat est occupé
     *
     * La latence totale vaut donc environ dbMs + appMs (+ attente éventuelle d'une connexion).
     */
    @GetMapping("/work")
    public Map<String, Object> work(@RequestParam(defaultValue = "5") int dbMs,
                                    @RequestParam(defaultValue = "35") int appMs) throws InterruptedException {
        long start = System.nanoTime();

        long waitForConnection = System.nanoTime();
        workService.db(dbMs);
        long dbDone = System.nanoTime();

        if (appMs > 0) {
            Thread.sleep(appMs);
        }

        long total = (System.nanoTime() - start) / 1_000_000;
        // dbPhase inclut l'attente d'une connexion dans le pool + le pg_sleep :
        // s'il dépasse nettement dbMs, c'est que la requête a attendu une connexion.
        long dbPhase = (dbDone - waitForConnection) / 1_000_000;

        return Map.of(
                "dbMs", dbMs,
                "appMs", appMs,
                "measuredDbPhaseMs", dbPhase,
                "measuredTotalMs", total,
                "thread", Thread.currentThread().getName()
        );
    }

    /** Même chose avec une vraie requête JPA en plus du sleep. */
    @GetMapping("/work/jpa")
    public Map<String, Object> workJpa(@RequestParam(defaultValue = "5") int dbMs,
                                       @RequestParam(defaultValue = "35") int appMs) throws InterruptedException {
        long start = System.nanoTime();
        long count = workService.dbWithQuery(dbMs);
        if (appMs > 0) {
            Thread.sleep(appMs);
        }
        return Map.of(
                "items", count,
                "measuredTotalMs", (System.nanoTime() - start) / 1_000_000
        );
    }
}
