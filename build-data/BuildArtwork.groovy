#!/usr/bin/env filebot -script

import static org.apache.commons.io.FileUtils.*


def originals = _args.outputPath.resolve "images/thetvdb/original/poster"
def thumbs = _args.outputPath.resolve "images/thetvdb/thumb/poster"

originals.mkdirs()
thumbs.mkdirs()


void ls(f) {
	if (f.exists()) {
		log.info "$f (${byteCountToDisplaySize(f.length())})"
	} else {
		log.warning "[FILE NOT FOUND] $f"
	}
}


def tvdbEntries = MediaDetection.seriesIndex.object as Set


def index = []


tvdbEntries.each{
	log.info "[$it.id] $it.name"

	def original = originals.resolve "${it.id}.jpg"
	def thumb = thumbs.resolve "${it.id}.png"

	if (thumb.exists()) {
		index << it
		return
	}

	def artwork = TheTVDB.getArtwork it.id, 'poster', Locale.ENGLISH
	if (!artwork) {
		return
	}

	if (!original.exists()) {
		sleep 2000
		artwork.findResult{ a ->
			return retry(2, 60000) {
				try {
					log.fine "Fetch $a"
					return a.url.saveAs(original)
				} catch (FileNotFoundException e) {
					log.warning "[FILE NOT FOUND] $e"
					return null
				}
			}
		}
		ls original
	}

	if (original.exists() && !thumb.exists()) {
		execute '/usr/local/bin/convert', original, '-strip', '-thumbnail', '48x48>', 'PNG8:' + thumb
		ls thumb
	}

	if (thumb.exists()) {
		index << it
		return
	}
}


def indexFile = index.toSorted().join('\n').saveAs thumbs.resolve('index.txt')
execute '/usr/local/bin/xz', indexFile, '--force'

println "Index: ${index.size()}"
