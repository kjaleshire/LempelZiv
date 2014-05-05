/*
 * LempelZiv
 * Template for the Lempel-Ziv compression/uncompression program.
 *
 * Programmer: Patrick O'Leary, David Meyers, Kyle Aleshire
 * Date: 13/12/01
 * Modified: 04 December 2009
 */

import java.io.*;

/*Provides the constant BITS_PER_BYTE: don't modify this value!!!*/
interface BitUtils {
	public static final int BITS_PER_BYTE = 8;
}

/* BitInputStream
 * Bit-input stream wrapper class.
 */


/*
	Provides utilities for reading from a file one bit at a time. The bit values are
	returned as type int with value 0 or 1 corresponding the the bit value from the
	file.
 */
class BitInputStream {
	public BitInputStream( InputStream is ) {
		in = is;
		bufferPos = BitUtils.BITS_PER_BYTE;
	}

	/*
		Returns an int with value 0 for a zero bit and 1 for a one bit. To construct
		a code from a compressed file, you'll need to get the correct number of bits, and
		assemble them into a code. This needs to be coordinated with the method used for
		placing codes into the compressed file: the number of bits needs to be the same...
	 */
	public int readBit( ) throws IOException {
		if ( bufferPos == BitUtils.BITS_PER_BYTE ) {
			buffer = in.read( );
			if( buffer == -1 ) return -1;
			bufferPos = 0;
		}
		return getBit( buffer, bufferPos++ );
	}

	public void close( ) throws IOException {
		in.close( );
	}

	private static int getBit( int pack, int pos ) {
		return ( pack & ( 1 << pos ) ) != 0 ? 1 : 0;
	}

	private InputStream in;
	private int buffer;
	private int bufferPos;
}

/* BitOutputStream
 * Bit-output stream wrapper class.
 */
/*
	Provides utilities for writing to a file one bit at a time. The bit values are
	specified as int values: 0 for a zero bit, 1 for a one bit. writeBit allows you to
	specify one bit. writeBits allows you to specify a number of bits at the same time
	with the individual bit values stored in an int array.
 */
class BitOutputStream {
	private int compBytes;
	public BitOutputStream( OutputStream os ) {
		compBytes = 0;
		bufferPos = 0;
		buffer = 0;
		out = os;
	}

	/*
		Writes a single bit specified as an int to the file.
	 */
	public void writeBit( int val ) throws IOException {
		buffer = setBit( buffer, bufferPos++, val );
		if( bufferPos == BitUtils.BITS_PER_BYTE ) flush( );
	}
	/*
		Writes an array of bits to the file: each int value specifies one bit.
	 */
	public void writeBits( int [ ] val ) throws IOException {
		for( int i = 0; i < val.length; i++ ) writeBit( val[ i ] );
	}

	public void flush( ) throws IOException {
		if( bufferPos == 0 ) return;
		out.write( buffer );
		compBytes++;
		bufferPos = 0;
		buffer = 0;
	}

	public void close( ) throws IOException {
		flush( );
		out.close( );
	}
	
	public int compressedBytes(){
		return compBytes;
	}
	/*
		private helper method that does the work of putting bits into an int that will
		then be written to the output
	 */
	private int setBit( int pack, int pos, int val ) {
		if( val == 1 ) pack |= ( val << pos );
		return pack;
	}
	private int bitCount;
	private OutputStream out;
	private int buffer;
	private int bufferPos;
}


public class LempelZiv {
	
	// A constant definition of what we are using for the bitsize of each code.
	
	private static final int CODE_BIT_SIZE = 12;
	
	// The compress method.
	public static void compress( String inFile ) throws IOException {
		String S, C;
		int currentCode = 0;
		String compressedFile = inFile + ".lz";
		Trie dictionary = new Trie();

		//put in codes for the lowercase alphabet + space
		for(int i = 97; i < 123; i++) dictionary.addString(String.valueOf((char) i), currentCode++);
		dictionary.addString(" ", currentCode++);

		try{
			InputStream in = new BufferedInputStream( new FileInputStream( inFile ) );

			OutputStream fout = new BufferedOutputStream( new FileOutputStream( compressedFile ) );
			BitOutputStream bout = new BitOutputStream(fout);
			
			// read input file into a byte array stream
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			int ch;
			while( ( ch = in.read( ) ) != -1 ) byteOut.write( ch );
			in.close( );

			// now convert the input file to an array of bytes in memory

			byte[] theInput = byteOut.toByteArray( );

			/*
		 	* Your compression routine goes here!
		 	*
		 	* Notes: bout is a BitOutputStream object; you can use its instance methods to write
		 	* bits. The code above reads the input file into an array of bytes in memory called
		 	* theInput. Convert byte to char via an explicit cast, e.g.:char firstchar = (char)
		 	* theInput[0];
		 	*/
			S = String.valueOf((char) theInput[0]);
			for(int i = 1; i < theInput.length; i++){
				C = String.valueOf((char) theInput[i]);
				if(dictionary.isWord(S + C)) S = S + C;
				else{
					//System.out.println(dictionary.getCode(S));
					bout.writeBits(toIntArray(dictionary.getCode(S)));
					if(((currentCode >> CODE_BIT_SIZE) | 0) != 1) dictionary.addString(S + C, currentCode++);
					S = C;
				}
			}
			//System.out.println(dictionary.getCode(S));
			bout.writeBits(toIntArray(dictionary.getCode(S)));
			bout.close();
			System.out.println("current code bit size: " + CODE_BIT_SIZE);
			System.out.println("current library size: " + currentCode);
			System.out.println("compression ratio: %" + (int)(100.0 - ((float) bout.compressedBytes() / theInput.length) * 100.0));
			//dictionary.printTrie();
		}
		catch(FileNotFoundException e){
			System.out.println(inFile + "\t: file not found!");
		}
	}
		
	// A helper method for converting an integer into an array of its constituent bits.
		
	private static int[] toIntArray(int currentCode){
		int[] newCode = new int[CODE_BIT_SIZE];
		for(int i = 0; i < CODE_BIT_SIZE; i++) newCode[i] = 1 & (currentCode >> i); 
		return newCode;
	}
	
	// A helper method for consolidating an array of integers representing bits into a single integer.
	
	private static int toInt(int[] code){
		int buffer= 0;
		for(int i = 0; i < CODE_BIT_SIZE; i++) buffer |= (code[i] << i);
		return buffer;
	}

	// A helper method for reading a single code of predefined size from the BitInputStream.

	private static int readCode(BitInputStream bin) throws IOException{
		int rtn[] = new int[2], code[] = new int[CODE_BIT_SIZE];
		for(int i = 0; i < CODE_BIT_SIZE; i++)
			if((code[i] = bin.readBit()) == -1) return -1;
		return toInt(code);
	}
	
	// A helper method for writing a String fetched from the Trie character by character.
	
	private static void writeString(String s, OutputStream fout) throws IOException{
		System.out.println(s);
		for(int i = 0; i < s.length(); i++) fout.write((int) s.charAt(i));
	}

	// The uncompress method.
	
	public static void uncompress( String compressedFile ) throws IOException {
		String inFile, extension, S, C;
		int O, N, currentCode = 0;
		Trie dictionary = new Trie();
		
		//put in codes for the lowercase alphabet + space
		for(int i = 97; i < 123; i++) dictionary.addString(String.valueOf((char) i), currentCode++);
		dictionary.addString(" ", currentCode++);
		
		inFile = compressedFile.substring( 0, compressedFile.length( ) - 3 );
		extension = compressedFile.substring( compressedFile.length( ) - 3 );

		if( !extension.equals( ".lz" ) ) {
			System.out.println( "Not a compressed file!" );
			return;
		}
		inFile += ".uc";	// for debugging, so as to not clobber original
		try{
		InputStream fin = new BufferedInputStream( new FileInputStream( compressedFile ) );
		DataInputStream in = new DataInputStream( fin );
		BitInputStream bin = new BitInputStream( in );
		OutputStream fout = new BufferedOutputStream( new FileOutputStream( inFile ) );

		/*
		 * Your decompression routine goes here!
		 *
		 * Note: bin is a BitInputStream object; you can use its methods to read bits from the
		 * compressed input file.
		 */

		O = readCode(bin);
		//System.out.println(O);
		writeString(C = dictionary.getWord(O), fout);
		while((N = readCode(bin)) != -1){
			//System.out.println(N);
			if(dictionary.getWord(N) == null){
				S = dictionary.getWord(O);
				S = S + C;
			}
			else S = dictionary.getWord(N);
			writeString(S, fout);
			C = String.valueOf(S.charAt(0));
			dictionary.addString(dictionary.getWord(O) + C, currentCode++);
			O = N;
		}
		
		bin.close( );
		fout.close( );
		}
		catch(FileNotFoundException e){
			System.out.println(compressedFile + "\t: file not found!");
		}
	}

	public static void main( String [ ] args ) throws IOException {
		if( args.length < 2 ) {
			System.out.println( "Usage: java LempelZiv -[cu] files" );
			return;
		}

		String option = args[ 0 ];
		for( int i = 1; i < args.length; i++ ) {
			String nextFile = args[ i ];
			if( option.equals( "-c" ) ) compress( nextFile );
			else if( option.equals( "-u" ) ) uncompress( nextFile );
			else {
				System.out.println( "Usage: java LempelZiv -[cu] files" );
				return;
			}
		}
	}
}
