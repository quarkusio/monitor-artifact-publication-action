package io.quarkus.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import io.quarkus.bot.MavenCentralRestClient.GAV;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MavenCentralRestClientTest {

    @Inject
    @RestClient
    MavenCentralRestClient mavenCentralRestClient;

    @Test
    void testExists() {
        GAV gav = GAV.of("io.quarkus", "quarkus-core", "3.6.3");

        assertThat(mavenCentralRestClient.getGAVDirectoryListing(gav))
                .contains(gav.getPom());
    }

    @Test
    void testDoesNotExist() {
        assertThatThrownBy(() -> {
            mavenCentralRestClient.getGAVDirectoryListing(GAV.of("io.quarkus", "quarkus-core", "totoz"));
        }).isInstanceOf(WebApplicationException.class)
                .hasMessageContaining("404");
    }
}
