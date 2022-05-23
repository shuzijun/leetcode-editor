import  * as core from "@actions/core"

export const GH_USERNAME: string = core.getInput("GH_USERNAME");
export const COMMIT_MESSAGE: string = core.getInput("COMMIT_MESSAGE");
export const COMMIT_EMAIL: string = core.getInput("COMMIT_EMAIL");
export const COMMIT_NAME: string = core.getInput("COMMIT_NAME");
export const BADGES_FILE: string = core.getInput("BADGES_FILE");
export const START_SECTION_FLAG: string = core.getInput("START_SECTION_FLAG");
export const END_SECTION_FLAG: string = core.getInput("END_SECTION_FLAG");
export const STATISTICS_DIRECTORY: string = core.getInput("STATISTICS_DIRECTORY");
export const LEETCODE_SITE: string = core.getInput("LEETCODE_SITE");


