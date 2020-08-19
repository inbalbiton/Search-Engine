import Index.Doc;
import ReadFile.*;
import Searcher.Searcher;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


public class basicGui {
    public static JFrame frame = new JFrame("RI_Project");

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
    //dictionary
    private HashMap<String, String[]> dictionary = new HashMap<>();

    /*
    PART_B
     */
    private Boolean clickSreamData = false;
    private Boolean semantic = false;
    private String pathToQueries ;
    private String query ;
    private String pathToSaveResult;

    private JTextField enterQueryTextField;
    private JButton searchSingleQueryButton;
    private JButton browseButton;
    private JCheckBox semanticCheckBox;
    private JTextField enterPathOfQueriesTextField;
    private JButton searchQueriesButton;


    // pair of query num and docs
    private  ArrayList<Pair<String,ArrayList<String>>> results = new ArrayList<>();
    // docNum and 5 important entity
    private HashMap<String, String[]> EntityResult = new HashMap<>();
    //private Searcher searcher;

    /**
     * All functions in this class are described in the README file
     * This class manages the interface with the user and allows him to perform operations
     * on the dictionary and To see the dictionary we have created
     */
    public basicGui() {
        /////////////////////////////////////////////////////////////////////////////////////////
        /*                                    PART_A                                           */
        /////////////////////////////////////////////////////////////////////////////////////////
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if(enterPathToCorpusTextField.getText().equals("") || enterPathToSaveTextField.getText().equals("") || pathToCorpus==null  || pathToSavePosting ==null){
                        JOptionPane.showMessageDialog(null,"enter path to save posting or path to corpus");
                    }
                    else{
                        long startTime = System.nanoTime();
                        ReadFile readFile = new ReadFile(false,enterPathToCorpusTextField.getText() ,enterPathToSaveTextField.getText(),stemming);
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
                boolean existFile1 = false;
                boolean existFile2 = false;
                boolean existFile3 = false;
                boolean existdic = false;
                if(pathToSavePosting!=null && !pathToSavePosting.equals("") ){
                    File directoryToBeDeleted1 = new File (pathToSavePosting+"//DocsPosting"+"true");
                    if(directoryToBeDeleted1.exists()){
                        existFile1=true;
                        deleteContentOfFolder(directoryToBeDeleted1);
                    }

                    File directoryToBeDeleted2 = new File (pathToSavePosting+"//TermsPosting"+"true");
                    if(directoryToBeDeleted2.exists()){
                        existFile2=true;
                        deleteContentOfFolder(directoryToBeDeleted2);
                    }
                    File directoryToBeDeleted3 = new File (pathToSavePosting+"//EntityPosting"+"true");
                    if(directoryToBeDeleted3.exists()){
                        existFile3=true;
                        deleteContentOfFolder(directoryToBeDeleted3);
                    }

                    File dictionaryToBeDeleted = new File(pathToSavePosting+"\\Dictionarytrue.txt");
                    if(dictionaryToBeDeleted.exists()) {
                        existdic = true;
                        dictionaryToBeDeleted.delete();
                    }
                    File directoryToBeDeleted4 = new File (pathToSavePosting+"//DocsPosting"+"false");
                    if(directoryToBeDeleted4.exists()){
                        existFile1=true;
                        deleteContentOfFolder(directoryToBeDeleted4);
                    }

                    File directoryToBeDeleted5 = new File (pathToSavePosting+"//TermsPosting"+"false");
                    if(directoryToBeDeleted5.exists()){
                        existFile2=true;
                        deleteContentOfFolder(directoryToBeDeleted5);
                    }
                    File directoryToBeDeleted6 = new File (pathToSavePosting+"//EntityPosting"+"false");
                    if(directoryToBeDeleted6.exists()){
                        existFile3=true;
                        deleteContentOfFolder(directoryToBeDeleted6);
                    }

                    File dictionaryToBeDeleted2 = new File(pathToSavePosting+"\\Dictionaryfalse.txt");
                    if(dictionaryToBeDeleted2.exists()) {
                        existdic = true;
                        dictionaryToBeDeleted2.delete();
                    }
                    dictionary.clear();
                    if(existdic||existFile1||existFile2||existFile3){
                        JOptionPane.showMessageDialog(null,"all posting files and dictionary are deleted");
                    }
                    else {
                        JOptionPane.showMessageDialog(null,"There are no files to delete");
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null,"please enter path to posting file first");
                }
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
                        JScrollPane panel = new JScrollPane(table);
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
                        pathToSavePosting = enterPathToSaveTextField.getText();
                    }
                });




        /////////////////////////////////////////////////////////////////////////////////////////
        /*                                    PART_B                                           */
        /////////////////////////////////////////////////////////////////////////////////////////
        semanticCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (semanticCheckBox.isSelected()) {
                    semanticCheckBox.setMnemonic(KeyEvent.VK_A);
                    semanticCheckBox.setSelected(true);
                    semantic = true;
                    //System.out.println(semantic);
                } else {
                    semanticCheckBox.setMnemonic(KeyEvent.VK_A);
                    semanticCheckBox.setSelected(false);
                    semantic = false;
                    //System.out.println(semantic);
                }

            }
        });
        enterQueryTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                query = enterQueryTextField.getText();
            }
        });
        enterPathOfQueriesTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pathToQueries = enterPathOfQueriesTextField.getText();
            }
        });
        /**
         *  this function is for click on the run single query
         **/
        searchSingleQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                results.clear();
                EntityResult.clear();
                File resultFile = new File(pathToSaveResult+"\\result.txt");
                if(resultFile.exists()){
                    resultFile.delete();
                }
                query = enterQueryTextField.getText();
                // Searcher(boolean semantic , boolean clickSreamData, boolean steming , HashMap<String , String[]> dictionary ,String pathToPostingFiles){
                if( pathToSavePosting == null || dictionary.size() == 0 || pathToCorpus.equals("") || pathToSavePosting.equals("") || query.equals("")){
                    JOptionPane.showMessageDialog(null, "please make sure that you filled the fields - pathToCorpus , pathToSavePosting , query \n" +
                            "and that you load the dictionary ");
                }
                else{
                    Searcher searcher = new Searcher(semantic,clickSreamData,stemming,dictionary,pathToCorpus,pathToSavePosting);
                    searcher.insertQuery(query);
                    ArrayList<String> docresult = searcher.getDocsResultsFromQuery();
                    results.add(new Pair<>("1",docresult));
                    saveEntity(searcher.getEnties());
                    showReasult();
                }
            }
        });
        /**
         * this function is for click on the Browse button
         */
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pathToQueries = showBrowserOnlyTxtFiles();
                enterPathOfQueriesTextField.setText(pathToQueries);
                if(pathToQueries.equals("file not choose")){
                    pathToQueries = "";
                }
              //  System.out.println(pathToQueries);
            }
        });
        /**
         *  this function is for click on the search queries!!
         */
        searchQueriesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Searcher searcher = new Searcher(semantic,clickSreamData,stemming,dictionary,pathToCorpus,pathToSavePosting);
                if(dictionary==null || (dictionary!=null && dictionary.size()==0)){
                    JOptionPane.showMessageDialog(null, "please load dictionary into memory first ");
                }
                if(pathToQueries!=null && !pathToQueries.equals("")) {
                    File queriesTextFile = new File(pathToQueries);
                    if (queriesTextFile != null) {
                        ArrayList<Pair<String, String>> queries = readQueriesFromPath(queriesTextFile);
                        // Comparator to sort the pair according to second element
                        queries.sort(new Comparator<Pair>() {
                            @Override public int compare(Pair p1, Pair p2)
                            {
                                return ((String)p1.getKey()).compareTo((String)p2.getKey());
                            }
                        });
                        results.clear();
                        EntityResult.clear();
                        File resultFile = new File(pathToSaveResult+"\\result.txt");
                        if(resultFile.exists()){
                            resultFile.delete();
                        }
                        for (int i=0 ; i < queries.size() ; i++){
                            String queryNum = queries.get(i).getKey();
                            System.out.println(queryNum);
                            searcher.insertQuery(queries.get(i).getValue());
                            /**
                             * save the result into textFile and into our database
                             */
                            results.add(new Pair<>(queryNum,searcher.getDocsResultsFromQuery()));
                            saveEntity(searcher.getEnties());
                        }
                        showReasult();
                    } else {
                        JOptionPane.showMessageDialog(null, "to queries text file in this path, please choose currect path");
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "choose path of queries txt file");
                }
            }
        });
    }
    /**
     * this function save to the data base the entities in one doc!
     * entityResult- hashmap of docNum and 5 important  entity
     */
    private void saveEntity(HashMap<String,String[]> entityResult) {
        if(entityResult!=null){
            if(EntityResult==null){
                EntityResult = new HashMap<>(entityResult);
            }
            else{
                for(String docNum : entityResult.keySet()){
                    String[] entities = entityResult.get(docNum);
                    EntityResult.put(docNum,entities);
                }
            }
        }
    }
    /**
     * this function write the result to txt file after showing the results
     */
   // private void writeResultTxtFile(String currentqueryNum , List<Map.Entry<Doc,Double>> result){
    private void writeResultTxtFile(ArrayList<Pair<String,ArrayList<String>>> resultsToWrite ){
        if(resultsToWrite != null &&  pathToSaveResult != null && !pathToSaveResult.equals("")){
            String fileResultPath = pathToSavePosting+"\\result.txt";
            File fileResult = null;
            fileResult =  new File(fileResultPath);
            if(fileResult.exists()){
                fileResult.delete();
            }
            fileResult =  new File(fileResultPath);
            FileWriter fileWriterResult = null;
            BufferedWriter bufferedWriterResult = null;
            try {
                fileWriterResult = new FileWriter(fileResult, true);
                bufferedWriterResult = new BufferedWriter(fileWriterResult);
                for(int i=0 ; i < resultsToWrite.size() ; i++){
                    ArrayList<String> docsResults = resultsToWrite.get(i).getValue();
                    for(int j = 0 ; j < docsResults.size() ; j++){
                        StringBuilder line = new StringBuilder(resultsToWrite.get(i).getKey()) ;
                        line.append(" ");
                        line.append(0);
                        String docNum = docsResults.get(j);
                        line.append(" " + docNum);
                        line.append(" " + 1 );
                        line.append(" " + 42.38);
                        line.append(" mt");
                        line.append("\n");
                        bufferedWriterResult.append(line);
                    }
                }
                bufferedWriterResult.close();
                fileWriterResult.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    /**
     * this function read the queries from path
     * @param textfileQueries
     * @return
     */
    private ArrayList<Pair<String,String>> readQueriesFromPath(File textfileQueries) {
        ArrayList<Pair<String,String>> queries = new ArrayList<>();
        Document xmlFile = null;
        try {
            xmlFile = Jsoup.parse(textfileQueries,"UTF-8");
            Elements ListOfQueries =  xmlFile.getElementsByTag("top");
            for(int i = 0 ; i < ListOfQueries.size() ; i++){

                Element query = ListOfQueries.get(i);
                Elements title = query.getElementsByTag("title");
                Elements text = query.getElementsByTag("desc");
                Elements queryNum = query.getElementsByTag("num");

                String[] queryNerrativ = text.text().substring(12).split("Narrative:");

                String[] queryText = queryNum.text().substring(8).split(" ");

                StringBuilder titleQuery = new StringBuilder(title.text());
                StringBuilder descQuery = new StringBuilder("");
               // System.out.println("query Num = " +queryText[0]);
                String[] qN = queryNerrativ[0].split(" ");
                for(String s : qN)
                    descQuery.append(" " + s + " ");
               // System.out.println("query Text = "+newQuery.toString());
                queries.add(new Pair<>(queryText[0],titleQuery.toString()+"#"+descQuery.toString()));
             //   queries.add(new Pair<>(queryText[0],titleQuery.toString() + " " + descQuery.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }
    /**
     * this function show browser for part-B only txt file for queries
     * @return
     */
    private String showBrowserOnlyTxtFiles(){
        JLabel l = new JLabel("no file selected");

        JPanel panel = new JPanel();
        LayoutManager layout = new FlowLayout();
        panel.setLayout(layout);
        // create an object of JFileChooser class
        JFileChooser j = new JFileChooser("D:\\documents\\users\\noashab");
        // resctrict the user to select files of all types
        j.setAcceptAllFileFilterUsed(false);
        // set a title for the dialog
        j.setDialogTitle("Select a .txt file");
        // only allow files of .txt extension
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
        j.addChoosableFileFilter(restrict);

        int r = j.showOpenDialog( frame );
        // if the user selects a file
        if (r == JFileChooser.APPROVE_OPTION)
        {
            // set the label to the path of the selected file
            l.setText(j.getSelectedFile().getAbsolutePath());
            File file = j.getSelectedFile();
            return file.getPath();
        }
        // if the user cancelled the operation
        else
            l.setText("the user cancelled the operation");
        return "file not choose";
    }
    /**
     * this function show browser for part-A only directories for posting and corpus
     * @return
     */
    private String showBrowser(){
        JPanel panel = new JPanel();
        LayoutManager layout = new FlowLayout();
        panel.setLayout(layout);
        final JLabel label = new JLabel();
        JFileChooser fileChooser = new JFileChooser("D:\\documents\\users\\noashab");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog( frame);

        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            return file.getPath();
        }
        //frame.getContentPane().add(panel, BorderLayout.CENTER);
        return "file not choose";
    }
    /**
     * this function load the dictionary in the path for saving posting file
     */
    private void loadDictionary() {
        File dictionary = new File(pathToSavePosting+"\\Dictionary"+this.stemming+".txt");
        try {
            this.dictionary.clear();
            FileReader fr = new FileReader((dictionary));
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while((line = br.readLine())!= null){
                String[] splitLine = line.split("--");
                String[] freqAndPath = splitLine[1].split(",");
                String oldPath = freqAndPath[0];
                int last;
                if(stemming){
                    last = oldPath.lastIndexOf("\\TermsPostingtrue");
                    if(last == -1){
                        last = oldPath.lastIndexOf("\\EntityPostingtrue");
                    }
                }
                else {
                    last = oldPath.lastIndexOf("\\TermsPostingfalse");
                    if(last == -1){
                        last = oldPath.lastIndexOf("\\EntityPostingfalse");
                    }
                }
                if(last == -1){
                    continue;
                }
                String[] newValue = new String[]{};
                try{
                     newValue = new String[]{pathToSavePosting+oldPath.substring(last)+","+freqAndPath[1] , freqAndPath[2]};

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
    /**
     * this function show result in a new frame
     */
    private void showReasult(){
        JFrame frame = new JFrame("Result");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 600));

        /*
        the results are query_number - List<docs_number>
         */
        int size = results.size() ;
        String[][] allResult = new String[50+1][size];
        String[] columnNames = new String[size];
        int i=0;
        for (Pair query_Docslist : results) {
            ArrayList<String> docsList = (ArrayList)query_Docslist.getValue();
            allResult[0][i] = (String)query_Docslist.getKey();
            columnNames[i] = "Query"+(i+1);
            for(int j=1 ; j < docsList.size()+1 ; j++){
                allResult[j][i] = docsList.get(j-1);
            }
            i++;
        }
        JTable table = new JTable(allResult,columnNames);
        table.setBounds(50 , 50, 50, 50);
        JScrollPane jtable = new JScrollPane(table);
        /*
        btn pannel
         */
        JPanel topPanel = new JPanel();
        JPanel btnPanel = new JPanel();
        JButton EntitiesBotton = new JButton("Get 5 Entities");
        JButton SaveBotton = new JButton("Save Results");
        JButton BrowseBotton = new JButton("choose path to save results");
        JTextField docNumber = new JTextField("enter doc number");
        btnPanel.add(EntitiesBotton);
        btnPanel.add(SaveBotton);
        btnPanel.add(BrowseBotton);
        btnPanel.add(docNumber);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(jtable);


        frame.getContentPane().add(topPanel , BorderLayout.CENTER);
        frame.getContentPane().add(btnPanel , BorderLayout.SOUTH);

        //frame.add(panel);
        frame.setVisible(true);
        JOptionPane.showMessageDialog(null, "finish to search !");

        ////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////listeners to this frame////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////

        EntitiesBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(docNumber.getText().equals("") || docNumber.getText().equals("enter doc number")){
                    JOptionPane.showMessageDialog(null, "enter doc num first !");
                }
                else{
                    docNumber.getText();
                    showEntities(docNumber.getText());
                }
            }

            private void showEntities(String docNumberChoosed) {
                StringBuilder entyResult = new StringBuilder("");
                //change after new function
                String[] result;
                if(EntityResult.containsKey(docNumberChoosed)){
                    result = EntityResult.get(docNumberChoosed);
                    if(result==null || result.length == 0){
                        JOptionPane.showMessageDialog(null, "there is no entities to this doc number ");
                    }
                    else{
                        for (int i=0 ; i< result.length-1 ; i++){
                            if(result[i] != null){
                                entyResult.append(result[i]);
                                entyResult.append(" , ");
                            }
                        }
                        if(result[result.length-1] !=null){
                            entyResult.append(result[result.length-1]);
                        }
                        JOptionPane.showMessageDialog(null, "most important Entities for doc number "+ docNumberChoosed + " : "+"\n"+ entyResult.toString() );
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "you entered wrong doc number ");
                }
            }
        });
        SaveBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(pathToSaveResult!=null && !pathToSaveResult.equals("") ){
                    writeResultTxtFile(results);
                }
                else{
                    JOptionPane.showMessageDialog(null, "choose path to save first" );
                }
            }
        });
        BrowseBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pathToSaveResult = showBrowser();
            }
        });

    }
    public static void main(String[] args) {
        frame.setContentPane(new basicGui().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
