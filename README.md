
# Monitor artifact publication

> Monitor artifact publication to Maven Central

This action is used to monitor the publication of a version of an artifact to Maven Central.
Once the artifact is released, it posts a comment to the issue provided as an input.

It is developed in Quarkus using the [Quarkus GitHub Action](https://github.com/quarkiverse/quarkus-github-action/) extension.

:warning: This action is automatically published when the `main` branch is pushed.

## The default action

### Example

```yaml
- name: Monitor artifact publication
  uses: quarkusio/monitor-artifact-publication-action@main
  with:
    github-token: ${{ secrets.RELEASE_GITHUB_TOKEN }}
    group-id: ${{ github.event.inputs.group-id }}
    artifact-id: ${{ github.event.inputs.artifact-id }}
    version: ${{ github.event.inputs.version }}
    issue-number: ${{ github.event.inputs.issue-number }}
    message-if-published: ${{ github.event.inputs.message-if-published }}
    message-if-not-published: ${{ github.event.inputs.message-if-not-published }}
    initial-delay: ${{ github.event.inputs.initial-delay }}
    poll-delay: ${{ github.event.inputs.poll-delay }}
    poll-iterations: ${{ github.event.inputs.poll-iterations }}
    post-delay: ${{ github.event.inputs.post-delay }}
```

### Inputs

| Name   | Description  |
|---|---|
| `group-id` | Group id of the artifact |
| `artifact-id` | Artifact id of the artifact |
| `version` | Version of the artifact |
| `issue-number` | Issue number to post to |
| `message-if-published` | Message to post if artifact is published |
| `message-if-not-published` | Message to post if artifact is not published |
| `initial-delay` | Initial delay in minutes before testing for the first time |
| `poll-delay` | Poll delay in minutes |
| `poll-iterations` | Number of polling iterations |
| `post-delay` | Delay in minutes to wait after this particular artifact is published |

### Outputs

| Name   | Description  |
|---|---|
| `published` | If the artifact has been published at the end of the action |
