package gui;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;

import client.FileSender;
import client.FileTransferClient;

public class ChatGui extends JFrame {

    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private String welcomeMessage;

    public ChatGui() {
        username = JOptionPane.showInputDialog(this,
                "Enter username: ",
                "KEYRYX Login", JOptionPane.QUESTION_MESSAGE);

        if (username == null || username.trim().isEmpty()) {
                return;
        }
        username = username.trim();

        Socket socket;

        try {
            socket = new Socket("localHost", 1000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(username);

            //reader
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            welcomeMessage= reader.readLine();
            if("Login Failed".equals(welcomeMessage))
            {
                JOptionPane.showMessageDialog(this,
                        "Username is already taken or invalid",
                        "Login Failed!", JOptionPane.ERROR_MESSAGE);

                socket.close();
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Connected to the server!",
                    "Connection Successful!", JOptionPane.INFORMATION_MESSAGE);


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the server!",
                    "connection failed!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ImageIcon image = new ImageIcon(
                ChatGui.class.getResource("img.png"));
        this.setIconImage(image.getImage());

        this.setTitle("Keyryx | A LAN Chat Application");
        this.setSize(720, 420);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                   socket.close();
                } catch (IOException ex) {
                    System.out.println("Error closing connection.");
                }
            }
        });
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        JPanel usersPanel = new JPanel(new BorderLayout());
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        JPanel sidebarPanel = new JPanel(new BorderLayout(5, 5));
        sidebarPanel.setPreferredSize(new Dimension(130, 0));

        //adding border to all panels
        headerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        sidebarPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        statusPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        usersPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel statusTitle = new JLabel("Connection Status");
        statusTitle.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel connectionLabel = new JLabel("Staus: Connected");
        JLabel usernameLabel = new JLabel("User: " + username);
        JLabel serverLabel = new JLabel("Server: localhost:1000");

        statusPanel.add(statusTitle);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(connectionLabel);
        statusPanel.add(usernameLabel);
        statusPanel.add(serverLabel);

        sidebarPanel.add(statusPanel, BorderLayout.NORTH);

        JLabel userTitle = new JLabel("Online Users", SwingConstants.CENTER);
        userTitle.setFont(new Font("Arial", Font.BOLD, 14));

        DefaultListModel<String> userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);

        userListModel.addElement("No users online");

        JScrollPane userScrollPane = new JScrollPane(userList);
        usersPanel.add(userTitle, BorderLayout.NORTH);
        usersPanel.add(userScrollPane, BorderLayout.CENTER);

        sidebarPanel.add(usersPanel, BorderLayout.CENTER);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(sidebarPanel, BorderLayout.WEST);

        JLabel chatLabel = new JLabel("Chat room — Everyone");
        chatLabel.setFont(new Font("Arial", Font.BOLD, 15));
        chatLabel.setBorder(BorderFactory.createEmptyBorder
                (8, 10, 8, 5));

        //reciepent selection listener
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();

                if (selectedUser != null &&
                        !selectedUser.equals("No users online")) {
                    chatLabel.setText("Private Chat — " + selectedUser);
                } else {
                    chatLabel.setText("Chatroom — Everyone");
                }
            }
        });

        //group chat label
        JButton groupChatButton= new JButton("Group Chat");
        groupChatButton.setFocusable(false);

        groupChatButton.addActionListener(e->{
            userList.clearSelection();
            chatLabel.setText("Chatroom — Everyone");
        });
        chatLabel.setText("Chatroom — Everyone");

        JPanel chatHeader= new JPanel(new BorderLayout());
        chatHeader.add(chatLabel, BorderLayout.CENTER);
        chatHeader.add(groupChatButton, BorderLayout.EAST);

        chatPanel.add(chatHeader, BorderLayout.NORTH);


        //message area

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.append(welcomeMessage+ "\n");

        JScrollPane messageScroll = new JScrollPane(messageArea);
        chatPanel.add(messageScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JTextField messageInput = new JTextField();

        //file receiver thread
        Thread fileReceiverThread = new Thread(() -> {
            FileTransferClient fileClient = new FileTransferClient();

            fileClient.connectAndListen("localhost", username,
                    message -> SwingUtilities.invokeLater(() ->
                            messageArea.append("[FILE] " + message + "\n")));
        });

        fileReceiverThread.setDaemon(true);
        fileReceiverThread.start();


        //send buttton
        JButton sendButton = new JButton("Send");
        sendButton.setFocusable(false);

        //send fileButton
        JButton sendFileButton= new JButton("Send File");
        sendFileButton.setFocusable(false);

        //open file buuton
        JButton openFileButton= new JButton("Open file");
        sendButton.setFocusable(false);


        JPanel buttonPanel= new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);
        buttonPanel.add(openFileButton);

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        //send-button actionlistner
        sendButton.addActionListener(e -> {
            String message = messageInput.getText().trim();

            if (!message.isEmpty()) {

                String recipient= userList.getSelectedValue();
                if(recipient==null || recipient.equals("No users online"))
                {
                    recipient= "ALL";
                }
                writer.println(recipient+ "|"+ message);
                // messageArea.append("You: "+message+ "\n");
                messageInput.setText("");
            }
        });
        messageInput.addActionListener(e -> sendButton.doClick());

        //sendFile button actionlister
        sendFileButton.addActionListener(e->{

            String recipient= userList.getSelectedValue();

            if(recipient==null || recipient.equals("No users online")
            ||recipient.equals(username))
            {
                JOptionPane.showMessageDialog(this,
                        "Please select another user first","Select Recipient",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            JFileChooser fileChooser= new JFileChooser();
            int result= fileChooser.showOpenDialog(this);

            if(result== JFileChooser.APPROVE_OPTION)
            {
                File selectedFile= fileChooser.getSelectedFile();

                Thread fileSendingThread = new Thread(() -> {
                    FileSender sender = new FileSender();

                    boolean success = sender.sendFile(
                            "localhost", recipient, selectedFile);

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            messageArea.append("[FILE] Sent "
                                    + selectedFile.getName()
                                    + " to " + recipient + "\n");
                        } else {
                            messageArea.append("[FILE] Failed to send "
                                    + selectedFile.getName() + "\n");
                        }
                    });
                });

                fileSendingThread.start();
            }

        });

        //open file buttons actionlistner
        openFileButton.addActionListener(actionEvent -> {
            File receivedFolder = new File("received_files");

            if (!receivedFolder.exists()) {
                JOptionPane.showMessageDialog(this,
                        "No received files folder found.",
                        "Open File",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser(receivedFolder);

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    Desktop.getDesktop().open(selectedFile);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Could not open the file: " + ex.getMessage(),
                            "Open File Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.add(chatPanel, BorderLayout.CENTER);

        //receiver thread
        Thread receiverThread = new Thread(() -> {
            try {
                String message;

                while ((message = reader.readLine()) != null) {
                    String receivedMessage = message;
                    SwingUtilities.invokeLater(() -> {
                        if (receivedMessage.startsWith("USERS|")) {
                            userListModel.clear();

                            String names = receivedMessage.substring(6);

                            if (names.isEmpty()) {
                                userListModel.addElement("No users online");
                            } else {
                                for (String name : names.split(",")) {
                                    if (!name.isEmpty()) {
                                        userListModel.addElement(name);
                                    }
                                }
                            }
                        } else if(receivedMessage.startsWith("HISTORY|")){
                            String oldMessage=receivedMessage.substring(8);
                            messageArea.append("[previous] "+oldMessage+ " \n");
                        }

                        else {
                            messageArea.append(receivedMessage + "\n");
                        }
                    });
                }
            } catch (IOException e) {

                System.out.println("Disconnected from server!");
            }
        });

        receiverThread.start();

        JLabel title = new JLabel("<html><center>" + "KEYRYX" + "<font size='4'>"
                + "<br>Every node has a voice" +
                "</font></center></html>", SwingConstants.CENTER);

        title.setFont(new Font("Arial", Font.BOLD, 24));

        headerPanel.add(title);

        this.setVisible(true);
    }

    public static void main(String[] args) {
        ChatGui chatgui = new ChatGui();
    }
}