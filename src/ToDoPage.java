import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import static javax.swing.BorderFactory.createEmptyBorder;

public class ToDoPage implements ActionListener {

    JFrame toDoFrame;
    JPanel toDoPanel;
    JLabel toDoName;
    JPanel listPanel;
    JPanel bottomPanel;
    JPanel resizePanel;
    JButton closeBtn;
    JButton minimizeBtn;
    JPanel topBar;
    JPanel bottomBar;
    boolean mouseEntered;

    //get the last main window location/size from settings
    String settings = getSettings();
    int x = Integer.parseInt(settings.substring(settings.indexOf(":")+2,settings.indexOf(",")));
    int y = Integer.parseInt(settings.substring(settings.indexOf(",")+1,settings.indexOf("|")));
    String sizeSettings = settings.substring(settings.indexOf("|")+1);
    int width = Integer.parseInt(sizeSettings.substring(sizeSettings.indexOf(":")+2,sizeSettings.indexOf(",")));
    int height = Integer.parseInt(sizeSettings.substring(sizeSettings.indexOf(",")+1));

    Point coords = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

    public ToDoPage(){

        toDoFrame = new JFrame();

        toDoFrame.setBounds(x,y,0,0);
        toDoFrame.setName("To Do");
        toDoFrame.setPreferredSize(new Dimension(width, height));
        toDoFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        toDoFrame.setUndecorated(true);

        FrameDragListener frameDragListener = new FrameDragListener(toDoFrame, true);
        toDoFrame.addMouseListener(frameDragListener);
        toDoFrame.addMouseMotionListener(frameDragListener);
        toDoFrame.setLayout(new BorderLayout());

        topBar = new JPanel();
        topBar.setBackground(new Color(30,30,30));
        topBar.setLayout(new BorderLayout());

        JPanel topBarBtnPanel = new JPanel();
        topBarBtnPanel.setBackground(new Color(30,30,30));
        topBarBtnPanel.setLayout(new GridLayout());
        topBarBtnPanel.setPreferredSize(new Dimension(100,30));

        closeBtn = new JButton("x");
        closeBtn.setBorder(createEmptyBorder(0,0,6,0));
        closeBtn.setPreferredSize(new Dimension(50,30));
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 38));
        prepBtn(closeBtn);

        minimizeBtn = new JButton("–");
        minimizeBtn.setBorder(createEmptyBorder(0,0,6,0));
        minimizeBtn.setPreferredSize(new Dimension(38,30));
        minimizeBtn.setFont(new Font("Arial", Font.PLAIN, 36));
        prepBtn(minimizeBtn);

        topBarBtnPanel.add(minimizeBtn);
        topBarBtnPanel.add(closeBtn);

        topBar.add(topBarBtnPanel, BorderLayout.EAST);

        toDoPanel = new JPanel();
        toDoPanel.setLayout(new BorderLayout());
        toDoPanel.setBackground(new Color(24,24,24));
        toDoPanel.setBorder(createEmptyBorder(20,50,10,50));

        toDoName = new JLabel("To Do:");
        toDoName.setForeground(new Color(222,222,222));
        toDoName.setFont(new Font("Arial", Font.PLAIN, 46));
        toDoName.setBorder(createEmptyBorder(10,0,40,0));
        toDoName.setHorizontalAlignment(SwingConstants.CENTER);

        toDoPanel.add(toDoName, BorderLayout.NORTH);

        listPanel = new JPanel();

        listPanel.setBackground(new Color(20,20,20));
        listPanel.setForeground(new Color(222,222,222));
        listPanel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();

        int loops=0;

        for(int i=0;i<ListItem.numOfListItems();i++){
            String itemI = ListItem.getListItemInfo(i);

            String internalSeparator = "❒";

            int iNum = Integer.parseInt(itemI.substring(0,itemI.indexOf(internalSeparator)));
            String forNextItem = itemI.substring(itemI.indexOf(internalSeparator)+1);
            int iPrio = Integer.parseInt(forNextItem.substring(0,forNextItem.indexOf(internalSeparator)));
            forNextItem = forNextItem.substring(forNextItem.indexOf(internalSeparator)+1);
            String iName = forNextItem.substring(0,forNextItem.indexOf(internalSeparator));

            JLabel lINumi = new JLabel(String.valueOf(iNum));
            prepLabel(lINumi);
            con.fill = GridBagConstraints.HORIZONTAL;
            con.anchor = GridBagConstraints.NORTH;
            con.gridy = i;
            con.gridx = 0;
            con.weighty = 0;
            con.ipady = 10;
            con.ipadx = 15;
            con.weightx = 0.05;
            listPanel.add(lINumi, con);

            JLabel lIPrioi = new JLabel(String.valueOf(iPrio));
            prepLabel(lIPrioi);
            con.gridy = i;
            con.gridx = 1;
            con.weightx = 0.05;
            listPanel.add(lIPrioi, con);

            JTextField lINamei = new JTextField(iName,20);
            lINamei.setToolTipText(iName);
            prepTextField(lINamei);
            con.gridy = i;
            con.gridx = 2;
            con.weightx = 0.65;

            lINamei.setAutoscrolls(true);
            listPanel.add(lINamei, con);

            JButton editBtni = new JButton("Edit");

            JButton binBtni = new JButton("Del");
            prepBtn(binBtni);
            binBtni.setFont(new Font("Arial",Font.PLAIN,12));
            binBtni.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
            binBtni.setBorderPainted(true);
            binBtni.setPreferredSize(new Dimension(30,18));
            binBtni.setHorizontalTextPosition(SwingConstants.CENTER);
            binBtni.setToolTipText("Delete Button "+(i+1));
            binBtni.addActionListener( e -> {
                        String buttonNum = binBtni.getToolTipText().substring(14);
                        ListItem.deleteItem(Integer.parseInt(buttonNum));
                        listPanel.remove(lINumi);
                        listPanel.remove(lIPrioi);
                        listPanel.remove(lINamei);
                        listPanel.remove(binBtni);
                        listPanel.remove(editBtni);
                        updateListNums();
                        updateBtnNums();
                        ListItem.updateListItems();
                        listPanel.repaint();
                        listPanel.updateUI();

            });
            con.gridy = i;
            con.gridx = 3;
            con.weightx = 0.05;
            listPanel.add(binBtni, con);

            prepBtn(editBtni);
            editBtni.setFont(new Font("Arial",Font.PLAIN,12));
            editBtni.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
            editBtni.setBorderPainted(true);
            editBtni.setPreferredSize(new Dimension(30,18));
            editBtni.setHorizontalTextPosition(SwingConstants.CENTER);
            editBtni.setToolTipText("Edit Button "+(i+1));

            editBtni.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFrame editFrame = new JFrame();
                    FrameDragListener frameDragListener = new FrameDragListener(editFrame, false);
                    editFrame.addMouseListener(frameDragListener);
                    editFrame.addMouseMotionListener(frameDragListener);
                    editFrame.setLayout(new BorderLayout());
                    editFrame.setUndecorated(true);

                    //update location of edit window to match the main window on open
                    String settings = getSettings();
                    x = Integer.parseInt(settings.substring(settings.indexOf(":")+2,settings.indexOf(",")));
                    y = Integer.parseInt(settings.substring(settings.indexOf(",")+1,settings.indexOf("|")));

                    editFrame.setBounds(x+15, y+60, 0, 0);
                    editFrame.setAlwaysOnTop(true);

                    topBar = new JPanel();
                    topBar.setBackground(new Color(30, 30, 30));
                    topBar.setLayout(new BorderLayout());

                    JLabel settingsLabel = new JLabel("Edit");
                    prepLabel(settingsLabel);
                    settingsLabel.setBorder(null);
                    settingsLabel.setFont(new Font("Arial", Font.BOLD, 26));
                    topBar.add(settingsLabel, BorderLayout.CENTER);

                    JPanel blank = new JPanel();
                    blank.setBackground(new Color(30, 30, 30));
                    blank.setPreferredSize(new Dimension(50, 0));
                    topBar.add(blank, BorderLayout.WEST);

                    JButton editCloseBtn = new JButton("x");
                    editCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
                    editCloseBtn.setPreferredSize(new Dimension(50, 30));
                    editCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
                    editCloseBtn.addActionListener(e1 -> editFrame.dispose());

                    prepBtn(editCloseBtn);

                    topBar.add(editCloseBtn, BorderLayout.EAST);

                    JPanel editPanel = new JPanel();

                    int itemToEdit = Integer.parseInt(editBtni.getToolTipText().substring(12));
                    String listItemInfo = ListItem.getListItemInfo(itemToEdit-1);
                    String infoForNextItem = listItemInfo.substring(listItemInfo.indexOf(internalSeparator)+1);
                    String itemPrio = infoForNextItem.substring(0,infoForNextItem.indexOf(internalSeparator));
                    infoForNextItem = infoForNextItem.substring(infoForNextItem.indexOf(internalSeparator)+1);
                    String itemName = infoForNextItem.substring(0,infoForNextItem.indexOf(internalSeparator));
                    infoForNextItem = infoForNextItem.substring(infoForNextItem.indexOf(internalSeparator)+1);
                    String itemInfo = infoForNextItem;

                    // add text fields - grab info from item
                    JLabel prioLabel = new JLabel("Priority:");
                    prepLabel(prioLabel);
                    JTextField editPrioField = new JTextField();
                    prepTextField(editPrioField);
                    editPrioField.setEditable(true);
                    editPrioField.setFocusable(true);
                    editPrioField.setText(itemPrio);

                    JLabel nameLabel = new JLabel("Name:");
                    prepLabel(nameLabel);
                    JTextField editNameField = new JTextField();
                    prepTextField(editNameField);
                    editNameField.setEditable(true);
                    editNameField.setFocusable(true);
                    editNameField.setText(itemName);

                    JLabel infoLabel = new JLabel("Info:");
                    prepLabel(infoLabel);
                    JTextArea editInfoField = new JTextArea(4, 0);
                    editInfoField.setBackground(new Color(20, 20, 20));
                    editInfoField.setForeground(new Color(220, 220, 220));
                    editInfoField.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
                    editInfoField.setCaretColor(Color.WHITE);
                    editInfoField.setLineWrap(true);
                    editInfoField.setText(itemInfo);

                    JScrollPane sp = new JScrollPane(editInfoField);
                    Border lineBorder = BorderFactory.createLineBorder(new Color(50, 50, 50));
                    Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
                    editInfoField.setBorder(emptyBorder);
                    sp.setBorder(lineBorder);
                    sp.getVerticalScrollBar().setUnitIncrement(2);
                    sp.getVerticalScrollBar().setBackground(new Color(20, 20, 20));
                    sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                        @Override
                        protected void configureScrollBarColors() {
                            this.thumbColor = new Color(50, 50, 50);
                        }

                        public Dimension getPreferredSize(JComponent c) {
                            return new Dimension(12, 12);
                        }
                    });

                    editPanel.add(prioLabel);
                    editPanel.add(editPrioField);
                    editPanel.add(nameLabel);
                    editPanel.add(editNameField);
                    editPanel.add(infoLabel);
                    editPanel.add(sp);

                    editPanel.setLayout(new GridLayout(0, 1));
                    editPanel.setBorder(createEmptyBorder(50, 50, 50, 50));

                    editPanel.setBackground(new Color(20, 20, 20));
                    editFrame.setPreferredSize(new Dimension(500, 300));

                    JButton editSaveBtn = new JButton("Update");
                    prepBtn(editSaveBtn);
                    editSaveBtn.setPreferredSize(new Dimension(40,80));
                    editSaveBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
                    editSaveBtn.setBorderPainted(true);

                    editSaveBtn.addActionListener(e2 ->{
                        int itemNumber = Integer.parseInt(editBtni.getToolTipText().substring(12));
                        String collatedItemInfo;
                        collatedItemInfo = itemNumber+ internalSeparator +editPrioField.getText()+ internalSeparator +editNameField.getText()+ internalSeparator +editInfoField.getText();
                        ListItem.saveUpdatedListItem(collatedItemInfo);
                        updateListNums();
                        ListItem.updateListItems();
                        lIPrioi.setText(editPrioField.getText());
                        lINamei.setText(editNameField.getText());
                        editFrame.dispose();
                        toDoFrame.repaint();
                        //Ensure updated list item is shown from left-most character
                            for(int i=0;i<ListItem.numOfListItems()-1;i++){
                                JTextField nameField;
                                nameField = (JTextField) listPanel.getComponent(2+(5*i));
                                nameField.setCaretPosition(0);
                            }
                    });

                    bottomBar = new JPanel();
                    bottomBar.setBackground(new Color(20,20,20));
                    bottomBar.setLayout(new BorderLayout());
                    bottomBar.add(editSaveBtn, BorderLayout.CENTER);
                    bottomBar.setPreferredSize(new Dimension(0,60));
                    bottomBar.setBorder(BorderFactory.createEmptyBorder(0,180,10,180));

                    editFrame.add(bottomBar, BorderLayout.SOUTH);
                    editFrame.add(topBar, BorderLayout.NORTH);
                    editFrame.add(editPanel);
                    editFrame.pack();
                    editFrame.setVisible(true);
                }
            });

            con.gridy = i;
            con.gridx = 4;
            con.weightx = 0.05;

            listPanel.add(editBtni, con);

            loops++;
        }

        JLabel blank = new JLabel();
        con.gridy = loops+1;
        con.gridx = 0;
        con.gridwidth = 5;
        con.weighty=100;
        listPanel.add(blank,con);

        bottomPanel = new JPanel();
        resizePanel = new JPanel();

        bottomPanel.setBackground(new Color(24,24,24));
        resizePanel.setBackground(new Color(24,24,24));

        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(0,60));

        JPanel resizeSpacingPanel = new JPanel();
        resizeSpacingPanel.setBackground(new Color(24,24,24));
        resizeSpacingPanel.setLayout(new GridLayout());


        JPanel blankPnl = new JPanel();
        blankPnl.setBackground(new Color(24,24,24));
        resizeSpacingPanel.add(blankPnl);
        resizeSpacingPanel.add(resizePanel);

        resizePanel.setBorder(createEmptyBorder(20,50,0,0));
        resizePanel.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));

        PanelResizeListener panelResizeListener = new PanelResizeListener(toDoFrame);
        resizePanel.addMouseListener(panelResizeListener);
        resizePanel.addMouseMotionListener(panelResizeListener);

        bottomPanel.add(resizeSpacingPanel, BorderLayout.EAST);

        JButton newListItemBtn = new JButton("Add Task");
        prepBtn(newListItemBtn);
        newListItemBtn.setPreferredSize(new Dimension(50,30));
        newListItemBtn.setFont(new Font("Arial", Font.PLAIN, 20));
        newListItemBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {

                //Popout for "Add list Item" window

                JFrame addFrame = new JFrame();
                JPanel addPanel = new JPanel();


                FrameDragListener frameDragListener = new FrameDragListener(addFrame, false);
                addFrame.addMouseListener(frameDragListener);
                addFrame.addMouseMotionListener(frameDragListener);
                addFrame.setLayout(new BorderLayout());
                addFrame.setUndecorated(true);

                //update location of add window to match the main window on open
                String settings = getSettings();
                x = Integer.parseInt(settings.substring(settings.indexOf(":")+2,settings.indexOf(",")));
                y = Integer.parseInt(settings.substring(settings.indexOf(",")+1,settings.indexOf("|")));

                addFrame.setBounds(x+15, y+60, 0, 0);
                addFrame.setAlwaysOnTop(false);

                topBar = new JPanel();
                topBar.setBackground(new Color(30, 30, 30));
                topBar.setLayout(new BorderLayout());

                JLabel addLabel = new JLabel("Add Item");
                prepLabel(addLabel);
                addLabel.setBorder(null);
                addLabel.setFont(new Font("Arial", Font.BOLD, 26));
                topBar.add(addLabel, BorderLayout.CENTER);

                JPanel blank = new JPanel();
                blank.setBackground(new Color(30, 30, 30));
                blank.setPreferredSize(new Dimension(50, 0));
                topBar.add(blank, BorderLayout.WEST);

                JButton addCloseBtn = new JButton("x");
                addCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
                addCloseBtn.setPreferredSize(new Dimension(50, 30));
                addCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
                addCloseBtn.addActionListener(e3 -> addFrame.dispose());
                prepBtn(addCloseBtn);

                topBar.add(addCloseBtn, BorderLayout.EAST);

                // add text fields - grab info from item
                JLabel prioLabel = new JLabel("Priority:");
                prepLabel(prioLabel);
                JTextField addPrioField = new JTextField();
                prepTextField(addPrioField);
                addPrioField.setEditable(true);
                addPrioField.setFocusable(true);

                JLabel nameLabel = new JLabel("Name:");
                prepLabel(nameLabel);
                JTextField addNameField = new JTextField();
                prepTextField(addNameField);
                addNameField.setEditable(true);
                addNameField.setFocusable(true);
                addNameField.setCaretPosition(0);

                JLabel infoLabel = new JLabel("Info:");
                prepLabel(infoLabel);
                JTextArea addInfoField = new JTextArea(4, 0);
                addInfoField.setBackground(new Color(20, 20, 20));
                addInfoField.setForeground(new Color(220, 220, 220));
                addInfoField.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
                addInfoField.setCaretColor(Color.WHITE);
                addInfoField.setLineWrap(true);

                JScrollPane sp = new JScrollPane(addInfoField);
                Border lineBorder = BorderFactory.createLineBorder(new Color(50, 50, 50));
                Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
                addInfoField.setBorder(emptyBorder);
                sp.setBorder(lineBorder);
                sp.getVerticalScrollBar().setUnitIncrement(2);
                sp.getVerticalScrollBar().setBackground(new Color(20, 20, 20));
                sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new Color(50, 50, 50);
                    }

                    public Dimension getPreferredSize(JComponent c) {
                        return new Dimension(12, 12);
                    }
                });

                addPanel.add(prioLabel);
                addPanel.add(addPrioField);
                addPanel.add(nameLabel);
                addPanel.add(addNameField);
                addPanel.add(infoLabel);
                addPanel.add(sp);

                addPanel.setLayout(new GridLayout(0, 1));
                addPanel.setBorder(createEmptyBorder(50, 50, 50, 50));

                addPanel.setBackground(new Color(20, 20, 20));
                addPanel.setPreferredSize(new Dimension(500, 300));

                JButton addSaveBtn = new JButton("Update");
                prepBtn(addSaveBtn);
                addSaveBtn.setPreferredSize(new Dimension(40,80));
                addSaveBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
                addSaveBtn.setBorderPainted(true);

                // add new Task to list
                addSaveBtn.addActionListener(e4 -> {
                    if(!(addPrioField.getText().length()>1 || intIsValid(addPrioField.getText()))){
                        JOptionPane.showMessageDialog(null, "Your priority number is invalid - please add a valid number.");
                        return;
                    }

                    if(addNameField.getText().isEmpty()){
                        JOptionPane.showMessageDialog(null, "Please add a task name.");
                        return;
                    }

                    ListItem nli = new ListItem(ListItem.numOfListItems()+1,Integer.parseInt(addPrioField.getText()),addNameField.getText(),addInfoField.getText());
                    updateListNums();
                    nli.saveListItem();
                    addFrame.dispose();
                    toDoFrame.dispose();
                    ToDoPage tdp = new ToDoPage();
                    //Ensure all updated list items are shown from left-most character
                    for(int i=0;i<ListItem.numOfListItems()-1;i++){
                        JTextField nameField;
                        nameField = (JTextField) tdp.listPanel.getComponent(2+(5*i));
                        nameField.setCaretPosition(0);
                    }
                });

                bottomBar = new JPanel();
                bottomBar.setBackground(new Color(20,20,20));
                bottomBar.setLayout(new BorderLayout());
                bottomBar.add(addSaveBtn, BorderLayout.CENTER);
                bottomBar.setPreferredSize(new Dimension(0,60));
                bottomBar.setBorder(BorderFactory.createEmptyBorder(0,180,10,180));

                addFrame.add(bottomBar, BorderLayout.SOUTH);

                addFrame.add(topBar, BorderLayout.NORTH);
                addFrame.add(addPanel);
                addFrame.pack();
                addFrame.setVisible(true);
            }
        });


        blankPnl = new JPanel();
        blankPnl.setBorder(createEmptyBorder(20,30,0,100));
        blankPnl.setBackground(new Color(24,24,24));

        JPanel btnSpacing = new JPanel();
        btnSpacing.setBackground(new Color(24,24,24));
        btnSpacing.setLayout(new BorderLayout());
        btnSpacing.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        btnSpacing.add(newListItemBtn);

        JScrollPane listSp = new JScrollPane(listPanel);
        listSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listSp.setBorder(null);

        bottomPanel.add(blankPnl, BorderLayout.WEST);
        bottomPanel.add(btnSpacing, BorderLayout.CENTER);

        toDoFrame.add(topBar, BorderLayout.NORTH);
        toDoFrame.add(bottomPanel, BorderLayout.SOUTH);
        toDoPanel.add(listSp, BorderLayout.CENTER);
        toDoFrame.add(toDoPanel);
        toDoFrame.pack();
        toDoFrame.setVisible(true);
    }

    public boolean intIsValid(String string){

        try{ Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void updateListNums(){
        for(int i=0;i<ListItem.numOfListItems();i++){
            if(i==0){
                JLabel example = (JLabel) listPanel.getComponent(0);
                example.setText(String.valueOf(i+1));
            } else {
                JLabel example = (JLabel) listPanel.getComponent((5*i));
                example.setText(i+1+"");
            }

        }
    }

    public void updateBtnNums(){
        for(int i=0;i<ListItem.numOfListItems();i++) {
            JButton example;
            example = (JButton) listPanel.getComponent(3 + (i * 5));
            example.setToolTipText("Delete Button "+(i+1));
        }
    }

    boolean minimized = false;

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == closeBtn){
            System.exit(0);
        }

        if(e.getSource() == minimizeBtn){
            minimized = true;
            ActionListener action = evt -> {
                minimizeBtn.setBackground(new Color(20,20,20));
                toDoFrame.setState(Frame.ICONIFIED);
            };

            Timer timer = new Timer(15,action);
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void prepLabel(JLabel label) {
        label.setForeground(new Color(220, 220, 220));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
//        label.addActionListener(this);
    }

    public void prepTextField(JTextField field) {
        field.setForeground(new Color(220, 220, 220));
        field.setBackground(new Color(20,20,20));
        field.setHorizontalAlignment(SwingConstants.LEFT);
        Border lineBorder = (BorderFactory.createLineBorder(new Color(50,50,50),1,false));
        Border emptyBorder = (createEmptyBorder(0,5,0,0));
        field.setBorder(BorderFactory.createCompoundBorder(lineBorder,emptyBorder));
        field.setCaretColor(Color.WHITE);
        field.setEditable(false);
        field.setFocusable(false);
    }

    public void prepBtn(JButton btn){
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(20,20,20));
        btn.setForeground(new Color(220,220,220));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.addActionListener(this);

        btn.addMouseListener(new MouseListener() {
            boolean pressed;

            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                if(mouseEntered) {
                    btn.setBackground(new Color(100, 100, 100));
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = true;
                if(mouseEntered & !minimized){
                    btn.setBackground(new Color(10,10,10));
                } else {
                    btn.setBackground(new Color(20,20,20));
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEntered = true;
                btn.setBackground(new Color(10,10,10));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouseEntered = false;
                btn.setBackground(new Color(20,20,20));
            }
        });
    }

    public static class FrameDragListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;
        private int newX;
        private int newY;
        private final boolean isMainFrame;

        public FrameDragListener(JFrame frame, boolean mainFrame){this.frame = frame; isMainFrame = mainFrame;}
        public void mouseReleased(MouseEvent e){
            mouseDownCompCoords = null;
            if(isMainFrame){
                saveWindowStatus(newX, newY, 0, 0,true);
            }
        }
        public void mousePressed(MouseEvent e){
            mouseDownCompCoords = e.getPoint();
        }
        public void mouseDragged(MouseEvent e){
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            newX = currCoords.x-mouseDownCompCoords.x;
            newY = currCoords.y - mouseDownCompCoords.y;
        }
    }

    public static class PanelResizeListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;
        int frameX;
        int frameY;
        int sizeWidth;
        int sizeHeight;

        public PanelResizeListener(JFrame frame){
            this.frame = frame;
        }
        public void mouseReleased(MouseEvent e){
            mouseDownCompCoords = null;
            saveWindowStatus(0, 0, sizeWidth, sizeHeight, false);
        }
        public void mousePressed(MouseEvent e){
            mouseDownCompCoords = e.getPoint();
            frameX = frame.getX();
            frameY = frame.getY();
        }
        public void mouseDragged(MouseEvent e){
            Point currCoords = e.getLocationOnScreen();
            int width = currCoords.x-mouseDownCompCoords.x-frame.getX()+25;
            if(width<500){
                width = 500;
            }
            int height = currCoords.y-mouseDownCompCoords.y-frameY+25;
            if(height<340){
                height = 340;
            }
            sizeWidth = width;
            sizeHeight = height;
            frame.setBounds(frameX,frameY,width,height);
        }
    }

    private static void saveWindowStatus(int locationX, int locationY, int width, int height, boolean location){
        String settings = getSettings();

        try(BufferedWriter bw = new BufferedWriter(new FileWriter("./settings/settings.txt"))){
            if(location){
                String settingsLocCoords = settings.substring(0,settings.indexOf("|"));
                settings = settings.replace(settingsLocCoords,"Last Location: "+locationX+","+locationY);
                bw.write(settings);
            }
            if(!location){
                String settingsLocSize = settings.substring(settings.indexOf("|")+1);
                settings = settings.replace(settingsLocSize," Last Size: "+width+","+height);
                bw.write(settings);
            }
        } catch (IOException e){
            throw new RuntimeException();
        }
    }

    private static String getSettings(){
        try(BufferedReader br = new BufferedReader(new FileReader("./settings/settings.txt"))){
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine()) != null){
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
