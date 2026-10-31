package com.example.littlelaw;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tout ce qui se passe dans une méthode @Transactional occupe une connexion HikariCP.
 * Dès que la méthode rend la main (commit), la connexion retourne dans le pool.
 * C'est "x" dans l'article.
 */
@Service
public class WorkService {

    @PersistenceContext
    private EntityManager em;

    private final ItemRepository items;

    public WorkService(ItemRepository items) {
        this.items = items;
    }

    /**
     * Simule une requête SQL qui dure {@code ms} millisecondes côté base de données.
     * pg_sleep bloque réellement le backend PostgreSQL : la connexion JDBC est tenue
     * pendant toute la durée, exactement comme une vraie requête lente.
     */
    @Transactional(readOnly = true)
    public void db(int ms) {
        em.createNativeQuery("select pg_sleep(:seconds)")
                .setParameter("seconds", ms / 1000.0)
                .getSingleResult();
    }

    /**
     * Variante "réaliste" : une vraie requête Hibernate plus un pg_sleep dans la même
     * transaction, pour voir que le comportement est identique.
     */
    @Transactional(readOnly = true)
    public long dbWithQuery(int ms) {
        long count = items.count();
        if (ms > 0) {
            db(ms); // même transaction : la connexion reste tenue
        }
        return count;
    }
}
