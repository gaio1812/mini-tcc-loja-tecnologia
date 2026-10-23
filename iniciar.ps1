$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Test-Path -LiteralPath dist/loja-tecnologia.jar)) { & "$PSScriptRoot/build.ps1" }
& java -jar dist/loja-tecnologia.jar
if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel iniciar. Instale Java 17 ou superior.' }
