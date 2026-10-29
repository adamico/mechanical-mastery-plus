# Releases

A release reaches Consumers through the local maven repository (`~/.m2`) at a version of its own. Consumers read the Module from there by version, and a Consumer that pins the jar pins its sha256. That only works if the jar published at a version is the one jar that version ever names.

A Module that names its Modrinth and CurseForge projects in `gradle.properties` also uploads a released version there, from the maintainer's machine (below); one that names none, like Voidworks, publishes nowhere else. CI builds and tests on every push but never publishes.

## The changelog

Each change a Consumer can use or will notice adds its line under `## Unreleased` in `CHANGELOG.md` when it lands, with its issue number, written for a Consumer's author in the glossary's terms. A Module whose changelog has other readers too, such as the players who read a release's notes on Modrinth and CurseForge, names them here, and how it writes for them. A release ships what Unreleased lists, so the changelog is written as the work is done and never reconstructed from commits.

## Cutting a release

`sh ./gradlew release -PreleaseVersion=<version>` from a clean main; the libworks plugin bundles the script it runs (libworks ADR 0003), tags `<archives_name>-v<version>` unless `libworks { tagPrefix }` says otherwise, and refuses to run under `-PsiblingBuilds`. ADR 0001 sets the semver: below 1.0 only a breaking change bumps the minor version, and an addition or a fix is the next patch. The script refuses a version that is already tagged or in `~/.m2`, and an empty Unreleased. It then:

1. sets `mod_version` and turns `## Unreleased` into `## <version>` under a fresh, empty Unreleased
2. runs the build and the game tests, putting both files back if either fails
3. commits `chore: release <version>`, runs `publishToMavenLocal`, and tags the release with the jar's sha256
4. with `-Pupload`, uploads the jar to Modrinth and CurseForge with the `upload` task (below), or says it skipped the upload when `gradle.properties` names neither project. A failed upload leaves the local release and the tag in place; the script names the site that failed, and `sh ./gradlew upload -PreleaseVersion=<version> -Psite=<site>` retries it. Without `-Pupload` it stops after the tag and prints the upload command, since an upload is public and for good and waits on the user's word; the release train runs it once the user says to push. Under `MAVEN_REPO_LOCAL`, a trial run, the upload is only a dry run.

It pushes nothing to git, and ends by printing the push command. To try a release out, set `MAVEN_REPO_LOCAL` to a scratch folder and run it in a throwaway clone: a version published to the real `~/.m2` is permanent.

A release that must reach a Consumer in another checkout, a Module or a pack that pins the jar, follows the `release-train` skill.

## Uploading to Modrinth and CurseForge

`sh ./gradlew upload -PreleaseVersion=<version>` uploads a version already in `~/.m2` to each site whose project `gradle.properties` names: the jar there, byte for byte, with that version's changelog section as its notes, for Minecraft `minecraft_version` on NeoForge, on client and server, as the release type `upload_release_type` names (`release`, `beta` or `alpha`) or, left empty, as beta below 1.0 and release from it. An unknown type is refused before either site is contacted, and `-PdryRun` shows the type it would send.

- `modrinth_project_id` and `curseforge_project_id` are the projects. A new Module leaves them empty until its projects exist on the sites (creating them: `docs/agents/publishing.md`), and never borrows another mod's; `MODRINTH_PROJECT_ID` and `CURSEFORGE_PROJECT_ID` override them. With neither set, the upload refuses and `release` skips it.
- `modrinth_dependencies` (Modrinth project ids) and `curseforge_dependencies` (CurseForge slugs) are the required dependencies, comma separated.

Each site's token comes from the environment and is never printed. The tokens live in 1Password, and `publish/upload.env` names them there; when a token is missing the upload runs itself again through `op run --env-file=publish/upload.env`, which fills them in for that run only. So `release -Pupload` and `upload` need nothing exported; 1Password asks to be unlocked. A token belongs to the account, not a project, so every Module uses Beltworks' items:

- Modrinth: `MODRINTH_TOKEN`, the 1Password item "Beltworks Modrinth": a personal access token with the scopes Create versions, Read versions and Read projects.
- CurseForge: `CURSEFORGE_TOKEN`, the 1Password item "Beltworks CurseForge": an upload API token. The upload API can't list a project's files, so the check for a version CurseForge already has reads the website's own listing (`www.curseforge.com/api/v1/mods/<id>/files`), which needs no key but is undocumented: if it changes, that check fails and the upload stops. It doesn't show a file still under CurseForge's review, so the upload records each upload a site takes beside the jar, in `<archives_name>-<version>.uploads.json` with the site's id for it and when, and refuses a recorded site before contacting it. The record only knows this machine's uploads; another machine's are left to each site's own check. CurseForge needs the environments named too, Client and Server, or it refuses the file.

It refuses, before contacting either site, a version missing from `~/.m2` or from the changelog, and a jar lacking the licensing `checkJarLicensing` requires (`LICENSE`, `NOTICE` if present and every file under `LICENSES/`, which the build puts in every jar). Each site then goes on its own: a site that already has the version, or whose upload fails, is refused without touching the other, and `-Psite=modrinth` or `-Psite=curseforge` retries just that one, and only a site the run said failed. `-PdryRun` prints the requests and what the record holds, and contacts nothing. `MODRINTH_API_URL`, `CURSEFORGE_UPLOAD_URL` and `CURSEFORGE_API_URL` (the listing's site) point it elsewhere, and libworks' tests (`python3 -m unittest discover scripts/tests`) run it against a stand-in server on localhost.

## A published version is final

A version in `~/.m2` never changes. A fix is the next patch version. `publishToMavenLocal` refuses a version that is already there (the libworks plugin), and nothing is deleted or overwritten by hand to get past it. The same holds on Modrinth and CurseForge: the upload refuses a version a site already has, and nothing there is deleted or replaced. The upload record beside the jar is kept with it: a site's entry is removed by hand only when that site rejected the file or never showed it, which is the one way the same version goes to it again.

## Tags

A release is tagged `<archives_name>-v<version>` (the plugin's `tagPrefix`), annotated with its jar's sha256. `git tag -l '*v*' -n9` lists them.
