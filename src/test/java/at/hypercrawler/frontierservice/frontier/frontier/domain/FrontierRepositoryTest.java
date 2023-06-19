package at.hypercrawler.frontierservice.frontier.frontier.domain;

import at.hypercrawler.frontierservice.frontier.domain.model.Page;
import at.hypercrawler.frontierservice.frontier.domain.repository.FrontierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@DataMongoTest
@Testcontainers
class FrontierRepositoryTest {
    @Container
    private static final MongoDBContainer mongoContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @Autowired
    private FrontierRepository frontierRepository;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Test
    void whenFindPageByIdWhenNotExisting_thenNoPageReturned() throws MalformedURLException {
        StepVerifier.create(frontierRepository.findById(new URL("http://gooiogle.com"))).expectNextCount(0).verifyComplete();
    }

    @Test
    void whenCreatePage_thenPageIsInDatabase() throws MalformedURLException {
        Page page = new Page(new URL("http://google.com"), 10, UUID.randomUUID());
        StepVerifier.create(frontierRepository.save(page)).expectNextMatches(
                c -> c.equals(page)).verifyComplete();
    }

}
