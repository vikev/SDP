#!
echo Please, entire filename (excluding .java)
read FN
echo nxjc $FN+.java
echo nxjlink $FN -o $FN+.nxj
echo nxjupload $N+.nxj
