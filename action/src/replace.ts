import {BADGES_FILE, START_SECTION_FLAG, END_SECTION_FLAG} from "./config";

import fs from "fs";


export const replaceFile = async (badges: string) => {

    const readmeContent: string = fs.readFileSync(BADGES_FILE, "utf-8");

    let startIdx = readmeContent.indexOf(START_SECTION_FLAG)

    if (startIdx < 0) {
        throw new Error(`Couldn't find the ${START_SECTION_FLAG}`);
    }

    const endIdx = readmeContent.indexOf(END_SECTION_FLAG, startIdx);

    if (endIdx < 0) {
        throw new Error(`Starting from the ${startIdx} Couldn't find the ${END_SECTION_FLAG}`);
    }

    let readmeContents: string[] = [readmeContent.substring(0, startIdx + START_SECTION_FLAG.length), '\n', badges, readmeContent.substring(endIdx)]

    fs.writeFileSync(BADGES_FILE, readmeContents.join(""));


}
