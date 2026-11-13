param([string]$OutputPath)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $OutputPath) { $OutputPath = Join-Path $projectRoot 'qa\render\loveall-api.tar.gz' }
$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$files = @(
    'Dockerfile', '.dockerignore', 'render.yaml', 'server/package.json', 'server/package-lock.json',
    'server/src', 'deploy/render/environment.env.example', 'deploy/admob/app-ads.txt', 'docs/RENDER-SUPABASE.md'
)
foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $file))) { throw "Arquivo ausente: $file" }
}
New-Item -ItemType Directory -Path (Split-Path $OutputPath -Parent) -Force | Out-Null
& tar.exe --create --gzip --file $OutputPath --directory $projectRoot @files
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar pacote da API.' }
Write-Output "Pacote preparado: $OutputPath"
Write-Output 'Sem banco, fotos, credenciais ou APK. O Blueprint ainda precisa de um repositorio Git no Render.'
