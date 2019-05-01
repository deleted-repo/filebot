#!/usr/bin/env filebot -script

import static org.apache.commons.io.FileUtils.*


void ls(f) {
	if (f.exists()) {
		log.info "$f (${byteCountToDisplaySize(f.length())})"
	} else {
		log.warning "[FILE NOT FOUND] $f"
	}
}


File getOriginalPath(db, id) {
	return _args.outputPath.resolve("images/${db}/original/poster/${id}.jpg")
}


File getThumbnailPath(db, id) {
	return _args.outputPath.resolve("images/${db}/thumb/poster/${id}.png")
}


void createThumbnail(original, thumb) {
	if (!thumb.dir.exists()) {
		thumb.dir.mkdirs()
	}
	execute '/usr/local/bin/convert', original, '-strip', '-thumbnail', '48x48>', 'PNG8:' + thumb
	ls thumb
}


void createIndexFile(db) {
	def indexFile = _args.outputPath.resolve("images/${db}/thumb/poster/index.txt")
	def index = indexFile.dir.listFiles{ it.image }.collect{ it.getNameWithoutExtension() }.toSorted()

	index.join('\n').saveAs(indexFile)
	execute '/usr/local/bin/xz', indexFile, '--force'

	println "Index: ${index.size()}"
	indexFile.dir.listFiles{ !it.image }.each{ ls it }
}




void build(ids, section, db, query) {
	ids.each{ id ->
		log.info "[$id]"

		def original = getOriginalPath(section, id)
		def thumb = getThumbnailPath(section, id)

		if (thumb.exists()) {
			return
		}

		def artwork = db.getArtwork id, query, Locale.ENGLISH
		if (!artwork) {
			return
		}

		if (!original.exists()) {
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
			createThumbnail(original, thumb)
		}
	}

	createIndexFile(section)
}




build(MediaDetection.seriesIndex.object.id as Set, 'thetvdb', TheTVDB, 'poster')
build(MediaDetection.movieIndex.object.id as Set, 'themoviedb', TheMovieDB, 'posters')
