QPKG_NAME := filebot
ICON := ../icons/filebot-round-rect.png

resize-icons:
	convert -verbose $(ICON) -resize 64x64 -gravity center -background transparent -extent 64x64 icons/$(QPKG_NAME).png
	convert -verbose $(ICON) -resize 64x64 -gravity center -background transparent -extent 64x64 -colorspace gray icons/$(QPKG_NAME)_gray.png
	convert -verbose $(ICON) -resize 80x80 -gravity center -background transparent -extent 80x80 icons/$(QPKG_NAME)_80.png
	convert -verbose $(ICON) -resize 100x100 -gravity center -background transparent -extent 100x100 icons/$(QPKG_NAME)_100.png
