$ErrorActionPreference='Stop'
Set-Location -LiteralPath $PSScriptRoot
$mavenCmd=Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($mavenCmd) { $mavenPath=$mavenCmd.Source }
elseif(Test-Path -LiteralPath 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd') { $mavenPath='C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' }
else { throw 'Cần Maven hoặc mở project trong NetBeans để Build.' }
& $mavenPath -f pom.xml clean package
if($LASTEXITCODE -ne 0){throw 'Build chưa thành công.'}
New-Item -ItemType Directory -Force -Path dist | Out-Null
Copy-Item -LiteralPath 'target/QuanLyDienThoai-1.0.0.jar' -Destination 'dist/QuanLyDienThoai.jar' -Force
Write-Output 'Build xong. Mở ChayUngDung.cmd để chạy.'
