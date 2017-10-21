
SOURCEPATH=app/src/main/java/
CLASSPATH=app/build/intermediates/classes/debug/
PACPATH=com/adellica/thumbkeyboard
JAVAS=${SOURCEPATH}/${PACPATH}/ThumbJoy.java ${SOURCEPATH}/${PACPATH}/ThumbReader.java ${SOURCEPATH}/${PACPATH}/JoyLibrary.java
CLASSS=${CLASSPATH}/${PACPATH}/ThumbJoy.class ${CLASSPATH}/${PACPATH}/ThumbReader.class ${CLASSPATH}/${PACPATH}/JoyLibrary.class

compile: ${CLASSS}

run: compile
	java -cp ${CLASSPATH} com.adellica.thumbkeyboard.ThumbReader

${CLASSPATH}/${PACPATH}/ThumbJoy.class: ${JAVAS}
	javac -sourcepath ${SOURCEPATH} -d ${CLASSPATH} ${SOURCEPATH}/${PACPATH}/ThumbJoy.java

${CLASSPATH}/${PACPATH}/ThumbReader.class: ${JAVAS}
	javac -sourcepath ${SOURCEPATH} -d ${CLASSPATH} ${SOURCEPATH}/${PACPATH}/ThumbReader.java






LAYOUT_JAVA_FILE = ./app/src/main/java/com/adellica/thumbkeyboard/ThumboardLayout.java

$(LAYOUT_JAVA_FILE): gen-ThumboardLayout.java.scm layout.scm
	csi -s gen-ThumboardLayout.java.scm > $(LAYOUT_JAVA_FILE) || rm $(LAYOUT_JAVA_FILE)

