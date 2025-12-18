import javax.swing.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class SwingChatWindow {

    private JTextPane chatPane = new JTextPane();
    private JTextField inputField = new JTextField();
    private PrintWriter out;
    private BufferedReader in;

    public SwingChatWindow() {
        JFrame frame = new JFrame("Swing Chat Window");
        frame.setSize(760, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setBackground(new Color(240, 240, 240));

        JButton sendBtn = new JButton("Send");

        sendBtn.addActionListener(e -> send());

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.add(sendBtn, BorderLayout.EAST);

        connectToServer();
        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 34567);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String tag = JOptionPane.showInputDialog("Choose a user tag:");
            out.println(tag);

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        append(msg);
                    }
                } catch (IOException ignored) {}
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Server not available");
        }
    }

    private void send() {
        if (!inputField.getText().trim().isEmpty()) {
            out.println(inputField.getText());
            inputField.setText("");
        }
    }

    private void append(String msg) {
        HTMLDocument doc = (HTMLDocument) chatPane.getDocument();
        try {
            ((HTMLEditorKit) chatPane.getEditorKit())
                    .insertHTML(doc, doc.getLength(), msg + "<br/>", 0, 0, null);
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        new SwingChatWindow();
    }
}
