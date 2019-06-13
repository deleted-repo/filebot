Next Release (4.8.6)
====================
* Enhanced `Selection Dialog` with thumbnails and tooltips
* Enhanced `Conflict Dialog` with detailed explanations
* Improved support for mapping episode information between different databases and numbering schemes (e.g. via `AnimeLists` or `XEM`)
* Added `{seasonize}` binding (e.g. map `AniDB` numbers to `TheTVDB` SxE numbers and vice versa)
* Added `{history}` binding for looking up the original file path of `{f}` (e.g. useful for `-exec` post-processing commands)
* Evaluate `{closures}` automatically in `String.plus(Closure)` constructs (e.g. `{"[" + {n} + " " + {s00e00} + "]"}`)
* Ensure that `ActionPopup` is always displayed on top of the Windows Task Bar
* Improved `-mediainfo -exec` pipeline
* Added `-no-history` and `-clear-history` CLI options
* Allow dynamic code evaluation (e.g. `evaluate('/path/to/snippet.groovy' as File)`)
* Allow `@file.groovy` syntax in `Format Editor` and `Preset Editor` (e.g. `@/path/to/MyFormat.groovy`)
* Added `--mapper` option (e.g. `--mapper XEM.TheTVDB`)
* Allow `*.groovy` files as argument value for `--format`, `--filter`, `--mapper` and `--file-filter` CLI options (e.g. `--format /path/to/MyFormat.groovy`)
* Support `bash_completion`


FileBot 4.8.5
=============
* Port to OpenJDK 11 / OpenJFX 11
* Improved syntax highlighting for format expressions
* Improved support for rare SxE patterns (i.e. S1-01)
* Added `{kodi}` binding (i.e. Kodi naming standard)
* Added `{ci}` binding (i.e. movie collection index)
* Match `{source}`, `{group}`, `{tags}` and `{s3d}` from `{media.title}`
* *Move to Trash* action in Filter tools (e.g. batch delete clutter files)
* *Paste License Key* button to simplify license activation for users who can't receive email attachments (i.e. some email providers block `*.psm` attachments)
* Built-in Automator Workflows for macOS (i.e. easily create Quick Actions and Folder actions)
* Fix UI deadlock issues on Linux
* Fix drag-n-drop issues on Linux / KDE / Dolphin
* Support for `7z` an `unrar` executables on Linux
* Support for xattr on FreeBSD / OpenBSD / NetBSD
* Support for writing xattr metadata to plain text files (i.e. improved support for rclone and gdfs)
* Support for a Dark Mode Look-and-Feel
* Fix various mediainfo / archive extract issues on QNAP NAS (especially on x86_64 devices)
* New 32-bit Windows packages (i.e. x86 msi installer)
* New multi-arch Debian packages (i.e. support armhf and aarch64 for Raspberry Pi devices or ARM-based servers)
* New multi-arch Fedora / openSUSE / CentOS packages (i.e. RPM packages)


FileBot 4.8.2
=============
* New license model and cross-platform support for all Java 8 / Java 10 platforms
* Improved episode / movie auto-detection
* Added `{hdr}` binding
* Added `--file-filter` option (e.g. `--file-filter f.video`)
* Added `--db exif` and `--db file` in addition to `--db xattr` (i.e. command-line equivalents for Preset datasources)
* [Windows] Improved HiDPI support for non-integer scale factors (e.g. 125%)
* [Linux] Support for ffprobe as replacement for libmediainfo (i.e. for armv7 / aarch64 platforms)
* [macOS] Disable 0-termination when reading / writing xattr String values


FileBot 4.7.15
==============
* Support for CoW clones (requires `APFS` or `BTRFS`)
* Improved movie auto-detection


FileBot 4.7.10
==============
* Support the new TheTVDB JSON API
* Support the new OMDb API
* Improved CD1/2 auto-detection
* Support for custom rename actions via the `--action` option
* Support for the new `-exec` option
* Support for the `FILEBOT_OPTS` environment variable for FileBot-specific Java options
* Use GnuPG signatures for all deployment artifacts


FileBot 4.7.9
=============
* Binding `{sdhd}` has been removed in favour of `{hd}` which now supports UHD/HD/SD as possible values
* Improved support for Photo mass-renaming (e.g. added `{exif}`, `{camera}` and `{location}` bindings)
* Improved streaming behaviour for `-mediainfo` commands and `--format` expressions no longer limited by file path validation (e.g. multi-line, special characters, etc)
* Support lookup by id for -list commands (e.g. `filebot -list --q 70327`)
* Support for renaming episodes files in linear order (e.g. `-list --q 70327 -rename *.mkv`)


FileBot 4.7.8
=============
* Additional language preferences
* Additional Episode Sort Order: `Absolute Airdate Order` (useful for matching by airdate or episode title instead of SxE numbers)
* Additional bindings: `{kbps}` and `{khz}`
* Unified `{localize}` and `{order}` binding usage (e.g. `localize.zho.n` or `order.airdate.sxe`)
* Use powershell instead of cmd when executing commands on Windows (e.g. `--def exec`)
* Improved behaviour for `-rename --q` command-line usage
* Improved desktop integration for Gnome and KDE
* Improved support for Debian Linux armhf ABI (e.g. Raspberry Pi)


FileBot 4.7.5
=============
* Keyboard shortcuts for calling user-defined Presets (Numpad 1..9)
* Improved episode auto-detection
* Improved movie part index auto-detection
* Improved file sort order
* Improved bindings: `{plex}`, `{t}`, `{votes}`, `{group}`, `{tags}`, `{audioLanguages}` and `{textLanguages}`
* Support ANSI color output (if `$TERM == xterm-256color`)
* Fixed Gnome GVFS drag-n-drop issues
* Reduce xattr metadata size
* Use `xz` compression for all packages (e.g. reduce download size by 40%)


FileBot 4.7.1
=============
* Improved Windows 7/8/10 integration
* Improved auto-delete behaviour (use system trash, preserve hidden user files, etc)
* `{plex}` binding now forces Windows-compatible paths (e.g. strip colons)
* New MediaInfo bindings: `{mediaTitle}` and `{bitdepth}`
* New Info Object bindings: `{id}` (series/movie ID), `{object}` and `{type}`
* New Episode bindings: `{sc}` (season count) and `{sy}` (season years)
* Support for `--action reflink` (requires Linux and a copy-on-write filesystem)
* Improved logging and debugging options


FileBot 4.7
===========
* Smart Mode for handling Movies, TV Shows, Anime and Music all at once
* Support for Renaming Folders (i.e. auto-delete left-behind empty folders)
* Resolve relative formats against the Media root folder (instead of the parent folder)
* Send To context menu for Episodes / Filter / List panels
* Improved Filter tools
* Improved List tool
* Support for TheMovieDB in Episode Mode
* Improved movie / episode auto-detection
* Fix various OpenSubtitles Search/Download and Upload issues
* Fix various TheTVDB / AniDB / TVMaze issues
* Fix various multi-episode detection issues
* Fix various ID3 Tags lookup issues
* HiDPI icons
* Fix various UI/UX issues
* Performance and caching improvements
* Improved logging and error messages
* Plex Naming Standard binding `{plex}`
* Use range multi-episode formatting by default when using `{sxe}` or `{s00e00}` (i.e. Plex naming standard)
* `{s00e00}` binding will now evaluate to TheTVDB Airdate Season / Episode for AniDB Absolute Number Episodes
* Subtitle language auto-detection when using the `{lang}` binding
* Subtitle language / category extension binding `{subt}`
* Spoken languages binding `{languages}`
* Stereoscopic 3D binding `{s3d}`
* A-Z folder binding `{az}`
* Just-in-time localization binding `{localize}`, e.g. `{localize.German.Title}`
* Filesize bindings `{bytes}`, `{megabytes}`, `{gigabytes}`
* Generic MediaInfo bindings `{video}`, `{audio}`, etc are now multi-stream bindings (and `{videos}`, `{audios}`, etc have consequently been removed)
* Cmdline operation `-revert` to revert previous `-rename` operations
* Cmdline option `--conflict` accepts index conflict resolution behaviour
* `@file` syntax for command-line argument passing
* Scripts from the online repository (e.g. `fn:sysinfo`) are now code signed and cryptographically secured against malicious tampering (not just HTTPS transport encryption)


FileBot 4.6.1
=============
* Added support user-defined Presets for repetitive tasks
* Added support for TVmaze
* Improved support for OpenSubtitles and subtitle matching
* Improved movie / episode auto-detection
* Improved ID3 Tags music mode
* Improved cache behaviour
* Improved support for Chinese & Brazilian languages
* Added helper function `String.asciiQuotes()` for normalizing various quotation marks
* Added `{model}` binding for querying the entire rename model
* Added convenience binding `{ny}` for `Name (Year)` formats
* Added bindings `{info.budget}`, `{info.revenue}` and `{info.popularity}` to the movie info object
* Changed `String.sortName()` default behaviour
* Support `--filter` as Groovy-based file filter in `filebot -mediainfo` calls
* Use `Apache Commons VFS2` and `junrar` to reduce native dependencies on some platforms
* Support `$JAVA_OPTS` convention in all `filebot.sh` scripts
* Update to FanartTV API v3
* Codesign Windows NSIS and MSI installers
* Publish sha256 checksums for all release files
* Updated Chocolatey install scripts with sha1 checksums


FileBot 4.5.6
=============
* Improved series / episode detection
* Optimize web service calls and provide more data via xattr metadata
* Extended metadata is now fetched from the originally selected data source (e.g. AniDB "generes" is no mapped to Anime categories, etc)
* Fixed various issues related to fetching Chinese subtitles
* Allow processing of `*.ac3` and `*.dts` files in Music mode
* Do not treat folders with `movie.nfo` as single units like disk folders anymore
* Fixed lots of issues that have been raised in the forums


FileBot 4.5.3
=============
* Batch `-extract` will now only extract new files
* `Set Output Folder` button in Format Editor
* Optimizations for subtitle search and lookup
* Prevent OpenSubtitles abuse
* Require OpenSubtitles login
* New script: `fn:verify`
* Force Nimbus as default cross platform LaF (mainly applies to KDE users)


FileBot 4.5
===========
* Make sure movie name `{n}` works as per user-defined Preferred Language (only affects non-English mode)
* Support choosing between (default) `Opportunistic` / (new) `Strict` mode matching
* Improved behavior when processing large sets of files
* Improved movie / episode detection
* Improved TheMovieDB / AcoustID support
* Inherit ACLs when moving / copying files to remote folders
* New bindings `{model}` and `{self}` for advanced use-cases
* In movie mode `{primaryTitle}` now maps to original movie name
* `--db xattr` for offline processed using previously stored xattr metadata
* `--action duplicate` to duplicate files via hardlink when possible or copy when necessary
* Fixed various UI layout and LaF issues
* Improved integration with OSX
* Support passing file arguments in single-panel mode

FileBot 4.3
===========
* Lots of optimizations and usability improvements
* Dropped support for Java 7 (so Java 8 is required now)
