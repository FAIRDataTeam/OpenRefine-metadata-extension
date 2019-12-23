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
*  __develop__ branch is used for development and as base for following development branches of features, 
   support stuff, and as base for releases
*  __feature/*__ (base develop, squash-merged back to develop when done)
*  __support/*__ (like feature but semantically different, not feature but some chore, e.g., cleanup or 
   update of Dockerfile)
*  __release/*__ (base develop, merged to master and develop when ready for release+tag)
*  __hotfix/*__ (base master, merged to master and develop)

Please note, that for tasks from [our Jira](https://dtl-fair.atlassian.net/projects/FOR/issues), we use 
such as `[FOR-XX]` identifying the project and task number.

## Release Management

For the release management we use (aside the [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)):

*  [Semantic versioning](https://semver.org)
*  Release Candidates - X.Y.Z-rc.N should be created if don’t expect any problems (in that case use alpha or 
   beta), and make a walkthrough to finally verify its functionality according to the manuals - it also verifies 
   that the documentation is up to date with the new version.
*  [CHANGELOG.md](https://keepachangelog.com/en/1.0.0/ )
*  GitHub releases and tags - make the release using GitHub (or hub extension), Travis CI will automatically upload 
   ZIP and TGZ distribution files there - better verify.
*  Docker Hub image - in case of release, Docker image with the same tag will be created automatically.

Also, never forget to update the joint [FAIR Data Point documentation](https://github.com/FAIRDataTeam/FAIRDataPoint-Docs)!

## New OpenRefine Versions

During the life-cycle of this extension, the OpenRefine will evolve in parallel. To handle this correctly, in case
of new OpenRefine version, we use this process:

1. Start new support branch when necessary changes will be made to use newer version of OpenRefine. Don’t forget to 
   update `project-repository`, assembly, documentation, and `Dockerfile`. Then merge to develop.
2. Create a new release with higher version according to OpenRefine version and semantic versioning (major breaks 
   compatibility, minor with new features, patch just hotfixes).
3. Filter out unfinished work that is no longer needed because of new OpenRefine (if any).
4. Update all other branches.
5. From now on, the newer version of OpenRefine is supported. For future releases, you should indicate if it works also 
   with the previous or not. In case of need, a new branch __legacy/OpenRefine-X.Y__ can be created for hotfixes.

_NOTE_: This still needs to be verified by practice!

## Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as
contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body
size, disability, ethnicity, gender identity and expression, level of experience,
nationality, personal appearance, race, religion, or sexual identity and
orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment
include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or
advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
  address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a
  professional setting

### Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable
behavior and are expected to take appropriate and fair corrective action in
response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community. Examples of
representing a project or community include using an official project e-mail
address, posting via an official social media account, or acting as an appointed
representative at an online or offline event. Representation of a project may be
further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting the project team. All complaints will be reviewed and 
investigated and will result in a response that is deemed necessary and appropriate 
to the circumstances. The project team is obligated to maintain confidentiality 
with regard to the reporter of an incident. Further details of specific enforcement 
policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good
faith may face temporary or permanent repercussions as determined by other
members of the project's leadership.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant][homepage], version 1.4,
available at [http://contributor-covenant.org/version/1/4][version]

[homepage]: http://contributor-covenant.org
[version]: http://contributor-covenant.org/version/1/4/
