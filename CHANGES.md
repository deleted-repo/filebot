Next Release (4.8.6)
====================

Features
--------
* Added `{history}` binding for looking up the original file path of `{f}` (e.g. useful for `-exec` post-processing commands)
* Evaluate `{closures}` automatically in `String.plus(Closure)` constructs (e.g. `{"[" + {n} + " " + {s00e00} + "]"}`)

Bug Fixes
---------
* Ensure that `ActionPopup` is always displayed on top of the Windows Task Bar
