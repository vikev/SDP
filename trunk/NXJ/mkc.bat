@echo off
set NLM=^


set NL=^^^%NLM%%NLM%^%NLM%%NLM%

@IF [%1] == [] GOTO PrintInfo

:Setup
set vc=c
set vcr=cr

ECHO call nxjc %1.java>src\%vc%.bat
ECHO call nxjlink -o %1.nxj %1>>src\%vc%.bat
ECHO call nxjupload %1.nxj>>src\%vc%.bat

ECHO call nxjc %1.java>src\%vcr%.bat
ECHO call nxjlink -o %1.nxj %1>>src\%vcr%.bat
ECHO call nxjupload -r %1.nxj>>src\%vcr%.bat

ECHO Successfully created 'src\%vc%.bat' and 'src\%vcr%.bat' for file '%1.java'

GOTO end

:PrintInfo
ECHO Creates a compile-and-upload and a compile-and-run script to the /src/ folder for easy use within Eclipse. This script should be run in the root folder of the project. 
ECHO Use:		mkc [main-class]	# src/[main-class].java
ECHO Example:	mkc heya		# src/heya.java

:end