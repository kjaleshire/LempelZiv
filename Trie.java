/*
	File: Trie.java
	Author: Kyle Aleshire, modifying from Patrick O'Leary and David Meyers
	Created: 24 October 2004
	Modified: 04 December 2009
	Purpose: Class skeleton for implementation of trie data structure.
 */

public class Trie {

	public static final int ALPH = 27;
	Trie[]  Links; //one element for each character in the alphabet
	private boolean isWord; //this is a branch node when false, also an element node when true
	private String theWord; //holds the word if this is an element node, should be null otherwise
	private int theCode;
    public Trie() {
        theWord = null;
        Links = new Trie[ALPH];
        isWord = false;
		theCode = Integer.MAX_VALUE;
    }

    public void addString(String s, int code) {
        Trie t = this;
        int limit = s.length();
        for(int i = 0; i < limit; i++){
            int dex = index(s.charAt(i));
                 if(t.Links[dex] == null) t.Links[dex] = new Trie();
            t = t.Links[dex];
        }
        t.isWord = true;
        t.theWord = s;
		t.theCode = code;
    }

    public void printTrie() {
        printTrie(this);
    }

    private void printTrie(Trie t) {
        if(t.theWord != null) System.out.println(t.theWord);
        for(int i = 0; i < 27; i++){
             if(t.Links[i] != null) printTrie(t.Links[i]);
        }
    }

    public boolean isWord(String s) {
		Trie node;
        if((node = findNode(s)) == null) return false;
		return node.isWord;
    }


    public String getWord() {
    	return theWord;
    }

	public String getWord(int code){
		Trie node;
		if((node = findNode(this, code)) == null) return null;
		return node.theWord;
	}

	public int getCode(String s){
		return findNode(s).theCode;
	}
	
	private Trie findNode(String s){
		Trie child, t = this;
        int limit = s.length();
        for(int i = 0; i < limit; i++){
             if((child = t.childAt(s.charAt(i))) == null) return null;
	         t = child;
        }
		return t;
	}
	
	private Trie findNode(Trie t, int code){
		if(t.theCode == code) return t;
		for(int i = 0; i < ALPH; i++){
			if(t.Links[i] != null){
				Trie node = findNode(t.Links[i], code);
				if(node != null) return node;
			}
		}
		return null;
	}

    private Trie childAt(char ch) {
		return Links[index(ch)];
    }

    // convert all unknown symbols to spaces
    //returns the index value of a character: used in traversing the trie
    public static int index(char ch) {
		int i = (int) (ch - 'a');
		if ((i < ALPH-1) && (i >= 0)) return i;
		else return ALPH-1;
    }

	//returns the character associated with the ith element of Links
    public static char letter(int i) {
		if (i == ALPH-1) return ' ';
		else return (char) (i + 'a');
    }

	/*
		A test routine provided to help you determine if your trie is working.
		You'll probably want to expand it a bit to do a better job of testing your
		implementation.
	 */

    public static void main(String[] args) {
		Trie t = new Trie();
		t.addString("cattle", 10);
		t.addString("bet", 0);
		t.addString("bear", 0);
		t.addString("cat", 0);
		t.addString("bett er", 0);
		t.printTrie();
		if (t.isWord("cattle")) System.out.println("cattle in the trie");
    }
}
