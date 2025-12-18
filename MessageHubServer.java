import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.awt.Color;

public class MessageHubServer {

    private int port;
    private List<ConnectedUser> users = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new MessageHubServer(34567).startServer();
    }

    public MessageHubServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("MessageHub Server running on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            Scanner sc = new Scanner(socket.getInputStream());

            String userTag = sc.nextLine().replace(" ", "_");

            ConnectedUser user = new ConnectedUser(socket, userTag);
            users.add(user);

            user.send("<b>Welcome " + user + "</b>");
            broadcastUserList();

            new Thread(new ClientProcessor(this, user)).start();
        }
    }

    public void removeUser(ConnectedUser user) {
        users.remove(user);
        broadcastUserList();
    }

    public void broadcast(String message, ConnectedUser sender) {
        for (ConnectedUser u : users) {
            u.send(sender + ": " + message);
        }
    }

    public void broadcastUserList() {
        for (ConnectedUser u : users) {
            u.send(users.toString());
        }
    }

    public void sendPrivate(String message, ConnectedUser sender, String target) {
        for (ConnectedUser u : users) {
            if (u.getUserTag().equals(target)) {
                u.send("<i>Private from " + sender + "</i>: " + message);
                sender.send("<i>Private to " + u + "</i>: " + message);
                return;
            }
        }
        sender.send("<b>User not found</b>");
    }
}

class ClientProcessor implements Runnable {

    private MessageHubServer server;
    private ConnectedUser user;

    public ClientProcessor(MessageHubServer server, ConnectedUser user) {
        this.server = server;
        this.user = user;
    }

    public void run() {
        Scanner sc = new Scanner(user.getInputStream());

        while (sc.hasNextLine()) {
            String msg = sc.nextLine();

            if (msg.startsWith("@")) {
                int space = msg.indexOf(" ");
                if (space > 0) {
                    server.sendPrivate(
                            msg.substring(space + 1),
                            user,
                            msg.substring(1, space)
                    );
                }
            } else if (msg.startsWith("#")) {
                user.changeNameColor(msg);
                server.broadcastUserList();
            } else {
                server.broadcast(msg, user);
            }
        }
        server.removeUser(user);
        sc.close();
    }
}

class ConnectedUser {

    private static int COUNTER = 0;
    private PrintStream out;
    private InputStream in;
    private String userTag;
    private String color;

    public ConnectedUser(Socket socket, String userTag) throws IOException {
        this.out = new PrintStream(socket.getOutputStream());
        this.in = socket.getInputStream();
        this.userTag = userTag;
        this.color = ColorWheel.pickColor(COUNTER++);
    }

    public void changeNameColor(String hex) {
        if (Pattern.matches("#[0-9a-fA-F]{6}", hex)) {
            Color c = Color.decode(hex);
            double brightness = 0.2126 * c.getRed()
                    + 0.7152 * c.getGreen()
                    + 0.0722 * c.getBlue();
            if (brightness < 160) {
                color = hex;
                send("<b>Color changed</b>");
                return;
            }
        }
        send("<b>Invalid color</b>");
    }

    public void send(String msg) {
        out.println(msg);
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getUserTag() {
        return userTag;
    }

    public String toString() {
        return "<span style='color:" + color + "'><b>" + userTag + "</b></span>";
    }
}

class ColorWheel {
    static String[] COLORS = {
            "#1abc9c", "#3498db", "#9b59b6",
            "#e67e22", "#e74c3c", "#2ecc71"
    };

    static String pickColor(int i) {
        return COLORS[i % COLORS.length];
    }
}
