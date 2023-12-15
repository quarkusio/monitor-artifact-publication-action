package io.quarkus.bot;

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

        wait(commands, inputs.getInteger(InputKeys.INITIAL_DELAY).getAsInt());

        if (isGAVPublished(gav)) {
            handlePublished(context, commands, inputs, gitHub, gav);
            return;
        }

        for (int i = 0; i < inputs.getInteger(InputKeys.POLL_ITERATIONS).getAsInt(); i++) {
            wait(commands, inputs.getInteger(InputKeys.POLL_DELAY).getAsInt());

            if (isGAVPublished(gav)) {
                handlePublished(context, commands, inputs, gitHub, gav);
                return;
            }
        }

        commands.setOutput(OutputKeys.PUBLISHED, "false");
        postMessage(context, commands, gitHub, inputs, false);
    }

    private void handlePublished(Context context, Commands commands, Inputs inputs, GitHub gitHub, GAV gav) {
        commands.notice("Artifact " + gav.toCoordinates() + " published, waiting for an additional "
                + inputs.getInteger(InputKeys.POST_DELAY).getAsInt() + " mn to let all artifacts to be published");

        commands.setOutput(OutputKeys.PUBLISHED, "true");
        wait(commands, inputs.getInteger(InputKeys.POST_DELAY).getAsInt());
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
        int issueNumber = inputs.getInteger(InputKeys.ISSUE_NUMBER).getAsInt();

        try {
            GHIssue issue = gitHub.getRepository(context.getGitHubRepositoryOwner() + "/" + context.getGitHubRepository())
                    .getIssue(issueNumber);
            issue.comment(published ? inputs.getRequired(InputKeys.MESSAGE_IF_PUBLISHED)
                    : inputs.getRequired(InputKeys.MESSAGE_IF_NOT_PUBLISHED));
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