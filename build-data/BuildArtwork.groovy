#!/usr/bin/env filebot -script

import static org.apache.commons.io.FileUtils.*


scaleFactor = [1, 2]
thumbnailSize = [48, 48]


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


File getThumbnailPath(db, id, scale) {
	def n = id as String
	if (scale != 1) {
		n += '@' + scale + 'x'
	}
	return _args.outputPath.resolve("images/${db}/thumb/poster/${n}.png")
}


void createThumbnail(original, thumb, scale) {
	thumb.dir.mkdirs()

	def size = thumbnailSize*.multiply(scale).join('x')
	execute 'convert', '-strip', original, '-thumbnail', size, '-gravity', 'center', '-background', 'transparent', '-extent', size, 'PNG8:' + thumb
}


void createIndexFile(db) {
	def indexFile = _args.outputPath.resolve("images/${db}/thumb/poster/index.txt")
	def index = indexFile.dir.listFiles()*.getNameWithoutExtension().findAll{ it ==~ /\d+/ }*.toInteger() as TreeSet

	index.join('\n').saveAs(indexFile)
	execute 'xz', indexFile, '--force', '--keep'

	println "Index: ${index.size()}"
	indexFile.dir.listFiles{ !it.image }.each{ ls it }
}




void build(ids, section, db, query) {
	def files = []

	ids.each{ id ->
		scaleFactor.each { scale ->
			def original = getOriginalPath(section, id)
			def thumb = getThumbnailPath(section, id, scale)

			if (thumb.exists()) {
				log.finest "[SKIP] $id"
				return
			}

			if (original.length() == 0 && original.exists() && System.currentTimeMillis() - original.lastModified() > 90 * 24 * 60 * 60 * 1000) {
				log.finest "[SKIP] $id"
				return
			}

			if (original.length() == 0 || !original.exists()) {
				def artwork = retry(2, 60000) {
					try {
						return db.getArtwork(id, query, Locale.ENGLISH)
					} catch (FileNotFoundException e) {
						log.warning "[ARTWORK NOT FOUND] $e"
						return null
					}
				}

				artwork?.findResult{ a ->
					return retry(2, 60000) {
						sleep(2000)
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
				if (original.length() == 0 || !original.exists()) {
					original.createNewFile()
					original.setLastModified(System.currentTimeMillis())
				}

				ls original
			}

			if (original.length() > 0 && !thumb.exists()) {
				createThumbnail(original, thumb, scale)
				files << thumb

				ls thumb
			}
		}
	}

	if (files) {
		createIndexFile(section)
	}
}




build(MediaDetection.animeIndex.object.id as HashSet, 'anidb', AniDB, 'poster')
build(MediaDetection.seriesIndex.object.id as HashSet, 'thetvdb', TheTVDB, 'poster')
build(MediaDetection.movieIndex.object.tmdbId as HashSet, 'themoviedb', TheMovieDB, 'posters')
