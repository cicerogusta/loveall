param([int]$VersionCode = 1, [string]$VersionName = '1.0.0', [switch]$Verify)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$keyPath = Join-Path $projectRoot '.secrets\loveall-upload.jks'
$credentialPath = Join-Path $projectRoot '.secrets\upload.signing.xml'
if (-not (Test-Path -LiteralPath $keyPath) -or -not (Test-Path -LiteralPath $credentialPath)) {
    throw 'Crie a chave com scripts/New-UploadKey.ps1 antes de preparar o release.'
}
$credential = Import-Clixml -LiteralPath $credentialPath
$names = @('LOVEALL_UPLOAD_STORE_FILE', 'LOVEALL_UPLOAD_STORE_PASSWORD', 'LOVEALL_UPLOAD_KEY_ALIAS', 'LOVEALL_UPLOAD_KEY_PASSWORD')
$previous = @{}
foreach ($name in $names) { $previous[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
try {
    $env:LOVEALL_UPLOAD_STORE_FILE = $keyPath
    $env:LOVEALL_UPLOAD_STORE_PASSWORD = $credential.GetNetworkCredential().Password
    $env:LOVEALL_UPLOAD_KEY_ALIAS = $credential.UserName
    $env:LOVEALL_UPLOAD_KEY_PASSWORD = $env:LOVEALL_UPLOAD_STORE_PASSWORD
    Push-Location $projectRoot
    try {
        $tasks = @(':app:preparePlayRelease')
        if ($Verify) { $tasks = @(':app:testDebugUnitTest', ':app:verifyBusinessCoverage', ':app:lintDebug', ':app:lintRelease', ':app:assembleDebug', ':app:connectedDebugAndroidTest', ':app:preparePlayRelease') }
        & .\gradlew.bat @tasks "-Ploveall.versionCode=$VersionCode" "-Ploveall.versionName=$VersionName" --console=plain --no-daemon
        if ($LASTEXITCODE -ne 0) { throw 'A preparacao do release falhou; verifique o resultado do Gradle.' }
    } finally { Pop-Location }
} finally {
    foreach ($name in $names) { [Environment]::SetEnvironmentVariable($name, $previous[$name], 'Process') }
}
