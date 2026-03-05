# Creates/updates local.properties with a valid sdk.dir for this machine.

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$localProps = Join-Path $projectRoot 'local.properties'

$sdk = $env:ANDROID_SDK_ROOT
if ([string]::IsNullOrWhiteSpace($sdk)) {
  $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}

if (-not (Test-Path $sdk)) {
  Write-Host "Android SDK directory not found: $sdk" -ForegroundColor Red
  Write-Host "Install it in Android Studio (SDK Manager) or set ANDROID_SDK_ROOT." -ForegroundColor Yellow
  exit 1
}

# Gradle local.properties on Windows uses escaped backslashes
$sdkEscaped = $sdk.Replace('\', '\\')

"sdk.dir=$sdkEscaped" | Set-Content -Encoding ASCII $localProps
Write-Host "Wrote $localProps" -ForegroundColor Green
Write-Host "sdk.dir=$sdk" -ForegroundColor Green
