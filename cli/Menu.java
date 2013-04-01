package cli;

import java.util.Scanner;

/**
 * A small class just for testing before the completion of the graphical interface.
 */
public class Menu
{
    public final static int backup = 1;
    public final static int restore = 2;
    public final static int delete = 3;
    public final static int exit = 4;

    public static int ask()
    {
        System.out.println("Distributed Backup");
        System.out.println("1 - Backup");
        System.out.println("2 - Restore");
        System.out.println("3 - Delete");
        System.out.println("4 - Exit");

        int option;
        Scanner in = new Scanner(System.in);
        option = in.nextInt();


        if (option >= 1 && option <= 4)
        {
            return option;
        }
        else
        {
            throw new IllegalStateException();
        }
    }
}
