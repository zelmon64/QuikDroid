# QuikDroid
Quikwriting for Android by Richard Královič from http://kedrigern.dcs.fmph.uniba.sk/~riso/Android/QuikDroid/

## Quikwriting for Android
This is an implementation of quickwriting (input method invented by Ken Perlin, patented by New York University, patent expired) for Android. Layouts are based on the implementation *qwo* for Openmoko.

Apk package: [quikdroid-release.apk](http://kedrigern.dcs.fmph.uniba.sk/~riso/Android/QuikDroid/quikdroid-release.apk)

Sources (GPL): [quikdroid.tar.gz](http://kedrigern.dcs.fmph.uniba.sk/~riso/Android/QuikDroid/quikdroid.tar.gz)

## Basic Usage
To write a letter, start drawing in the center, move to the segment with the letter, then move in the direction indicated by the letter position within the segment, then move back to the center. Symbol '>' means space, '<' is backspace, and 'arrow up' is shift. Gesture 'center, left-top, left-bottom, middle-bottom, center' is caps lock.
To type numbers, just tap the segment corresponding to the number. To write 0, drag from center to bottom-center.

If you start drawing in the left center area, move into left bottom area and stop, you toggle the display to show punctuation hints. To draw punctuation, you need to start gesture in the cell containing the symbol and end the gesture in the cell specified by the relative position of the symbol within the cell. Accents characters are composed with the following letter. You do not have to toggle the display mode to write punctuation, the display mode is just a hint.
