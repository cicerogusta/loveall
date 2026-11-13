import 'dotenv/config';
import bcrypt from 'bcryptjs';
import { randomUUID as uuid } from 'node:crypto';
import db from './database.js';

if (process.env.NODE_ENV === 'production' || process.env.DATABASE_URL)
  throw new Error('Seed permitido somente no SQLite de desenvolvimento.');

const hash = bcrypt.hashSync('senha1234', 12);

const users = [
  {
    name: 'Ana Silva',
    email: 'ana@demo.com',
    birthdate: '1996-03-15',
    gender: 'woman',
    bio: 'Amo viajar e conhecer culturas novas. Procuro alguém para dividir aventuras! 🌍',
    city: 'São Paulo',
    photos: ['https://i.pravatar.cc/400?img=1'],
  },
  {
    name: 'Carlos Oliveira',
    email: 'carlos@demo.com',
    birthdate: '1994-07-22',
    gender: 'man',
    bio: 'Desenvolvedor de software de dia, guitarrista de noite. 🎸',
    city: 'Rio de Janeiro',
    photos: ['https://i.pravatar.cc/400?img=3'],
  },
  {
    name: 'Mariana Santos',
    email: 'mariana@demo.com',
    birthdate: '1998-11-10',
    gender: 'woman',
    bio: 'Fotógrafa, amante de café e bons livros 📚☕',
    city: 'Belo Horizonte',
    photos: ['https://i.pravatar.cc/400?img=5'],
  },
  {
    name: 'Rafael Costa',
    email: 'rafael@demo.com',
    birthdate: '1995-01-28',
    gender: 'man',
    bio: 'Personal trainer e amante de vida saudável. Bora treinar juntos? 💪',
    city: 'São Paulo',
    photos: ['https://i.pravatar.cc/400?img=7'],
  },
  {
    name: 'Julia Mendes',
    email: 'julia@demo.com',
    birthdate: '1997-05-03',
    gender: 'non_binary',
    bio: 'Artista e ativista. Acredito que o amor não tem rótulos. 🌈',
    city: 'Curitiba',
    photos: ['https://i.pravatar.cc/400?img=9'],
  },
  {
    name: 'Lucas Ferreira',
    email: 'lucas@demo.com',
    birthdate: '1993-09-18',
    gender: 'man',
    bio: 'Chef de cozinha e sommelier. Posso te conquistar pelo estômago 🍷',
    city: 'Porto Alegre',
    photos: ['https://i.pravatar.cc/400?img=11'],
  },
  {
    name: 'Camila Rocha',
    email: 'camila@demo.com',
    birthdate: '1999-12-25',
    gender: 'woman',
    bio: 'Médica veterinária que ama animais mais que pessoas (às vezes) 🐾',
    city: 'Florianópolis',
    photos: ['https://i.pravatar.cc/400?img=16'],
  },
  {
    name: 'Alex Rivera',
    email: 'alex@demo.com',
    birthdate: '1996-06-14',
    gender: 'genderqueer',
    bio: 'DJ e produtor(a) musical. Quer dançar comigo? 🎧',
    city: 'São Paulo',
    photos: ['https://i.pravatar.cc/400?img=12'],
  },
  {
    name: 'Fernanda Lima',
    email: 'fernanda@demo.com',
    birthdate: '1995-08-30',
    gender: 'woman',
    bio: 'Advogada de dia, cantora de karaokê à noite. Procuro meu dueto 🎤',
    city: 'Brasília',
    photos: ['https://i.pravatar.cc/400?img=20'],
  },
  {
    name: 'Thiago Souza',
    email: 'thiago@demo.com',
    birthdate: '1992-02-14',
    gender: 'man',
    bio: 'Piloto, surfista e fã de pôr do sol. Vem voar comigo? ✈️🏄',
    city: 'Recife',
    photos: ['https://i.pravatar.cc/400?img=14'],
  },
  {
    name: 'Sam Almeida',
    email: 'sam@demo.com',
    birthdate: '1997-04-20',
    gender: 'agender',
    bio: 'Streamer e gamer profissional. Bora jogar co-op? 🎮',
    city: 'São Paulo',
    photos: ['https://i.pravatar.cc/400?img=33'],
  },
  {
    name: 'Bianca Torres',
    email: 'bianca@demo.com',
    birthdate: '1998-10-05',
    gender: 'transgender_woman',
    bio: 'Modelo e influenciadora digital. A vida é uma passarela! 💃',
    city: 'Rio de Janeiro',
    photos: ['https://i.pravatar.cc/400?img=23'],
  },
];

console.log('Criando usuários de demonstração...');

for (const user of users) {
  const id = uuid();
  await db
    .prepare(
      `
    INSERT OR IGNORE INTO users (id, email, password, name, birthdate, gender, bio, city, photos, looking_for, is_verified)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `
    )
    .run(
      id,
      user.email,
      hash,
      user.name,
      user.birthdate,
      user.gender,
      user.bio,
      user.city,
      JSON.stringify(user.photos),
      '[]',
      1
    );

  const saved = await db.prepare('SELECT id FROM users WHERE email = ?').get(user.email);
  await db
    .prepare('INSERT OR IGNORE INTO subscriptions (id, user_id, plan) VALUES (?, ?, ?)')
    .run(uuid(), saved.id, 'free');
}

console.log(`✅ ${users.length} usuários criados com sucesso!`);
console.log('Credenciais de teste: qualquer email acima / senha: senha1234');

await db.close();
