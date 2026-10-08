#!/usr/bin/env node
// Sign an APK using the existing TubePreview identity stored in macOS Keychain.
// This tool never creates or replaces a signing identity.
import { spawnSync } from 'node:child_process';
import { chmodSync, mkdtempSync, writeFileSync, rmSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { homedir, tmpdir } from 'node:os';

const service = 'com.renflare.tubepreview.release-signing';
const account = 'renflare';
const expectedCertificate = 'e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3';
const args = process.argv.slice(2);
const exporting = args[0] === '--export-keystore';

function run(file, parameters, options = {}) {
  const result = spawnSync(file, parameters, {
    encoding: 'utf8', timeout: 60000, ...options,
  });
  if (result.status !== 0) {
    throw new Error(file + ' failed. Check installation, keychain access, and input paths.');
  }
  return result.stdout;
}

function readRecord(name) {
  return run('/usr/bin/security', [
    'find-generic-password', '-a', account, '-s', name, '-w',
  ]).trim();
}

let scratch;
try {
  if (process.platform !== 'darwin') throw new Error('This signing helper requires macOS Keychain.');
  if (args.length !== 2) {
    throw new Error('Usage: node scripts/sign-release.mjs <unsigned.apk> <signed.apk>\nOr: node scripts/sign-release.mjs --export-keystore <backup.p12>');
  }
  let identity;
  try {
    identity = JSON.parse(readRecord(service));
  } catch {
    throw new Error('Cannot read the signing record. Check Keychain access; do not replace the key.');
  }
  if (identity.version !== 2 || identity.alias !== 'upload' ||
      !Number.isInteger(identity.parts) || identity.parts < 1 || identity.parts > 20 ||
      !/^[A-Za-z0-9_-]{64}$/.test(identity.password)) {
    throw new Error('Unexpected signing record. Do not generate a replacement key.');
  }
  let encodedKey = '';
  for (let i = 0; i < identity.parts; i++) {
    const part = readRecord(service + '.part' + i);
    if (!/^[A-Za-z0-9+/=]+$/.test(part)) throw new Error('Invalid keystore record.');
    encodedKey += part;
  }
  const keystore = Buffer.from(encodedKey, 'base64');
  const javaHome = process.env.JAVA_HOME ||
    '/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home';
  scratch = mkdtempSync(join(tmpdir(), 'tubepreview-signing-'));
  chmodSync(scratch, 0o700);
  const keyPath = join(scratch, 'release.p12');
  writeFileSync(keyPath, keystore, { mode: 0o600, flag: 'wx' });
  const env = { ...process.env, JAVA_HOME: javaHome,
    TUBEPREVIEW_SIGNING_PASSWORD: identity.password };
  const certificate = run(join(javaHome, 'bin', 'keytool'), [
    '-list', '-v', '-keystore', keyPath, '-storetype', 'PKCS12',
    '-alias', identity.alias, '-storepass:env', 'TUBEPREVIEW_SIGNING_PASSWORD',
  ], { env });
  if (!certificate.toLowerCase().replaceAll(':', '').includes(expectedCertificate)) {
    throw new Error('Keystore differs from the published certificate. Do not replace the signing key.');
  }
  if (exporting) {
    // The PKCS12 output remains encrypted with the password retained in Keychain.
    writeFileSync(args[1], keystore, { mode: 0o600, flag: 'wx' });
    console.log('Encrypted keystore exported. Back up its password separately using Keychain Access.');
  } else {
    if (!existsSync(args[0])) throw new Error('Unsigned APK not found.');
    if (existsSync(args[1])) throw new Error('Output already exists; choose a new output path.');
    const sdk = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT ||
      join(homedir(), 'Library', 'Android', 'sdk');
    const apksigner = process.env.APKSIGNER_PATH ||
      join(sdk, 'build-tools', '37.0.0', 'apksigner');
    run(apksigner, [
      'sign', '--ks', keyPath, '--ks-key-alias', identity.alias,
      '--ks-pass', 'env:TUBEPREVIEW_SIGNING_PASSWORD',
      '--key-pass', 'env:TUBEPREVIEW_SIGNING_PASSWORD',
      '--out', args[1], args[0],
    ], { env });
    const verification = run(apksigner, ['verify', '--verbose', '--print-certs', args[1]], { env });
    if (!verification.includes('certificate SHA-256 digest: ' + expectedCertificate)) {
      throw new Error('Signing certificate differs from the published release. Do not distribute this APK.');
    }
    console.log(verification);
    console.log('Signed APK verified with the published TubePreview certificate.');
  }
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
} finally {
  if (scratch) rmSync(scratch, { recursive: true, force: true });
}
