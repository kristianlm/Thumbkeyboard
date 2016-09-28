LAYOUT_JAVA_FILE = ./app/src/main/java/com/adellica/thumbkeyboard/ThumboardLayout.java

$(LAYOUT_JAVA_FILE): gen-ThumboardLayout.java.scm layout.scm
	csi -s gen-ThumboardLayout.java.scm > $(LAYOUT_JAVA_FILE)

