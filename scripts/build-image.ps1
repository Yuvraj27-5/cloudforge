<#
.SYNOPSIS
    Builds the sample-app image with CloudForge's tagging convention.

.DESCRIPTION
    Every build is tagged with the full commit SHA. That tag is immutable and is
    what Kubernetes references, so Phase 11 can roll back to a specific previous
    build. A short-SHA tag is added for humans.

    "latest" is written only for a clean working tree on the default branch. An
    image built from uncommitted changes is not reproducible and must not be
    reachable under a name that implies it is current.

.EXAMPLE
    .\scripts\build-image.ps1
    .\scripts\build-image.ps1 -Push -Registry ghcr.io/yourname
#>

[CmdletBinding()]
param(
    [string]$Registry = "",
    [switch]$Push
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$commitSha = (git rev-parse HEAD).Trim()
$shortSha = (git rev-parse --short HEAD).Trim()
$branch = (git rev-parse --abbrev-ref HEAD).Trim()
$isDirty = [bool](git status --porcelain)
$buildTime = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")

$name = if ($Registry) { "$Registry/cloudforge-sample-app" } else { "cloudforge-sample-app" }

if ($isDirty) {
    Write-Warning "Working tree has uncommitted changes. The commit tag will not match the image contents."
    $commitSha = "$commitSha-dirty"
    $shortSha = "$shortSha-dirty"
}

$tags = @("${name}:${commitSha}", "${name}:${shortSha}")

if ($branch -eq "main" -and -not $isDirty) {
    $tags += "${name}:latest"
}

Write-Host "Building $name" -ForegroundColor Cyan
$tags | ForEach-Object { Write-Host "  tag: $_" }

$tagArgs = $tags | ForEach-Object { @("--tag", $_) }

docker build `
    --build-arg "COMMIT_SHA=$commitSha" `
    --build-arg "APP_VERSION=$shortSha" `
    --build-arg "BUILD_TIME=$buildTime" `
    @tagArgs `
    ./sample-app

if ($LASTEXITCODE -ne 0) {
    throw "docker build failed"
}

Write-Host "`nBuilt:" -ForegroundColor Green
docker images $name --format "  {{.Tag}}`t{{.Size}}`t{{.CreatedSince}}"

if ($Push) {
    if (-not $Registry) {
        throw "-Push requires -Registry, for example: -Registry ghcr.io/yourname"
    }
    foreach ($tag in $tags) {
        Write-Host "Pushing $tag"
        docker push $tag
        if ($LASTEXITCODE -ne 0) { throw "docker push failed for $tag" }
    }
}
