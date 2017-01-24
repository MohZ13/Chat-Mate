package chat_mate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Document;

/**
 * @author Mohil Zalavadiya
 */
class Chat_Mate {
    String userName = "Anonymous";
    
    String fontFamily = Font.SANS_SERIF;
    int fontSize = 16;
    int styleNum = Font.PLAIN;
    
    Boolean isServer;
    ArrayList<Socket> fip = new ArrayList<Socket>();
    ArrayList<PrintWriter> friendsOut = new ArrayList<PrintWriter>();
    
    JFrame frame, startFrame;
    JTextArea incoming;
    JButton send;
    JTextField outgoing;
    JMenuBar menuBar;
   
    ServerSocket ss;
    Socket s;
    BufferedReader br;
    PrintWriter pw;
    InetAddress ip;
    
    public static void main(String[] args) {
        new Chat_Mate().startUpGUI();
    }
    
    void setFont(String field) {
        JFrame cbframe = new JFrame("Choose font...");
        cbframe.setSize(300, 140);
        cbframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(new FlowLayout());
        
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox cbFonts = new JComboBox(fonts);
        cbFonts.setSelectedItem(fontFamily);
        panel.add(cbFonts, FlowLayout.LEFT);
        
        Integer[] size = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30};
        JComboBox cbSize = new JComboBox(size);
        cbSize.setEditable(true);
        cbSize.setSelectedItem(fontSize);
        panel.add(cbSize, FlowLayout.CENTER);
        
        String[] style = {"Plain", "Bold", "Italic            "};
        JComboBox cbStyle = new JComboBox(style);
        panel.add(cbStyle, FlowLayout.RIGHT);
        
        JButton cbButton = new JButton("OK");
        cbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fontFamily = (String) cbFonts.getSelectedItem();
                fontSize = (Integer) cbSize.getSelectedItem();
                
                String typeStr = (String) cbStyle.getSelectedItem();
                if(typeStr.matches("Plain"))    styleNum = Font.PLAIN;
                else if(typeStr.matches("Bold"))    styleNum = Font.BOLD;
                else styleNum = Font.ITALIC;
                
                Font n = new Font(fontFamily, styleNum, fontSize);
                if(field.matches("incoming")) incoming.setFont(n);
                else outgoing.setFont(n);
                cbframe.dispose();
            }
        });
        panel.add(cbButton);
        panel.setSize(300, 115);
        
        cbframe.add(panel);
        cbframe.setVisible(true);
    }
    
    void startUpGUI() {
        startFrame = new JFrame("What you want to do?");
        startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startFrame.setSize(425, 100);
        startFrame.setResizable(false);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = startFrame.getWidth();
        int h = startFrame.getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;        
        startFrame.setLocation(x, y);
        
        JButton serverButton, clientButton;
        serverButton = new JButton("    Start up a new connection!    ");
        clientButton = new JButton("Connect to existing connection!");
        
        serverButton.addActionListener(new serverButtonListener());
        clientButton.addActionListener(new clientButtonListener());
        
        startFrame.getContentPane().add(BorderLayout.WEST, serverButton);
        startFrame.getContentPane().add(BorderLayout.EAST, clientButton);
        
        startFrame.setVisible(true);
    }
    
    class serverButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            startFrame.dispose();
            goServer();
        }
    }
    
    class clientButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            startFrame.dispose();
            goClient();
        }
    }
    
    void goServer() {
        setUpGUI();
        isServer = true;
        
        String input = JOptionPane.showInputDialog("Enter your name:");
        if(input != null) {
            userName = input;
            incoming.append("Your name is set to " + userName + "!\n\n");
        } else {
            incoming.append("Your name is set to Anonymous!\n\n");
        }
        
        Thread net = new Thread() {
            @Override
            public void run() {
                incoming.append("Waiting for others to connect...\n");
                try {
                    incoming.append("Tell your friend(s) to enter this IP: ");
                    incoming.append(String.valueOf(Inet4Address.getLocalHost()) + "\n");
                } catch(Exception ex) {}
                setUpServerNetworking();
            }
        };
        net.start(); 
    }
    
    void goClient() {
        setUpGUI();
        isServer = false;
        
        String input = JOptionPane.showInputDialog("Enter your name:");
        if(input != null) {
            userName = input;
            incoming.append("Your name is set to " + userName + "!\n\n");
        } else {
            incoming.append("Your name is set to Anonymous!\n\n");
        }
        
        boolean isIP;
        String ipInString = "";
        do {
            try { 
                ipInString = JOptionPane.showInputDialog("Enter IP:");
                ip = InetAddress.getByName(ipInString);
                isIP = true;
            } catch(HeadlessException ex) {
                JOptionPane.showMessageDialog(null, "Some problem has occured");
                frame.dispose();
                return;
            } catch(UnknownHostException ex) {
                isIP = false;
            }
        }while(isIP == false || ipInString == null ||ip == null);
        
        Thread net = new Thread() {
            @Override
            public void run() {
                incoming.append("Connecting...\n");
                setUpClientNetworking();
                incoming.append("Connection established!\n");
                pw.println(userName + " connected!");
                pw.flush();
            }
        };
        net.start();
    }
    
    void setUpGUI() {
        frame = new JFrame("Chat mate...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = frame.getWidth();
        int h = frame.getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;        
        frame.setLocation(x, y);

        incoming = new JTextArea();
        incoming.setEditable(false);
        incoming.setLineWrap(true);
        incoming.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        
        JScrollPane scroller = new JScrollPane(incoming);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(BorderLayout.CENTER, scroller);
        
        Action sm = new SendMessage();
        send = new JButton(sm);
        send.setText(" Send ");
        
        outgoing = new JTextField(30);
        outgoing.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        outgoing.setAction(sm);
        
        JPanel south = new JPanel();
        south.add(outgoing);
        south.add(send);
        frame.getContentPane().add(BorderLayout.SOUTH, south);
        
        setMenuBar();
        frame.setVisible(true);
    }
    
    void setMenuBar() {
        menuBar = new JMenuBar();
        JMenu menu, subMenu;
        JMenuItem menuItem;
        
        // first menu...
        menu = new JMenu("General");
        
        menuItem = new JMenuItem("Change your name");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String oldName = userName;
                userName = JOptionPane.showInputDialog(frame, "Enter your name:");
                if(!oldName.equals(userName)) {
                    try {
                    if(isServer == false) {
                        pw.println(oldName + " has changed its name to " + userName);
                        pw.flush();
                    } else {
                        deliverMessage(oldName + " has changed its name to " + userName);
                        incoming.append(oldName + " has changed its name to " + userName + "\n");
                        incoming.setCaretPosition(incoming.getDocument().getLength() - 1);
                    }
                    } catch(Exception ex) {}
                }
            }
        });
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Info related to connection ");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String str = "";
                try {
                        str = "Your name: " + userName
                        + "\nYour IP: " + String.valueOf(Inet4Address.getLocalHost());
                        
                        if(isServer == false) {
                            str = str + "\nYou are connected to IP: " + ip;
                        } else if(!friendsOut.isEmpty()) {
                            str = str + "\nYou are connected to this IP addresses: ";
                        
                            Iterator i = fip.iterator();
                            while(i.hasNext()) {
                                Socket sip = (Socket) i.next();
                                str = str + String.valueOf(sip.getInetAddress()) + "\n";
                            }
                        } else {
                            throw new Exception();
                        }
                } catch(Exception ex) { str = "No Connection is made!"; }
                JOptionPane.showMessageDialog(null, str);
            }
        });
        menu.add(menuItem);
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save conversation");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                JOptionPane.showMessageDialog(fc, "Save in .rtf format for proper formatting\nof your Conversation");
                int returnValue  = fc.showSaveDialog(incoming);
               
                if(returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    if(!f.exists()) {
                        try {
                            FileWriter fw = new FileWriter(f);
                            Document d = incoming.getDocument();
                            fw.write(d.getText(0, d.getLength()-1));
                            fw.close();
                            
                        } catch (Exception ex) {}
                    } else {
                        JOptionPane.showMessageDialog(frame, "File already exist!");
                    }
                }
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Clear conversation");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                incoming.setText("");
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        menu.addSeparator();
        
        menuItem = new JMenuItem("Quit    ");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        menu.add(menuItem);
        
        menuBar.add(menu);
        
        // second menu...
        menu = new JMenu("Customize");
        
        subMenu = new JMenu("Change text color of...    ");
        
        menuItem = new JMenuItem("Input area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Color c = JColorChooser.showDialog(frame, "Choose color...", outgoing.getBackground());
                if(c != null) outgoing.setForeground(c);
            }
        });
        subMenu.add(menuItem);
        
        menuItem = new JMenuItem("Incoming message area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Color c = JColorChooser.showDialog(frame, "Choose color...", incoming.getBackground());
                if(c != null) incoming.setForeground(c);
            }
        });
        subMenu.add(menuItem);
        menu.add(subMenu);
        
        subMenu = new JMenu("Change background color of...    ");
        
        menuItem = new JMenuItem("Input area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Color c = JColorChooser.showDialog(frame, "Choose color...", outgoing.getForeground());
                if(c != null) outgoing.setBackground(c);
            }
        });
        subMenu.add(menuItem);
        
        menuItem = new JMenuItem("Incoming message area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Color c = JColorChooser.showDialog(frame, "Choose color...", incoming.getForeground());
                if(c != null) incoming.setBackground(c);
            }
        });
        subMenu.add(menuItem);
        menu.add(subMenu);
        menu.addSeparator();
        
        subMenu = new JMenu("Change font of...");
        
        menuItem = new JMenuItem("Input area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setFont("outgoing");
            }
        });
        subMenu.add(menuItem);
        
        menuItem = new JMenuItem("Incoming message area");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setFont("incoming");
            }
        });
        subMenu.add(menuItem);
        menu.add(subMenu);
        menu.addSeparator();
        
        menuItem = new JMenuItem("Random rainbow!");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Color c;
                c = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
                outgoing.setBackground(c);
                c = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
                outgoing.setForeground(c);
                
                c = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
                incoming.setBackground(c);
                c = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
                incoming.setForeground(c);
            }
        });
        menuItem.setToolTipText("Set all GUI colors randomly!  Give it a try!!!");
        menu.add(menuItem);
        
        menuBar.add(menu);
        
        // Third menu
        menu = new JMenu("Help");
        
        menuItem = new JMenuItem("How to use?");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String str = ">> Create new connection using \"Start up a new connection\" button"
                        + "\n>> Connect to that connection from other computer using \"Connect to existing Connection\" button"
                        + "\n>> Use the IP shown in other PC's app to make connection"
                        + "\n>> Have Fun"
                        + "\n\n>> From General menu:"
                        + "\n       > Change your name"
                        + "\n       > Save or Clear conversation"
                        + "\n>> From customize menu!"
                        + "\n       > Change Fonts"
                        + "\n       > Change Text colors and Background colors"
                        + "\n>> Use Random Rainbow to change colors randomly!";
                JOptionPane.showMessageDialog(null, str);
            }
        });
        menu.add(menuItem);
        
        menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String str = "Chat Mate"
                        + "\nA simple local network chat application   "
                        + "\n\nCreated by:"
                        + "\nMohZ";
                JOptionPane.showMessageDialog(null, str);
            }
        });
        menu.add(menuItem);
        
        menuBar.add(menu);
        
        frame.setJMenuBar(menuBar);
    }
    
    void setUpServerNetworking() {
        try {
            ss = new ServerSocket(1313);
            Thread accept = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            s = ss.accept();
                            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            pw = new PrintWriter(s.getOutputStream());
                            friendsOut.add(pw);
                            fip.add(s);
                            
                            Runnable recieve = new RecieveMessage(br);
                            Thread t = new Thread(recieve);
                            t.start();
                        }
                        catch(Exception e) {}
                    }
                }
            };
            accept.start();
        }
        catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "Some problem has occured. Please, try again!");
            frame.dispose();
            startUpGUI();
        }
    }
   
    void setUpClientNetworking() {
        try {
            s = new Socket(ip, 1313);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream());
            Runnable recieve = new RecieveMessage(br);
            Thread t = new Thread(recieve);
            t.start();
        }
        catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "There are no connection available from that IP");
            frame.dispose();
            startUpGUI();
        }
    }
    
    void deliverMessage(String s) throws IOException {
        Iterator<PrintWriter> i = friendsOut.iterator();
        PrintWriter dest;
        
        while(i.hasNext()) {
            dest = i.next();
            
            dest.println(s);
            dest.flush();
        }
    }
    
    class SendMessage extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent event) {
            String s = outgoing.getText();
            try {
                if(isServer == false) {
                    pw.println(userName + " --> " + s);
                    pw.flush();
                } else {
                    deliverMessage(userName + " --> " + s);
                    incoming.append(userName + " --> " + s + "\n");
                    incoming.setCaretPosition(incoming.getDocument().getLength() - 1);
                }
            } catch(Exception ex) {}
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }
    
    class RecieveMessage implements Runnable {
        BufferedReader brm;
        public RecieveMessage(BufferedReader in) {
            brm = in;
        }

        @Override
        public void run() {
            String s;
            try {
                while(true) {
                    while((s = brm.readLine()) != null) {
                        incoming.append(s + "\n");
                        incoming.setCaretPosition(incoming.getDocument().getLength() - 1);
                        if(isServer == true) {
                            deliverMessage(s);
                        }
                    }
                }
            } catch(Exception ex) {}
        }
    }
}