package at.hypercrawler.frontierservice.frontier.domain.repository;

import at.hypercrawler.frontierservice.frontier.domain.model.Page;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.net.URL;

@Repository
public interface FrontierRepository
        extends ReactiveMongoRepository<Page, URL> {
}