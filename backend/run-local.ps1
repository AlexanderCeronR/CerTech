$ErrorActionPreference = 'Stop'
$rootEnvPath = Join-Path $PSScriptRoot '..\.env'
$javaCommand = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }

try {
    $javaVersion = (& $javaCommand -version 2>&1 | Select-Object -First 1).ToString()
}
catch {
    throw 'CerTech requires Java 21. Set JAVA_HOME to an installed Java 21 JDK.'
}
if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'version "?21(?:\.|\")') {
    throw 'CerTech requires Java 21. Set JAVA_HOME to an installed Java 21 JDK.'
}

if (-not (Test-Path -LiteralPath $rootEnvPath)) {
    throw 'Copy .env.example to .env in the project root, then set DB_PASSWORD.'
}

$settings = ConvertFrom-StringData (Get-Content -Raw -LiteralPath $rootEnvPath)
foreach ($name in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'CORS_ALLOWED_ORIGINS')) {
    if (-not $settings.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($settings[$name])) {
        throw "The root .env file must define $name."
    }
    [Environment]::SetEnvironmentVariable($name, $settings[$name], 'Process')
}
[Environment]::SetEnvironmentVariable('SPRING_PROFILES_ACTIVE', 'local', 'Process')

Push-Location $PSScriptRoot
try {
    if (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'mvnw.cmd')) {
        & (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
    }
    else {
        mvn spring-boot:run
    }
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}
