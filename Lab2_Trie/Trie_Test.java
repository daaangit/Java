public class Trie_Test
{
    public static void main(String[] args)
    {
        Trie newT = new Trie();
        newT.insert("test");
        System.out.println(newT.contains("test"));
    }
}