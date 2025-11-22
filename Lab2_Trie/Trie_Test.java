import java.util.List;

public class Trie_Test
{
    public static void main(String[] args)
    {
        Trie newT = new Trie();
        newT.loadTrieFromFile("test.txt");
        UI mainUI = new UI(newT);
        mainUI.draw();
        newT.saveTrieToFile("test.txt");
    }
}