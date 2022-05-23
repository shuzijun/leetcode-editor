import {STATISTICS_DIRECTORY, LEETCODE_SITE} from "./config";

import fs from "fs";
import convert from 'xml-js'

export const createBadges = async (): Promise<string> => {

    const statisticsXml: string = fs.readFileSync(STATISTICS_DIRECTORY + 'statistics.xml', "utf-8");
    const entry = JSON.parse(convert.xml2json(statisticsXml, {compact: true})).project.component.option.map.entry;
    let statistics;
    if (Array.isArray(entry)) {
        for (const item of entry) {
            if (item._attributes.key === LEETCODE_SITE) {
                statistics = item;
                break
            }
        }
    } else {
        if (entry._attributes.key === LEETCODE_SITE) {
            statistics = entry;
        }
    }
    if (statistics === null) {
        throw new Error("No matching site data")
    }
    let value = new Map<string, number>();
    for (const item of statistics.value.Statistics.option) {
        value.set(item._attributes.name, item._attributes.value);
    }

    return `![Progress](https://img.shields.io/static/v1?logo=leetcode&label=Progress&message=${value.get('solvedTotal')}%2F${value.get('questionTotal')}&color=brightgreen)  ` +
        `![Easy](https://img.shields.io/static/v1?logo=leetcode&label=Easy&message=${value.get('easy')}&color=5CB85C)  ` +
        `![Medium](https://img.shields.io/static/v1?logo=leetcode&label=Medium&message=${value.get('hard')}&color=F0AD4E)  ` +
        `![Hard](https://img.shields.io/static/v1?logo=leetcode&label=Hard&message=${value.get('medium')}&color=D9534F)  `;
};
