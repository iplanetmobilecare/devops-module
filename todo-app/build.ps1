$ErrorActionPreference = "Stop"

$rootDir = $PSScriptRoot
Set-Location $rootDir

$srcDir = "src\main\java"
$resDir = "src\main\resources"
$webDir = "src\main\webapp"
$libDir = "lib"
$targetDir = "target"
$classesDir = "$targetDir\classes"
$stagingDir = "$targetDir\war-staging"
$warFile = "$targetDir\todo-app.war"

if (Test-Path $classesDir) { Remove-Item -Recurse -Force $classesDir }
if (Test-Path $stagingDir) { Remove-Item -Recurse -Force $stagingDir }
if (Test-Path $warFile) { Remove-Item -Force $warFile }

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path "$stagingDir\WEB-INF\classes" | Out-Null
New-Item -ItemType Directory -Force -Path "$stagingDir\WEB-INF\lib" | Out-Null

$classpath = "$libDir\jakarta.servlet-api-5.0.0.jar;$libDir\gson-2.10.1.jar;$libDir\mysql-connector-j-8.3.0.jar"
$javaFiles = Get-ChildItem -Path $srcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

$javacArgs = @(
    "-encoding", "UTF-8",
    "-cp", $classpath,
    "-d", $classesDir
) + $javaFiles

& javac $javacArgs
if ($LASTEXITCODE -ne 0) {
    exit 1
}

if (Test-Path $resDir) {
    Copy-Item -Path "$resDir\*" -Destination $classesDir -Recurse -Force
}
Copy-Item -Path "$classesDir\*" -Destination "$stagingDir\WEB-INF\classes" -Recurse -Force

Copy-Item -Path "$webDir\*" -Destination $stagingDir -Recurse -Force
Copy-Item -Path "$libDir\gson-2.10.1.jar" -Destination "$stagingDir\WEB-INF\lib\" -Force
Copy-Item -Path "$libDir\mysql-connector-j-8.3.0.jar" -Destination "$stagingDir\WEB-INF\lib\" -Force

Push-Location $stagingDir
& jar -cvf "$rootDir\$warFile" . | Out-Null
Pop-Location

Write-Host "Build finished: $warFile"
