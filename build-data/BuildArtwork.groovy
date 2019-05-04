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
	thumb.dir.mkdirs()
	execute '/usr/local/bin/convert', original, '-strip', '-thumbnail', '48x48>', 'PNG8:' + thumb
}


void printIndex(db) {
	def files = getThumbnailPath(db, 0).dir.listFiles()
	log.info "[INDEX] $db ${files.size()} (${byteCountToDisplaySize(files*.length().sum())})"
}




void build(ids, section, db, query) {
	ids.each{ id ->
		def original = getOriginalPath(section, id)
		def thumb = getThumbnailPath(section, id)

		if (thumb.exists()) {
			log.finest "[SKIP] $id"
			return
		}

		if (original.length() == 0 && original.exists() && System.currentTimeMillis() - original.lastModified() < 90 * 24 * 60 * 60 * 1000) {
			log.finest "[SKIP] $id"
			return
		}

		def artwork = retry(2, 60000) {
			try {
				return db.getArtwork(id, query, Locale.ENGLISH)
			} catch (FileNotFoundException e) {
				log.warning "[ARTWORK NOT FOUND] $e"
				return null
			}
		}

		if (!original.exists()) {
			artwork.findResult{ a ->
				return retry(2, 60000) {
					try {
						log.fine "Fetch $a"
						return a.url.saveAs(original)
					} catch (FileNotFoundException e) {
						log.warning "[IMAGE NOT FOUND] $e"
						return null
					}
				}
			}

			// create empty placeholder if there is no artwork
			if (!original.exists()) {
				original.createNewFile()
			}

			ls original
		}

		if (original.length() > 0 && !thumb.exists()) {
			createThumbnail(original, thumb)
			ls thumb
		}
	}

	printIndex(section)
}




build(MediaDetection.seriesIndex.object.id as HashSet, 'thetvdb', TheTVDB, 'poster')
build(MediaDetection.movieIndex.object.tmdbId as HashSet, 'themoviedb', TheMovieDB, 'posters')
