param([switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'Instale um JDK 17 ou superior e adicione sua pasta bin ao PATH.'
}
New-Item -ItemType Directory -Force -Path lib,build/classes,dist/lib | Out-Null
$dependencies = @(
    @{ File='h2-2.3.232.jar'; Path='com/h2database/h2/2.3.232'; Hash='8DAE62D22DB8982C3DCB3826EDB9C727C5D302063A67EEF7D63D82DE401F07D3' },
    @{ File='mysql-connector-j-9.2.0.jar'; Path='com/mysql/mysql-connector-j/9.2.0'; Hash='7E9941BBDCCA244D878EA95BFFF788FD9BA6A65AF757F24BE6C632930D61C7ED' }
)
foreach ($dep in $dependencies) {
    $file = Join-Path 'lib' $dep.File
    if (-not (Test-Path -LiteralPath $file)) {
        Write-Host "Baixando $($dep.File)..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -UseBasicParsing -Uri "https://repo.maven.apache.org/maven2/$($dep.Path)/$($dep.File)" -OutFile $file
    }
    if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $dep.Hash) {
        throw "O arquivo $file falhou na verificacao SHA-256. Remova-o e execute novamente."
    }
    Copy-Item -LiteralPath $file -Destination 'dist/lib' -Force
}
$sources = @(Get-ChildItem -LiteralPath src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& javac --release 17 -encoding UTF-8 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o aplicativo.' }
Copy-Item -Path 'src/main/resources/*' -Destination build/classes -Recurse -Force
$manifest = "Manifest-Version: 1.0`r`nMain-Class: br.com.lojatecnologia.Main`r`nClass-Path: lib/h2-2.3.232.jar lib/mysql-connector-j-9.2.0.jar`r`n`r`n"
[IO.File]::WriteAllText((Join-Path $PSScriptRoot 'build/MANIFEST.MF'), $manifest, [Text.Encoding]::ASCII)
& java --source 17 scripts/PackageJar.java --create --file dist/loja-tecnologia.jar --manifest build/MANIFEST.MF -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'Falha ao empacotar o aplicativo.' }
Copy-Item -LiteralPath scripts/Abrir-Loja.bat -Destination dist/Abrir-Loja.bat -Force
Copy-Item -LiteralPath config.example.properties -Destination dist/config.example.properties -Force
Copy-Item -LiteralPath scripts/LEIA-ME.txt -Destination dist/LEIA-ME.txt -Force
if ($Test) {
    New-Item -ItemType Directory -Force -Path build/test-classes | Out-Null
    $testSources = @(Get-ChildItem -LiteralPath src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -cp 'build/classes;lib/*' -d build/test-classes @testSources
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar as verificacoes.' }
    & java -ea '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes;lib/*' br.com.lojatecnologia.IntegrationTest
    if ($LASTEXITCODE -ne 0) { throw 'As verificacoes encontraram uma falha.' }
}
Write-Host 'Pronto: dist/loja-tecnologia.jar'
