package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class of the GUI of framework.
 */
public class WeatherIndexFrameworkGui implements ChangeListener {
    private static final String FRAME_TITLE = "Weather Index";
    private static final String DATA_MENU_TITLE = "Data Source";
    private static final String DISPLAY_MENU_TITLE = "Display Type";
    private static final String WARNING_TITLE = "Warning";

    private final JFrame frame;
    private final JPanel outerPanel;
    private final JPanel dataPanel;
    private final JPanel dataInputPanel;
    private final JPanel displayPanel;
    private final JPanel displayInputPanel;
    private final JPanel graphPanel;
    private final JMenu dataMenu;
    private final JMenu displayMenu;
    private final JLabel currentDataPluginLabel;
    private final JLabel currentDisplayPluginLabel;
    private final JButton loadButton;
    private final JButton generateButton;
    private Map<InputEntry, JTextField> dataInputTextFields;
    private Map<InputEntry, JList> dataInputLists;
    private Map<InputEntry, JTextField> displayInputTextFields;
    private Map<InputEntry, JList> displayInputLists;
    private final ButtonGroup dataPluginGroup;
    private final ButtonGroup displayPluginGroup;
    private final WeatherIndexFramework core;

    /**
     * Constructs a GUI for the framework.
     * @param core the framework reference.
     */
    public WeatherIndexFrameworkGui(WeatherIndexFramework core) {
        this.core = core;
        frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1280, 720));

        outerPanel = new JPanel(new BorderLayout());

        dataPanel = new JPanel();
        dataInputPanel = new JPanel();
        dataInputPanel.setLayout(new BoxLayout(dataInputPanel, BoxLayout.Y_AXIS));
        dataPanel.add(dataInputPanel);


        currentDataPluginLabel = new JLabel();
        currentDataPluginLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dataInputPanel.add(currentDataPluginLabel);

        graphPanel = new JPanel(new GridLayout(1,1));
        displayPanel = new JPanel();
        displayInputPanel = new JPanel();
        displayInputPanel.setLayout(new BoxLayout(displayInputPanel, BoxLayout.Y_AXIS));
        displayPanel.add(displayInputPanel);

        currentDisplayPluginLabel = new JLabel();
        currentDisplayPluginLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 50));
        displayInputPanel.add(currentDisplayPluginLabel);

        outerPanel.add(dataPanel, BorderLayout.WEST);
        outerPanel.add(displayPanel, BorderLayout.EAST);
        outerPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        frame.add(outerPanel);

        loadButton = new JButton("Load");
        loadButton.addActionListener(e1 -> {
            for (InputEntry entry : core.getDataInputEntries()) {
                switch (entry.getEntryType()) {
                    case TEXT_INPUT_ENTRY:
                    case PATH_INPUT_ENTRY:
                        entry.setValue(dataInputTextFields.get(entry).getText());
                        break;
                    case SELECT_ONE_INPUT_ENTRY:
                        entry.setValue((String) dataInputLists.get(entry).getSelectedValue());
                        break;
                    case SELECT_MULTIPLE_INPUT_ENTRY:
                        StringBuilder builder = new StringBuilder();
                        for (Object value : dataInputLists.get(entry).getSelectedValuesList()) {
                            builder.append((String) value).append(";");
                        }
                        entry.setValue(builder.toString());
                        break;
                }
            }
            try {
                core.processData();
                if(core.isDisplayPluginSelected()){
                    displayInputTextFields = new HashMap<>();
                    displayInputLists = new HashMap<>();
                    displayInputPanel.removeAll();
                    displayInputPanel.add(currentDisplayPluginLabel);
                    displayInputPanel.revalidate();
                    displayInputPanel.repaint();
                    if(core.isDataPluginLoaded())
                        addDisplayInputEntries();
                }
            } catch (IllegalStateException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
            }
        });

        generateButton = new JButton("Generate");
        generateButton.addActionListener(e -> {
            for (InputEntry entry : core.getDisplayInputEntries()) {
                switch (entry.getEntryType()) {
                    case TEXT_INPUT_ENTRY:
                    case PATH_INPUT_ENTRY:
                        entry.setValue(displayInputTextFields.get(entry).getText());
                        break;
                    case SELECT_ONE_INPUT_ENTRY:
                        entry.setValue((String) displayInputLists.get(entry).getSelectedValue());
                        break;
                    case SELECT_MULTIPLE_INPUT_ENTRY:
                        StringBuilder builder = new StringBuilder();
                        for (Object value : displayInputLists.get(entry).getSelectedValuesList()) {
                            builder.append((String) value).append(";");
                        }
                        entry.setValue(builder.toString());
                        break;
                }
            }
            try {
                graphPanel.removeAll();
                JPanel chartPanel = core.generateGraph();
                graphPanel.add(chartPanel);
                graphPanel.revalidate();
                graphPanel.repaint();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
            }
        });

        JMenuBar menuBar = new JMenuBar();
        dataMenu = new JMenu(DATA_MENU_TITLE);
        displayMenu = new JMenu(DISPLAY_MENU_TITLE);

        dataMenu.setMnemonic(KeyEvent.VK_F);
        displayMenu.setMnemonic(KeyEvent.VK_F);

        menuBar.add(dataMenu);
        menuBar.add(displayMenu);

        dataPluginGroup = new ButtonGroup();
        displayPluginGroup = new ButtonGroup();

        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onDataPluginRegistered(DataPlugin plugin) {
        JMenuItem dataMenuItem = new JMenuItem(plugin.getName());
        dataMenuItem.setSelected(false);
        dataMenuItem.addActionListener(e -> {
            core.setCurrentDataPlugin(plugin);
            dataInputTextFields = new HashMap<>();
            dataInputLists = new HashMap<>();
            dataInputPanel.removeAll();
            currentDataPluginLabel.setText(DATA_MENU_TITLE + " : " + plugin.getName());
            dataInputPanel.add(currentDataPluginLabel);
            for (InputEntry entry : core.getDataInputEntries()) {
                Icon icon = UIManager.getIcon("OptionPane.informationIcon");
                JLabel helpLabel = new JLabel(icon);

                helpLabel.setToolTipText(entry.getHelpMessage());
                JPanel entryPanel = new JPanel();
                JLabel entryLabel = new JLabel(entry.getLabel());
                entryLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                entryPanel.add(entryLabel);
                entryPanel.add(helpLabel);
                JList entryList;
                String[] entryStr;
                List<String> entryListFromSelect;
                JScrollPane listScroller;
                switch (entry.getEntryType()) {
                    case TEXT_INPUT_ENTRY:
                        JTextField entryTextField = new JTextField(10);
                        entryTextField.setPreferredSize(new Dimension(30, 30));
                        entryLabel.setLabelFor(entryTextField);
                        entryPanel.add(entryTextField);
                        entryTextField.setMaximumSize(entryLabel.getMaximumSize());
                        dataInputTextFields.put(entry, entryTextField);
                        break;
                    case SELECT_ONE_INPUT_ENTRY:
                        entryListFromSelect = entry.getScope();
                        entryStr = new String[entryListFromSelect.size()];
                        entryStr = entryListFromSelect.toArray(entryStr);
                        entryList = new JList(entryStr);
                        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        entryList.setLayoutOrientation(JList.VERTICAL);
                        entryList.setVisibleRowCount(10);
                        listScroller = new JScrollPane(entryList);
                        entryPanel.add(listScroller, BorderLayout.CENTER);
                        dataInputLists.put(entry, entryList);
                        break;
                    case SELECT_MULTIPLE_INPUT_ENTRY:
                        entryListFromSelect = entry.getScope();
                        entryStr = new String[entryListFromSelect.size()];
                        entryStr = entryListFromSelect.toArray(entryStr);
                        entryList = new JList(entryStr);
                        entryList.setSelectionModel(new DefaultListSelectionModel() {
                            @Override
                            public void setSelectionInterval(int index0, int index1) {
                                if(super.isSelectedIndex(index0)) {
                                    super.removeSelectionInterval(index0, index1);
                                }
                                else {
                                    super.addSelectionInterval(index0, index1);
                                }
                            }
                        });
                        entryList.setLayoutOrientation(JList.VERTICAL);
                        entryList.setVisibleRowCount(10);
                        listScroller = new JScrollPane(entryList);
                        entryPanel.add(listScroller, BorderLayout.CENTER);
                        dataInputLists.put(entry, entryList);
                        break;
                    case PATH_INPUT_ENTRY:
                        JTextField filePathField = new JTextField(20);
                        filePathField.setPreferredSize(new Dimension(30,30));
                        entryLabel.setLabelFor(filePathField);
                        entryPanel.add(filePathField);
                        filePathField.setMaximumSize(entryLabel.getMaximumSize());
                        dataInputTextFields.put(entry, filePathField);
                        JButton openFileButton = new JButton(UIManager.getIcon("FileView.directoryIcon"));
                        openFileButton.addActionListener(e1 -> {
                            JFileChooser chooser = new JFileChooser();
                            int returnVal = chooser.showOpenDialog(null);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
                            }
                        });
                        entryPanel.add(openFileButton);
                        break;
                }
                dataInputPanel.add(entryPanel);
            }
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(loadButton);
            dataInputPanel.add(buttonPanel);
            dataInputPanel.revalidate();
            dataInputPanel.repaint();
        });
        dataPluginGroup.add(dataMenuItem);
        dataMenu.add(dataMenuItem);
    }

    @Override
    public void onDisplayPluginRegistered(DisplayPlugin plugin) {
        JMenuItem displayMenuItem = new JMenuItem(plugin.getName());
        displayMenuItem.setSelected(false);
        displayMenuItem.addActionListener(e -> {
            core.setCurrentDisplayPlugin(plugin);
            displayInputTextFields = new HashMap<>();
            displayInputLists = new HashMap<>();
            displayInputPanel.removeAll();
            currentDisplayPluginLabel.setText(DISPLAY_MENU_TITLE + " : " + plugin.getName());
            displayInputPanel.add(currentDisplayPluginLabel);
            displayInputPanel.revalidate();
            displayInputPanel.repaint();
            if(core.isDataPluginLoaded())
                addDisplayInputEntries();
        });
        displayMenu.add(displayMenuItem);
    }

    private void addDisplayInputEntries() {
        for (InputEntry entry : core.getDisplayInputEntries()) {
            Icon icon = UIManager.getIcon("OptionPane.informationIcon");
            JLabel helpLabel = new JLabel(icon, JLabel.CENTER);
            helpLabel.setToolTipText(entry.getHelpMessage());
            JPanel entryPanel = new JPanel();
            JLabel entryLabel = new JLabel(entry.getLabel());
            entryLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            entryPanel.add(entryLabel);
            entryPanel.add(helpLabel);

            JList entryList;
            String[] entryStr;
            List<String> entryListFromSelect;
            JScrollPane listScroller;
            switch (entry.getEntryType()) {
                case TEXT_INPUT_ENTRY:
                    JTextField entryTextField = new JTextField(10);
                    entryLabel.setLabelFor(entryTextField);
                    entryPanel.add(entryTextField);
                    entryTextField.setMinimumSize(new Dimension(100, 100));
                    displayInputTextFields.put(entry, entryTextField);
                    break;
                case SELECT_ONE_INPUT_ENTRY:
                    entryListFromSelect = entry.getScope();
                    entryStr = new String[entryListFromSelect.size()];
                    entryStr = entryListFromSelect.toArray(entryStr);
                    entryList = new JList(entryStr);
                    entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    entryList.setLayoutOrientation(JList.VERTICAL);
                    entryList.setVisibleRowCount(10);
                    listScroller = new JScrollPane(entryList);
                    entryPanel.add(listScroller);
                    displayInputLists.put(entry, entryList);
                    break;
                case SELECT_MULTIPLE_INPUT_ENTRY:
                    entryListFromSelect = entry.getScope();
                    entryStr = new String[entryListFromSelect.size()];
                    entryStr = entryListFromSelect.toArray(entryStr);
                    entryList = new JList(entryStr);
                    entryList.setSelectionModel(new DefaultListSelectionModel() {
                        @Override
                        public void setSelectionInterval(int index0, int index1) {
                            if(super.isSelectedIndex(index0)) {
                                super.removeSelectionInterval(index0, index1);
                            }
                            else {
                                super.addSelectionInterval(index0, index1);
                            }
                        }
                    });
                    entryList.setLayoutOrientation(JList.VERTICAL);
                    entryList.setVisibleRowCount(10);
                    listScroller = new JScrollPane(entryList);
                    entryPanel.add(listScroller);
                    displayInputLists.put(entry, entryList);
                    break;
            }
            displayInputPanel.add(entryPanel);
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateButton);
        displayInputPanel.add(buttonPanel);
        displayInputPanel.revalidate();
        displayInputPanel.repaint();
    }
}
