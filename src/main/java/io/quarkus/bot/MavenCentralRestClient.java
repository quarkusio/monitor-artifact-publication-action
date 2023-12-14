package io.quarkus.bot;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://repo1.maven.org/maven2/")
public interface MavenCentralRestClient {

    @GET
    @Path("/{gav:.+}/")
    String getGAVDirectoryListing(@PathParam("gav") GAV gav);

    static class GAV {

        private final String groupId;
        private final String artifactId;
        private final String version;

        private GAV(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public static GAV of(String groupId, String artifactId, String version) {
            return new GAV(groupId, artifactId, version);
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getPom() {
            return artifactId + "-" + version + ".pom";
        }

        @Override
        public String toString() {
            return groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        }
    }
}