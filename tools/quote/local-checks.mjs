// Requires an owner-provisioned disposable database. Never accepts a public/production database.
import {spawnSync} from 'node:child_process';
import assert from 'node:assert/strict';
import path from 'node:path';
const root=path.resolve(new URL('../../',import.meta.url).pathname);
assert.equal(process.env.CERTECH_SYNTHETIC_DB_ONLY,'YES','Explicit synthetic-database acknowledgement required');
assert.match(process.env.MAIL_TEST_DB_URL||'',/^jdbc:postgresql:\/\/127\.0\.0\.1:[0-9]+\/certech_mail_ci$/,'Disposable loopback database only');
assert.ok(process.env.MAIL_TEST_DB_PASSWORD,'Synthetic database password required');
function run(command,args,env=process.env){const r=spawnSync(command,args,{cwd:root,env,stdio:'inherit'});if(r.status!==0)throw new Error('Local checks failed');}
run('npm',['--prefix','frontend','test']);
run('npm',['--prefix','frontend','run','build']);
run(process.execPath,['frontend/scripts/verify-dist.mjs']);
const args=['backend/mvnw','-f','backend/pom.xml','-B','-ntp','test'];
if(process.env.QUOTE_TEST_MAVEN_REPO)args.push('-Dmaven.repo.local='+process.env.QUOTE_TEST_MAVEN_REPO);
if(process.env.QUOTE_TEST_OFFLINE==='true')args.push('-o');
run('sh',args,{...process.env,CONTACT_NOTIFICATIONS_ENABLED:'false',SMTP_HOST:'127.0.0.1',SMTP_USERNAME:'',SMTP_PASSWORD:''});
console.log('Local quote checks complete. No public SMTP or deployment was used.');
