package com.pgeadas

class GitUtilities implements Serializable {

    private final def script

    GitUtilities(def script) {
        this.script = script
    }

    String getBranchDiff(branch, aux_branch) {
        this.script.sh(script: "git diff --name-only ${aux_branch}..${branch}", returnStdout: true)
    }

    String getCurrentBranchName() {
        this.script.sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
    }

    String commitMessage() {
        trimOutput("git log --format=%B -n 1 HEAD | head -n 1", 180)
    }

    String commitAuthor() {
        trimOutput("git log --format=\'%an\' -n 1 HEAD", 80)
    }

    String commitHash() {
        trimOutput("git rev-parse HEAD", 7)
    }

    String getVersion() {
        trimOutput("git rev-parse --short=8 HEAD", 8)
    }

    private String trimOutput(String script, int maxLength) {
        String content = this.script.sh(script: script, returnStdout: true)
        content.substring(0, Math.min(maxLength, content.length())).trim()
    }

    boolean isMasterBranch() {
        script.env.BRANCH_NAME == 'master'
    }

    boolean isPRBranch() {
        script.env.BRANCH_NAME.startsWith('PR')
    }

    boolean isStagingBranch() {
        script.env.BRANCH_NAME == 'staging'
    }

    boolean isDevelopmentBranch() {
        script.env.BRANCH_NAME == 'development'
    }

    boolean isPatchBranch() {
        script.env.BRANCH_NAME == 'patch'
    }

    boolean isPRTargetDevelopment() {
        script.env.CHANGE_TARGET == 'development'
    }

    boolean isPRTargetMaster() {
        script.env.CHANGE_TARGET == 'master'
    }

    String forcePushToBranch(from, to) {
        this.script.sh(script: "git push --force origin ${from}:${to}", returnStdout: true)
    }
}

