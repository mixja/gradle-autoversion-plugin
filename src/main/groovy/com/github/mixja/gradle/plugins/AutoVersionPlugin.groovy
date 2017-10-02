package com.github.mixja.gradle.plugins

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AutoVersionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.add("autoVersion", AutoVersionPluginExtension)
        LocalDate latestCommitDate
        LocalDate month
        Repository repository
        String branch = "master"
        String patchVersion = "0"
        String version
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        builder.setMustExist(true)
        builder.setGitDir(new File(".git"))
        // Load local repository and get latest commit date
        try {
            repository = builder.build()
            branch = repository.getBranch()
            Ref head = repository.exactRef("HEAD")
            RevWalk walk = new RevWalk(repository)
            RevCommit latestCommit = walk.parseCommit(head.getObjectId());
            latestCommitDate = Instant.ofEpochSecond(latestCommit.getCommitTime())
                    .atZone(ZoneId.of("UTC")).toLocalDate()
            walk.dispose()

            // Get month of latest commit
            month = latestCommitDate.withDayOfMonth(1)

            // Get count of commits since beginning of the month
            Iterable<RevCommit> commits = new Git(repository).log().call()
            Integer count = 0
            for (commit in commits) {
                LocalDate commitDate = Instant.ofEpochSecond(commit.getCommitTime())
                        .atZone(ZoneId.of("UTC")).toLocalDate()
                if (commitDate < month) {
                    break
                } else {
                    count++
                }
            }
            patchVersion = String.valueOf(count)
        } catch (RepositoryNotFoundException e) {
            latestCommitDate = LocalDate.now(ZoneId.of("UTC"))
            month = latestCommitDate.withDayOfMonth(1)
        }

        // Build version
        String baseVersion = month.format(DateTimeFormatter.ofPattern("YY.MM"))
        String buildVersion = project.autoVersion.buildVersion ? ".${project.autoVersion.buildVersion}" : ""
        String branchVersion = branch != project.autoVersion.defaultBranch ? ".${branch}" : ""
        version = "${baseVersion}.${patchVersion}${branchVersion}${buildVersion}"

        // Set version
        project.setVersion(version)
        project.getAllprojects().forEach { p -> p.setVersion(version) }

        // Version task
        project.task('version') {
            doLast {
                println version
            }
        }
    }
}

class AutoVersionPluginExtension {
    String buildVersion = System.getenv("BUILD_ID")
    String defaultBranch = "master"
}
