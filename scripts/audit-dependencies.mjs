import fs from 'node:fs/promises';
import path from 'node:path';

// Input is Gradle's resolved dependency inventory, not declarations in a build file.
// A network error or incomplete scan must fail CI rather than report a clean result.
const input = process.argv[2] ?? 'build/security/dependencies.json';
const output = process.argv[3] ?? 'build/security/osv.json';
const entries = JSON.parse(await fs.readFile(input, 'utf8'));
if (!Array.isArray(entries) || entries.length === 0) throw new Error('Empty dependency inventory');
const unique = new Map();
for (const entry of entries) {
  if (typeof entry.name !== 'string' || typeof entry.version !== 'string' ||
      typeof entry.scope !== 'string') throw new Error('Invalid dependency inventory');
  const key = entry.name + '@' + entry.version;
  const item = unique.get(key) ?? { name: entry.name, version: entry.version, scopes: [] };
  if (!item.scopes.includes(entry.scope)) item.scopes.push(entry.scope);
  unique.set(key, item);
}
const packages = [...unique.values()];
const findings = [];
for (let offset = 0; offset < packages.length; offset += 100) {
  const batch = packages.slice(offset, offset + 100);
  const queries = batch.map(p => ({ package: { ecosystem: 'Maven', name: p.name }, version: p.version }));
  const response = await fetch('https://api.osv.dev/v1/querybatch', {
    method: 'POST', headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ queries }), signal: AbortSignal.timeout(25000)
  });
  if (!response.ok) throw new Error('OSV request failed: ' + response.status);
  const data = await response.json();
  if (!Array.isArray(data.results) || data.results.length !== batch.length) {
    throw new Error('Incomplete OSV response');
  }
  for (let i = 0; i < batch.length; i++) {
    if (data.results[i].next_page_token) throw new Error('Incomplete paginated OSV result');
    if (data.results[i].vulns?.length) findings.push({ ...batch[i], vulnerabilities: data.results[i].vulns });
  }
}
const result = { checkedAt: new Date().toISOString(), packageCount: packages.length, findings };
await fs.mkdir(path.dirname(output), { recursive: true });
await fs.writeFile(output, JSON.stringify(result, null, 2) + '\n');
console.log(JSON.stringify(result, null, 2));
if (findings.length) process.exitCode = 1;
