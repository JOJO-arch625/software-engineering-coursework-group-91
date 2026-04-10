$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent (Resolve-Path -LiteralPath $MyInvocation.MyCommand.Path)
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $scriptRoot "..\..\.."))
$outputDirectory = Join-Path $scriptRoot "out"
$modelDirectory = Join-Path $projectRoot "src\main\java\com\group91\tars\model"
$testSourceDirectory = Join-Path $scriptRoot "src"

if (Test-Path -LiteralPath $outputDirectory) {
    Remove-Item -LiteralPath $outputDirectory -Recurse -Force
}

New-Item -ItemType Directory -Path $outputDirectory | Out-Null

$modelSources = Get-ChildItem -LiteralPath $modelDirectory -Filter *.java | ForEach-Object { $_.FullName }
$testSources = Get-ChildItem -LiteralPath $testSourceDirectory -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not $modelSources) {
    throw "No model source files were found for backend Java tests."
}

if (-not $testSources) {
    throw "No backend Java test source files were found."
}

$javacArguments = @("-encoding", "UTF-8", "-d", $outputDirectory) + $modelSources + $testSources
& javac @javacArguments
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& java -cp $outputDirectory com.group91.tars.tests.BackendModelTestMain
exit $LASTEXITCODE
