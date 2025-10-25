import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Random;
import java.math.BigDecimal;
import java.util.InputMismatchException;

public class FinalBank {
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);
        
        Account newAccount = new Account();
        UI mainUI = new UI(newAccount, scanner);
        mainUI.mainUI();
    }

    public static class Transaction
    {
        Transaction(String operation, double value, String date, String time, boolean successful, String transferInfo)
        {
            this.operation = operation;
            this.value = value;
            this.date_time = date + " " + time.substring(0,8);
            this.successful = successful;
            this.transferInfo = transferInfo;
        }

        Transaction(Transaction other)
        {
            this.operation = other.operation;
            this.value = other.value;
            this.date_time = other.date_time;
            this.successful = other.successful;
            this.transferInfo = other.transferInfo;
        }

        String operation;;
        String date_time;
        String transferInfo;
        double value;
        boolean successful;
    }

    public static class Account
    {
        private List<Transaction> transactionList;
        private String name;
        final public String accountID;
        double balance;
        public LocalDateTime now = LocalDateTime.now();

        public Account() 
        {
            balance = 0;
            transactionList = new ArrayList<>();
            name = "UNNAMED";
            String tempID = "";
            Random random = new Random();
            for(byte ch = 0; ch < 4; ++ch) //65 - 90 uppercase eng letters
            {
                int tempRand = random.nextInt(26) + 65;
                tempID += (char)tempRand;
            }
            for(byte i = 0; i < 8; ++i)
            {
                int tempRand = random.nextInt(0, 10);
                tempID += tempRand;
            }
            accountID = tempID;
        }

        public Transaction deposit(double value)
        {
            balance += value;
            Transaction current = 
                new Transaction("Deposit +", 
                value,LocalDate.now().toString(),LocalTime.now().toString(), true, "None");
            transactionList.add(current);
            return current;
        }

        public Transaction withdrawal(double value)
        {
            Transaction current = 
                new Transaction("Withdrawal -", value,
                LocalDate.now().toString(), LocalTime.now().toString(), true, "None");
            if(balance < value) 
            {
                // add to UI System.out.println("insufficient funds in the account");
                current.successful = false;
            }
            else 
            {
                balance -= value;
            }
            transactionList.add(current);
            return current;
        }

        public Transaction transfer(double value, Account account)
        {
            Transaction current = new Transaction("Transfer -", value, LocalDate.now().toString(), 
                LocalTime.now().toString(), false, accountID + " to " + account.getID());
            if(this.balance >= value)
            {
                balance -= value;
                current.successful = true;
                account.incomingTransfer(current);
            }
            transactionList.add(current);
            return current;
        }

        public void incomingTransfer(Transaction transfer)
        {
            Transaction current = new Transaction(transfer);
            current.operation = "Transfer +";
            balance += current.value;
            transactionList.add(current);
        }

        public double getBalance()
        {
            return balance;
        }

        public String getID()
        {
            return accountID;
        }

        public List<Transaction> getTransactions()
        {
            return transactionList;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

    }

    public static class UI
    {
        List<Account> accountsList = new ArrayList<>();
        DecimalFormat numform = new DecimalFormat("#,##0.00000");
        boolean active;
        Scanner scanner;
        int currentAccountsNumber = 0;
        UI(Account account, Scanner scanner)
        {
            this.accountsList.add(new Account());
            currentAccountsNumber++;
            this.scanner = scanner;
            //numform.setRoundingMode(RoundingMode.UNNECESSARY);
        }

        public void mainUI()
        {
            boolean running = true;
            while(running)
            {   
                clearTerminal();
                System.out.println("Available accounts: ");    
                for(int i = 0; i < currentAccountsNumber; ++i)
                {
                    System.out.println((i + 1) + ". " + accountsList.get(i).getID() + " " + accountsList.get(i).getName());
                }
                System.out.println("Enter account number to log in or enter \"0\" to create new, \"-1\" to terminate");
                try
                {
                    int option = scanner.nextInt();
                    scanner.nextLine();
                    if(option == -1)
                        break;
                    else if(option == 0)
                    {
                        this.accountsList.add(new Account());
                        currentAccountsNumber++;
                    }
                    else if(option > 0 && option <= currentAccountsNumber)
                    {
                        draw_window(accountsList.get(option - 1), option - 1);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Enter valid number");
                    }
                }
                catch(IllegalArgumentException | InputMismatchException e)
                {
                    System.out.println("Enter valid number");
                    scanner.nextLine();
                }                
                
            }
        }

        public void draw_window(Account account, int index)
        {
            active = true;
            while(active)
            {
                try
                {
                    clearTerminal();
                    System.out.println("Account №" + ConsoleColors.GREEN_BOLD + account.getID() + 
                                ConsoleColors.RESET + " " + account.getName() +"\n");
                    System.out.println("""
                                1.Deposit
                                2.Withdrawal
                                3.Check Balance
                                4.Transaction List
                                5.Transfer
                                6.Set name
                                (-1).exit
                                """);
                    System.out.println(ConsoleColors.GREEN + "Enter option number:" + ConsoleColors.RESET);
                    int window_num = scanner.nextInt();
                    scanner.nextLine();

                    if(window_num > 6 || window_num < -1)
                        throw new IllegalArgumentException("Exception: Enter valid option number");

                    switch(window_num)
                    {
                        case 0:// main window 
                        {
                            clearTerminal();
                            System.out.println("Account №" + ConsoleColors.GREEN_BOLD + account.getID() + 
                            ConsoleColors.RESET + " " + account.getName() + "\n");
                            System.out.println("""
                            1.Deposit
                            2.Withdrawal
                            3.Check Balance
                            4.Transaction List
                            5.Transfer
                            6.Set name
                            (-1).exit
                            """);
                            break;
                        }

                        case 1:// deposit
                        {
                            clearTerminal();
                            System.out.println("Enter value to deposit(-1 to exit): ");

                            boolean successfulOperation = false;
                            while(!successfulOperation)
                            {
                                try
                                {
                                    double value = scanner.nextDouble(); // <---
                                    scanner.nextLine();
                                    if(value == -1)
                                        break;
                                    else if(isValueValid(value))
                                    {
                                        Transaction current = account.deposit(value);
                                        successfulOperation = true;
                                        printTransaction(current);
                                        System.out.println("Enter any key to proceed");
                                        scanner.nextLine();
                                    }
                                    else
                                        throw new IllegalArgumentException(); // goes to lower catch block
                                }
                                catch(IllegalArgumentException | InputMismatchException e)
                                {
                                    System.out.println(ConsoleColors.RED_BOLD + 
                                        "Enter valid value(-1 to exit) ///inner catch" + ConsoleColors.RESET);
                                    scanner.nextLine();
                                }
                            }
                            System.out.println("Enter 0 to return to the menu");
                            break;
                        }

                        case 2:// withdrawal
                        {
                            clearTerminal();
                            System.out.println("Enter value to withdraw(-1 to exit): ");

                            boolean successfulOperation = false;
                            while(!successfulOperation)
                            {
                                try
                                {
                                    double value = scanner.nextDouble();
                                    scanner.nextLine();
                                    
                                    if(value == -1)
                                        break;
                                    else if(isValueValid(value))
                                    {
                                        Transaction current = account.withdrawal(value);
                                        if(!current.successful)
                                            System.out.println(ConsoleColors.RED_BOLD + 
                                                "Insufficient funds in the account. Try again(-1 to exit)" + ConsoleColors.RESET);
                                        else
                                        {
                                            successfulOperation = true;
                                            printTransaction(current);
                                            System.out.println( );
                                            scanner.nextLine();
                                        }
                                    }
                                    else
                                        throw new IllegalArgumentException();
                                }
                                catch(IllegalArgumentException | InputMismatchException e)
                                {
                                    System.out.println("Enter valid value(-1 to exit) ///inner catch");
                                        scanner.nextLine();/// double input issue
                                }
                            }
                            break;
                        }

                        case 3:// check_balance
                        {
                            clearTerminal();
                            System.out.println("Current balance is: " + ConsoleColors.CYAN_BOLD + 
                                numform.format(account.getBalance()) + ConsoleColors.RESET + " RUB");
                            System.out.println("Enter any key to proceed:");
                            scanner.nextLine();
                            break;
                        }

                        case 4:// transaction list
                        {
                            clearTerminal();
                            System.out.println("Account №" + ConsoleColors.CYAN_BOLD +  account.getID()+ ConsoleColors.RESET + " transaction list: ");
                            Transaction current;
                            List<Transaction> transactionList = account.getTransactions();
                            if(transactionList.isEmpty())
                                System.out.println("No transactions yet");
                            else
                            {
                                for(int i = 0; i < account.transactionList.size(); i++)
                                {
                                    current = transactionList.get(i);
                                    printTransaction(current);
                                }
                            }
                            System.out.println(ConsoleColors.BLUE_BOLD + 
                                "\nEnter \"s\" to search transactions by attributes or enter any key to proceed" + ConsoleColors.RESET);
                            String option = scanner.nextLine();
                            if("s".equals(option))
                            {
                                System.out.println("""
                                1.Show transactions greater than int(x).
                                    (Enter: '1.x')
                                2.Show transactions less than int(x).
                                    (Enter: 2.x)
                                3.Show successful/unsuccessful transactions.
                                    (Enter 3.S or 3.U)
                                4.Show all Deposit transactions.
                                    Enter(4)
                                5.Show all Withdrawal transactions
                                    Enter(5)
                                6.Show all transfer transactions
                                    Enter(6)
                                7.Exit.
                                    (Enter -1)  
                                        """);

                                String optionInput = scanner.nextLine();
                                
                                if(optionInput.equals("-1"))
                                    break;
                                
                                char optionNumber = optionInput.charAt(0);

                                switch(optionNumber) // attribute search
                                {
                                    case '1': 
                                    {
                                        double x;
                                        if(optionInput.length() > 1 && optionInput.contains("."))
                                        {
                                            try
                                            {
                                                x = Double.parseDouble((optionInput.substring(2)));

                                                if(transactionList.isEmpty())
                                                    System.out.println("No transactions yet");
                                                else
                                                {
                                                    boolean isEmpty = true;
                                                    for(int i = 0; i < account.transactionList.size(); i++)
                                                    {
                                                        current = transactionList.get(i);
                                                        if(current.value > x)
                                                        {
                                                            printTransaction(current);
                                                            isEmpty = false;
                                                        }
                                                    }
                                                    if(isEmpty)
                                                        System.out.println("No matching transactions found");
                                                }
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                System.out.println(ConsoleColors.RED_BOLD + "Incorrect input" + ConsoleColors.RESET);
                                                break;
                                            }
                                        }
                                        else
                                        {
                                            throw new InputMismatchException(ConsoleColors.RED_BOLD + "Incorrect input" + ConsoleColors.RESET);
                                        }
                                        break;
                                    }

                                    case '2': 
                                    {
                                        double x;
                                        if(optionInput.length() > 1 && optionInput.contains("."))
                                        {
                                            try
                                            {
                                                x = Double.parseDouble((optionInput.substring(2)));

                                                if(transactionList.isEmpty())
                                                    System.out.println("No transactions yet");
                                                else
                                                {
                                                    boolean isEmpty = true;
                                                    for(int i = 0; i < account.transactionList.size(); i++)
                                                    {
                                                        current = transactionList.get(i);
                                                        if(current.value < x)
                                                        {
                                                            printTransaction(current);
                                                            isEmpty = false;
                                                        }
                                                    }
                                                    if(isEmpty)
                                                        System.out.println("No matching transactions found");
                                                }
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                System.out.println(ConsoleColors.RED_BOLD + "Incorrect input" + ConsoleColors.RESET);
                                                break;
                                            }
                                        }
                                        else
                                        {
                                            throw new InputMismatchException(ConsoleColors.RED_BOLD + "Incorrect input" + ConsoleColors.RESET);
                                        }
                                        break;
                                    }
                                    case '3':
                                    {  
                                        if(optionInput.equals("3.U") || optionInput.equals("3.S"))
                                        {
                                            boolean successfulTransactionsSearch;
                                            successfulTransactionsSearch = optionInput.charAt(2) != 'U';

                                            if(transactionList.isEmpty())
                                                System.out.println("No transactions yet");
                                            else
                                            {
                                                boolean isEmpty = true;
                                                for(int i = 0; i < account.transactionList.size(); i++)
                                                {
                                                    current = transactionList.get(i);
                                                    if(current.successful == successfulTransactionsSearch)
                                                    {
                                                        printTransaction(current);
                                                        isEmpty = false;
                                                    }
                                                }
                                                if(isEmpty)
                                                    System.out.println("No matching transactions found");
                                            }
                                        }
                                        else
                                            throw new InputMismatchException(ConsoleColors.RED_BOLD + "Incorrect input" + ConsoleColors.RESET);
                                        break;
                                    }

                                    case '4':
                                    {
                                        if(transactionList.isEmpty())
                                                System.out.println("No transactions yet");
                                        else
                                        {
                                            boolean isEmpty = true;

                                            for(int i = 0; i <  account.transactionList.size(); ++i)
                                            {
                                                current = transactionList.get(i);
                                                if(current.operation.contains("Deposit"))
                                                {
                                                    printTransaction(current);
                                                    isEmpty = false;
                                                }
                                            }
                                            if(isEmpty)
                                                System.out.println("No matching transactions found");
                                        }
                                        break;
                                    }

                                    case '5':
                                    {
                                        if(transactionList.isEmpty())
                                                System.out.println("No transactions yet");
                                        else
                                        {
                                            boolean isEmpty = true;

                                            for(int i = 0; i <  account.transactionList.size(); ++i)
                                            {
                                                current = transactionList.get(i);
                                                if(current.operation.contains("Withdrawal"))
                                                {
                                                    printTransaction(current);
                                                    isEmpty = false;
                                                }
                                            }
                                            if(isEmpty)
                                                System.out.println("No matching transactions found");
                                        }
                                        break;
                                    }

                                    case '6':
                                    {

                                    }
                                }

                            }
                            else
                            {
                                break;
                            }

                            System.out.println("Enter any key to proceed:");
                            scanner.nextLine();

                            break;
                        }

                        case 5: //transfer
                        {
                            clearTerminal();    
                            System.out.println("Accounts available for transfer: ");    
                            if(currentAccountsNumber < 2)
                            {
                                System.out.println("No other accounts to transfer");
                                System.out.println("Enter any key to proceed:");
                                scanner.nextLine();
                                break;
                            }
                            for(int i = 0; i < currentAccountsNumber; ++i)
                            {
                                if(i != index)
                                    System.out.println((i + 1) + ". " + accountsList.get(i).getID());
                            }    
                            System.out.println("Enter account number: (-1 to exit)");
                            try
                            {   
                                int option = scanner.nextInt();
                                scanner.nextLine();
                                if(option == -1)
                                    break;
                                else if(option > 0 && option <= currentAccountsNumber && option != index + 1)
                                {
                                    try
                                    {
                                        System.out.println("Enter value to transfer");
                                        double value = scanner.nextDouble();
                                        scanner.nextLine();
                                        if(isValueValid(value))
                                        {
                                            Transaction current = account.transfer(value, accountsList.get(option - 1));
                                            printTransaction(current);
                                            System.out.println("Enter any key to proceed");
                                            scanner.nextLine();
                                        }
                                        else
                                            throw new IllegalArgumentException("Enter valid value");

                                    }
                                    catch(IllegalArgumentException | InputMismatchException e)
                                    {
                                        System.out.println("Invalid option");
                                        System.out.println("Enter any key to proceed");
                                            scanner.nextLine();
                                    }
                                }
                                else
                                {
                                    throw new IllegalArgumentException("Invalid number");
                                }
                            }
                            catch(IllegalArgumentException | InputMismatchException e)
                            {
                                System.out.println("Invalid number");
                                System.out.println("Enter any key to proceed");
                                scanner.nextLine();
                            }
                            break;
                        }

                        case 6:
                        {
                            System.out.println("Enter your username");
                            try{
                                String name = scanner.nextLine();
                                account.name = name;
                                System.out.println("Name was succesfully set");
                                System.out.println("Enter any key to proceed");
                                scanner.nextLine();
                            }
                            catch(InputMismatchException | IllegalArgumentException e)
                            {
                                System.out.println("Invalid input");
                                System.out.println("Enter any key to proceed");
                                scanner.nextLine();
                            }
                            

                        }

                        case -1: //exit
                        {
                            active = false;
                            break;
                        }
                    }
                }
                catch(InputMismatchException e)
                {
                    System.out.println(ConsoleColors.RED_BOLD + 
                        "Exception: Enter valid option number ///   " + ConsoleColors.RESET);
                    scanner.nextLine();
                }
                catch(IllegalArgumentException e)
                {
                    System.out.println(e.getMessage());
                }
            }

        }

        public void printTransaction(Transaction current)
        {
            if(current.successful)
            System.out.println(current.operation + numform.format(current.value)
                + " RUB "+ ConsoleColors.GREEN_BOLD +  "SUCCESSFUL " + ConsoleColors.RESET + current.date_time + " " + current.transferInfo);
            else
            System.out.println(current.operation + numform.format(current.value)
                + " RUB " + ConsoleColors.RED_BOLD +  "FAILED " + ConsoleColors.RESET + current.date_time + " " + current.transferInfo);
        }

        public boolean isValueValid(double value)
        {
            String val = Double.toString(value);
            ValidateNumber.ExceptionInfo info = ValidateNumber.validateValue(val);
            if(info.isValid)
                return true;
            else
            {
                System.out.println(info.message);
                return false;
            }
        }

        public class InvalidInputException extends RuntimeException
        {
            public InvalidInputException()
            {
                super("Enter correct option number");
            }
        }

        public static void clearTerminal()
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

    public static class ValidateNumber
    {
        private static final BigDecimal MIN_VALUE = new BigDecimal("0");
        private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000000000"); // max = 10^15
        private static final int MAX_DECIMALS = 4;

        public static ExceptionInfo validateValue(String input)
        {
            BigDecimal value = new BigDecimal(input);
            if(value.compareTo(MIN_VALUE) <= 0)
            {
                return new ExceptionInfo(false, "\nException: Positive value expected");
            }

            if(value.compareTo(MAX_VALUE) > 0)
            {
                return new ExceptionInfo(false, "\nException: Amount can not exceed 10^15");
            }

            if(value.scale() > MAX_DECIMALS)
            {
                return new ExceptionInfo(false, 
                    "\nException: Maximum " + MAX_DECIMALS + " decimal places allowed");
            }

            if(!(input.trim().matches("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?")))
            {
                return new ExceptionInfo(false,
                    "\nException: Double value expected");
            }
            return new ExceptionInfo(true, "VALID");

        }

        public static class ExceptionInfo
        {

            public ExceptionInfo(boolean isValid, String message) 
            {
                this.isValid = isValid;
                this.message = message;
            }
            
            boolean isValid;
            String message;
        }

    }

    public class ConsoleColors {
        public static final String RESET = "\033[0m";
        public static final String BLACK = "\033[0;30m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String WHITE = "\033[0;37m";
        public static final String BLACK_BOLD = "\033[1;30m";
        public static final String RED_BOLD = "\033[1;31m";
        public static final String GREEN_BOLD = "\033[1;32m";
        public static final String YELLOW_BOLD = "\033[1;33m";
        public static final String BLUE_BOLD = "\033[1;34m";
        public static final String PURPLE_BOLD = "\033[1;35m";
        public static final String CYAN_BOLD = "\033[1;36m";
        public static final String WHITE_BOLD = "\033[1;37m";
    }
}
