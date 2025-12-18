import java.io.*;
import java.net.*;
import java.util.*;

public class TerminalChatClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 34567);
        Scanner keyboard = new Scanner(System.in);
        PrintStream out = new PrintStream(socket.getOutputStream());

        System.out.print("Enter user tag: ");
        out.println(keyboard.nextLine());

        new Thread(() -> {
            try {
                Scanner in = new Scanner(socket.getInputStream());
                while (in.hasNextLine()) {
                    System.out.println(in.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        while (keyboard.hasNextLine()) {
            out.println(keyboard.nextLine());
        }
    }
}
