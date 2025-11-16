import java.util.List;

public class Trie
{ 
    private Vertex root;
    final static public int alphabetLength = 26;

    Trie()
    {
        root = new Vertex();
    }

    void insert(String word)
    {
        Vertex v = root;
        for(int i = 0; i < word.length(); ++i)
        {
            char c = word.charAt(i);
            c -= 'a';
            if(v.children[c] == null)
                v.children[c] = new Vertex();
            v = v.children[c];
        }
        v.isTerminal = true;
    }

    boolean contains(String word)
    {
        Vertex v = root;
        for(int i = 0; i < word.length(); ++i)
        {
            char c = word.charAt(i);
            c -= 'a';
            if(v.children[c] == null)
                return false;
            v = v.children[c];
        }
        return v.isTerminal;
    }

    /*boolean startsWith(String prefix)
    {

    }

    List<String> getByPrefix(String prefix)
    {
        
    }*/
}

class Vertex
{
    public boolean isTerminal;
    public Vertex[] children;

    Vertex()
    {
        this.children = new Vertex[Trie.alphabetLength];
        this.isTerminal = false;
    }
}