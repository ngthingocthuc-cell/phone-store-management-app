@echo off
setlocal
cd /d "%~dp0"
set "NOVA_JAVA=java"
where java >nul 2>nul
if errorlevel 1 (
  if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
    set "NOVA_JAVA=C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
  ) else (
    echo Can cai Java 17 tro len de chay ung dung.
    pause
    exit /b 1
  )
)
"%NOVA_JAVA%" --enable-native-access=ALL-UNNAMED "-Dnovamobile.dataDir=%~dp0data" -jar "%~dp0dist\QuanLyDienThoai.jar"
if errorlevel 1 pause
