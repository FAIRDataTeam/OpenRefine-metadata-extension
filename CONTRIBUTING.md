# Contributing

When contributing to this repository, please first discuss the change you wish to make via issue,
email, or any other method with the owners of this repository before making a change. 

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Pull Request Process

1. Ensure any unnecessary install or build dependencies and other files are removed before the end of 
   the layer when doing a build.
2. Explain the changes and update the README.md file and other documentation if necessary.
3. Be ready to communicate about the Pull Request and make changes if required by reviewers.
4. The Pull Request may be merged once it passes the review and automatic checks.

## Gitflow Workflow

We use the standard [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow):
*  __master__ branch is used only for releases (and eventually hotfixes), this branch is also protected on 
   GitHub (pull requests with review and all checks must pass)
*  __develop__ branch is used for development and as a base for following development branches of features, 
   support stuff, and as a base for releases
*  __feature/*__ (base develop, squash-merged back to develop when done)
*  __support/*__ (like the feature but semantically different, not the feature but some chore, e.g., cleanup or 
   update of Dockerfile)
*  __release/*__ (base develop, merged to master and develop when ready for release+tag)
*  __hotfix/*__ (base master, merged to master and develop)

Please note, that for tasks from [our Jira](https://dtl-fair.atlassian.net/projects/FOR/issues), we use 
such as `[FOR-XX]` identifying the project and task number.

## Release Management

For the release management we use (aside from the [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)):

*  [Semantic versioning](https://semver.org)
*  Release Candidates - X.Y.Z-rc.N should be created if don’t expect any problems (in that case use alpha or 
   beta), and make a walkthrough to verify its functionality according to the manuals finally - it also verifies that the documentation is up to date with the new version.
*  [CHANGELOG.md](https://keepachangelog.com/en/1.0.0/ )
*  GitHub releases and tags - make the release using GitHub (or hub extension), Travis CI will automatically upload 
   ZIP and TGZ distribution files there - better verify.
*  Docker Hub image - in case of release, Docker image with the same tag will be created automatically.

Also, never forget to update the joint [FAIR Data Point documentation](https://github.com/FAIRDataTeam/FAIRDataPoint-Docs)!

## New OpenRefine Versions

During the life-cycle of this extension, the OpenRefine will evolve in parallel. To handle this correctly, in case
of new OpenRefine version, we use this process:

1. Start new support branch when necessary changes are made to use the newer version of OpenRefine. Don’t forget to 
   update `project-repository`, assembly, documentation, and `Dockerfile`. Then merge to develop.
2. Create a new release with higher version according to OpenRefine version and semantic versioning (major breaks 
   compatibility, minor with new features, patch just hotfixes).
3. Filter out unfinished work that is no longer needed because of new OpenRefine (if any).
4. Update all other branches.
5. From now on, the newer version of OpenRefine is supported. For future releases, you should indicate if it works also 
   with the previous or not. In case of need, a new branch __legacy/OpenRefine-X.Y__ can be created for hotfixes.

_NOTE_: This still needs to be verified by practice!
