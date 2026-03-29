param(
    [ValidateSet("app-image", "exe")]
    [string]$Type = "app-image"
)

$profile = if ($Type -eq "exe") { "windows-installer" } else { "windows-app-image" }

Write-Host "Packaging WH40KEasyCombat as $Type using Maven profile '$profile'..."
mvn clean package "-P$profile"

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if ($Type -eq "exe") {
    Write-Host "Installer output: target\\installer"
} else {
    Write-Host "App image output: target\\jpackage"
}
