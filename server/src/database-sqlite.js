import Database from 'better-sqlite3';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dbDir = path.join(__dirname, '..', 'data');

if (!fs.existsSync(dbDir)) {
  fs.mkdirSync(dbDir, { recursive: true });
}

const dbPath = process.env.DB_PATH || path.join(dbDir, 'loveall.db');
const db = new Database(dbPath);

db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    birthdate TEXT NOT NULL,
    gender TEXT NOT NULL,
    gender_custom TEXT,
    looking_for TEXT NOT NULL DEFAULT '[]',
    bio TEXT DEFAULT '',
    photos TEXT DEFAULT '[]',
    location_lat REAL,
    location_lng REAL,
    city TEXT DEFAULT '',
    max_distance INTEGER DEFAULT 50,
    age_min INTEGER DEFAULT 18,
    age_max INTEGER DEFAULT 99,
    is_verified INTEGER DEFAULT 0,
    is_premium INTEGER DEFAULT 0,
    is_active INTEGER DEFAULT 1,
    is_banned INTEGER DEFAULT 0,
    ban_reason TEXT,
    last_active TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS likes (
    id TEXT PRIMARY KEY,
    from_user TEXT NOT NULL,
    to_user TEXT NOT NULL,
    is_super_like INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (from_user) REFERENCES users(id),
    FOREIGN KEY (to_user) REFERENCES users(id),
    UNIQUE(from_user, to_user)
  );

  CREATE TABLE IF NOT EXISTS dislikes (
    id TEXT PRIMARY KEY,
    from_user TEXT NOT NULL,
    to_user TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (from_user) REFERENCES users(id),
    FOREIGN KEY (to_user) REFERENCES users(id),
    UNIQUE(from_user, to_user)
  );

  CREATE TABLE IF NOT EXISTS matches (
    id TEXT PRIMARY KEY,
    user1 TEXT NOT NULL,
    user2 TEXT NOT NULL,
    is_active INTEGER DEFAULT 1,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user1) REFERENCES users(id),
    FOREIGN KEY (user2) REFERENCES users(id),
    UNIQUE(user1, user2)
  );

  CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    match_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    content TEXT NOT NULL,
    msg_type TEXT DEFAULT 'text',
    is_read INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (match_id) REFERENCES matches(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS reports (
    id TEXT PRIMARY KEY,
    reporter_id TEXT NOT NULL,
    reported_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending',
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (reported_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS blocks (
    id TEXT PRIMARY KEY,
    blocker_id TEXT NOT NULL,
    blocked_id TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (blocker_id) REFERENCES users(id),
    FOREIGN KEY (blocked_id) REFERENCES users(id),
    UNIQUE(blocker_id, blocked_id)
  );

  CREATE TABLE IF NOT EXISTS call_history (
    id TEXT PRIMARY KEY,
    caller_id TEXT NOT NULL,
    receiver_id TEXT NOT NULL,
    match_id TEXT NOT NULL,
    call_type TEXT NOT NULL,
    duration INTEGER DEFAULT 0,
    status TEXT DEFAULT 'missed',
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (caller_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    FOREIGN KEY (match_id) REFERENCES matches(id)
  );

  CREATE TABLE IF NOT EXISTS subscriptions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,
    plan TEXT NOT NULL DEFAULT 'free',
    call_minutes_remaining INTEGER DEFAULT 0,
    expires_at TEXT,
    transaction_id TEXT,
    payment_status TEXT DEFAULT 'pending',
    payment_url TEXT,
    payment_created_at TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS payment_logs (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    transaction_id TEXT NOT NULL,
    plan TEXT NOT NULL,
    amount REAL NOT NULL,
    status TEXT DEFAULT 'pending',
    payment_method TEXT,
    error_message TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
  );
`);

// Migrações aditivas: preservam contas e dados anteriores.
db.exec(`
  CREATE TABLE IF NOT EXISTS play_accounts (
    user_id TEXT PRIMARY KEY REFERENCES users(id), obfuscated_id TEXT NOT NULL UNIQUE
  );
  CREATE TABLE IF NOT EXISTS play_purchases (
    token_hash TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id), product_id TEXT NOT NULL,
    token_cipher TEXT NOT NULL, state TEXT NOT NULL, expires_at TEXT,
    active INTEGER NOT NULL DEFAULT 0, acknowledged INTEGER NOT NULL DEFAULT 0,
    superseded INTEGER NOT NULL DEFAULT 0,
    verified_at INTEGER NOT NULL
  );
  CREATE INDEX IF NOT EXISTS play_purchase_owner ON play_purchases(user_id);
  CREATE TABLE IF NOT EXISTS uploaded_photos (
    reference TEXT PRIMARY KEY, user_id TEXT NOT NULL,
    attached INTEGER NOT NULL DEFAULT 0, pending_delete INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );
  CREATE INDEX IF NOT EXISTS uploaded_photos_owner ON uploaded_photos(user_id);
`);
if (!db.prepare('PRAGMA table_info(play_purchases)').all().some(info => info.name === 'superseded')) {
  db.exec('ALTER TABLE play_purchases ADD COLUMN superseded INTEGER NOT NULL DEFAULT 0');
}
for (const column of ['terms_version', 'terms_accepted_at']) {
  if (!db.prepare('PRAGMA table_info(users)').all().some(info => info.name === column)) {
    db.exec(`ALTER TABLE users ADD COLUMN ${column} TEXT`);
  }
}
if (!db.prepare('PRAGMA table_info(uploaded_photos)').all().some(info => info.name === 'ready')) {
  db.exec('ALTER TABLE uploaded_photos ADD COLUMN ready INTEGER NOT NULL DEFAULT 1');
}
// Importa somente referências locais já associadas a perfis, sem buscar URLs externas.
const importPhotos = db.transaction(() => {
  for (const user of db.prepare('SELECT id, photos FROM users').all()) {
    let photos;
    try { photos = JSON.parse(user.photos || '[]'); } catch { continue; }
    if (!Array.isArray(photos)) continue;
    for (const reference of photos) {
      if (typeof reference === 'string' && /^\/api\/photos\/[0-9a-f-]{36}\.jpg$/.test(reference)) {
        db.prepare('INSERT OR IGNORE INTO uploaded_photos (reference, user_id, attached) VALUES (?, ?, 1)').run(reference, user.id);
      }
    }
  }
});
importPhotos();

export default db;
