import {spawn} from "child_process"

export async function exec(cmd: string, args: string[] = []): Promise<string> {
    return new Promise((resolve, reject) => {
        const app = spawn(cmd, args, {stdio: "pipe"});
        let stdout = "";
        app.stdout.on("data", (data: string) => {
            stdout = data;
        });
        app.on("close", (code: number) => {
            if (code !== 0 && !stdout.includes("nothing to commit")) {
                let err = new Error(`Invalid status code: ${code}`);
                return reject(err);
            }
            return resolve(code.toString());
        });
        app.on("error", reject);
    })
}