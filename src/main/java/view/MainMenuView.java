package main.java.view;
import java.util.Scanner;

public class MainMenuView {
    private final Scanner scanner = new Scanner(System.in);

    public int menu() {
        System.out.println("\n\n================================================================");
        System.out.println("             CCCCCCC  TTTTTTTT  RRRRRRR   LL");
        System.out.println("            CC           TT     RR   RR   LL");
        System.out.println("           CC            TT     RRRRRRR   LL");
        System.out.println("           CC            TT     RR  RR    LL");
        System.out.println("            CC           TT     RR   RR   LL");
        System.out.println("             CCCCCCC     TT     RR    RR  LLLLLLLL");
        System.out.println("================================================================");
        System.out.println("\nSelection:");
        System.out.println("1. Staff");
        System.out.println("2. Item");
        System.out.println("3. Order");
        System.out.println("4. Transaction");
        System.out.println("5. Exit");
        System.out.print("\nEnter your selection: ");
        return scanner.nextInt();
    }
}

