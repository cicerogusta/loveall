$ErrorActionPreference = 'Stop'
$serverDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'server'
$localNode = Get-ChildItem (Join-Path $env:LOCALAPPDATA 'LoveAll\tools\node-v22.*-win-x64\node.exe') -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending | Select-Object -First 1
$nodeExe = if ($localNode) { $localNode.FullName } else { (Get-Command node -ErrorAction Stop).Source }
if ((& $nodeExe --version) -notmatch '^v22\.') {
    throw 'Instale o Node.js 22 LTS para executar a API e suas dependencias SQLite.'
}
$env:PATH = "$(Split-Path $nodeExe -Parent);$env:PATH"
$envFile = Join-Path $serverDir '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    $secretBytes = New-Object byte[] 64
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($secretBytes) } finally { $random.Dispose() }
    $secret = [Convert]::ToBase64String($secretBytes)
    Set-Content -LiteralPath $envFile -Value "NODE_ENV=development`nPORT=3001`nJWT_SECRET=$secret" -Encoding ascii
}
Push-Location $serverDir
try {
    if (-not (Test-Path 'node_modules/better-sqlite3/build/Release/better_sqlite3.node')) {
        $npmCli = Join-Path (Split-Path $nodeExe -Parent) 'node_modules/npm/bin/npm-cli.js'
        & $nodeExe $npmCli ci --no-audit --no-fund
        if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar as dependencias da API.' }
    }
    & $nodeExe src/index.js
    if ($LASTEXITCODE -ne 0) { throw 'A API foi encerrada com erro.' }
} finally { Pop-Location }
