package gui;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import client.FileSender;
import client.FileTransferClient;
import notification.ChatNotification;
import notification.AppNotification;

/**
 * KEYRYX desktop client.
 * The visual layer is redesigned while the existing server and file-transfer
 * wire protocols remain unchanged.
 */
public class ChatGui extends JFrame {

    private static final Color BG = new Color(9, 14, 29);
    private static final Color SIDEBAR = new Color(17, 26, 47);
    private static final Color CHAT = new Color(13, 22, 41);
    private static final Color SURFACE = new Color(27, 39, 65);
    private static final Color BUBBLE_IN = new Color(35, 49, 76);
    private static final Color GREEN = new Color(108, 92, 231);
    private static final Color MINT = new Color(103, 232, 249);
    private static final Color TEXT = new Color(241, 245, 255);
    private static final Color MUTED = new Color(153, 169, 199);
    private static final Color BORDER = new Color(45, 61, 94);

    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private String serverIP;
    private String welcomeMessage;
    private TrayIcon trayIcon;
    private Socket socket;

    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JPanel messageList;
    private JScrollPane messageScroll;
    private JTextField messageInput;
    private JLabel chatTitle;
    private JLabel chatSubtitle;
    private JLabel connectionStatus;
    private CardLayout contentCards;
    private JPanel contentPanel;
    private String pendingMessageHeader;
    private String pendingMessageType;
    private boolean connected;
    private final java.util.List<JButton> navigationButtons = new ArrayList<JButton>();

    public ChatGui() {
        showSplashScreen();
        if (!loginAndConnect()) {
            return;
        }
        configureWindow();
        buildInterface();
        startFileReceiver();
        startMessageReceiver();
        setVisible(true);
    }

    private boolean loginAndConnect() {
        username = JOptionPane.showInputDialog(null, "Choose a username:",
                "KEYRYX • Welcome", JOptionPane.QUESTION_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        username = username.trim();

        serverIP = JOptionPane.showInputDialog(null, "Enter the server IP address:",
                "Connect to KEYRYX", JOptionPane.QUESTION_MESSAGE);
        if (serverIP == null || serverIP.trim().isEmpty()) {
            return false;
        }
        serverIP = serverIP.trim();

        try {
            socket = new Socket(serverIP, 1000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(username);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            welcomeMessage = reader.readLine();

            if ("Login Failed".equals(welcomeMessage)) {
                JOptionPane.showMessageDialog(null,
                        "That username is already in use or invalid.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                socket.close();
                return false;
            }
            connected = true;
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to " + serverIP + " on port 1000.\n"
                            + ex.getMessage(),
                    "Connection failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void configureWindow() {
        ImageIcon icon = new ImageIcon(ChatGui.class.getResource("img.png"));
        setIconImage(icon.getImage());
        setTitle("KEYRYX | Every node has a voice");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        if (SystemTray.isSupported()) {
            try {
                trayIcon = new TrayIcon(icon.getImage(), "KEYRYX");
                trayIcon.setImageAutoSize(true);
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException ex) {
                System.out.println("Could not add tray icon: " + ex.getMessage());
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                connected = false;
                try {
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    System.out.println("Error closing connection: " + ex.getMessage());
                }
                if (trayIcon != null) {
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            }
        });
    }

    private void buildInterface() {
        setLayout(new BorderLayout());
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildNavigationRail(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(BG);
        workspace.add(buildConversationSidebar(), BorderLayout.WEST);

        contentCards = new CardLayout();
        contentPanel = new JPanel(contentCards);
        contentPanel.setBackground(CHAT);
        contentPanel.add(buildChatView(), "chats");
        contentPanel.add(buildContactsView(), "contacts");
        contentPanel.add(buildSettingsView(), "settings");
        workspace.add(contentPanel, BorderLayout.CENTER);

        root.add(workspace, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildNavigationRail() {
        JPanel rail = new JPanel();
        rail.setBackground(new Color(7, 11, 24));
        rail.setPreferredSize(new Dimension(72, 0));
        rail.setBorder(new EmptyBorder(22, 10, 18, 10));
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("K", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 25));
        brand.setForeground(TEXT);
        brand.setOpaque(true);
        brand.setBackground(GREEN);
        brand.setPreferredSize(new Dimension(48, 48));
        brand.setMaximumSize(new Dimension(48, 48));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        rail.add(brand);
        rail.add(Box.createVerticalStrut(38));

        rail.add(navButton("Chats", "◈", "chats", true));
        rail.add(Box.createVerticalStrut(12));
        rail.add(navButton("Contacts", "♙", "contacts", false));
        rail.add(Box.createVerticalGlue());

        JLabel railStatus = new JLabel("●", SwingConstants.CENTER);
        railStatus.setForeground(MINT);
        railStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        rail.add(railStatus);
        rail.add(Box.createVerticalStrut(14));
        rail.add(navButton("Settings", "⚙", "settings", false));
        return rail;
    }

    private JButton navButton(String tooltip, String symbol, final String card, boolean active) {
        JButton button = new JButton("<html><center><font size='5'>" + symbol
                + "</font></center></html>");
        button.setToolTipText(tooltip);
        button.setFont(new Font("SansSerif", Font.PLAIN, 20));
        button.setForeground(active ? MINT : MUTED);
        button.setBackground(active ? SURFACE : new Color(7, 11, 24));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11, 10, 11, 10));
        button.setMaximumSize(new Dimension(50, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHover(button, active ? SURFACE : new Color(7, 11, 24),
                active ? new Color(48, 62, 99) : new Color(25, 37, 64));
        navigationButtons.add(button);
        button.addActionListener(e -> {
            contentCards.show(contentPanel, card);
            setActiveNavigation(button);
        });
        return button;
    }

    private void setActiveNavigation(JButton selected) {
        for (JButton button : navigationButtons) {
            boolean active = button == selected;
            button.setForeground(active ? MINT : MUTED);
            button.setBackground(active ? SURFACE : new Color(7, 11, 24));
            button.setBorder(active
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 3, 0, 0, MINT),
                            BorderFactory.createEmptyBorder(11, 7, 11, 10))
                    : BorderFactory.createEmptyBorder(11, 10, 11, 10));
        }
    }

    private JPanel buildConversationSidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 14));
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(292, 0));
        side.setBorder(new EmptyBorder(24, 18, 18, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel brandRow = new JPanel(new BorderLayout());
        brandRow.setOpaque(false);
        JLabel brand = new JLabel("KEYRYX");
        brand.setForeground(TEXT);
        brand.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel live = new JLabel("● LIVE");
        live.setForeground(MINT);
        live.setFont(new Font("SansSerif", Font.BOLD, 10));
        brandRow.add(brand, BorderLayout.WEST);
        brandRow.add(live, BorderLayout.EAST);
        top.add(brandRow);

        JLabel tagline = new JLabel("EVERY NODE HAS A VOICE");
        tagline.setForeground(MUTED);
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 9));
        top.add(Box.createVerticalStrut(4));
        top.add(tagline);
        top.add(Box.createVerticalStrut(25));

        JLabel section = new JLabel("YOUR CONVERSATIONS");
        section.setForeground(MUTED);
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        top.add(section);
        top.add(Box.createVerticalStrut(10));

        JTextField search = new JTextField();
        final String searchHint = "Search people...";
        search.setText(searchHint);
        search.setForeground(MUTED);
        search.setBackground(new Color(13, 22, 41));
        search.setCaretColor(TEXT);
        search.setFont(new Font("SansSerif", Font.PLAIN, 13));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 8, 10, 8)));
        top.add(search);
        search.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (search.getText().trim().equals(searchHint)) {
                    search.setText("");
                    search.setForeground(TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (search.getText().trim().isEmpty()) {
                    search.setText(searchHint);
                    search.setForeground(MUTED);
                }
            }
        });
        side.add(top, BorderLayout.NORTH);

        userListModel = new DefaultListModel<String>();
        userList = new JList<String>(userListModel);
        userList.setBackground(SIDEBAR);
        userList.setForeground(TEXT);
        userList.setSelectionBackground(SURFACE);
        userList.setSelectionForeground(MINT);
        userList.setFixedCellHeight(64);
        userList.setBorder(new EmptyBorder(4, 0, 4, 0));
        userList.setCellRenderer(new UserCellRenderer());
        userListModel.addElement("No users online");

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSelectedConversation();
        });

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = search.getText().trim().toLowerCase();
                if (query.equals(searchHint.toLowerCase())) return;
                for (int i = 0; i < userListModel.size(); i++) {
                    if (userListModel.get(i).toLowerCase().contains(query)) {
                        userList.setSelectedIndex(i);
                        return;
                    }
                }
                if (query.isEmpty()) userList.clearSelection();
            }
        });

        JScrollPane listScroll = new JScrollPane(userList);
        listScroll.setBorder(null);
        listScroll.getViewport().setBackground(SIDEBAR);
        side.add(listScroll, BorderLayout.CENTER);

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(15, 2, 0, 2)));

        JLabel avatar = avatarLabel(initials(username), 42, GREEN);
        JPanel profileText = new JPanel();
        profileText.setOpaque(false);
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(username);
        name.setForeground(TEXT);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel status = new JLabel("Connected to server");
        status.setForeground(MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 10));
        profileText.add(name);
        profileText.add(Box.createVerticalStrut(4));
        profileText.add(status);
        profile.add(avatar, BorderLayout.WEST);
        profile.add(profileText, BorderLayout.CENTER);
        side.add(profile, BorderLayout.SOUTH);

        return side;
    }

    private JPanel buildChatView() {
        JPanel chat = new JPanel(new BorderLayout());
        chat.setBackground(CHAT);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CHAT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(20, 28, 18, 28)));

        JPanel identity = new JPanel(new BorderLayout(12, 0));
        identity.setOpaque(false);
        JLabel avatar = avatarLabel("ALL", 44, GREEN);
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        chatTitle = new JLabel("Group conversation");
        chatTitle.setForeground(TEXT);
        chatTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        chatSubtitle = new JLabel("A shared space for everyone on this network");
        chatSubtitle.setForeground(MUTED);
        chatSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labels.add(chatTitle);
        labels.add(Box.createVerticalStrut(5));
        labels.add(chatSubtitle);
        identity.add(avatar, BorderLayout.WEST);
        identity.add(labels, BorderLayout.CENTER);
        header.add(identity, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        JButton groupButton = subtleButton("Group chat");
        groupButton.addActionListener(e -> {
            userList.clearSelection();
            updateSelectedConversation();
        });
        JButton filesButton = subtleButton("Files");
        filesButton.addActionListener(e -> openReceivedFile());
        actions.add(statusPill("● CONNECTED"));
        actions.add(groupButton);
        actions.add(filesButton);
        header.add(actions, BorderLayout.EAST);
        chat.add(header, BorderLayout.NORTH);

        messageList = new JPanel();
        messageList.setBackground(CHAT);
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBorder(new EmptyBorder(16, 28, 22, 28));

        messageScroll = new JScrollPane(messageList);
        messageScroll.setBorder(null);
        messageScroll.getViewport().setBackground(CHAT);
        messageScroll.getVerticalScrollBar().setUnitIncrement(18);
        chat.add(messageScroll, BorderLayout.CENTER);

        chat.add(buildComposer(), BorderLayout.SOUTH);
        appendSystemMessage(welcomeMessage == null ? "Connected to KEYRYX" : welcomeMessage);
        return chat;
    }

    private JPanel buildComposer() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(CHAT);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 24, 22, 24)));

        JPanel composer = new JPanel(new BorderLayout(10, 0));
        composer.setBackground(SURFACE);
        composer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 12, 7, 8)));

        JButton attach = new JButton("＋");
        attach.setToolTipText("Send a file");
        attach.setFont(new Font("SansSerif", Font.PLAIN, 22));
        attach.setForeground(MINT);
        attach.setBackground(SURFACE);
        attach.setBorder(null);
        attach.setFocusPainted(false);
        attach.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHover(attach, SURFACE, new Color(43, 59, 91));
        attach.addActionListener(e -> sendFile());

        messageInput = new JTextField();
        messageInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageInput.setForeground(TEXT);
        messageInput.setCaretColor(MINT);
        messageInput.setBackground(SURFACE);
        messageInput.setBorder(null);
        messageInput.setToolTipText("Write a message and press Enter");
        messageInput.addActionListener(e -> sendMessage());

        JButton send = new JButton("Send  ➤");
        send.setFont(new Font("SansSerif", Font.BOLD, 12));
        send.setForeground(Color.WHITE);
        send.setBackground(GREEN);
        send.setFocusPainted(false);
        send.setBorder(new EmptyBorder(11, 17, 11, 17));
        send.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHover(send, GREEN, new Color(133, 119, 255));
        send.addActionListener(e -> sendMessage());

        composer.add(attach, BorderLayout.WEST);
        composer.add(messageInput, BorderLayout.CENTER);
        composer.add(send, BorderLayout.EAST);
        footer.add(composer, BorderLayout.CENTER);

        JLabel hint = new JLabel("PRIVATE NETWORK  •  KEYRYX CONNECTION");
        hint.setForeground(MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hint.setBorder(new EmptyBorder(8, 4, 0, 0));
        footer.add(hint, BorderLayout.SOUTH);
        return footer;
    }

    private JPanel buildContactsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT);
        panel.setBorder(new EmptyBorder(38, 42, 38, 42));
        JLabel title = pageTitle("People on your network");
        panel.add(title, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JLabel info = new JLabel("<html><div style='width:420px'>"
                + "Your online contacts appear in the conversation list. "
                + "Select a person to start a private conversation, or use Group chat "
                + "to message everyone connected to this KEYRYX server.</div></html>");
        info.setForeground(MUTED);
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        body.add(info, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSettingsView() {
        JPanel panel = new JPanel();
        panel.setBackground(CHAT);
        panel.setBorder(new EmptyBorder(38, 42, 38, 42));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = pageTitle("Settings");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(24));

        JPanel account = settingsCard("ACCOUNT", "Signed in as " + username,
                "Your KEYRYX identity on this local network");
        account.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(account);
        panel.add(Box.createVerticalStrut(14));

        JPanel network = settingsCard("NETWORK", "Server address: " + serverIP + ":1000",
                "Connection status: " + (connected ? "Connected" : "Disconnected")
                        + "  •  File transfer port: 1001");
        network.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(network);
        panel.add(Box.createVerticalStrut(14));

        JPanel appearance = settingsCard("APPEARANCE", "Aurora Night",
                "Midnight navy surfaces with electric violet and cyan accents");
        appearance.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(appearance);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel settingsCard(String eyebrow, String heading, String detail) {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(SIDEBAR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 20, 18, 20)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(650, 105));
        JLabel small = new JLabel(eyebrow);
        small.setForeground(MINT);
        small.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel main = new JLabel(heading);
        main.setForeground(TEXT);
        main.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel sub = new JLabel(detail);
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(small);
        card.add(Box.createVerticalStrut(7));
        card.add(main);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        return card;
    }

    private JLabel pageTitle(String value) {
        JLabel label = new JLabel(value);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, 25));
        return label;
    }

    private void updateSelectedConversation() {
        String selected = userList.getSelectedValue();
        if (selected == null || "No users online".equals(selected)) {
            chatTitle.setText("Group conversation");
            chatSubtitle.setText("A shared space for everyone on this network");
        } else {
            chatTitle.setText(selected);
            chatSubtitle.setText("Private conversation  •  ● Online");
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty() || writer == null) return;

        String recipient = userList.getSelectedValue();
        if (recipient == null || "No users online".equals(recipient)) recipient = "ALL";
        writer.println(recipient + "|" + message);
        messageInput.setText("");
        messageInput.requestFocusInWindow();
    }

    private void sendFile() {
        String recipient = userList.getSelectedValue();
        if (recipient == null || "No users online".equals(recipient)
                || recipient.equals(username)) {
            JOptionPane.showMessageDialog(this,
                    "Select another online user before sending a file.",
                    "Choose a recipient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = chooser.getSelectedFile();

        Thread sending = new Thread(() -> {
            boolean success = new FileSender().sendFile(serverIP, recipient, selectedFile);
            SwingUtilities.invokeLater(() -> {
                appendSystemMessage(success
                        ? "File sent: " + selectedFile.getName() + " → " + recipient
                        : "Could not send file: " + selectedFile.getName());
            });
        }, "KEYRYX-file-sender");
        sending.setDaemon(true);
        sending.start();
    }

    private void openReceivedFile() {
        File folder = new File("received_files");
        if (!folder.exists()) {
            JOptionPane.showMessageDialog(this, "No received files folder found.",
                    "Received files", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser(folder);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Could not open file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startFileReceiver() {
        Thread fileReceiver = new Thread(() -> {
            FileTransferClient client = new FileTransferClient();
            client.connectAndListen(serverIP, username, message ->
                    SwingUtilities.invokeLater(() -> appendSystemMessage("[FILE] " + message)));
        }, "KEYRYX-file-receiver");
        fileReceiver.setDaemon(true);
        fileReceiver.start();
    }

    private void startMessageReceiver() {
        Thread receiver = new Thread(() -> {
            try {
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    final String incoming = line;
                    SwingUtilities.invokeLater(() -> processIncomingLine(incoming));
                }
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    connected = false;
                    if (connectionStatus != null) connectionStatus.setText("Disconnected");
                    appendSystemMessage("Connection to the server was closed.");
                });
                System.out.println("Disconnected from server: " + ex.getMessage());
            }
        }, "KEYRYX-message-receiver");
        receiver.setDaemon(true);
        receiver.start();
    }

    private void processIncomingLine(String incoming) {
        if (incoming.startsWith("USERS|")) {
            String selected = userList.getSelectedValue();
            userListModel.clear();
            String names = incoming.substring(6);
            if (names.isEmpty()) {
                userListModel.addElement("No users online");
            } else {
                String[] users = names.split(",");
                for (String name : users) {
                    if (!name.isEmpty() && !name.equals(username)) userListModel.addElement(name);
                }
            }
            if (selected != null) userList.setSelectedValue(selected, true);
            updateSelectedConversation();
            return;
        }

        if (incoming.startsWith("HISTORY|")) {
            appendHistoryMessage(incoming.substring(8));
            return;
        }

        if (incoming.startsWith("GROUP|")) {
            pendingMessageType = "GROUP";
            pendingMessageHeader = incoming.substring(6).trim();
            return;
        }

        if (incoming.startsWith("PRIVATE|")) {
            pendingMessageType = "PRIVATE";
            String[] fields = incoming.split("\\|", 3);
            pendingMessageHeader = fields.length > 2 ? fields[2].trim() : "";
            return;
        }

        if (pendingMessageType != null) {
            String type = pendingMessageType;
            pendingMessageType = null;
            pendingMessageHeader = null;
            appendProtocolMessage(incoming, type);
        } else {
            appendSystemMessage(incoming);
        }

        if ((getExtendedState() & JFrame.ICONIFIED) != 0) {
            AppNotification notification = new ChatNotification(incoming);
            showDesktopNotification(notification.getNotificationMessage());
        }
    }

    private void appendProtocolMessage(String line, String type) {
        int separator = line.indexOf(':');
        String sender = separator > 0 ? line.substring(0, separator).trim() : "Message";
        String content = separator > 0 ? line.substring(separator + 1).trim() : line;
        appendBubble(sender, content, sender.equals(username), false);
    }

    private void appendHistoryMessage(String history) {
        appendBubble("Previous message", history, false, true);
    }

    private void appendSystemMessage(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.ITALIC, 11));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setOpaque(false);
        row.add(label);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageList.add(row);
        messageList.add(Box.createVerticalStrut(12));
        refreshMessages();
    }

    private void appendBubble(String sender, String text, boolean mine, boolean history) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 8, 0));

        JPanel bubble = new RoundedPanel(mine ? GREEN : BUBBLE_IN, 18);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(10, 14, 9, 14));
        bubble.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));

        JLabel senderLabel = new JLabel(history ? "HISTORY" : sender);
        senderLabel.setForeground(mine ? new Color(218, 244, 228) : MINT);
        senderLabel.setFont(new Font("SansSerif", Font.BOLD, 10));

        JTextArea content = new JTextArea(text);
        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setOpaque(false);
        content.setForeground(TEXT);
        content.setFont(new Font("SansSerif", Font.PLAIN, 13));
        content.setFocusable(false);
        content.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        bubble.add(senderLabel);
        bubble.add(Box.createVerticalStrut(5));
        bubble.add(content);

        JLabel time = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setForeground(mine ? new Color(226, 220, 255) : MUTED);
        time.setFont(new Font("SansSerif", Font.PLAIN, 9));
        time.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bubble.add(Box.createVerticalStrut(5));
        bubble.add(time);

        if (mine) row.add(bubble, BorderLayout.EAST);
        else row.add(bubble, BorderLayout.WEST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageList.add(row);
        refreshMessages();
    }

    private void refreshMessages() {
        messageList.revalidate();
        messageList.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = messageScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public void showDesktopNotification(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage("KEYRYX", message, TrayIcon.MessageType.INFO);
        }
    }

    private void addButtonHover(JButton button, Color normal, Color hover) {
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });
    }

    private JLabel statusPill(String value) {
        JLabel pill = new JLabel(value);
        pill.setFont(new Font("SansSerif", Font.BOLD, 9));
        pill.setForeground(MINT);
        pill.setOpaque(true);
        pill.setBackground(new Color(20, 57, 62));
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 93, 99)),
                new EmptyBorder(7, 10, 7, 10)));
        return pill;
    }

    private JButton subtleButton(String label) {
        JButton button = new JButton(label);
        button.setForeground(MINT);
        button.setBackground(SURFACE);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHover(button, SURFACE, new Color(43, 59, 91));
        return button;
    }

    private JLabel avatarLabel(String text, int size, Color color) {
        JLabel label = new CircularAvatarLabel(text, size, color);
        label.setPreferredSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, size > 40 ? 12 : 10));
        return label;
    }

    private String initials(String value) {
        if (value == null || value.trim().isEmpty()) return "K";
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase();
    }

    private static class CircularAvatarLabel extends JLabel {
        private final Color avatarColor;
        CircularAvatarLabel(String text, int size, Color color) {
            super(text, SwingConstants.CENTER);
            avatarColor = color;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setForeground(TEXT);
            setFont(new Font("SansSerif", Font.BOLD, size > 40 ? 12 : 10));
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, avatarColor.brighter(), getWidth(), getHeight(), avatarColor.darker()));
            g.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
            g.setColor(new Color(255, 255, 255, 65));
            g.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private class UserCellRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel avatar;
        private JLabel name;
        private JLabel status;

        UserCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setOpaque(true);
            avatar = avatarLabel("U", 38, GREEN);
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            name = new JLabel();
            name.setFont(new Font("SansSerif", Font.BOLD, 12));
            status = new JLabel("●  On this network");
            status.setFont(new Font("SansSerif", Font.PLAIN, 9));
            status.setForeground(MUTED);
            text.add(name);
            text.add(Box.createVerticalStrut(5));
            text.add(status);
            add(avatar, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean selected, boolean focus) {
            boolean empty = "No users online".equals(value);
            name.setText(value);
            name.setForeground(selected ? MINT : TEXT);
            avatar.setText(empty ? "—" : initials(value));
            status.setText(empty ? "Waiting for people" : "●  Online");
            status.setForeground(empty ? MUTED : MINT);
            setBackground(selected ? SURFACE : SIDEBAR);
            return this;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final int radius;

        RoundedPanel(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, fill.brighter(), 0, getHeight(), fill));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g.setColor(new Color(255, 255, 255, 24));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    public void showSplashScreen() {
        final JWindow splash = new JWindow();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        ImageIcon original = new ImageIcon(ChatGui.class.getResource("img_1.png"));
        Image scaled = original.getImage().getScaledInstance(520, 284, Image.SCALE_SMOOTH);
        JLabel image = new JLabel(new ImageIcon(scaled));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(image, BorderLayout.CENTER);

        JLabel loading = new JLabel("  EVERY NODE HAS A VOICE     •     STARTING KEYRYX  ",
                SwingConstants.CENTER);
        loading.setForeground(MINT);
        loading.setBackground(BG);
        loading.setOpaque(true);
        loading.setFont(new Font("SansSerif", Font.BOLD, 11));
        loading.setBorder(new EmptyBorder(13, 8, 13, 8));
        panel.add(loading, BorderLayout.SOUTH);

        splash.setContentPane(panel);
        splash.setSize(520, 330);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        splash.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatGui::new);
    }
}
