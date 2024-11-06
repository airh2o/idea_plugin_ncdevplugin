@echo off
color 17
 
if "%1" == "" (
    for /f "delims=" %%i in ('dir /s /b /a-d /o-s *.jar') do (
        echo 正在反编译 %%~ni...
        title 正在反编译 %%i...
        java -jar H:\Downloads\cfr-0.152.jar "%%i" --caseinsensitivefs true --hideutf false  --outputdir "%cd%"
        echo ----%%i已经翻反编译---
    )
    goto :end 
) else (
    title 正在反编译 %1...
    java -jar H:\Downloads\cfr-0.152.jar %1 --caseinsensitivefs true --hideutf false  --outputdir "%cd%"
    echo 反编译完成.
    goto :end
)
 
echo 反编译完成.
@pause>nul
 
:end
pause
exit