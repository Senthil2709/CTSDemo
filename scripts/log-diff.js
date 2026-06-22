/**
 * log-diff.js
 *
 * Runs inside a GitHub Actions job (on a self-hosted runner) triggered by
 * a `push` event. Captures the diff introduced by the push and writes it
 * to a timestamped log file, plus prints it to the console.
 *
 * Required env vars (set automatically by the workflow):
 *   BEFORE_SHA - github.event.before
 *   AFTER_SHA  - github.event.after
 *   REPO_NAME  - github.repository
 *   PUSHER     - github.actor
 *   REF        - github.ref
 *
 * Optional:
 *   LOG_DIR    - absolute path to write logs to (defaults to ./push-logs
 *                inside the repo workspace)
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const ZERO_SHA = '0000000000000000000000000000000000000000';

const beforeSha = process.env.BEFORE_SHA || '';
const afterSha = process.env.AFTER_SHA || '';
const repo = process.env.REPO_NAME || 'unknown-repo';
const pusher = process.env.PUSHER || 'unknown-user';
const ref = process.env.REF || 'unknown-ref';

const LOG_DIR = process.env.LOG_DIR
  ? process.env.LOG_DIR
  : path.join(process.cwd(), 'push-logs');

if (!fs.existsSync(LOG_DIR)) {
  fs.mkdirSync(LOG_DIR, { recursive: true });
}

function isRealSha(sha) {
  return /^[0-9a-f]{40}$/i.test(sha) && sha !== ZERO_SHA;
}

function run(cmd) {
  return execSync(cmd, { maxBuffer: 1024 * 1024 * 100 }).toString();
}

let diffOutput;
let mode;

try {
  if (isRealSha(beforeSha) && isRealSha(afterSha)) {
    // Normal push: diff everything introduced between the two SHAs
    mode = 'range-diff';
    diffOutput = run(`git diff ${beforeSha} ${afterSha}`);
  } else if (!isRealSha(beforeSha) && isRealSha(afterSha)) {
    // New branch being pushed for the first time: show the tip commit
    mode = 'new-branch (showing latest commit)';
    diffOutput = run(`git show ${afterSha}`);
  } else if (isRealSha(beforeSha) && !isRealSha(afterSha)) {
    // Branch deletion: nothing to diff
    mode = 'branch-deleted';
    diffOutput = '(branch was deleted, no diff to show)';
  } else {
    mode = 'unknown';
    diffOutput = '(could not determine before/after SHAs)';
  }
} catch (err) {
  mode = 'error';
  diffOutput = `Error generating diff: ${err.message}`;
}

const timestamp = new Date().toISOString();
const fileSafeTimestamp = timestamp.replace(/[:.]/g, '-');
const shortSha = (afterSha || 'unknown').slice(0, 7);
const logFile = path.join(LOG_DIR, `${fileSafeTimestamp}-${shortSha}.log`);

const header =
  `Repo:      ${repo}\n` +
  `Branch:    ${ref}\n` +
  `Pusher:    ${pusher}\n` +
  `Before:    ${beforeSha}\n` +
  `After:     ${afterSha}\n` +
  `Mode:      ${mode}\n` +
  `Timestamp: ${timestamp}\n` +
  `${'='.repeat(60)}\n\n`;

fs.writeFileSync(logFile, header + diffOutput);

console.log(header);
console.log(diffOutput);
console.log(`\n✔ Diff logged to: ${logFile}`);
