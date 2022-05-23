import {COMMIT_EMAIL, COMMIT_NAME, COMMIT_MESSAGE, BADGES_FILE} from "./config"
import {exec} from "./execute"

export const commitFile = async () => {
    await exec("git", ["config", "--global", "user.email", COMMIT_EMAIL]);
    await exec("git", ["config", "--global", "user.name", COMMIT_NAME]);
    await exec("git", ["add", BADGES_FILE]);
    await exec("git", ["commit", "-m", COMMIT_MESSAGE]);
    await exec("git", ["push"]);
};
