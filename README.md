
# Monitor artifact publication

> Monitor artifact publication to Maven Central

<p align="center"><img src="https://design.jboss.org/quarkus/bot/final/images/quarkusbot_full.svg" width="128" height="128" /></p>

This action is used to monitor the publication of a version of an artifact to Maven Central.
Once the artifact is released, it (optionally) posts a comment to the issue provided as an input.

It is developed in Quarkus using the [Quarkus GitHub Action](https://github.com/quarkiverse/quarkus-github-action/) extension.

:warning: This action is automatically published when the `main` branch is pushed.

## The default action

### Example

```yaml
- name: Monitor artifact publication
  uses: quarkusio/monitor-artifact-publication-action@main
  with:
    github-token: ${{ secrets.GITHUB_TOKEN }}
    group-id: io.quarkus
    artifact-id: quarkus-core
    version: 3.6.4
    initial-delay: 15
    poll-delay: 5
    poll-iterations: 4
    post-delay: 5
    # Optional (used in Quarkus release process to notify on the release issue)
    issue-number: 114
    message-if-published: Artifact has been published
    message-if-not-published: Artifact had not been published at the end of the delay
```

### Inputs

| Name   | Description  |
|---|---|
| `group-id` | Group id of the artifact |
| `artifact-id` | Artifact id of the artifact |
| `version` | Version of the artifact |
| `initial-delay` | Initial delay in minutes before testing for the first time (use 0 to disable) |
| `poll-delay` | Poll delay in minutes |
| `poll-iterations` | Number of polling iterations |
| `post-delay` | Delay in minutes to wait after this particular artifact is published (use 0 to disable) |
| `issue-number` (optional) | Issue number to post to |
| `message-if-published` (optional) | Message to post if artifact is published |
| `message-if-not-published` (optional) | Message to post if artifact is not published |

### Outputs

| Name   | Description  |
|---|---|
| `published` | If the artifact has been published at the end of the action |
