import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;
import java.util.Locale;

class UI
{
    Trie trie;
    Scanner scanner;
    public UI(Trie trie)
    {
        this.trie = trie;
        this.scanner = new Scanner(System.in);
        this.scanner.useLocale(Locale.US);
    }
    public void draw()
    {
        boolean active = true;
        while(active)
        {
            clearTerminal();
            try
            {
                System.out.println("""
                                1.insert
                                2.contains
                                3.starts with
                                4.get by prefix
                                5.erase
                                6.draw trie
                                (-1).exit
                                """);
                int option = Integer.parseInt(scanner.nextLine());

                if(option != -1 && (option < 1 || option > 6))
                    throw new InputMismatchException();
                
                switch(option)
                {
                    case(-1):
                    {
                        active = false;
                        break;
                    }
                    
                    case(1): // insert word
                    {
                        System.out.println("Enter word(only english alphabet permitted): ");
                        String input = scanner.nextLine().toLowerCase();
                        if(!input.matches("[a-zA-Z]+"))
                            throw new InputMismatchException();
                        trie.insert(input);
                        System.out.println("word was inserted successfuly");    
                        System.out.println("Enter any key to proceed");
                        scanner.nextLine();
                        break;
                    }

                    case(2): // contains
                    {
                        System.out.println("Enter word(only english alphabet permitted): ");
                        String input = scanner.nextLine().toLowerCase();
                        if(!input.matches("[a-zA-Z]+"))
                            throw new InputMismatchException();
                        if(trie.contains(input))
                            System.out.println("word is contained in trie");
                        else
                            System.out.println("word is not contained in trie");
                        System.out.println("Enter any key to proceed");
                        scanner.nextLine();
                        break;
                    }

                    case(3): // starts with
                    {
                        System.out.println("Enter prefix(only english alphabet permitted): ");
                        String input = scanner.nextLine().toLowerCase();
                        if(!input.matches("[a-zA-Z]+"))
                            throw new InputMismatchException();      
                        if(trie.startsWith(input))
                            System.out.println("Word with prefix \"" + input + "\" is contained in trie");
                        else
                            System.out.println("Word with prefix " + input + " is not contained in trie");
                        System.out.println("Enter any key to proceed");
                        scanner.nextLine();
                        break;
                    }

                    case(4): // get by prefix
                    {
                        System.out.println("Enter prefix (only english alphabet permitted, empty line to show all words): ");
                        String input = scanner.nextLine().toLowerCase();
                        List<String> listByPrefix;
                        if(input.isEmpty())
                            listByPrefix = trie.getByPrefix("");
                        else if(!input.matches("[a-zA-Z]+"))
                            throw new InputMismatchException();
                        else
                            listByPrefix = trie.getByPrefix(input);
                        System.out.println("Words by prefix \"" + input + "\" in trie: ");
                        for(int i = 0; i < listByPrefix.size(); ++i)
                        {
                            System.out.print(listByPrefix.get(i) + " ");
                        }
                        System.out.println("\nEnter any key to proceed");
                        scanner.nextLine();
                        break;
                    }

                    case(5): //erase
                    {
                        System.out.println("Enter word(only english alphabet permitted): ");
                        String input = scanner.nextLine().toLowerCase();
                        if(!input.matches("[a-zA-Z]+"))
                            throw new InputMismatchException();
                        if(trie.erase(input))
                            System.out.println("word \"" + input + "\" has been erased from trie");
                        else
                            System.out.println("word \"" + input + "\" does not exist in trie");
                        System.out.println("\nEnter any key to proceed");
                        scanner.nextLine();
                        break;
                    }

                    case(6):
                    {
                        trie.printTrie();
                        System.out.println("\nEnter any key to proceed");
                        scanner.nextLine();
                        break;
                    }
                }
            }
            catch(InputMismatchException e)
            {
                System.out.println("Input error! Only english alphabet letters permitted. All letters will be formatted to lowercase");
                System.out.println("Enter any key to proceed");
                scanner.nextLine();
            }
            catch(NumberFormatException e)
            {
                System.out.println("Invalid option!");
                System.out.println("Enter any key to proceed");
                scanner.nextLine();
            }
        }
    }

    private void clearTerminal()
        {
            try
            {
                String os = System.getProperty("os.name").toLowerCase();

                if(os.contains("win"))
                {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                }
                else
                {
                    new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
                }
            }
            catch(IOException | InterruptedException e)
            {
                System.out.println("Failed to clear the terminal");
            }
        }
}