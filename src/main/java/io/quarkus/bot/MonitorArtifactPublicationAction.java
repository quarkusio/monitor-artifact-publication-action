package io.quarkus.bot;

import java.util.Optional;
import java.util.OptionalInt;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;

import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import io.quarkus.bot.MavenCentralRestClient.GAV;

public class MonitorArtifactPublicationAction {

    @Inject
    @RestClient
    MavenCentralRestClient mavenCentralRestClient;

    @Action
    void monitor(Context context, Commands commands, Inputs inputs, GitHub gitHub) {
        GAV gav = GAV.of(inputs.getRequired(InputKeys.GROUP_ID), inputs.getRequired(InputKeys.ARTIFACT_ID),
                inputs.getRequired(InputKeys.VERSION));

        commands.notice("Monitoring publication for artifact " + gav.toCoordinates());

        int initialDelay = inputs.getRequiredInt(InputKeys.INITIAL_DELAY);
        if (initialDelay > 0) {
            wait(commands, initialDelay);
        }

        if (isGAVPublished(gav)) {
            handlePublished(context, commands, inputs, gitHub, gav);
            return;
        }

        for (int i = 0; i < inputs.getRequiredInt(InputKeys.POLL_ITERATIONS); i++) {
            wait(commands, inputs.getRequiredInt(InputKeys.POLL_DELAY));

            if (isGAVPublished(gav)) {
                handlePublished(context, commands, inputs, gitHub, gav);
                return;
            }
        }

        commands.warning("Artifact " + gav.toCoordinates() + " has not been published to Maven Central in due time");
        commands.setOutput(OutputKeys.PUBLISHED, "false");
        postMessage(context, commands, gitHub, inputs, false);
    }

    private void handlePublished(Context context, Commands commands, Inputs inputs, GitHub gitHub, GAV gav) {
        commands.notice("Artifact " + gav.toCoordinates() + " published, waiting for an additional "
                + inputs.getRequiredInt(InputKeys.POST_DELAY) + " mn to give some time to the other artifacts");

        commands.setOutput(OutputKeys.PUBLISHED, "true");
        int postDelay = inputs.getRequiredInt(InputKeys.POST_DELAY);
        if (postDelay > 0) {
            wait(commands, postDelay);
        }
        postMessage(context, commands, gitHub, inputs, true);
    }

    private boolean isGAVPublished(GAV gav) {
        try {
            return mavenCentralRestClient.getGAVDirectoryListing(gav).contains(gav.getPom());
        } catch (Exception e) {
            return false;
        }
    }

    private static void postMessage(Context context, Commands commands, GitHub gitHub, Inputs inputs, boolean published) {
        OptionalInt issueNumber = inputs.getInt(InputKeys.ISSUE_NUMBER);
        if (issueNumber.isEmpty()) {
            return;
        }

        try {
            GHIssue issue = gitHub.getRepository(context.getGitHubRepository())
                    .getIssue(issueNumber.getAsInt());
            if (published) {
                Optional<String> messageIfPublished = inputs.get(InputKeys.MESSAGE_IF_PUBLISHED);
                if (messageIfPublished.isPresent()) {
                    issue.comment(messageIfPublished.get());
                }
            } else {
                Optional<String> messageIfNotPublished = inputs.get(InputKeys.MESSAGE_IF_NOT_PUBLISHED);
                if (messageIfNotPublished.isPresent()) {
                    issue.comment(messageIfNotPublished.get());
                }
            }
        } catch (Exception e) {
            commands.error("Unable to post message in issue " + issueNumber + ": " + e.getMessage());
            return;
        }
    }

    private static void wait(Commands commands, int delayInMinutes) {
        commands.notice("Start waiting for " + delayInMinutes + " mn");
        for (int i = 0; i < delayInMinutes; i++) {
            try {
                Thread.sleep(60_000);
            } catch (Exception e) {
                commands.warning("Wait interrupted: " + e.getMessage());
                return;
            }

            commands.notice("... waited for " + (i + 1) + " mn");
        }
    }
}