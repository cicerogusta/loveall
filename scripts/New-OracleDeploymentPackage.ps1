param([string]$OutputPath)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $OutputPath) { $OutputPath = Join-Path $projectRoot 'qa\oracle\loveall-api.tar.gz' }
$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$files = @(
    'Dockerfile', '.dockerignore', 'server/package.json', 'server/package-lock.json', 'server/src',
    'deploy/oracle/compose.yaml', 'deploy/oracle/Caddyfile',
    'deploy/oracle/prepare.sh', 'deploy/oracle/deploy.sh', 'deploy/oracle/backup.sh',
    'deploy/oracle/monetization.env.example'
)
foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $file))) { throw "Arquivo ausente: $file" }
}
New-Item -ItemType Directory -Path (Split-Path $OutputPath -Parent) -Force | Out-Null
& tar.exe --create --gzip --file $OutputPath --directory $projectRoot @files
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar pacote da API.' }
Write-Output "Pacote preparado: $OutputPath"
Write-Output 'O pacote não inclui banco de dados, fotos, .env, chaves SSH ou o aplicativo Android.'
