import * as core from '@actions/core'

import {replaceFile} from "./replace"
import {commitFile} from "./commit_file"
import {createBadges} from './badges'


async function run(): Promise<void> {
    try {
        let badges = await createBadges();
        await replaceFile(badges)
        await commitFile()
    } catch (error) {
        if (error instanceof Error) core.setFailed(error.message)
    }
}

run()
