
SOURCEPATH=app/src/main/java/
CLASSPATH=app/build/intermediates/classes/debug/
PACPATH=com/adellica/thumbkeyboard
MYFILES=${SOURCEPATH}/${PACPATH}
MYCLS=${CLASSPATH}/${PACPATH}

JAVAS=${MYFILES}/ThumbJoy.java \
      ${MYFILES}/ThumbReader.java \
      ${MYFILES}/JoyLibrary.java \
      ${MYFILES}/Keypress.java \
      ${MYFILES}/ThumboardKeycodes.java

CLASSS=${MYCLS}/ThumbJoy.class \
       ${MYCLS}/ThumbReader.class \
       ${MYCLS}/JoyLibrary.class \
       ${MYCLS}/Keypress.class

compile: ${CLASSS}

run: compile
	java -cp ${CLASSPATH} com.adellica.thumbkeyboard.ThumbReader

${CLASSPATH}/${PACPATH}/ThumbReader.class: ${JAVAS}
	javac -sourcepath ${SOURCEPATH} -d ${CLASSPATH} ${SOURCEPATH}/${PACPATH}/ThumbReader.java






LAYOUT_JAVA_FILE = ./app/src/main/java/com/adellica/thumbkeyboard/ThumboardLayout.java

$(LAYOUT_JAVA_FILE): gen-ThumboardLayout.java.scm layout.scm
	csi -s gen-ThumboardLayout.java.scm > $(LAYOUT_JAVA_FILE) || rm $(LAYOUT_JAVA_FILE)

