// Somente SQL interno é traduzido. Valores continuam parametrizados.
export function postgresSql(sql) {
  let converted = sql
    .replace(
      /datetime\('now',\s*'-1 day'\)/gi,
      "to_char(timezone('UTC', CURRENT_TIMESTAMP) - INTERVAL '1 day', 'YYYY-MM-DD HH24:MI:SS')"
    )
    .replace(/datetime\('now'\)/gi, "to_char(timezone('UTC', CURRENT_TIMESTAMP), 'YYYY-MM-DD HH24:MI:SS')");
  if (/INSERT OR IGNORE/i.test(converted)) {
    converted =
      converted.replace(/INSERT OR IGNORE/i, 'INSERT').replace(/;\s*$/, '') + ' ON CONFLICT DO NOTHING';
  }
  let quote = null,
    index = 0,
    result = '';
  for (let i = 0; i < converted.length; i++) {
    const char = converted[i];
    if (quote) {
      result += char;
      if (char === quote) {
        if (converted[i + 1] === quote) result += converted[++i];
        else quote = null;
      }
    } else if (char === "'" || char === '"') {
      quote = char;
      result += char;
    } else result += char === '?' ? `$${++index}` : char;
  }
  return result;
}
