@echo off

@IF [%1] == [] GOTO PrintInfo

:Setup

set vc=c%1
set vcr=cr%1

ECHO call nxjc %1.java>%vc%.bat
ECHO call nxjlink -o %1.nxj %1>>%vc%.bat
ECHO call nxjupload %1.nxj>>%vc%.bat

ECHO call nxjc %1.java>%vcr%.bat
ECHO call nxjlink -o %1.nxj %1>>%vcr%.bat
ECHO call nxjupload -r %1.nxj>>%vcr%.bat

ECHO Successfully created '%vc%.bat' and '%vcr%.bat' for file '%1.java'
GOTO end

:PrintInfo
ECHO Creates a compile-and-upload and a compile-and-run script to the /src/ folder for easy use within Eclipse. This script should be run in the root folder of the project. 
ECHO Use:		mkc [main-class]	# src/[main-class].java
ECHO Example:	mkc heya		# src/heya.java

:end