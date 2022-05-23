import * as process from 'process'
import * as cp from 'child_process'
import * as path from 'path'
import {expect, test} from '@jest/globals'
import  convert from  'xml-js'


// shows how the runner will run a javascript action with env / stdout protocol
test('test runs', () => {
  process.env['INPUT_MILLISECONDS'] = '500'
  const np = process.execPath
  const ip = path.join(__dirname, '..', 'lib', 'main.js')
  const options: cp.ExecFileSyncOptions = {
    env: process.env
  }
  console.log(cp.execFileSync(np, [ip], options).toString())
})

test('xml2json',() => {
  var xml =
      '<?xml version="1.0" encoding="UTF-8"?>\n' +
      '<project version="4">\n' +
      '  <component name="LeetcodeEditorStatistics">\n' +
      '    <option name="statistics">\n' +
      '      <map>\n' +
      '        <entry key="leetcode.cn">\n' +
      '          <value>\n' +
      '            <Statistics>\n' +
      '              <option name="easy" value="57" />\n' +
      '              <option name="hard" value="12" />\n' +
      '              <option name="medium" value="106" />\n' +
      '              <option name="questionTotal" value="2643" />\n' +
      '              <option name="solvedTotal" value="175" />\n' +
      '            </Statistics>\n' +
      '          </value>\n' +
      '        </entry>\n' +
      '      </map>\n' +
      '    </option>\n' +
      '  </component>\n' +
      '</project>';
  var result1 = convert.xml2json(xml, {compact: true});
  console.log(JSON.parse(result1).project.component.option.map.entry.value.Statistics.option);
})