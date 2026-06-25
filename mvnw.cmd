@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file to
@REM you under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir=[System.IO.Path]::GetDirectoryName([System.IO.Path]::GetFullPath('%~f0')); $scriptDir; $wrapperJar=$scriptDir+'\.mvn\wrapper\maven-wrapper.jar'; $distPropFile=$scriptDir+'\.mvn\wrapper\maven-wrapper.properties'; $mwProps=@{}; if(Test-Path $distPropFile){Get-Content $distPropFile|%{if($_ -match '^([^#=]+)=(.*)$'){$mwProps[$matches[1].Trim()]=$matches[2].Trim()}}}; $distUrl=$mwProps['distributionUrl']; $wrapperUrl=$mwProps['wrapperUrl']; if(-not (Test-Path $wrapperJar)){$tmpJar=[System.IO.Path]::GetTempFileName(); try{(New-Object System.Net.WebClient).DownloadFile($wrapperUrl,$tmpJar); Move-Item $tmpJar $wrapperJar -Force}catch{Write-Error 'Failed to download maven-wrapper.jar'; exit 1}}; $mvnHome=$env:USERPROFILE+'\.m2\wrapper\dists'; $distName='apache-maven-3.9.9'; $mvnDir=$mvnHome+'\'+$distName; if(-not (Test-Path ($mvnDir+'\bin\mvn.cmd'))){if(-not (Test-Path $mvnHome)){New-Item -ItemType Directory $mvnHome|Out-Null}; $tmpZip=[System.IO.Path]::GetTempFileName()+'.zip'; (New-Object System.Net.WebClient).DownloadFile($distUrl,$tmpZip); Expand-Archive $tmpZip $mvnHome -Force; Remove-Item $tmpZip; $extracted=Get-ChildItem $mvnHome -Directory|Where-Object{$_.Name -like 'apache-maven*'}|Select-Object -First 1; Rename-Item $extracted.FullName $mvnDir -ErrorAction SilentlyContinue}; $mvnDir+'\bin\mvn.cmd'}" 2>NUL`) DO (
  IF "%%A"=="%~dp0" (SET __MVNW_CMD__=%%B) ELSE (SET __MVNW_ERROR__=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=

@IF NOT "%__MVNW_CMD__%"=="" (
  @"%__MVNW_CMD__%" %*
  @SET __MVNW_EXIT_CODE__=%ERRORLEVEL%
  @GOTO :mvn_end
)

@ECHO Could not find mvn.cmd, falling back to PATH
@mvn %*
:mvn_end
@EXIT /B %__MVNW_EXIT_CODE__%
