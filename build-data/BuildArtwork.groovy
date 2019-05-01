#!/usr/bin/env filebot -script

import static org.apache.commons.io.FileUtils.*


def originals = _args.outputPath.resolve "images/thetvdb/original/poster"
def thumbs = _args.outputPath.resolve "images/thetvdb/thumb/poster"

originals.mkdirs()
thumbs.mkdirs()


void ls(f) {
	println "${f} (${byteCountToDisplaySize(f.length())})"
}


def tvdbEntries = MediaDetection.seriesIndex.object as Set


def index = tvdbEntries.collect{ it.id }.toSorted().join('\n').saveAs thumbs.resolve('index.txt')
execute '/usr/local/bin/xz', index, '--force'


tvdbEntries.each{
	println "[$it.id] $it.name"
	def artwork = TheTVDB.getArtwork it.id, 'poster', Locale.ENGLISH
	if (artwork) {
		def original = originals.resolve "${it.id}.jpg"
		def thumb = thumbs.resolve "${it.id}.png"

		if (!original.exists()) {
			artwork[0].url.saveAs original
			ls original
		}

		execute '/usr/local/bin/convert', original, '-strip', '-thumbnail', '48x48>', 'PNG8:' + thumb
		ls thumb

		sleep 2000
	}
}
