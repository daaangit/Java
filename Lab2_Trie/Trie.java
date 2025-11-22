import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Trie
{ 
    private Vertex root;
    final static public int alphabetLength = 26;

    public Trie()
    {
        root = new Vertex();
    }

    public void insert(String word)
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

    public boolean contains(String word)
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

    public boolean startsWith(String prefix)
    {
        Vertex v = root;
        for(int i = 0; i < prefix.length(); ++i)
        {
            char c = prefix.charAt(i);
            c -= 'a';
            if(v.children[c] == null)
                return false;
            v = v.children[c];
        }
        return true;
    }

    public List<String> getByPrefix(String prefix) // empty prefix will return list with all data in Trie
    {
        Vertex v = root;
        List<String> wordList = new ArrayList<>();

        for(int i = 0; i < prefix.length(); ++i)
        {
            char c = prefix.charAt(i);
            c -= 'a';
            if(v.children[c] == null)
                return wordList;
            v = v.children[c];
        }
        dfs(v, prefix, wordList);
        return wordList;   
    }

    public boolean erase(String word) {
        return erase(root, word, 0);
    }
    
    private boolean erase(Vertex v, String word, int depth) {
        if (v == null)
            return false;
    
        if (depth == word.length()) 
            {
            if (!v.isTerminal)
                return false;
            v.isTerminal = false;
            return true;
        }
        int index = word.charAt(depth) - 'a';
        if (index < 0 || index >= Trie.alphabetLength)
            return false;

        Vertex child = v.children[index];
        if (child == null)
            return false;

    
        boolean deleted = erase(child, word, depth + 1);
    
        if (deleted)
            if (!child.isTerminal && hasNoChildren(child))
                v.children[index] = null;
        return deleted;
    }
    
    private boolean hasNoChildren(Vertex v) 
    {
        for (int i = 0; i < Trie.alphabetLength; i++)
            if (v.children[i] != null)
                return false;
        return true;
    }

    private void dfs(Vertex v, String prefix, List<String> wordList)
    {
        if(v.isTerminal)
        {
            wordList.add(prefix);
        }

        for(int i = 0; i < alphabetLength; ++i)
        {
            if(v.children[i] != null)
            {
                char c = (char)('a' + i);
                dfs(v.children[i], prefix + c, wordList);
            }
        }
    }

    public void loadTrieFromFile(String filename)
    {
        try
        {
            loadTrie(filename);
            System.out.println("Trie loaded from " + filename);
        }
        catch(IOException e)
        {
            System.out.println("Error while loading trie from " + filename + ": " + e.getMessage());
        }
    }

    private void loadTrie(String filename) throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get(filename)));
        String[] words = data.split("\\s+");

        for(String w: words)
        {
            String formatString = w.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if(!formatString.isEmpty())
                this.insert(formatString);
        }
    }

    public void saveTrieToFile(String filename)
    {
        List<String> words = new ArrayList<>();
        dfs(root, "", words);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); ++i)
        {
            sb.append(words.get(i));
            if (i + 1 < words.size())
                sb.append(" ");
        }
        try
        {
            Files.write(Paths.get(filename), sb.toString().getBytes());
            System.out.println("Trie saved to " + filename);
        }
        catch (IOException e)
        {
            System.out.println("Error while saving trie to " + filename + ": " + e.getMessage());
        }
    }
    
    public void printTrie() 
    {
        System.out.println("(root)");
        printTrie(root, "", true, '\0'); 
    }

    private void printTrie(Vertex v, String shift, boolean isLast, char c) 
    {
        if (v == null) {
            return;
        }

        if (c != '\0') {
            System.out.print(shift);
            System.out.print(isLast ? "└── " : "├── ");
            System.out.print(c);
            if (v.isTerminal)
                System.out.print("*");
            System.out.println();
            shift += isLast ? "    " : "│   ";
        }

        int last = -1;
        for (int i = alphabetLength - 1; i >= 0; i--) {
            if (v.children[i] != null)
                {
                last = i;
                break;
            }
        }

        for (int i = 0; i < alphabetLength; i++)
        {
            if (v.children[i] != null)
            {
                char nextChar = (char) ('a' + i);
                printTrie(v.children[i], shift, i == last, nextChar);
            }
        }
    }

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