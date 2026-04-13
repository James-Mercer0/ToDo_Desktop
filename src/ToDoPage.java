import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
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
    JButton settingsBtn;
    JPanel topBar;
    JPanel bottomBar;
    boolean mouseEntered;
    JFrame settingsFrame;
    JCheckBox onTopCB;
    boolean onTopBool;
    JCheckBox opacityCB;
    boolean subWindowOpacity;
    float opacity;
    JCheckBox taskLabelsCB;
    static boolean taskLabelsEnabled;
    JCheckBox moveBtnCB;
    static boolean moveBtnsEnabled;
    JCheckBox taskNumCB;
    static boolean taskNumsEnabled;
    JButton newListBtn;
    JButton listNameBtn;

    //get the last main window location/size from settings
    String settings = getSettings();
    int x = parseInt(settings.substring(settings.indexOf(":")+2,settings.indexOf(",")));
    int y = parseInt(settings.substring(settings.indexOf(",")+1,settings.indexOf("|")));
    String sizeSettings = settings.substring(settings.indexOf("|")+1);
    int width = parseInt(sizeSettings.substring(sizeSettings.indexOf(":")+2,sizeSettings.indexOf(",")));
    int height = parseInt(sizeSettings.substring(sizeSettings.indexOf(",")+1,sizeSettings.indexOf("❂")-1));
    final boolean[] editWindowAlreadyOpen = new boolean[1];
    final boolean[] newItemWindowAlreadyOpen = new boolean[1];
    final boolean[] settingsWindowAlreadyOpen = new boolean[1];

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

        opacity = checkOpacity();

        toDoFrame.setOpacity(opacity);

        //Check settings to see if main window should be Always On Top.
        confirmAlwaysOnTop();
        toDoFrame.setAlwaysOnTop(onTopBool);

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

        settingsBtn = new JButton("≡");
        settingsBtn.setBorder(createEmptyBorder(10,0,6,0));
        settingsBtn.setPreferredSize(new Dimension(42,30));
        settingsBtn.setFont(new Font("Arial", Font.PLAIN,34));
        prepBtn(settingsBtn);

        topBar.add(topBarBtnPanel, BorderLayout.EAST);
        topBar.add(settingsBtn, BorderLayout.WEST);

        toDoPanel = new JPanel();
        toDoPanel.setLayout(new BorderLayout());
        toDoPanel.setBackground(new Color(24,24,24));
        toDoPanel.setBorder(createEmptyBorder(10,20,20,20));

        toDoName = new JLabel("To Do:");
        toDoName.setForeground(new Color(222,222,222));
        toDoName.setFont(new Font("Arial", Font.BOLD, 38));
        toDoName.setBorder(createEmptyBorder(5,0,10,0));
        toDoName.setHorizontalAlignment(SwingConstants.CENTER);

        JButton orderBtn = new JButton("Order Tasks");
        prepBtn(orderBtn);

        final boolean[] ascending = {false};

        orderBtn.addActionListener(e -> {
            if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0]){
                return;
            }
            if(ascending[0]){
                  ascending[0] = false;
                 sortList(listPanel,ascending[0],orderBtn);
            } else {
                ascending[0] = true;
                sortList(listPanel,ascending[0],orderBtn);
            }
        });

        //Customization for dialog boxes:
        UIManager.put("OptionPane.background", new Color(24,24,24));
        UIManager.put("Panel.background", new Color(24,24,24));

        JPanel aboveList = new JPanel();
        aboveList.setBackground(new Color(24,24,24));
        aboveList.setLayout(new BorderLayout());

        aboveList.add(toDoName, BorderLayout.CENTER);

        JPanel aboveListBtnPnl = new JPanel();
        aboveListBtnPnl.setBackground(new Color(24,24,24));
        aboveListBtnPnl.setLayout(new BorderLayout());

        aboveListBtnPnl.add(orderBtn, BorderLayout.WEST);

        aboveList.add(aboveListBtnPnl, BorderLayout.SOUTH);
        toDoPanel.add(aboveList, BorderLayout.NORTH);

        listPanel = new JPanel();

        listPanel.setBackground(new Color(20,20,20));
        listPanel.setForeground(new Color(222,222,222));
        listPanel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();

        int loops=0;

        JLabel listNumLabel = new JLabel("Num");
        prepLabel(listNumLabel);
        con.fill = GridBagConstraints.HORIZONTAL;
        con.anchor = GridBagConstraints.NORTH;
        con.gridy = 0;
        con.gridx = 0;
        con.weighty = 0;
        con.ipady = 10;
        con.ipadx = 15;
        con.weightx = 0.05;
        listPanel.add(listNumLabel, con);

        JLabel listPrioLabel = new JLabel("Prio");
        prepLabel(listPrioLabel);
        con.gridx = 1;
        con.weightx = 0.05;
        listPanel.add(listPrioLabel, con);

        JTextField listNameLabel = new JTextField("Name");
        prepTextField(listNameLabel);
        con.gridx = 2;
        con.weightx = 0.65;

        listPanel.add(listNameLabel, con);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(new Color(20,20,20));
        emptyPanel.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));

        con.gridx = 3;
        con.weightx = 0.05;
        con.fill = GridBagConstraints.BOTH;
        listPanel.add(emptyPanel, con);

        JPanel emptyPanel2 = new JPanel();
        emptyPanel2.setBackground(new Color(20,20,20));
        emptyPanel2.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));

        con.gridx = 4;
        con.weightx = 0.05;
        listPanel.add(emptyPanel2, con);

        JPanel emptyPanel3 = new JPanel();
        emptyPanel3.setBackground(new Color(20,20,20));
        emptyPanel3.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));

        con.gridx = 5;
        con.weightx = 0.05;
        listPanel.add(emptyPanel3, con);

        //for each task in the list, create the task and buttons for each task
        for(int i=0;i<ListItem.numOfListItems();i++){
            int j = i+1;
            String itemI = ListItem.getListItemInfo(i);

            String internalSeparator = "❒";

            int iNum = parseInt(itemI.substring(0,itemI.indexOf(internalSeparator)));
            String forNextItem = itemI.substring(itemI.indexOf(internalSeparator)+1);
            int iPrio = parseInt(forNextItem.substring(0,forNextItem.indexOf(internalSeparator)));
            forNextItem = forNextItem.substring(forNextItem.indexOf(internalSeparator)+1);
            String iName = forNextItem.substring(0,forNextItem.indexOf(internalSeparator));

            JLabel lINumi = new JLabel(String.valueOf(iNum));
            prepLabel(lINumi);
            con.fill = GridBagConstraints.HORIZONTAL;
            con.anchor = GridBagConstraints.NORTH;
            con.gridy = j;
            con.gridx = 0;
            con.weighty = 0;
            con.ipady = 10;
            con.ipadx = 15;
            con.weightx = 0.05;
            listPanel.add(lINumi, con);

            JLabel lIPrioi = new JLabel(String.valueOf(iPrio));
            prepLabel(lIPrioi);
            con.gridy = j;
            con.gridx = 1;
            con.weightx = 0.05;
            listPanel.add(lIPrioi, con);

            JTextField lINamei = new JTextField(iName,20);
            lINamei.setToolTipText(iName);
            prepTextField(lINamei);
            con.gridy = j;
            con.gridx = 2;
            con.weightx = 0.65;

            lINamei.setAutoscrolls(true);
            listPanel.add(lINamei, con);

            JButton editBtni = new JButton("Edit");

            JPanel moveBtnsPaneli = new JPanel();
            JButton moveUpi = new JButton("△");
            JButton moveDowni = new JButton("▽");

            JButton binBtni = new JButton("Del");
            prepBtn(binBtni);
            binBtni.setFont(new Font("Arial",Font.PLAIN,12));
            binBtni.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
            binBtni.setBorderPainted(true);
            binBtni.setPreferredSize(new Dimension(30,18));
            binBtni.setHorizontalTextPosition(SwingConstants.CENTER);
            binBtni.setToolTipText("Delete Button "+(i+1));
            binBtni.addActionListener( e -> {
                if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0]){
                    return;
                }

                Object confirmationResult = createDialogWindow("<html><b style=\"color:#c8c8c8; font-size: 12px;\">Are you sure you want to delete this task?</b></html>","Delete Task?", true);

                if(!confirmationResult.equals(JOptionPane.YES_OPTION)){
                    return;
                }

                String buttonNum = binBtni.getToolTipText().substring(14);
                ListItem.deleteItem(parseInt(buttonNum));
                listPanel.remove(lINumi);
                listPanel.remove(lIPrioi);
                listPanel.remove(lINamei);
                listPanel.remove(binBtni);
                listPanel.remove(editBtni);
                listPanel.remove(moveBtnsPaneli);
                ListItem.updateListItems();
                updateListNums();
                updateBtnNums();
                listPanel.repaint();
                listPanel.updateUI();
            });
            con.gridy = j;
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

            //
            // == Edit button action - creates edit window ==
            //
            editBtni.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0] || newItemWindowAlreadyOpen[0]){
                        return;
                    }

                    JFrame editFrame = new JFrame();

                    editWindowAlreadyOpen[0] = true;

                    FrameDragListener frameDragListener = new FrameDragListener(editFrame, false);
                    editFrame.addMouseListener(frameDragListener);
                    editFrame.addMouseMotionListener(frameDragListener);
                    editFrame.setLayout(new BorderLayout());
                    editFrame.setUndecorated(true);

                    confirmSubOpacity();
                    if(subWindowOpacity) {
                        opacity = checkOpacity();
                        editFrame.setOpacity(opacity);
                    }

                    editFrame.setAlwaysOnTop(true);

                    topBar = new JPanel();
                    topBar.setBackground(new Color(30, 30, 30));
                    topBar.setLayout(new BorderLayout());

                    JLabel settingsLabel = new JLabel("Edit");
                    prepLabel(settingsLabel);
                    settingsLabel.setBorder(null);
                    settingsLabel.setFont(new Font("Arial", Font.BOLD, 22));
                    topBar.add(settingsLabel, BorderLayout.CENTER);

                    JPanel blank = new JPanel();
                    blank.setBackground(new Color(30, 30, 30));
                    blank.setPreferredSize(new Dimension(50, 0));
                    topBar.add(blank, BorderLayout.WEST);

                    JButton editCloseBtn = new JButton("x");
                    editCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
                    editCloseBtn.setPreferredSize(new Dimension(50, 30));
                    editCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
                    editCloseBtn.addActionListener(e1 -> {
                        editFrame.dispose();
                        editWindowAlreadyOpen[0] = false;
                    });

                    prepBtn(editCloseBtn);

                    topBar.add(editCloseBtn, BorderLayout.EAST);

                    JPanel editPanel = new JPanel();

                    int itemToEdit = parseInt(editBtni.getToolTipText().substring(12));
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
                    Border blankBorder = createEmptyBorder(15,0,0,0);
                    Border lineBorder = BorderFactory.createLineBorder((new Color(50,50,50)),1);
                    nameLabel.setBorder(BorderFactory.createCompoundBorder(blankBorder,lineBorder));
                    JTextField editNameField = new JTextField(1);
                    prepTextField(editNameField);
                    editNameField.setEditable(true);
                    editNameField.setFocusable(true);
                    editNameField.setText(itemName);

                    JLabel infoLabel = new JLabel("Info:");
                    prepLabel(infoLabel);
                    infoLabel.setBorder(BorderFactory.createCompoundBorder(blankBorder,lineBorder));
                    JTextArea editInfoField = new JTextArea(3, 0);
                    editInfoField.setBackground(new Color(20, 20, 20));
                    editInfoField.setForeground(new Color(220, 220, 220));
                    editInfoField.setCaretColor(Color.WHITE);
                    editInfoField.setLineWrap(true);
                    editInfoField.setText(itemInfo);

                    JScrollPane sp = new JScrollPane(editInfoField);
                    Border emptyBorder = createEmptyBorder(5, 5, 5, 5);
                    editInfoField.setBorder(emptyBorder);
                    sp.setBorder(lineBorder);
                    sp.setBackground(new Color(20,20,20));
                    sp.getVerticalScrollBar().setUnitIncrement(3);
                    sp.getVerticalScrollBar().setBackground(new Color(20, 20, 20));
                    sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                        @Override
                        protected void configureScrollBarColors() {
                            this.thumbColor = new Color(50, 50, 50);
                        }
                        public Dimension getPreferredSize(JComponent c) {
                            return new Dimension(12, 12);
                        }
                        @Override
                        protected JButton createDecreaseButton(int orientation) {
                            return createZeroButton();
                        }
                        @Override
                        protected JButton createIncreaseButton(int orientation) {
                            return createZeroButton();
                        }
                        private JButton createZeroButton() {
                            JButton jbutton = new JButton();
                            Dimension zero = new Dimension(0,0);
                            jbutton.setPreferredSize(zero);
                            jbutton.setMinimumSize(zero);
                            jbutton.setMaximumSize(zero);
                            return jbutton;
                        }
                    });

                    //Edit window - add elements with appropriate spacing + layout
                    GridBagConstraints gbc = new GridBagConstraints();
                    editPanel.setLayout(new GridBagLayout());

                    gbc.fill = GridBagConstraints.BOTH;
                    gbc.weightx = 1;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.gridy = 1;
                    editPanel.add(prioLabel, gbc);
                    gbc.gridy = 2;
                    editPanel.add(editPrioField, gbc);
                    gbc.gridy = 3;
                    editPanel.add(nameLabel, gbc);
                    gbc.gridy = 4;
                    editPanel.add(editNameField, gbc);
                    gbc.gridy = 5;
                    editPanel.add(infoLabel, gbc);
                    gbc.gridy = 6;
                    editPanel.add(sp, gbc);

                    editPanel.setBorder(createEmptyBorder(0, 50, 0, 50));

                    editPanel.setBackground(new Color(20, 20, 20));
                    editFrame.setPreferredSize(new Dimension(500, 350));

                    JButton editSaveBtn = new JButton("Update");
                    prepBtn(editSaveBtn);
                    editSaveBtn.setPreferredSize(new Dimension(40,80));
                    editSaveBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
                    editSaveBtn.setBorderPainted(true);

                    //Edit window save button actions --
                    editSaveBtn.addActionListener(e2 ->{
                        // Check if updated priority is a valid number
                        if((editPrioField.getText().isEmpty() || !intIsValid(editPrioField.getText()))){
                            createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\"> Your priority number is invalid - please add a valid whole number. </b></html>","Invalid Priority Number", false);
                            return;
                        }
                        //check if updated task has a name
                        if(editNameField.getText().isEmpty()){
                            createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\">   Please add a task name.</b></html>","  Invalid Task Name  ", false);
                            return;
                        }

                        //If list is already in order, when editing priority, keep the same ordering
                        boolean inOrder = true;
                        boolean ascending = false;

                        int iterator = 0;

                        JLabel priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                        int currentListItemPrio = parseInt(priorityLabel.getText());
                        JLabel nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                        int nextListItemPrio = parseInt(nextPriorityLabel.getText());

                        if(currentListItemPrio>nextListItemPrio){
                            while(6+(1+((iterator+1)*6))<=listPanel.getComponentCount()-1){
                                priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                                currentListItemPrio = parseInt(priorityLabel.getText());
                                nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                                nextListItemPrio = parseInt(nextPriorityLabel.getText());
                                iterator++;

                                if(currentListItemPrio<nextListItemPrio){
                                    inOrder = false;
                                }

                            }
                        }

                        if(currentListItemPrio<nextListItemPrio){
                            ascending = true;
                            while(6+(1+((iterator+1)*6))<=listPanel.getComponentCount()-1){
                                priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                                currentListItemPrio = parseInt(priorityLabel.getText());
                                nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                                nextListItemPrio = parseInt(nextPriorityLabel.getText());
                                iterator++;

                                if(currentListItemPrio>nextListItemPrio){
                                    inOrder = false;
                                }

                            }
                        }

                        int itemNumber = parseInt(editBtni.getToolTipText().substring(12),10);
                        String collatedItemInfo;
                        collatedItemInfo = itemNumber+ internalSeparator +editPrioField.getText()+ internalSeparator +editNameField.getText()+ internalSeparator +editInfoField.getText();
                        ListItem.saveUpdatedListItem(collatedItemInfo);
                        updateListNums();
                        ListItem.updateListItems();
                        lIPrioi.setText(editPrioField.getText());
                        lINamei.setText(editNameField.getText());
                        editFrame.dispose();
                        editWindowAlreadyOpen[0] = false;
                        if(inOrder){
                            sortList(listPanel,ascending,null);
                        }
                        toDoFrame.repaint();
                        //Ensure updated list item is shown from left-most character
                            for(int i=0;i<ListItem.numOfListItems();i++){
                                JTextField nameField;
                                nameField = (JTextField) listPanel.getComponent(6+(2+(i*6)));
                                nameField.setCaretPosition(0);
                            }
                    });

                    bottomBar = new JPanel();
                    bottomBar.setBackground(new Color(20,20,20));
                    bottomBar.setLayout(new BorderLayout());
                    bottomBar.add(editSaveBtn, BorderLayout.CENTER);
                    bottomBar.setPreferredSize(new Dimension(0,60));
                    bottomBar.setBorder(createEmptyBorder(0,180,10,180));

                    editFrame.add(bottomBar, BorderLayout.SOUTH);
                    editFrame.add(topBar, BorderLayout.NORTH);
                    editFrame.add(editPanel);
                    editFrame.pack();

                    //update location of edit window to match the main window on open
                    editFrame.setLocation((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(editFrame.getWidth()/2)), (toDoFrame.getY()+(toDoFrame.getHeight()/2)-(editFrame.getHeight()/2)));

                    editFrame.setVisible(true);
                }
            });
            //
            // == END - edit window ==
            //

            con.gridy = j;
            con.gridx = 4;
            con.weightx = 0.05;

            listPanel.add(editBtni, con);

            moveBtnsPaneli.setBackground(new Color(20,20,20));
            moveBtnsPaneli.setLayout(new GridLayout(1,2));


            prepBtn(moveUpi);
            moveUpi.setPreferredSize(new Dimension(30,18));
            moveUpi.setFont(new Font("",Font.BOLD,12));
            moveUpi.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));
            moveUpi.setBorderPainted(true);
            moveUpi.setToolTipText("Move up button "+(i+1));


            moveUpi.addActionListener( e->{
                if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0]){
                    return;
                }
                String example = moveUpi.getToolTipText();
                moveTask(true,example);
                updateListNums();
            });

            prepBtn(moveDowni);
            moveDowni.setPreferredSize(new Dimension(30,18));
            moveDowni.setFont(new Font("",Font.BOLD,12));
            moveDowni.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));
            moveDowni.setBorderPainted(true);
            moveDowni.setToolTipText("Move down button "+(i+1));

            moveDowni.addActionListener( e->{
                if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0]){
                    return;
                }
                String example = moveDowni.getToolTipText();
                moveTask(false, example);
                updateListNums();
            });

            moveBtnsPaneli.add(moveUpi);
            moveBtnsPaneli.add(moveDowni);

            con.gridy = j;
            con.gridx = 5;
            con.weightx = 0.05;

            listPanel.add(moveBtnsPaneli, con);

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

                if(settingsWindowAlreadyOpen[0] || newItemWindowAlreadyOpen[0] || editWindowAlreadyOpen[0]){
                    return;
                }

                //Popout for "Add list Item" window

                JFrame addFrame = new JFrame();
                JPanel addPanel = new JPanel();

                newItemWindowAlreadyOpen[0] = true;

                FrameDragListener frameDragListener = new FrameDragListener(addFrame, false);
                addFrame.addMouseListener(frameDragListener);
                addFrame.addMouseMotionListener(frameDragListener);
                addFrame.setLayout(new BorderLayout());
                addFrame.setUndecorated(true);

                confirmSubOpacity();
                if(subWindowOpacity) {
                    opacity = checkOpacity();
                    addFrame.setOpacity(opacity);
                }

                addFrame.setAlwaysOnTop(true);

                topBar = new JPanel();
                topBar.setBackground(new Color(30, 30, 30));
                topBar.setLayout(new BorderLayout());

                JLabel addLabel = new JLabel("Add Item");
                prepLabel(addLabel);
                addLabel.setBorder(null);
                addLabel.setFont(new Font("Arial", Font.BOLD, 22));
                topBar.add(addLabel, BorderLayout.CENTER);

                JPanel blank = new JPanel();
                blank.setBackground(new Color(30, 30, 30));
                blank.setPreferredSize(new Dimension(50, 0));
                topBar.add(blank, BorderLayout.WEST);

                JButton addCloseBtn = new JButton("x");
                addCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
                addCloseBtn.setPreferredSize(new Dimension(50, 30));
                addCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
                addCloseBtn.addActionListener(e3 -> {
                    addFrame.dispose();
                    newItemWindowAlreadyOpen[0] = false;
                });
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
                Border blankBorder = createEmptyBorder(15,0,0,0);
                Border lineBorder = BorderFactory.createLineBorder((new Color(50,50,50)),1);
                nameLabel.setBorder(BorderFactory.createCompoundBorder(blankBorder,lineBorder));

                JTextField addNameField = new JTextField();
                prepTextField(addNameField);
                addNameField.setEditable(true);
                addNameField.setFocusable(true);
                addNameField.setCaretPosition(0);

                JLabel infoLabel = new JLabel("Info:");
                prepLabel(infoLabel);
                infoLabel.setBorder(BorderFactory.createCompoundBorder(blankBorder,lineBorder));
                JTextArea addInfoField = new JTextArea(3, 0);
                addInfoField.setBackground(new Color(20, 20, 20));
                addInfoField.setForeground(new Color(220, 220, 220));
                addInfoField.setCaretColor(Color.WHITE);
                addInfoField.setLineWrap(true);

                JScrollPane sp = new JScrollPane(addInfoField);
                Border emptyBorder = createEmptyBorder(5, 5, 5, 5);
                addInfoField.setBorder(emptyBorder);
                sp.setBorder(lineBorder);
                sp.setBackground(new Color(20,20,20));
                sp.getVerticalScrollBar().setUnitIncrement(3);
                sp.getVerticalScrollBar().setBackground(new Color(20, 20, 20));
                sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new Color(50, 50, 50);
                    }
                    public Dimension getPreferredSize(JComponent c) {
                        return new Dimension(12, 12);
                    }
                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    private JButton createZeroButton() {
                        JButton jbutton = new JButton();
                        Dimension zero = new Dimension(0,0);
                        jbutton.setPreferredSize(zero);
                        jbutton.setMinimumSize(zero);
                        jbutton.setMaximumSize(zero);
                        return jbutton;
                    }
                });

                //Add new task window - add elements with appropriate spacing + layout
                GridBagConstraints gbc = new GridBagConstraints();
                addPanel.setLayout(new GridBagLayout());

                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.gridy = 1;
                addPanel.add(prioLabel, gbc);
                gbc.gridy = 2;
                addPanel.add(addPrioField, gbc);
                gbc.gridy = 3;
                addPanel.add(nameLabel, gbc);
                gbc.gridy = 4;
                addPanel.add(addNameField, gbc);
                gbc.gridy = 5;
                addPanel.add(infoLabel, gbc);
                gbc.gridy = 6;
                addPanel.add(sp, gbc);

                addPanel.setBorder(createEmptyBorder(0, 50, 0, 50));

                addPanel.setBackground(new Color(20, 20, 20));
                addPanel.setPreferredSize(new Dimension(500, 258));

                JButton addSaveBtn = new JButton("Add Task");
                prepBtn(addSaveBtn);
                addSaveBtn.setPreferredSize(new Dimension(40,80));
                addSaveBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
                addSaveBtn.setBorderPainted(true);

                // add new Task to list
                addSaveBtn.addActionListener(e4 -> {
                    //ensure that the priority number is valid
                    if((addPrioField.getText().isEmpty() || !intIsValid(addPrioField.getText()))){
                        createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\"> Your priority number is invalid - please add a valid whole number. </b></html>","Invalid Priority Number", false);
                        return;
                    }
                    //ensure that the task has a title
                    if(addNameField.getText().isEmpty()){
                        createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\">   Please add a task name.</b></html>","  Invalid Task Name  ", false);
                        return;
                    }

                    //Check if list is already ordered - reorder if so.
                    boolean inOrder = true;
                    boolean ascending = false;

                    int iterator = 0;

                    JLabel priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                    int currentListItemPrio = parseInt(priorityLabel.getText());
                    JLabel nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                    int nextListItemPrio = parseInt(nextPriorityLabel.getText());

                    if(currentListItemPrio>nextListItemPrio){
                        while(6+(1+((iterator+1)*6))<=listPanel.getComponentCount()-1){
                            priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                            currentListItemPrio = parseInt(priorityLabel.getText());
                            nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                            nextListItemPrio = parseInt(nextPriorityLabel.getText());
                            iterator++;

                            if(currentListItemPrio<nextListItemPrio){
                                inOrder = false;
                            }

                        }
                    }

                    if(currentListItemPrio<nextListItemPrio){
                        ascending = true;
                        while(6+(1+((iterator+1)*6))<=listPanel.getComponentCount()-1){
                            priorityLabel = (JLabel) listPanel.getComponent(6+(1+(iterator*6)));
                            currentListItemPrio = parseInt(priorityLabel.getText());
                            nextPriorityLabel = (JLabel) listPanel.getComponent(6+(1+((iterator+1)*6)));
                            nextListItemPrio = parseInt(nextPriorityLabel.getText());
                            iterator++;

                            if(currentListItemPrio>nextListItemPrio){
                                inOrder = false;
                            }

                        }
                    }

                    ListItem nli = new ListItem(ListItem.numOfListItems()+1, parseInt(addPrioField.getText(),10),addNameField.getText(),addInfoField.getText());
                    updateListNums();
                    nli.saveListItem();
                    addFrame.dispose();
                    toDoFrame.dispose();
                    Point location = toDoFrame.getLocation();
                    ToDoPage tdp = new ToDoPage();
                    tdp.toDoFrame.setLocation(location);
                    if(inOrder){
                        sortList(tdp.listPanel, ascending,null);
                    }

                    //Ensure all updated list items are shown from left-most character
                    for(int i=0;i<ListItem.numOfListItems();i++){
                        JTextField nameField;
                        nameField = (JTextField) tdp.listPanel.getComponent(6+(2+(i*6)));
                        nameField.setCaretPosition(0);
                    }
                });

                bottomBar = new JPanel();
                bottomBar.setBackground(new Color(20,20,20));
                bottomBar.setLayout(new BorderLayout());
                bottomBar.add(addSaveBtn, BorderLayout.CENTER);
                bottomBar.setPreferredSize(new Dimension(0,60));
                bottomBar.setBorder(createEmptyBorder(0,180,10,180));

                addFrame.add(bottomBar, BorderLayout.SOUTH);

                addFrame.add(topBar, BorderLayout.NORTH);
                addFrame.add(addPanel);
                addFrame.pack();

                //update location of Add Task window to match the main window on open
                addFrame.setLocation((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(addFrame.getWidth()/2)), (toDoFrame.getY()+(toDoFrame.getHeight()/2)-(addFrame.getHeight()/2)));

                addFrame.setVisible(true);
            }
        });


        blankPnl = new JPanel();
        blankPnl.setBorder(createEmptyBorder(20,30,0,100));
        blankPnl.setBackground(new Color(24,24,24));

        JPanel btnSpacing = new JPanel();
        btnSpacing.setBackground(new Color(24,24,24));
        btnSpacing.setLayout(new BorderLayout());
        btnSpacing.setBorder(createEmptyBorder(0,0,10,0));

        btnSpacing.add(newListItemBtn);

        JScrollPane listSp = new JScrollPane(listPanel);
        listSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listSp.getVerticalScrollBar().setUnitIncrement(3);
        listSp.setBorder(null);
        listSp.getVerticalScrollBar().setBackground(new Color(20,20,20));
        listSp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 50, 50);
            }
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(12, 12);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                Dimension zero = new Dimension(0,0);
                jbutton.setPreferredSize(zero);
                jbutton.setMinimumSize(zero);
                jbutton.setMaximumSize(zero);
                return jbutton;
            }
        });

        bottomPanel.add(blankPnl, BorderLayout.WEST);
        bottomPanel.add(btnSpacing, BorderLayout.CENTER);

        checkMoveBtnEnabled();
        if(!moveBtnsEnabled){
            for(int i=0;i<ListItem.numOfListItems()+1;i++){
                boolean visible = listPanel.getComponent(5+(i*6)).isVisible();
                listPanel.getComponent(5+(i*6)).setVisible(!visible);
            }
        }

        checkTaskNumsEnabled();
        if(!taskNumsEnabled){
            for(int i=0;i<ListItem.numOfListItems()+1;i++){
                boolean visible = listPanel.getComponent((i*6)).isVisible();
                listPanel.getComponent((i*6)).setVisible(!visible);
            }
        }

        checkTaskLabelsEnabled();
        if(!taskLabelsEnabled){
            for(int i=0;i<6;i++){
                listPanel.getComponent(i).setVisible(false);
            }
        }

        toDoFrame.add(topBar, BorderLayout.NORTH);
        toDoFrame.add(bottomPanel, BorderLayout.SOUTH);
        toDoPanel.add(listSp, BorderLayout.CENTER);
        toDoFrame.add(toDoPanel);
        toDoFrame.pack();

        //Check for updated min with allowing for optional buttons/nums
        int minWidth = 434;
        if(taskNumsEnabled){
            minWidth = minWidth +28;
        }
        if(moveBtnsEnabled){
            minWidth = minWidth +69;
        }
        if(toDoFrame.getWidth()< minWidth){
            toDoFrame.setSize(minWidth,toDoFrame.getHeight());
        }

        toDoFrame.setVisible(true);
    }



    private void openSettingsWindow(){

        if(settingsWindowAlreadyOpen[0] || editWindowAlreadyOpen[0] || newItemWindowAlreadyOpen[0]){
            return;
        }

        settingsWindowAlreadyOpen[0] = true;

        settingsFrame = new JFrame();
        settingsFrame.setAlwaysOnTop(true);
        settingsFrame.setUndecorated(true);
        int settingsWidth = 500;
        int settingsHeight = 500;
        settingsFrame.setPreferredSize(new Dimension(settingsWidth,settingsHeight));
        settingsFrame.setBounds((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(settingsWidth/2)),(toDoFrame.getY()+(toDoFrame.getHeight()/2)-(settingsHeight/2)),0,0);
        if(settingsFrame.getY()<0){
            settingsFrame.setLocation((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(settingsWidth/2)),0);
        }

        FrameDragListener dialogFrameDragListener = new FrameDragListener(settingsFrame, false);
        settingsFrame.addMouseListener(dialogFrameDragListener);
        settingsFrame.addMouseMotionListener(dialogFrameDragListener);

        confirmSubOpacity();
        if(subWindowOpacity) {
            opacity = checkOpacity();
            settingsFrame.setOpacity(opacity);
        }

        //Settings TopBar
        topBar = new JPanel();
        topBar.setBackground(new Color(30, 30, 30));
        topBar.setLayout(new BorderLayout());

        JLabel dialogLabel = new JLabel("Settings");
        prepLabel(dialogLabel);
        dialogLabel.setBorder(null);
        dialogLabel.setFont(new Font("Arial", Font.BOLD, 22));
        topBar.add(dialogLabel, BorderLayout.CENTER);

        JPanel blank = new JPanel();
        blank.setBackground(new Color(30, 30, 30));
        blank.setPreferredSize(new Dimension(50, 0));
        topBar.add(blank, BorderLayout.WEST);

        JButton settingsCloseBtn = new JButton("x");
        prepBtn(settingsCloseBtn);
        settingsCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
        settingsCloseBtn.setPreferredSize(new Dimension(50, 30));
        settingsCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
        settingsCloseBtn.addActionListener(e1 -> {
            settingsFrame.dispose();
            settingsWindowAlreadyOpen[0] = false;
        });

        topBar.add(settingsCloseBtn, BorderLayout.EAST);

        //Settings Main Panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(new Color(24,24,24));
        settingsPanel.setBorder(BorderFactory.createCompoundBorder((createEmptyBorder(30,30,30,30)),(BorderFactory.createLineBorder(new Color(50,50,50),1))));
        settingsPanel.setLayout(new GridLayout(0,1));

        //List Selection
        JPanel settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel switchListsLabel = new JLabel("Change Current List:");
        prepLabel(switchListsLabel);
        switchListsLabel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));

        ArrayList<String> listOptions = new ArrayList<>();
        File listDir = new File("./listStorage/");

        File[] files = listDir.listFiles();
        assert files != null;
        if(files.length>0){
            for(int i=0;i<files.length;i++){
                String fileName = files[i].toString();
                listOptions.add(fileName.substring(ListItem.dirPath.length()+1));
            }
        }

        String[] fileNameList = listOptions.toArray(new String[files.length]);

        JComboBox<String> listsList = new JComboBox<>(fileNameList);

        int currentOptionIndex = 0;
        for(int i=1;i<fileNameList.length;i++){
            if(fileNameList[i].equals(ListItem.getSavedList())){
                currentOptionIndex = i;
            }
        }
        listsList.setSelectedIndex(currentOptionIndex);
        listsList.setForeground(new Color(220,220,220));
        listsList.setBackground(new Color(24,24,24));
        listsList.setBorder(BorderFactory.createCompoundBorder(
                createEmptyBorder(10,10,10,10),
                BorderFactory.createLineBorder(new Color(50,50,50),1)
        ));
        listsList.setCursor(new Cursor(Cursor.HAND_CURSOR));

        listsList.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    public void show() {
                        int x = comboBox.getInsets().left-1;
                        int y = comboBox.getHeight() - comboBox.getInsets().bottom;
                        show(comboBox, x, y);
                    }
                };
                popup.setPopupSize(new Dimension(171,(fileNameList.length*20)));
                return popup;
            }
            @Override
            protected JButton createArrowButton() {
                JButton newArrowBtn = new JButton("˅");
                prepBtn(newArrowBtn);
                newArrowBtn.setBorder(null);
                newArrowBtn.setFont(new Font("Arial", Font.BOLD,20));
                newArrowBtn.setBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(50,50,50)));
                newArrowBtn.setBorderPainted(true);
            return newArrowBtn;
            }
        });

        listsList.addActionListener(e -> {
            boolean listAlreadyInUse = !ListItem.updateSavedList((String) listsList.getSelectedItem());
            if(listAlreadyInUse){
                return;
            }
            settingsFrame.dispose();
            toDoFrame.dispose();
            ListItem.listFileName = ListItem.getSavedList();
            Point location = toDoFrame.getLocation();
            ToDoPage newToDo = new ToDoPage();
            newToDo.toDoFrame.setLocation(location);
            newToDo.openSettingsWindow();
            for(int i=0;i<ListItem.numOfListItems();i++){
                JTextField taskName = (JTextField) newToDo.listPanel.getComponent(6+(2+(i*6)));
                taskName.setCaretPosition(0);
            }
        });


        settingsDiv.add(switchListsLabel);
        settingsDiv.add(listsList);

        settingsPanel.add(settingsDiv);

        //Create New List
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,1));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)),
                createEmptyBorder(8,90,8,90)
        ));

        newListBtn = new JButton("Create New List");
        prepBtn(newListBtn);
        newListBtn.setBorderPainted(true);
        newListBtn.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));

        settingsDiv.add(newListBtn);

        settingsPanel.add(settingsDiv);

        //Rename Current List
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,1));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)),
                createEmptyBorder(8,90,8,90)
        ));

        listNameBtn = new JButton("Rename Current List");
        prepBtn(listNameBtn);
        listNameBtn.setBorderPainted(true);
        listNameBtn.setBorder(BorderFactory.createLineBorder(new Color(50,50,50),1));

        settingsDiv.add(listNameBtn);

        settingsPanel.add(settingsDiv);

        //Always On Top
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel onTopLabel = new JLabel("Always On Top:");
        prepLabel(onTopLabel);
        onTopLabel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(onTopLabel);

        confirmAlwaysOnTop();

        onTopCB = new JCheckBox();
        onTopCB.setSelected(onTopBool);
        onTopCB.setBackground(new Color(20,20,20));
        onTopCB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        onTopCB.addActionListener(this);
        onTopCB.setBorder(createEmptyBorder(20,100,20,20));
        settingsDiv.add(onTopCB);

        settingsPanel.add(settingsDiv);


        //Opacity for main window

        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel opacityLabel = new JLabel("Opacity");
        prepLabel(opacityLabel);
        opacityLabel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(opacityLabel);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(2,0));
        sliderPanel.setBackground(new Color(24,24,24));
        sliderPanel.setBorder(BorderFactory.createMatteBorder(0,0,0,0,new Color(50,50,50)));

        opacity = checkOpacity();

        JTextField opacityVal = new JTextField();

        JSlider opacitySlider = new JSlider();
        opacitySlider.setMinimum(2);
        opacitySlider.setMaximum(10);
        opacitySlider.setPaintTicks(false);
        opacitySlider.setBackground(new Color(20,20,20));
        opacitySlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        int opacityForSlider = Math.round(opacity*10);
        opacitySlider.setValue(opacityForSlider);
        opacitySlider.addChangeListener(e->{
            JSlider prop = (JSlider)e.getSource();
            float val = prop.getValue();
            if(settingsFrame.isVisible() && !String.valueOf(val).equals(opacityVal.getText())){
                opacityVal.setText(String.valueOf(val/10));
                toDoFrame.setOpacity(val/10);
                if(subWindowOpacity) {
                    settingsFrame.setOpacity(val / 10);
                }
            }
            try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
                String updatedSettings = settings.replaceFirst("Opacity: "+opacity,"Opacity: "+((float) opacitySlider.getValue()/10));
                bw.write(updatedSettings);
            } catch (IOException e1){
                throw new RuntimeException(e1);
            }
        });

        settingsPanel.add(opacitySlider);

        sliderPanel.add(opacitySlider);

        opacityVal.setEditable(false);
        opacityVal.setText(""+opacity);
        opacityVal.setBackground(new Color(15,15,15));
        opacityVal.setForeground(new Color(150,150,150));
        opacityVal.setFont(new Font("Arial",Font.PLAIN,18));
        opacityVal.setHighlighter(null);
        opacityVal.setFocusable(false);
        opacityVal.setBorder(createEmptyBorder(0,60,0,60));
        opacityVal.setHorizontalAlignment(JTextField.CENTER);


        sliderPanel.add(opacityVal);

        settingsDiv.add(sliderPanel);

        settingsPanel.add(settingsDiv);

        //Opacity for Sub-Windows
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel subOpacityLabel = new JLabel("Enable Opacity for sub-windows:");
        prepLabel(subOpacityLabel);
        subOpacityLabel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(subOpacityLabel);

        confirmAlwaysOnTop();

        opacityCB = new JCheckBox();
        opacityCB.setSelected(subWindowOpacity);
        opacityCB.setBackground(new Color(20,20,20));
        opacityCB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        opacityCB.addActionListener(this);
        opacityCB.setBorder(createEmptyBorder(20,100,20,20));
        settingsDiv.add(opacityCB);

        settingsPanel.add(settingsDiv);

        //Task Info Labels toggle
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel disableTaskLabels = new JLabel("Enable Task information Labels?");
        prepLabel(disableTaskLabels);
        disableTaskLabels.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(disableTaskLabels);

        taskLabelsCB = new JCheckBox();

        checkTaskLabelsEnabled();

        taskLabelsCB.setSelected(taskLabelsEnabled);
        taskLabelsCB.setBackground(new Color(20,20,20));
        taskLabelsCB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        taskLabelsCB.addActionListener(this);
        taskLabelsCB.setBorder(createEmptyBorder(20,100,20,20));
        settingsDiv.add(taskLabelsCB);

        settingsPanel.add(settingsDiv);

        //Move Task buttons toggle
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(50,50,50)));

        JLabel disableMoveTaskBtns = new JLabel("Enable Move Task Buttons?");
        prepLabel(disableMoveTaskBtns);
        disableMoveTaskBtns.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(disableMoveTaskBtns);

        moveBtnCB = new JCheckBox();

        checkMoveBtnEnabled();

        moveBtnCB.setSelected(moveBtnsEnabled);
        moveBtnCB.setBackground(new Color(20,20,20));
        moveBtnCB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moveBtnCB.addActionListener(this);
        moveBtnCB.setBorder(createEmptyBorder(20,100,20,20));
        settingsDiv.add(moveBtnCB);

        settingsPanel.add(settingsDiv);

        //Task Number toggle
        settingsDiv = new JPanel();
        settingsDiv.setLayout(new GridLayout(0,2));
        settingsDiv.setBackground(new Color(24,24,24));
//        settingsDiv.setBorder(BorderFactory.createMatteBorder(0,0,0,0,new Color(50,50,50)));

        JLabel disableTaskNums = new JLabel("Enable Task Numbers?");
        prepLabel(disableTaskNums);
        disableTaskNums.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(50,50,50)));
        settingsDiv.add(disableTaskNums);

        taskNumCB = new JCheckBox();

        checkTaskNumsEnabled();

        taskNumCB.setSelected(taskNumsEnabled);
        taskNumCB.setBackground(new Color(20,20,20));
        taskNumCB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        taskNumCB.addActionListener(this);
        taskNumCB.setBorder(createEmptyBorder(20,100,20,20));
        settingsDiv.add(taskNumCB);

        settingsPanel.add(settingsDiv);

        //Settings Combine elements
        settingsFrame.setLayout(new BorderLayout());
        settingsFrame.add(settingsPanel, BorderLayout.CENTER);
        settingsFrame.add(topBar, BorderLayout.NORTH);

        settingsFrame.pack();
        settingsFrame.setVisible(true);

    }

    private void checkTaskLabelsEnabled(){
        settings=getSettings();
        String taskLabelSetting = getSpecificSetting(6,(settings.substring(settings.indexOf("❂")+1)));
        taskLabelsEnabled = Boolean.parseBoolean(taskLabelSetting);
    }

    private void checkTaskNumsEnabled(){
        settings=getSettings();
        String taskNumSetting = getSpecificSetting(5,(settings.substring(settings.indexOf("❂")+1)));
        taskNumsEnabled = Boolean.parseBoolean(taskNumSetting);
    }

    private void checkMoveBtnEnabled(){
        settings=getSettings();
        String moveBtnSetting = getSpecificSetting(3,(settings.substring(settings.indexOf("❂")+1)));
        moveBtnsEnabled = Boolean.parseBoolean(moveBtnSetting);
    }

    private float checkOpacity(){
        settings=getSettings();
        String opacitySetting = getSpecificSetting(1, (settings.substring(settings.indexOf("❂")+1)));
        opacity = Float.parseFloat(opacitySetting);

        return opacity;
    }

    private void confirmAlwaysOnTop(){
        String onTopSetting =  getSpecificSetting(0, (settings.substring(settings.indexOf("❂")+1)));
        onTopBool = onTopSetting.equals("true");
    }

    private void confirmSubOpacity(){
        String subOpSetting =  getSpecificSetting(2, settings.substring(settings.indexOf("❂")+1));
        subWindowOpacity = subOpSetting.equals("true");
    }

    private static String getSpecificSetting(int line, String settingString){
        for(int i=0;i<line;i++){
            settingString = settingString.substring(settingString.indexOf("❂")+1);
        }
        settingString = settingString.substring(settingString.indexOf(":")+2,settingString.indexOf("❂")-1);

        return settingString;
    }

    private Object createDialogWindow(String message, String title, Boolean question){
        JDialog dialog = new JDialog(toDoFrame,"");
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setUndecorated(true);

        JPanel optionPanePanel = new JPanel();
        optionPanePanel.setBackground(new Color(20,20,20));

        JOptionPane confirmationPane;

        if(question == true) {
            confirmationPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);
        } else {
            confirmationPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE);
        }

        topBar = new JPanel();
        topBar.setBackground(new Color(30, 30, 30));
        topBar.setLayout(new BorderLayout());

        JLabel dialogLabel = new JLabel(title);
        prepLabel(dialogLabel);
        dialogLabel.setBorder(null);
        dialogLabel.setFont(new Font("Arial", Font.BOLD, 22));
        topBar.add(dialogLabel, BorderLayout.CENTER);

        JPanel blank = new JPanel();
        blank.setBackground(new Color(30, 30, 30));
        blank.setPreferredSize(new Dimension(50, 0));
        topBar.add(blank, BorderLayout.WEST);

        JButton dialogCloseBtn = new JButton("x");
        dialogCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
        dialogCloseBtn.setPreferredSize(new Dimension(50, 30));
        dialogCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
        dialogCloseBtn.addActionListener(e1 -> {
            dialog.dispose();
        });

        topBar.add(dialogCloseBtn, BorderLayout.EAST);

        optionPanePanel.setLayout(new BorderLayout());
        optionPanePanel.add(confirmationPane, BorderLayout.CENTER);
        optionPanePanel.add(topBar, BorderLayout.NORTH);

        dialog.setContentPane(optionPanePanel);

        applyPrepToButtons(dialog);
        prepBtn(dialogCloseBtn);

        FrameDragListener dialogFrameDragListener = new FrameDragListener(dialog, false);
        dialog.addMouseListener(dialogFrameDragListener);
        dialog.addMouseMotionListener(dialogFrameDragListener);

        dialog.setLocation(toDoFrame.getX()+(toDoFrame.getWidth()/2)-(170),toDoFrame.getY()+(toDoFrame.getHeight()/2)-(60));

        confirmationPane.addPropertyChangeListener( e -> {
            String prop = e.getPropertyName();

            if (dialog.isVisible() && JOptionPane.VALUE_PROPERTY.equals(prop)) {
                dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setVisible(true);

        return confirmationPane.getValue();
    }

    private void applyPrepToButtons(Container container){
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton btn) {
                prepBtn(btn);
                Border padding = createEmptyBorder(10,10,10,10);
                Border line = BorderFactory.createLineBorder(new Color(50,50,50),1);
                btn.setBorder(BorderFactory.createCompoundBorder(line,padding));
                btn.setBorderPainted(true);
            } else if (comp instanceof Container c) {
                applyPrepToButtons(c);
            }
        }
    }

    private void moveTask(boolean moveUp, String buttonToolTip) {

        //Visually swap Tasks by replacing Priority and Name Text between them
        int taskNum;
        int taskToSwapNum;
        if(moveUp){
            taskNum = parseInt(buttonToolTip.substring(15));
        } else {
            taskNum = parseInt(buttonToolTip.substring(17));
        }

        JLabel currentPriority;
        currentPriority = (JLabel) this.listPanel.getComponent(6+(1+((taskNum-1)*6)));

        JTextField currentName;
        currentName = (JTextField) this.listPanel.getComponent(6+(2+((taskNum-1)*6)));

        JLabel toSwapPriority;
        JTextField toSwapName;

        if(moveUp){
            if(taskNum==1){
                return;
            }
            taskToSwapNum = taskNum-1;
            toSwapPriority = (JLabel) this.listPanel.getComponent(6+(1+((taskNum-2)*6)));
            toSwapName = (JTextField) this.listPanel.getComponent(6+(2+((taskNum-2)*6)));
        } else {
            taskToSwapNum = taskNum+1;
            if(taskToSwapNum>ListItem.numOfListItems()){
                return;
            }
            taskToSwapNum = taskNum+1;
            toSwapPriority = (JLabel) this.listPanel.getComponent(6+(1+((taskNum)*6)));
            toSwapName = (JTextField) this.listPanel.getComponent(6+(2+((taskNum)*6)));
        }

        String holdingP = currentPriority.getText();
        String holdingN = currentName.getText();
        String holdingTT = currentName.getToolTipText();

        currentPriority.setText(toSwapPriority.getText());
        currentName.setText(toSwapName.getText());
        currentName.setToolTipText(toSwapName.getToolTipText());

        toSwapPriority.setText(holdingP);
        toSwapName.setText(holdingN);
        toSwapName.setToolTipText(holdingTT);

        //Update Swapped tasks in the tasklist file
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<ListItem.numOfListItems();i++){
            if(i+1==taskNum){
                String taskToSwapTo = ListItem.getListItemInfo(taskToSwapNum-1);
                String taskToSwapToNum = taskToSwapTo.substring(0,taskToSwapTo.indexOf("❒"));
                taskToSwapTo = taskToSwapTo.replaceFirst(taskToSwapToNum,String.valueOf(i+1));
                sb.append(taskToSwapTo).append("❂");
            } else if (i+1==taskToSwapNum) {
                String taskToSwap = ListItem.getListItemInfo(taskNum-1);
                String toSwapNum = taskToSwap.substring(0,taskToSwap.indexOf("❒"));
                taskToSwap = taskToSwap.replaceFirst(toSwapNum,String.valueOf(i+1));
                sb.append(taskToSwap).append("❂");
            } else {
                sb.append(ListItem.getListItemInfo(i)).append("❂");
            }
        }
        ListItem.saveUpdatedItemList(sb.toString());
        for(int i=0;i<ListItem.numOfListItems();i++){
            JTextField taskName = (JTextField) this.listPanel.getComponent(6+(2+(i*6)));
            taskName.setCaretPosition(0);
        }
    }

    private void sortList(JPanel listPanel, boolean ascending,JButton orderBtn) {
        TreeMap<String,String> orderedList = new TreeMap<>();
        for(int i=0;i<ListItem.numOfListItems();i++){
            JLabel numLabel = (JLabel) listPanel.getComponent(6+(i*6));
            String itemNum = numLabel.getText();
            JLabel prioLabel = (JLabel) listPanel.getComponent(6+(1+(i*6)));
            String itemPrio = prioLabel.getText();
            orderedList.put(itemNum,itemPrio);
        }
        HashMap<String, ArrayList<String>> reOrderedList = new HashMap<>();

        if(ascending){
            if(orderBtn != null){
                orderBtn.setText("Order Tasks ▼");
            }
            for(int h=0;h<ListItem.numOfListItems();h++) {
                String original = orderedList.firstKey();
                if(orderedList.size()>1) {
                    int lowest = parseInt(original);
                    int current = parseInt(orderedList.higherKey(original));
                    for (int i = 1; i < orderedList.size() + 1; i++) {
                        int lowestValue = parseInt(orderedList.get(String.valueOf(lowest)));
                        int currentValue = parseInt(orderedList.get(String.valueOf(current)));
                        if (lowestValue > currentValue) {
                            lowest = current;
                        }
                        if (i < orderedList.size() - 1) {
                            current = parseInt(orderedList.higherKey(String.valueOf(current)));
                        }
                    }
                    String currentLowest = String.valueOf(lowest);
                    ArrayList<String> ar = new ArrayList<>(2);
                    ar.add(currentLowest);
                    ar.add(orderedList.get(currentLowest));
                    reOrderedList.put(String.valueOf(h), ar);
                    orderedList.remove(currentLowest);
                } else {
                    ArrayList<String> ar = new ArrayList<>(2);
                    ar.add(orderedList.firstKey());
                    ar.add(orderedList.get(orderedList.firstKey()));
                    reOrderedList.put(String.valueOf(h), ar);
                }
            }
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<reOrderedList.size();i++){
                ArrayList<String> returnedArray = reOrderedList.get(String.valueOf(i));
                String orderedListItemNumber = returnedArray.get(0);
                String currentItem = ListItem.getListItemInfo(parseInt(orderedListItemNumber)-1);
                String toReplace = currentItem.substring(0,currentItem.indexOf("❒"));
                String updatedItem = currentItem.replaceFirst(toReplace,String.valueOf(i+1));
                sb.append(updatedItem).append("❂");
            }
            ListItem.saveUpdatedItemList(sb.toString());
            updateList(listPanel);
            orderedList.clear();
            reOrderedList.clear();
        }
        if(!ascending){
            if(orderBtn != null) {
                orderBtn.setText("Order Tasks ▲");
            }
            for(int h=0;h<ListItem.numOfListItems();h++) {
                String original = orderedList.firstKey();
                if(orderedList.size()>1) {
                    int highest = parseInt(original);
                    int current = parseInt(orderedList.higherKey(original));
                    for (int i = 1; i < orderedList.size() + 1; i++) {
                        int highestValue = parseInt(orderedList.get(String.valueOf(highest)));
                        int currentValue = parseInt(orderedList.get(String.valueOf(current)));
                        if (highestValue < currentValue) {
                            highest = current;
                        }
                        if (i < orderedList.size() - 1) {
                            current = parseInt(orderedList.higherKey(String.valueOf(current)));
                        }
                    }
                    String currentLowest = String.valueOf(highest);
                    ArrayList<String> ar = new ArrayList<>(2);
                    ar.add(currentLowest);
                    ar.add(orderedList.get(currentLowest));
                    reOrderedList.put(String.valueOf(h), ar);
                    orderedList.remove(currentLowest);
                } else {
                    ArrayList<String> ar = new ArrayList<>(2);
                    ar.add(orderedList.firstKey());
                    ar.add(orderedList.get(orderedList.firstKey()));
                    reOrderedList.put(String.valueOf(h), ar);
                }
            }
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<reOrderedList.size();i++){
                ArrayList<String> returnedArray = reOrderedList.get(String.valueOf(i));
                String orderedListItemNumber = returnedArray.get(0);
                String currentItem = ListItem.getListItemInfo(parseInt(orderedListItemNumber)-1);
                String toReplace = currentItem.substring(0,currentItem.indexOf("❒"));
                String updatedItem = currentItem.replaceFirst(toReplace,String.valueOf(i+1));
                sb.append(updatedItem).append("❂");
            }
            ListItem.saveUpdatedItemList(sb.toString());
            updateList(listPanel);
            orderedList.clear();
            reOrderedList.clear();
        }

    }

    private void updateList(JPanel listPanel) {

        for(int i=0;i<ListItem.numOfListItems();i++) {
            String itemI = ListItem.getListItemInfo(i);

            String internalSeparator = "❒";

            int iNum = parseInt(itemI.substring(0, itemI.indexOf(internalSeparator)));
            String forNextItem = itemI.substring(itemI.indexOf(internalSeparator) + 1);
            int iPrio = parseInt(forNextItem.substring(0, forNextItem.indexOf(internalSeparator)));
            forNextItem = forNextItem.substring(forNextItem.indexOf(internalSeparator) + 1);
            String iName = forNextItem.substring(0, forNextItem.indexOf(internalSeparator));

            JLabel itemNameLabel = (JLabel)listPanel.getComponent(6+(i*6));
            itemNameLabel.setText(String.valueOf(iNum));

            JLabel itemPrioLabel = (JLabel)listPanel.getComponent(6+(1+(i*6)));
            itemPrioLabel.setText(String.valueOf(iPrio));

            JTextField itemName = (JTextField)listPanel.getComponent(6+(2+(i*6)));
            itemName.setText(iName);
            itemName.setToolTipText(iName);
            itemName.setCaretPosition(0);
        }
    }

    public boolean intIsValid(String string){
        try{ parseInt(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateListNums(){
        for(int i=0;i<ListItem.numOfListItems();i++){
            JLabel example = (JLabel) listPanel.getComponent(6+((i*6)));
            example.setText(i+1+"");
        }
    }

    public void updateBtnNums(){
        for(int i=0;i<ListItem.numOfListItems();i++) {
            JButton example;
            example = (JButton) listPanel.getComponent(6+(3 + (i*6)));
            example.setToolTipText("Delete Button "+(i+1));

            JButton editBtn;
            editBtn = (JButton) listPanel.getComponent(6+(4 + (i*6)));
            editBtn.setToolTipText("Edit Button "+(i+1));

            JPanel movePnl;
            movePnl = (JPanel) listPanel.getComponent(6+(5 + (i*6)));

            JButton moveUpBtn;
            moveUpBtn = (JButton) movePnl.getComponent(0);
            moveUpBtn.setToolTipText("Move up button "+(i+1));

            JButton moveDownBtn;
            moveDownBtn = (JButton) movePnl.getComponent(1);
            moveDownBtn.setToolTipText("Move down button "+(i+1));
        }
    }

    boolean minimized = false;
    boolean alwaysOnTopEnabled;

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

        if(e.getSource() == settingsBtn){
            openSettingsWindow();
        }

        if(e.getSource() == onTopCB){
            alwaysOnTopEnabled = onTopCB.isSelected();
            updateAlwaysOnTop(alwaysOnTopEnabled);
        }

        if(e.getSource() == opacityCB){
            updateSubOpacity(opacityCB.isSelected());
        }

        if(e.getSource() == taskLabelsCB){
            checkTaskLabelsEnabled();
            checkTaskNumsEnabled();
            checkMoveBtnEnabled();
            for(int i=0;i<6;i++){
                if(i==0){
                    listPanel.getComponent(i).setVisible((taskLabelsCB.isSelected() && taskNumsEnabled));
                } else if(i==5){
                    listPanel.getComponent(i).setVisible((taskLabelsCB.isSelected() && moveBtnsEnabled));
                } else {
                    listPanel.getComponent(i).setVisible(taskLabelsCB.isSelected());
                }
            }
            taskLabelsEnabled = taskLabelsCB.isSelected();
            updateTaskLabelsEnabled(taskLabelsEnabled);
        }

        if(e.getSource() == moveBtnCB){
            checkTaskLabelsEnabled();

            if(taskLabelsEnabled){
                boolean visible = listPanel.getComponent(5).isVisible();
                listPanel.getComponent(5).setVisible(!visible);
            }

            for(int i=0;i<ListItem.numOfListItems();i++){
                boolean visible = listPanel.getComponent(6+(5+(i*6))).isVisible();
                listPanel.getComponent(6+(5+(i*6))).setVisible(!visible);
            }
            moveBtnsEnabled = moveBtnCB.isSelected();
            int minWidth = 434;
            if(taskNumsEnabled){
                minWidth = minWidth +28;
            }
            if(moveBtnsEnabled){
                minWidth = minWidth +69;
            } else {
                toDoFrame.setSize(toDoFrame.getWidth()-69,toDoFrame.getHeight());
            }
            if(toDoFrame.getWidth()< minWidth){
                toDoFrame.setSize(minWidth,toDoFrame.getHeight());
            }
            updateMoveBtnsEnabled(moveBtnsEnabled);
        }

        if(e.getSource() == taskNumCB){
            checkTaskLabelsEnabled();

            if(taskLabelsEnabled){
                boolean visible = listPanel.getComponent(0).isVisible();
                listPanel.getComponent(0).setVisible(!visible);
            }
            for(int i=0;i<ListItem.numOfListItems();i++){
                boolean visible = listPanel.getComponent(6+(i*6)).isVisible();
                listPanel.getComponent(6+(i*6)).setVisible(!visible);
            }
            taskNumsEnabled = taskNumCB.isSelected();
            int minWidth = 434;
            if(taskNumsEnabled){
                minWidth = minWidth +28;
            } else {
                toDoFrame.setSize(toDoFrame.getWidth()-24,toDoFrame.getHeight());
            }
            if(moveBtnsEnabled){
                minWidth = minWidth +69;
            }
            if(toDoFrame.getWidth()< minWidth){
                toDoFrame.setSize(minWidth,toDoFrame.getHeight());
            }
            updateTaskNumsEnabled(taskNumsEnabled);
        }

        if(e.getSource() == newListBtn){
            JFrame newListFrame = new JFrame();
            JPanel newListPanel = new JPanel();

            FrameDragListener frameDragListener = new FrameDragListener(newListFrame, false);
            newListFrame.addMouseListener(frameDragListener);
            newListFrame.addMouseMotionListener(frameDragListener);
            newListFrame.setLayout(new BorderLayout());
            newListFrame.setUndecorated(true);

            confirmSubOpacity();
            if(subWindowOpacity) {
                opacity = checkOpacity();
                newListFrame.setOpacity(opacity);
            }

            newListFrame.setAlwaysOnTop(true);

            topBar = new JPanel();
            topBar.setBackground(new Color(30, 30, 30));
            topBar.setLayout(new BorderLayout());

            JLabel addLabel = new JLabel("Create New List");
            prepLabel(addLabel);
            addLabel.setBorder(null);
            addLabel.setFont(new Font("Arial", Font.BOLD, 20));
            topBar.add(addLabel, BorderLayout.CENTER);

            JPanel blank = new JPanel();
            blank.setBackground(new Color(30, 30, 30));
            blank.setPreferredSize(new Dimension(50, 0));
            topBar.add(blank, BorderLayout.WEST);

            JButton addCloseBtn = new JButton("x");
            addCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
            addCloseBtn.setPreferredSize(new Dimension(50, 30));
            addCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
            addCloseBtn.addActionListener(e3 -> {
                newListFrame.dispose();
            });
            prepBtn(addCloseBtn);

            topBar.add(addCloseBtn, BorderLayout.EAST);

            //New List Name input
            JLabel listNameLabel = new JLabel("New List's Name:");
            listNameLabel.setFont(new Font("Arial",Font.BOLD,15));
            prepLabel(listNameLabel);
            JTextField listNameField = new JTextField();
            prepTextField(listNameField);
            listNameField.setEditable(true);
            listNameField.setFocusable(true);


            newListPanel.setLayout(new GridLayout(3,0));

            newListPanel.add(listNameLabel);
            newListPanel.add(listNameField);

            newListPanel.setBorder(createEmptyBorder(20, 20, 0, 20));
            newListPanel.setBackground(new Color(20, 20, 20));
            newListPanel.setPreferredSize(new Dimension(350, 140));

            JButton newListSaveBtn = new JButton("Save New List");
            prepBtn(newListSaveBtn);
            newListSaveBtn.setPreferredSize(new Dimension(40,80));
            newListSaveBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
            newListSaveBtn.setBorderPainted(true);

            // add new Task to list
            newListSaveBtn.addActionListener(e4 -> {

                //ensure NO Special Chars in List Name
                Pattern pattern = Pattern.compile("[^a-z0-9 ,'()@_~-]", Pattern.CASE_INSENSITIVE);
                Matcher match = pattern.matcher(listNameField.getText());
                boolean foundSpecialChar = match.find();

                //ensure New List Name does not already exist
                ArrayList<String> listOptions = new ArrayList<>();
                File listDir = new File(ListItem.dirPath+"/");
                File[] files = listDir.listFiles();
                assert files != null;

                if(files.length>0){
                    for(int i=0;i<files.length;i++){
                        String fileName = files[i].toString();
                        listOptions.add(fileName.substring(ListItem.dirPath.length()+1));
                    }
                }

                String[] fileNameList = listOptions.toArray(new String[files.length]);
                boolean listNameAlreadyExists = false;

                for(int i=0;i<fileNameList.length;i++){
                    String existingListName = fileNameList[i].substring(0,fileNameList[i].indexOf("."));
                    if(existingListName.equals(listNameField.getText())){
                        listNameAlreadyExists = true;
                    }
                }

                String noSpacesName = listNameField.getText().replace(" ","_");

                //List Name Valid?
                if(noSpacesName.isEmpty() || foundSpecialChar || listNameAlreadyExists){
                    createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\">Please add a Valid and Unique list name with No Special Characters.</b></html>","  Invalid Task Name  ", false);
                    return;
                }

                //Create New List File
                File newList = new File(ListItem.dirPath+"/"+noSpacesName+".tdli");
                try {
                    newList.createNewFile();
                } catch (IOException e1){
                    throw new RuntimeException (e1);
                }

                //Set new list as current list
                ListItem.updateSavedList(noSpacesName+".tdli");
                ListItem.listFileName = ListItem.getSavedList();
                newListFrame.dispose();
                settingsFrame.dispose();
                toDoFrame.dispose();
                Point location = toDoFrame.getLocation();
                ToDoPage newToDo = new ToDoPage();
                newToDo.toDoFrame.setLocation(location);
                newToDo.openSettingsWindow();
            });

            bottomBar = new JPanel();
            bottomBar.setBackground(new Color(20,20,20));
            bottomBar.setLayout(new BorderLayout());
            bottomBar.add(newListSaveBtn, BorderLayout.CENTER);
            bottomBar.setPreferredSize(new Dimension(0,60));
            bottomBar.setBorder(createEmptyBorder(0,100,10,100));

            newListFrame.add(bottomBar, BorderLayout.SOUTH);

            newListFrame.add(topBar, BorderLayout.NORTH);
            newListFrame.add(newListPanel);
            newListFrame.pack();

            //update location of Add Task window to match the main window on open
            newListFrame.setLocation((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(newListFrame.getWidth()/2)), (toDoFrame.getY()+(toDoFrame.getHeight()/2)-(newListFrame.getHeight()/2)));

            newListFrame.setVisible(true);
        }

        if (e.getSource() == listNameBtn){
            JFrame listNameFrame = new JFrame();
            JPanel listNamePanel = new JPanel();

            FrameDragListener frameDragListener = new FrameDragListener(listNameFrame, false);
            listNameFrame.addMouseListener(frameDragListener);
            listNameFrame.addMouseMotionListener(frameDragListener);
            listNameFrame.setLayout(new BorderLayout());
            listNameFrame.setUndecorated(true);

            confirmSubOpacity();
            if(subWindowOpacity) {
                opacity = checkOpacity();
                listNameFrame.setOpacity(opacity);
            }

            listNameFrame.setAlwaysOnTop(true);

            topBar = new JPanel();
            topBar.setBackground(new Color(30, 30, 30));
            topBar.setLayout(new BorderLayout());

            JLabel addLabel = new JLabel("Rename this List?");
            prepLabel(addLabel);
            addLabel.setBorder(null);
            addLabel.setFont(new Font("Arial", Font.BOLD, 20));
            topBar.add(addLabel, BorderLayout.CENTER);

            JPanel blank = new JPanel();
            blank.setBackground(new Color(30, 30, 30));
            blank.setPreferredSize(new Dimension(50, 0));
            topBar.add(blank, BorderLayout.WEST);

            JButton addCloseBtn = new JButton("x");
            addCloseBtn.setBorder(createEmptyBorder(0, 0, 6, 0));
            addCloseBtn.setPreferredSize(new Dimension(50, 30));
            addCloseBtn.setFont(new Font("Arial", Font.PLAIN, 38));
            addCloseBtn.addActionListener(e3 -> {
                listNameFrame.dispose();
            });
            prepBtn(addCloseBtn);

            topBar.add(addCloseBtn, BorderLayout.EAST);

            //New List Name input
            JLabel listNameLabel = new JLabel("New name for the Current List:");
            listNameLabel.setFont(new Font("Arial",Font.BOLD,15));
            prepLabel(listNameLabel);
            JTextField listNameField = new JTextField();
            prepTextField(listNameField);
            listNameField.setEditable(true);
            listNameField.setFocusable(true);


            listNamePanel.setLayout(new GridLayout(3,0));

            listNamePanel.add(listNameLabel);
            listNamePanel.add(listNameField);

            listNamePanel.setBorder(createEmptyBorder(20, 20, 0, 20));
            listNamePanel.setBackground(new Color(20, 20, 20));
            listNamePanel.setPreferredSize(new Dimension(350, 140));

            JButton newListNameBtn = new JButton("Save New List");
            prepBtn(newListNameBtn);
            newListNameBtn.setPreferredSize(new Dimension(40,80));
            newListNameBtn.setBorder(BorderFactory.createLineBorder((new Color(50,50,50)),1));
            newListNameBtn.setBorderPainted(true);

            // add new Task to list
            newListNameBtn.addActionListener(e4 -> {

                //ensure NO Special Chars in List Name
                Pattern pattern = Pattern.compile("[^a-z0-9 ,'()@_~-]", Pattern.CASE_INSENSITIVE);
                Matcher match = pattern.matcher(listNameField.getText());
                boolean foundSpecialChar = match.find();

                //ensure New List Name does not already exist
                ArrayList<String> listOptions = new ArrayList<>();
                File listDir = new File(ListItem.dirPath+"/");
                File[] files = listDir.listFiles();
                assert files != null;

                if(files.length>0){
                    for(int i=0;i<files.length;i++){
                        String fileName = files[i].toString();
                        listOptions.add(fileName.substring(ListItem.dirPath.length()+1));
                    }
                }

                String[] fileNameList = listOptions.toArray(new String[files.length]);
                boolean listNameAlreadyExists = false;

                for(int i=0;i<fileNameList.length;i++){
                    String existingListName = fileNameList[i].substring(0,fileNameList[i].indexOf("."));
                    if(existingListName.equals(listNameField.getText())){
                        listNameAlreadyExists = true;
                    }
                }

                String noSpacesName = listNameField.getText().replace(" ","_");

                //List Name Valid?
                if(noSpacesName.isEmpty() || foundSpecialChar || listNameAlreadyExists){
                    createDialogWindow("<html><b style=\" color:#c8c8c8; font-size:12px;\">Please add a Valid and Unique list name with No Special Characters.</b></html>","  Invalid Task Name  ", false);
                    return;
                }

                //Create New List File with the new name
                File newList = new File(ListItem.dirPath+"/"+noSpacesName+".tdli");
                try {
                    newList.createNewFile();
                } catch (IOException e1){
                    throw new RuntimeException (e1);
                }

                //Copy info from old list to new list
                String oldList = ListItem.getSavedList();
                String listContents;

                try(BufferedReader br = new BufferedReader(new FileReader("./listStorage/"+oldList))){
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while((line=br.readLine()) != null){
                        sb.append(line);
                    }
                    listContents = sb.toString();

                    try(BufferedWriter bw = new BufferedWriter(new FileWriter(newList))){
                        bw.write(listContents);
                    }

                } catch (IOException e1){
                    throw new RuntimeException(e1);
                }

                //Remove the now redundant file with old name
                File oldListFile = new File("./listStorage/"+oldList);

                //Set new list as current list
                ListItem.updateSavedList(noSpacesName+".tdli");
                ListItem.listFileName = ListItem.getSavedList();
                listNameFrame.dispose();
                settingsFrame.dispose();
                toDoFrame.dispose();
                Point location = toDoFrame.getLocation();
                ToDoPage newToDo = new ToDoPage();
                newToDo.toDoFrame.setLocation(location);
                newToDo.openSettingsWindow();
            });

            bottomBar = new JPanel();
            bottomBar.setBackground(new Color(20,20,20));
            bottomBar.setLayout(new BorderLayout());
            bottomBar.add(newListNameBtn, BorderLayout.CENTER);
            bottomBar.setPreferredSize(new Dimension(0,60));
            bottomBar.setBorder(createEmptyBorder(0,100,10,100));

            listNameFrame.add(bottomBar, BorderLayout.SOUTH);

            listNameFrame.add(topBar, BorderLayout.NORTH);
            listNameFrame.add(listNamePanel);
            listNameFrame.pack();

            //update location of Add Task window to match the main window on open
            listNameFrame.setLocation((toDoFrame.getX()+(toDoFrame.getWidth()/2)-(listNameFrame.getWidth()/2)), (toDoFrame.getY()+(toDoFrame.getHeight()/2)-(listNameFrame.getHeight()/2)));

            listNameFrame.setVisible(true);
        }
    }

    private void updateTaskNumsEnabled(boolean enabled){
        String currentSettings = getSettings();
        currentSettings = currentSettings.replaceFirst("Task Numbers Enabled: "+!enabled,"Task Numbers Enabled: "+enabled);

        try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
            bw.write(currentSettings);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private void updateMoveBtnsEnabled(boolean enabled){
        String currentSettings = getSettings();
        currentSettings = currentSettings.replaceFirst("Move Buttons Enabled: "+!enabled,"Move Buttons Enabled: "+enabled);

        try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
            bw.write(currentSettings);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private void updateTaskLabelsEnabled(boolean enabled){
        String currentSettings = getSettings();
        currentSettings = currentSettings.replaceFirst("Task Labels Enabled: "+!enabled,"Task Labels Enabled: "+enabled);

        try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
            bw.write(currentSettings);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public void updateSubOpacity(boolean enabled){
        String currentSettings = getSettings();
        currentSettings = currentSettings.replaceFirst("Window Opacity: "+!enabled,"Window Opacity: "+enabled);
        if(enabled){
            opacity = checkOpacity();
            settingsFrame.setOpacity(opacity);
            subWindowOpacity = true;
        } else {
            settingsFrame.setOpacity(1f);
            subWindowOpacity = false;
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
            bw.write(currentSettings);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public void updateAlwaysOnTop(boolean enabled){
        String currentSettings = getSettings();
        currentSettings = currentSettings.replaceFirst("Always On Top: "+!enabled,"Always On Top: "+enabled);
        toDoFrame.setAlwaysOnTop(enabled);
        settingsFrame.toFront();
        try(BufferedWriter bw = new BufferedWriter(new FileWriter("settings/settings.txt"))){
            bw.write(currentSettings);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public void prepLabel(JLabel label) {
        label.setForeground(new Color(220, 220, 220));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        private final Container frame;
        private Point mouseDownCompCoords = null;
        private int newX;
        private int newY;
        private final boolean isMainFrame;
        private boolean dragged;

        public FrameDragListener(Container frame, boolean mainFrame){this.frame = frame; isMainFrame = mainFrame;}
        public void mouseReleased(MouseEvent e){
            mouseDownCompCoords = null;
            if(isMainFrame && dragged){
                saveWindowStatus(newX, newY, 0, 0,true);
                dragged=false;
            }
        }
        public void mousePressed(MouseEvent e){
                mouseDownCompCoords = e.getPoint();
        }
        public void mouseDragged(MouseEvent e){
            dragged = true;
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

            String settings=getSettings();
            String taskNumSetting = getSpecificSetting(5,(settings.substring(settings.indexOf("❂")+1)));
            taskNumsEnabled = Boolean.parseBoolean(taskNumSetting);

            String moveBtnSetting = getSpecificSetting(3,(settings.substring(settings.indexOf("❂")+1)));
            moveBtnsEnabled = Boolean.parseBoolean(moveBtnSetting);
            int minWidth = 434;
            if(moveBtnsEnabled){
                minWidth = minWidth +69;
            }
            if(taskNumsEnabled){
                minWidth = minWidth +28;
            }

            if(width< minWidth){
                width = minWidth;
            }
            int height = currCoords.y-mouseDownCompCoords.y-frameY+25;
            if(height<262){
                height = 262;
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
                String settingsLocSize = settings.substring(settings.indexOf("|")+1,settings.indexOf("❂")-1);
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
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
