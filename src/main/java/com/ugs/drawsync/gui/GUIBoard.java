package com.ugs.drawsync.gui;

import com.ugs.drawsync.server.ActionType;
import com.ugs.drawsync.server.ClientHandler;
import com.ugs.drawsync.server.Message;
import com.ugs.drawsync.server.ServerManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class GUIBoard extends JFrame {

    private JPanel introScreen;
    private JPanel mainScreen;
    private JButton hostButton;
    private JTextField ipInput;
    private JButton connectButton;
    private JTextField username;
    private JTextField textInput;
    private JButton sendButton;
    private JTextArea chatText;
    private transient ServerManager server;
    private transient ClientHandler client;
    private JTextArea users;
    private boolean isServer;
    private JPanel usernameValidationPanel;
    private JLabel usernameValidation;
    private JPanel ipValidationPanel;
    private JLabel ipValidation;
    private JButton clearButton;
    private Canvas canvas;
    private JButton colorButton;
    private JSlider strokeSliderButton;
    private JButton lineButton;
    private JPanel canvasContainer;
    private JButton eraserButton;
    private JButton brushButton;

    public GUIBoard(String title, int width, int height) {
        initFrame(title, width, height);
        initIntroScreen();
        initMainScreen();
        addEventListeners();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void initIntroScreen() {
        introScreen = new JPanel();
        JPanel headerPanel = new JPanel();
        introScreen.setLayout(new BoxLayout(introScreen, BoxLayout.Y_AXIS));
        ImageIcon headerIcon = new ImageIcon("src/main/resources/images/headerLogo.png");
        resizeIcon(headerIcon, 300, 300);
        JLabel headerImage = new JLabel(headerIcon);
        headerPanel.add(headerImage);
        JLabel addressLabel = new JLabel("Port or IP:Port");
        addressLabel.setMaximumSize(new Dimension(90, 5));
        addressLabel.setAlignmentX(SwingConstants.LEFT);
        ipInput = new JTextField(15);
        ipInput.setMaximumSize(new Dimension(200, 40));
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setMaximumSize(new Dimension(90, 5));
        usernameLabel.setAlignmentX(SwingConstants.LEFT);
        username = new JTextField(15);
        username.setMaximumSize(new Dimension(200, 40));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        connectButton = new JButton("Connect");
        connectButton.setEnabled(false);
        hostButton = new JButton("Be a Host");
        hostButton.setEnabled(false);
        connectButton.setFocusable(false);
        hostButton.setFocusable(false);
        buttonsPanel.add(hostButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Width of the gap
        buttonsPanel.add(connectButton);
        usernameValidationPanel = new JPanel();
        usernameValidationPanel.setMaximumSize(new Dimension(250, 5));
        usernameValidation = new JLabel();
        usernameValidation.setForeground(Color.RED);
        usernameValidationPanel.setVisible(false);
        usernameValidationPanel.add(usernameValidation);
        ipValidationPanel = new JPanel();
        ipValidationPanel.setMaximumSize(new Dimension(200, 5));
        ipValidation = new JLabel();
        ipValidation.setForeground(Color.RED);
        ipValidationPanel.setVisible(false);
        ipValidationPanel.add(ipValidation);

        introScreen.add(headerPanel);
        introScreen.add(usernameLabel);
        introScreen.add(Box.createRigidArea(new Dimension(0, 5))); // Width of the gap
        introScreen.add(username);
        introScreen.add(usernameValidationPanel);
        introScreen.add(Box.createRigidArea(new Dimension(0, 10))); // Width of the gap
        introScreen.add(addressLabel);
        introScreen.add(Box.createRigidArea(new Dimension(0, 5))); // Width of the gap
        introScreen.add(ipInput);
        introScreen.add(ipValidationPanel);
        introScreen.add(Box.createRigidArea(new Dimension(0, 15))); // Width of the gap
        introScreen.add(buttonsPanel);
        introScreen.add(Box.createVerticalGlue());
        introScreen.add(Box.createRigidArea(new Dimension(0, 250))); // Width of the gap
        add(introScreen, BorderLayout.CENTER);
    }

    private void initMainScreen() {
        mainScreen = new JPanel(new BorderLayout());
        ImageIcon headerIcon = new ImageIcon("src/main/resources/images/headerLogo.png");
        resizeIcon(headerIcon, 50, 50);
        JPanel header = new JPanel();
        JLabel label = new JLabel("DrawSync", headerIcon, SwingConstants.CENTER);
        label.setFont(new Font("Impact", Font.PLAIN, 44));
        label.setIconTextGap(15);
        header.setBackground(new Color(.45f, .45f, .45f, 1));
        header.add(label);

        ImageIcon sendIcon = new ImageIcon("src/main/resources/images/sendIcon.png");
        resizeIcon(sendIcon, 25, 25);
        JPanel chat = new JPanel();
        chat.setBackground(Color.RED);
        chat.setLayout(new BoxLayout(chat, BoxLayout.Y_AXIS));
        chatText = new JTextArea("CHAT ROOM");
        chatText.setMargin(new Insets(5, 5, 5, 5));
        chatText.setFont(getSegoeFont());
        chatText.setEditable(false);
        chatText.setLineWrap(true);
        JScrollPane chatScroll = new JScrollPane(chatText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setPreferredSize(new Dimension(0, 150));
        JPanel chatInput = new JPanel();
        chatInput.setLayout(new BoxLayout(chatInput, BoxLayout.X_AXIS));
        chat.setLayout(new BoxLayout(chat, BoxLayout.Y_AXIS));
        textInput = new JTextField(10);
        textInput.setToolTipText("Insert your message");
        sendButton = new JButton(sendIcon);
        sendButton.setFocusable(false);
        chatInput.add(textInput);
        chatInput.add(sendButton);
        chat.add(chatScroll);
        chat.add(chatInput);

        users = new JTextArea();
        users.setFont(getSegoeFont());
        JScrollPane usersScroll = new JScrollPane(users, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        usersScroll.setPreferredSize(new Dimension(180, 0));
        users.setEditable(false);
        users.setLineWrap(true);
        users.setBackground(Color.lightGray);
        users.setMargin(new Insets(5, 5, 5, 5));

        JPanel canvasOptions = getCanvasOptions();

        canvasContainer = new JPanel();
        canvasContainer.setBackground(new Color(.61f, .61f, .61f, 1));
        canvas = new Canvas();
        canvasContainer.add(canvas);
        canvasContainer.setDoubleBuffered(true);
        canvas.setStroke(strokeSliderButton.getValue());

        mainScreen.add(header, BorderLayout.NORTH);
        mainScreen.add(usersScroll, BorderLayout.EAST);
        mainScreen.add(chat, BorderLayout.SOUTH);
        mainScreen.add(canvasOptions, BorderLayout.WEST);
        mainScreen.add(canvasContainer, BorderLayout.CENTER);
    }

    private JPanel getCanvasOptions() {
        JPanel canvasOptions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        canvasOptions.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        canvasOptions.setPreferredSize(new Dimension(100, 0));
        JPanel sliderPanel = new JPanel();
        JLabel sliderLabel = new JLabel("Brush Size");
        sliderPanel.setPreferredSize(new Dimension(90, 60));
        strokeSliderButton = new JSlider(0, 100, 10);
        strokeSliderButton.setPreferredSize(new Dimension(90, 32));
        sliderPanel.add(sliderLabel);
        sliderPanel.add(strokeSliderButton);
        strokeSliderButton.setPaintLabels(true);
        Hashtable labelTable = new Hashtable();
        labelTable.put(0, new JLabel("\u2022"));
        labelTable.put((int) (100 / 3.2), new JLabel("\u26AB"));
        labelTable.put((int) (100 / 1.5), new JLabel("\u25CF"));
        labelTable.put(100, new JLabel("\u2B24"));
        strokeSliderButton.setLabelTable(labelTable);
        clearButton = createCanvasButton("src/main/resources/images/clearIcon.png");
        colorButton = createCanvasButton("src/main/resources/images/colorIcon.png");
        lineButton = createCanvasButton("src/main/resources/images/lineIcon.png");
        JButton textButton = createCanvasButton("src/main/resources/images/textIcon.png");
        brushButton = createCanvasButton("src/main/resources/images/brushIcon.png");
        JButton shapesButton = createCanvasButton("src/main/resources/images/shapesIcon.png");
        JButton undoButton = createCanvasButton("src/main/resources/images/undoIcon.png");
        JButton redoButton = createCanvasButton("src/main/resources/images/redoIcon.png");
        JButton saveButton = createCanvasButton("src/main/resources/images/saveIcon.png");
        eraserButton = createCanvasButton("src/main/resources/images/eraserIcon.png");
        canvasOptions.add(colorButton);
        canvasOptions.add(lineButton);
        canvasOptions.add(textButton);
        canvasOptions.add(brushButton);
        canvasOptions.add(shapesButton);
        canvasOptions.add(eraserButton);
        canvasOptions.add(sliderPanel);
        canvasOptions.add(undoButton);
        canvasOptions.add(redoButton);
        canvasOptions.add(saveButton);
        canvasOptions.add(clearButton);
        return canvasOptions;
    }

    private JButton createCanvasButton(String filename) {
        ImageIcon icon = new ImageIcon(filename);
        JButton button = new JButton(icon);
        resizeIcon(icon, 25, 25);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusable(false);
        return button;
    }

    private void initFrame(String title, int width, int height) {
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLayout(new BorderLayout());
    }

    public void resizeIcon(ImageIcon icon, int width, int height) {
        icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public void addEventListeners() {
        createButtonsListener();
        createInputListeners();
        canvasContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == 3) {
                    changeMode(Mode.MOVING);
                    canvas.setMouseX(e.getX());
                    canvas.setMouseY(e.getY());
                    canvasContainer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.getButton() == 3) {
                    canvasContainer.setCursor(Cursor.getDefaultCursor());
                    canvas.setMode(canvas.getLastMode());
                }
            }
        });
        canvasContainer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (canvas.getMode() == Mode.MOVING) {
                    int diffX = e.getX() - canvas.getMouseX();
                    int diffY = e.getY() - canvas.getMouseY();
                    canvas.setMouseX(e.getX());
                    canvas.setMouseY(e.getY());
                    canvas.setLocation(canvas.getX() + diffX, canvas.getY() + diffY);
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                if (isServer && server != null) {
                    ServerManager.broadcastMessage(Message.buildMessage(ActionType.SERVER_CLOSING, username.getText() + " left the room. Server closed."));
                    try {
                        server.getConnection().close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (!isServer && client != null && !client.getConnection().isClosed()) {
                    try {
                        client.sendMessage(Message.buildMessage(ActionType.CLIENT_CLOSING, username.getText()));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                canvas.doInit(true);
            }
        });
    }

    private void createInputListeners() {
        username.getDocument().addDocumentListener(new DocumentListener() {
            public void checkingInput(int length, boolean isInsert) {
                if (isInsert && length > 0) {
                    hostButton.setEnabled(true);
                    connectButton.setEnabled(true);
                } else if (!isInsert && length == 0) {
                    hostButton.setEnabled(false);
                    connectButton.setEnabled(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkingInput(e.getDocument().getLength(), true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkingInput(e.getDocument().getLength(), false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //Not used
            }
        });
        textInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                try {
                    sendMessage(e.getKeyChar() == KeyEvent.VK_ENTER && !textInput.getText().isEmpty());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void createButtonsListener() {
        createIntroButtonsListeners();
        createMainButtonsListeners();
    }

    private void createMainButtonsListeners() {
        clearButton.addActionListener(b -> canvas.clear());
        colorButton.addActionListener(b -> {
            Color color = JColorChooser.showDialog(null, "Pick your drawing Color", canvas.getColor(), false);
            if (color != null) {
                canvas.setColor(color);
            }
        });
        brushButton.addActionListener(b -> changeMode(Mode.DRAWER));
        eraserButton.addActionListener(b -> changeMode(Mode.ERASER));
        sendButton.addActionListener(b -> {
            try {
                sendMessage(!textInput.getText().isEmpty());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        strokeSliderButton.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            if (!slider.getValueIsAdjusting()) {
                canvas.setStroke(Math.max(2, slider.getValue()));
            }
        });
    }

    private void changeMode(Mode mode) {
        canvas.setLastMode(canvas.getMode());
        canvas.setMode(mode);
    }

    private void createIntroButtonsListeners() {
        connectButton.addActionListener(b -> {
            boolean isOk = usernameValidation(true);
            if (!Pattern.matches("\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?):\\d{1,5}?\\b", ipInput.getText())) {
                ipValidation.setText("IP Address:Port format isn't valid");
                ipValidationPanel.setVisible(true);
                isOk = false;
            } else {
                ipValidationPanel.setVisible(false);
            }
            if (isOk) {
                introScreen.setVisible(false);
                add(mainScreen, BorderLayout.CENTER);
                try {
                    String[] address = ipInput.getText().split(":");
                    client = new ClientHandler(new Socket(address[0], Integer.parseInt(address[1])), this, username.getText());
                    client.start();
                    isServer = false;
                } catch (IOException e) {
                    introScreen.setVisible(true);
                    remove(mainScreen);
                    JOptionPane.showMessageDialog(this, "No room detected with that IP Address", "Failed connection", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(e);
                }
            }

        });
        hostButton.addActionListener(b -> {
            int port = 0;
            boolean isOk = usernameValidation(true);
            if (ipInput.getText().length() > 5) {
                ipValidation.setText("Port can't have more than 5 digits");
                ipValidationPanel.setVisible(true);
                isOk = false;
            } else {
                try {
                    port = Integer.parseInt(ipInput.getText());
                } catch (NumberFormatException e) {
                    ipValidation.setText("Port must be numeric");
                    ipValidationPanel.setVisible(true);
                    isOk = false;
                }
            }
            if (isOk) {
                introScreen.setVisible(false);
                add(mainScreen, BorderLayout.CENTER);
                users.setText("[ROOM n" + (int) (Math.random() * 1000) + "] Online Users:\n - " + username.getText());
                server = new ServerManager(port);
                server.startServer(this);
                isServer = true;
            }
        });
    }

    private void sendMessage(boolean sendMessage) throws IOException {
        if (sendMessage) {
            String text = username.getText() + ": " + textInput.getText();
            chatText.append("\n " + text);
            if (isServer) {
                ServerManager.broadcastMessage(Message.buildMessage(ActionType.SERVER_MESSAGE, text));
            } else {
                client.sendMessage(Message.buildMessage(ActionType.CLIENT_MESSAGE, text));
            }
            textInput.setText("");
        }
    }

    private boolean usernameValidation(boolean isOk) {
        if (username.getText().length() > 16) {
            usernameValidation.setText("User can't have more than 16 characters");
            usernameValidationPanel.setVisible(true);
            isOk = false;
        } else {
            usernameValidationPanel.setVisible(false);
        }
        return isOk;
    }

    public JTextArea getChat() {
        return chatText;
    }

    public JTextArea getUsers() {
        return users;
    }

    public Font getSegoeFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

}
