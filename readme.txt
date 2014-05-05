LempelZiv: A compression algorithm in Java
Written by Patrick O'Leary, David Meyers and Kyle Aleshire
Last built on December 4, 2009

This Java class implements the Lempel Ziv compression algorithm for compressing and uncompressing relatively simple plain text files. This class and supporting class Trie were developed on Mac OS X using TextPad and Apple's stock JDK. Along with this readme and the testing.txt file, this archive also includes two demo files, short.txt and longer.txt, for testing purposes.

Installation on Mac OS X, Linux, *BSD and Solaris (with installed JDK):
- Unzip the LempelZiv.java and Trie.java files into a convenient location ("location").
- Run "javac location/*.java".

Usage:
	"java LempelZiv -uc <file>"
	
	Options:
	-c <file>		Compress <file>. Outputs the compressed form as "<file>.lz".
	-u <file>		Uncompress <file>. Will only process single files with the extension ".lz". Outputs the uncompressed file with a ".uc" extension.

Caveat: The compress action only processes lowercase characters and spaces. All other unique characters are converted to spaces when stored.