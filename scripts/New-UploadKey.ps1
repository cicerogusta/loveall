param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$secretDirectory = Join-Path $projectRoot '.secrets'
$keyPath = Join-Path $secretDirectory 'loveall-upload.jks'
$credentialPath = Join-Path $secretDirectory 'upload.signing.xml'
if ((Test-Path -LiteralPath $keyPath) -or (Test-Path -LiteralPath $credentialPath)) {
    throw 'A chave ou credencial ja existe. Nenhum arquivo foi sobrescrito.'
}
$keytool = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\keytool.exe' } else { (Get-Command keytool.exe -ErrorAction Stop).Source }
if (-not (Test-Path -LiteralPath $keytool)) { throw 'Configure JAVA_HOME para um JDK com keytool.' }
New-Item -ItemType Directory -Force -Path $secretDirectory | Out-Null
$identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
& icacls.exe $secretDirectory /inheritance:r /grant:r "${identity}:(OI)(CI)F" '*S-1-5-18:(OI)(CI)F' /q | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel restringir as permissoes da pasta da chave.' }
$random = [byte[]]::new(48)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($random)
$password = [Convert]::ToBase64String($random)
$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
$credential = [System.Management.Automation.PSCredential]::new('loveall-upload', $securePassword)
$oldPassword = $env:LOVEALL_KEYTOOL_PASSWORD
try {
    # A senha nao e passada na linha de comando nem gravada em texto simples.
    $env:LOVEALL_KEYTOOL_PASSWORD = $password
    $credential | Export-Clixml -LiteralPath $credentialPath
    & $keytool -genkeypair -keystore $keyPath -storetype JKS -alias $credential.UserName -keyalg RSA -keysize 3072 -validity 10000 -storepass:env LOVEALL_KEYTOOL_PASSWORD -keypass:env LOVEALL_KEYTOOL_PASSWORD -dname 'CN=Cicero Pinheiro, OU=LoveAll, O=LoveAll, C=BR' -noprompt
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar a chave de upload. Preserve os arquivos para diagnostico.' }
    Write-Output 'Chave criada em .secrets/loveall-upload.jks; senha protegida pelo DPAPI do Windows em upload.signing.xml.'
    Write-Output 'A credencial so pode ser aberta pelo mesmo usuario neste Windows. Prepare backup recuperavel antes do envio a Play.'
} finally {
    $env:LOVEALL_KEYTOOL_PASSWORD = $oldPassword
    $password = $null
}
