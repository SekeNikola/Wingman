#!/usr/bin/env bash
# Run this once to generate silent OGG placeholder files.
# Requires: ffmpeg (brew install ffmpeg)
# Replace with real audio later.

DEST="app/src/main/res/raw"
mkdir -p "$DEST"

for name in sfx_click sfx_scroll sfx_boot sfx_back; do
    ffmpeg -f lavfi -i "anullsrc=r=44100:cl=mono" -t 0.1 -c:a libvorbis -q:a 2 "$DEST/${name}.ogg" -y 2>/dev/null
    echo "Created $DEST/${name}.ogg"
done

echo "Done. Replace these with real audio assets before release."
