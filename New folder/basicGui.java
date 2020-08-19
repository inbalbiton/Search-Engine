import Index.Indexer;
import ReadFile.ReadFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class basicGui {

    private Boolean stemming = false;
    private String pathToSavePosting ;
    private String pathToCorpus;
    private JCheckBox stemmingCheckBox;
    private JButton startButton;
    private JButton chooseButton;
    private JButton clearButton;
    private JButton showDictionaryButton;
    private JButton loadingDictionaryIntoMemoryButton;
    private JButton chooseButton1;
    private JPanel panelMain;
    private JTextField enterPathToSaveTextField;
    private JTextField enterPathToCorpusTextField;

    private Map<String, String[]> dictionary = new HashMap<>();


    /**
     * All functions in this class are described in the README file
     * This class manages the interface with the user and allows him to perform operations
     * on the dictionary and To see the dictionary we have created
     */
    public basicGui() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if(enterPathToCorpusTextField.getText().equals("") || enterPathToSaveTextField.getText().equals("") || pathToCorpus==null  || pathToSavePosting ==null){
                        JOptionPane.showMessageDialog(null,"enter path to save posting or path tp corpus");
                    }
                    else{
                        long startTime = System.nanoTime();
                        ReadFile readFile = new ReadFile(enterPathToCorpusTextField.getText() ,enterPathToSaveTextField.getText(),stemming);
                        long endTime = System.nanoTime();
                        long totalTime = endTime-startTime;
                        long totalInSecond = totalTime/1000000000;
                        String information = readFile.getInformation();
                        information += "total Time in second - " + totalInSecond +"\n";
                        JOptionPane.showMessageDialog(null,information);
                    }
            }
        });
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pathToSavePosting = showBrowser();
                enterPathToSaveTextField.setText(pathToSavePosting);
            }
        });
        clearButton.addActionListener(new ActionListener() {
            private void deleteContentOfFolder(File directoryToBeDeleted){
                File[] allContents = directoryToBeDeleted.listFiles();
                if (allContents != null) {
                    for (File file : allContents) {
                        file.delete();
                    }
                }
                directoryToBeDeleted.delete();
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                //delete Posting files!
                File directoryToBeDeleted1 = new File (pathToSavePosting+"//DocsPosting"+"true");
                if(directoryToBeDeleted1.exists()){
                    deleteContentOfFolder(directoryToBeDeleted1);
                }

                File directoryToBeDeleted2 = new File (pathToSavePosting+"//TermsPosting"+"true");
                if(directoryToBeDeleted2.exists()){
                    deleteContentOfFolder(directoryToBeDeleted2);
                }
                File directoryToBeDeleted3 = new File (pathToSavePosting+"//EntityPosting"+"true");
                if(directoryToBeDeleted3.exists()){
                    deleteContentOfFolder(directoryToBeDeleted3);
                }

                File dictionaryToBeDeleted = new File(pathToSavePosting+"Dictionary"+"true"+".txt");
                if(dictionaryToBeDeleted.exists())
                    dictionaryToBeDeleted.delete();

                File directoryToBeDeleted4 = new File (pathToSavePosting+"//DocsPosting"+"false");
                if(directoryToBeDeleted4.exists()){
                    deleteContentOfFolder(directoryToBeDeleted4);
                }

                File directoryToBeDeleted5 = new File (pathToSavePosting+"//TermsPosting"+"false");
                if(directoryToBeDeleted5.exists()){
                    deleteContentOfFolder(directoryToBeDeleted5);
                }
                File directoryToBeDeleted6 = new File (pathToSavePosting+"//EntityPosting"+"false");
                if(directoryToBeDeleted6.exists()){
                    deleteContentOfFolder(directoryToBeDeleted6);
                }

                File dictionaryToBeDeleted2 = new File(pathToSavePosting+"Dictionaryfalse.txt");
                if(dictionaryToBeDeleted2.exists())
                    dictionaryToBeDeleted2.delete();
                dictionary.clear();
                JOptionPane.showMessageDialog(null,"all posting files and dictionary are deleted");
            }
        });
        loadingDictionaryIntoMemoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(pathToSavePosting!=null ){
                    File f = new File(pathToSavePosting+"\\Dictionary"+stemming +".txt");
                    if(f.exists()) {
                        loadDictionary();
                    }
                    else {
                        JOptionPane.showMessageDialog(null,"Dictionary didn't exist");
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null,"Please enter the path to save posting for loading the dictionary");
                }
            }
        });
        showDictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pathToSavePosting != null) {
                    JFrame frame = new JFrame("Dictionary");
                    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    frame.setPreferredSize(new Dimension(560, 200));
                    if (!enterPathToSaveTextField.getText().equals("")) {
                        loadDictionary();
                        ArrayList<String> sort = new ArrayList<>(dictionary.keySet());
                        Collections.sort(sort);
                        String[][] allTerms = new String[dictionary.size()][2];
                        int i=0;
                        for (String term : sort) {
                            String[] termFreq = {term, dictionary.get(term)[1]};
                            allTerms[i] = termFreq;
                            i++;
                        }
                        String[] columnNames = {"Terms in dictionary", "Total tf"};
                        JTable table = new JTable(allTerms, columnNames);
                        table.setBounds(100, 100, 200, 300);
                        //table.addMouseListener(new Scrollable();_
                        JScrollPane panel = new JScrollPane(table);
                        //  add(panel, BorderLayout.CENTER);
                        frame.add(panel);
                        frame.setVisible(true);

                    }else {
                        JOptionPane.showMessageDialog(null, "showDictionary button clicked but the path is empty");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "please insert a path to the dictionary in path to save posting files");
                }
            }
        });
        chooseButton1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        pathToCorpus = showBrowser();
                        enterPathToCorpusTextField.setText(pathToCorpus);
                    }
                });
        stemmingCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (stemmingCheckBox.isSelected()) {
                            //   JOptionPane.showMessageDialog(null, "stemming is selected");
                            stemmingCheckBox.setMnemonic(KeyEvent.VK_A);
                            stemmingCheckBox.setSelected(true);
                            stemming = true;
                        } else {
                            //    JOptionPane.showMessageDialog(null,"stemming is unselected");
                            stemmingCheckBox.setMnemonic(KeyEvent.VK_A);
                            stemmingCheckBox.setSelected(false);
                            stemming = false;
                        }
                    }
                });
        enterPathToCorpusTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        pathToCorpus = enterPathToCorpusTextField.getText();
                    }
                });
        enterPathToSaveTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                boolean ok = true;
//                while (ok)
//
                        pathToSavePosting = enterPathToSaveTextField.getText();
                    }
                });
    }

    public String showBrowser(){
        JFrame frame = new JFrame("choose path");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(560, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        JPanel panel = new JPanel();
        LayoutManager layout = new FlowLayout();
        panel.setLayout(layout);
        JButton button = new JButton("Click Me!");
        final JLabel label = new JLabel();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog( frame);

        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            label.setText("Folder Selected: " + file.getName());
            return file.getPath();
        }else{
            label.setText("Open command canceled");
        }
        panel.add(button);
        panel.add(label);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        return "file not choose";
    }
    public void loadDictionary() {
        File dictionary = new File(pathToSavePosting+"\\Dictionary"+this.stemming+".txt");
        try {
            FileReader fr = new FileReader((dictionary));
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while((line = br.readLine())!= null){
                String[] splitLine = line.split("--");
                String[] freqAndPath = splitLine[1].split(",");
                String[] newValue = new String[]{};
                try{
                     newValue = new String[]{freqAndPath[0]+","+freqAndPath[1] , freqAndPath[2]};

                }catch(Exception ex){
                    continue;
                }
                this.dictionary.put(splitLine[0],newValue);
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("RI_Project");
        frame.setContentPane(new basicGui().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

}
