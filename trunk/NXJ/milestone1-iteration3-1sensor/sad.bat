call nxjc %1.java
call nxjlink -o %1.nxj %1
ECHO (call nxjupload %1.nxj)