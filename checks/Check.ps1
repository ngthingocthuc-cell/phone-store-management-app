$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force -Path work | Out-Null
& javac -encoding UTF-8 -cp 'dist/QuanLyDienThoai.jar' -d work 'checks/ProjectCheck.java'
if($LASTEXITCODE -ne 0){throw 'Biên dịch kiểm thử chưa thành công.'}
& java --enable-native-access=ALL-UNNAMED '-Dnovamobile.jdbcUrl=jdbc:h2:mem:projectcheck;DB_CLOSE_DELAY=-1' '-Dnovamobile.dataDir=work/test-config' -cp 'work;dist/QuanLyDienThoai.jar' ProjectCheck
if($LASTEXITCODE -ne 0){throw 'Kiểm thử chưa thành công.'}
