package Index;

import java.io.*;
import java.util.*;

public class Indexer {
    private String pathToSavePostings;
    private String pathOfTermsPosting;
    private String pathOfDocsPosting;
    private String pathOfEntityPosting;

    private int numberOfPosting;

    private ArrayList<Doc> listOfDocs = new ArrayList<>();
    private Map<String,Term> entities = new HashMap<>();
    private Map<String,Term> Terms  = new HashMap<>(); // data for posting file

    private Boolean stemming ;

    // the pointer is a path to posting file where the term is appear and a line number
    private Map<String, String[]> dictionary = new HashMap<>();
    //The record number of a particular document in the posting file
    private int indexDoc = 1;
    /**
     * These fields save us the relevant information for a single document that is being processed
     */
    private String currentDocNum = "";
    private Map<String , Integer> appearanceOfTermsInCurrentDoc = new HashMap<>() ;
    //New Part B
    private Map<String , Integer> appearanceOfEntiesInCurrentDoc = new HashMap<>() ;

    //New Part B
    private int numberOFDocsTotal =0;
    private int TotalLengthOfDocs = 0;
    private int AvarageLengtOfAllDocs = 0;
    /////////////////////////////////////////////////functions//////////////////////////////////////////////////////////


    /**
     * This constructor get a Path to save The Posting Files,
     * And boolean variable if we want to use stamming or not
     * @param path
     * @param stemming
     */
    public Indexer(String path , Boolean stemming){
        this.stemming = stemming;
        pathToSavePostings = path;
        this.pathOfTermsPosting = pathToSavePostings+"\\TermsPosting" + String.valueOf(this.stemming);
        this.pathOfDocsPosting = pathToSavePostings+"\\DocsPosting" + String.valueOf(this.stemming);
        this.pathOfEntityPosting = pathToSavePostings+"\\EntityPosting"+String.valueOf(this.stemming);

        deletePreviosFiles();
        initCorpus();
    }

    /**
     * Return the number of unique Terms in the corpus
     * @return
     */
    public int getNumOfUniqTerms() {
        return this.dictionary.size();
    }

    /**
     * this method delete the previos files if exist - dictionary and all the posting files
     */
    private void deletePreviosFiles() {

        File directoryToBeDeleted1 = new File (this.pathOfTermsPosting);
        if(directoryToBeDeleted1.exists()){
            deleteContentOfFolder(directoryToBeDeleted1);
        }

        File directoryToBeDeleted2 = new File (this.pathOfDocsPosting);
        if(directoryToBeDeleted2.exists()){
            deleteContentOfFolder(directoryToBeDeleted2);
        }

        File directoryToBeDeleted3 = new File (this.pathOfEntityPosting);
        if(directoryToBeDeleted3.exists()){
            deleteContentOfFolder(directoryToBeDeleted3);
        }

    }
    /**
     * this method delete the Content of Folders
     */
    private void deleteContentOfFolder(File directoryToBeDeleted){
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        directoryToBeDeleted.delete();
    }
    /**
     * This function is private function.
     * The function helps as to intialize folders anf files to be used for Posting Files
     */
    private void initCorpus() {
        File directory1 = new File(this.pathOfTermsPosting);
        if (!directory1.exists()) {
            directory1.mkdir();
        }

        File directory2 = new File(this.pathOfDocsPosting);
        if (!directory2.exists()) {
            directory2.mkdir();
        }

        File directory3 = new File(this.pathOfEntityPosting);
        if (!directory3.exists()) {
            directory3.mkdir();
        }

        try {
            File newTxtFile = new File(this.pathOfDocsPosting + "\\posting.txt");
            newTxtFile.createNewFile();
            this.numberOfPosting = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * This function is the main work of the Indexer.
     * The function get a Term's name from the parser and create a Term Object.
     * The Term Object are insert to the collection od terms and in the next step will be write into posting file.
     * If we already found this term in other documents we update the term that already exist.
     * @param term , the string represent the term.
     * @param docNum ,String that represent the document where we found the term.
     * @param position ,String that represent the position of the term in the document.
     * @param inHeader, boolen variable that tekk us if the term were found in the header or not.
     */
    public void startIndex( String term , String docNum , String position , Boolean inHeader ){

        //check if this is a new Document - we need to save the information befor we start the new document
        if(!this.currentDocNum.equals(docNum)){
            updateDocNum(docNum);
        }

        Term tmpTerm = null;

        /*If the term is a word we need to check if it is:
                                            Entity
                                            Term with capital letter in the beginning
                                            Regular term without capital letter
         */
        if(checkIfWord(term)) {
            if (checkUpperCase(term)) {
                if (term.contains(" ") || term.contains("-")) {
                    /*ENTITY - if this is the first time we found this entity we will create new term.
                                Else We will add the entity to the number of entities that are in 2 or more documents
                     */
                    if (!this.entities.containsKey(term)) {
                        this.entities.put(term, new Term(term));
                    }
                    if (!this.appearanceOfEntiesInCurrentDoc.containsKey(term)) {
                        this.appearanceOfEntiesInCurrentDoc.put(term, 0);
                    }
                    this.appearanceOfEntiesInCurrentDoc.replace(term, this.appearanceOfEntiesInCurrentDoc.get(term) + 1);
                    tmpTerm = this.entities.get(term);
                } else {
                    //לא הופיע בכלל עד כאן
                    if (!this.dictionary.containsKey(term.toUpperCase()) && !this.dictionary.containsKey(term.toLowerCase())) {
                        tmpTerm = checkInTermForUpper(term);
                    }
                    //הופיע באותיות גדולות
                    else if (this.dictionary.containsKey(term.toUpperCase())) {
                        tmpTerm = checkInTermForUpper(term);
                    }
                    //הופיע באותיות קטנות
                    else if (this.dictionary.containsKey(term.toLowerCase())) {
                        tmpTerm = checkInTermForUpper(term);
                    }
                }
            }

            else if (checkLowerCase(term)) {
                //לא הופיע בכלל עד כאן
                if(!this.dictionary.containsKey(term.toUpperCase()) && !this.dictionary.containsKey(term.toLowerCase())){
                    if(Terms.containsKey(term.toUpperCase())){
                        updatFromUpperToLower(term);
                        tmpTerm = this.Terms.get(term.toLowerCase());
                    }
                    else if(this.Terms.containsKey(term.toLowerCase())){
                        tmpTerm = this.Terms.get(term.toLowerCase());
                    }
                    else{
                        tmpTerm = new Term(term.toLowerCase());
                        this.Terms.put(tmpTerm.getName() , tmpTerm);
                    }
                }
                else if (this.dictionary.containsKey(term.toLowerCase())){
                    tmpTerm = checkInTermForLower(term);
                }
                else if (this.dictionary.containsKey(term.toUpperCase())){
                    tmpTerm = checkInTermForLower(term);
                }
            }
        }
        else{
            if(Terms.containsKey(term)){
                tmpTerm = Terms.get(term);
            }
            else{
                tmpTerm = new Term(term);
                Terms.put(term , tmpTerm);
            }
        }
        tmpTerm.updateDocCounter(this.currentDocNum , position , false , String.valueOf(this.indexDoc) );

        addToCurrentDocCollect(tmpTerm.getName());
    }

    //New Part B Only For Us!!!!
    private Term checkInTermForUpper(String term){
        Term tmpTerm;
        if(this.Terms.containsKey(term.toUpperCase())){
            tmpTerm = this.Terms.get(term.toUpperCase());
        }
        else if(this.Terms.containsKey(term.toLowerCase())){
            tmpTerm = this.Terms.get(term.toLowerCase());
        }
        else{
            tmpTerm = new Term(term.toUpperCase());
            Terms.put(tmpTerm.getName(), tmpTerm);
        }
        return tmpTerm;
    }
    private Term checkInTermForLower(String term){
        Term tmpTerm;
        if(this.Terms.containsKey(term.toLowerCase())){
            tmpTerm = this.Terms.get(term.toLowerCase());
        }
        else if(this.Terms.containsKey(term.toUpperCase())){
            updatFromUpperToLower(term);
            tmpTerm = this.Terms.get(term.toLowerCase());
        }
        else{
            tmpTerm = new Term(term.toLowerCase());
            this.Terms.put(tmpTerm.getName() , tmpTerm);
        }
        return tmpTerm;
    }
    private void updatFromUpperToLower(String term){
        Term tmpTerm;
        int counter = 0;
        if(appearanceOfTermsInCurrentDoc.containsKey(term.toUpperCase())){ //
            counter = appearanceOfTermsInCurrentDoc.remove(term.toUpperCase());
        }
        tmpTerm = Terms.remove(term.toUpperCase());
        tmpTerm.changeNameToLowerCase();
        this.appearanceOfTermsInCurrentDoc.put(tmpTerm.getName() , counter);
        this.Terms.put(tmpTerm.getName() , tmpTerm);
    }
    ///////////////////////////////////////////////////////

    /**
     * These functions help us decide what kind of Type we got
     * @param word
     * @return אTrue if it capital letter , if it lower letter , if it a word
     */
    public boolean checkLowerCase(String word) {
        if(word.length()>0){
            return (word.charAt(0) >= 'a' && word.charAt(0) <= 'z');
        }
        return false;
    }
    public boolean checkIfWord(String word){
        return checkLowerCase(word) ||
                checkUpperCase(word);
    }
    public boolean checkUpperCase(String word) {
        if(word.length() > 0) {
            return (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z');
        }
        return false;
    }

    /**
     * This method receives a word and adds it to the repository of the current document
     * @param term
     */
    private void addToCurrentDocCollect(String term){
        if(!this.appearanceOfTermsInCurrentDoc.containsKey(term)){
            this.appearanceOfTermsInCurrentDoc.put(term , 0);
        }
        int counter = this.appearanceOfTermsInCurrentDoc.get(term);
        this.appearanceOfTermsInCurrentDoc.replace(term , counter , ++counter);
    }

    /**
     * This function update the Document Id number that the Indexer are working on.
     * If this is the first Document that we get we will continue,
     * Else we need to update all the information we collect on the current document befor we move to the next Document
     * The data we save on the Document available in listOfDocs
     * @param docNum
     */
    private void updateDocNum(String docNum){
        if(currentDocNum == ""){
            this.numberOFDocsTotal++;
        }
        else{
            if(docNum.equals("LA123090-0195")){
                System.out.println(this.indexDoc + " : " + this.numberOFDocsTotal + " : " );
            }
            this.numberOFDocsTotal++;
            String maxTerm = Collections.max(appearanceOfTermsInCurrentDoc.entrySet(), Map.Entry.comparingByValue()).getKey();
            int sum = 0;
            for(String term : this.appearanceOfTermsInCurrentDoc.keySet()){
                sum += this.appearanceOfTermsInCurrentDoc.get(term);
            }
            for(String term : this.appearanceOfEntiesInCurrentDoc.keySet()){
                sum += this.appearanceOfEntiesInCurrentDoc.get(term);
            }
            int maxFreqTerm = appearanceOfTermsInCurrentDoc.get(maxTerm);
            int numOfUniqTerm = appearanceOfTermsInCurrentDoc.size();
            Doc newDoc = new Doc(currentDocNum , maxFreqTerm , numOfUniqTerm, sum , maxTerm);
            this.TotalLengthOfDocs += sum;
            this.appearanceOfTermsInCurrentDoc.clear();
            newDoc.addEntitiesToDoc(this.appearanceOfEntiesInCurrentDoc);
            this.appearanceOfEntiesInCurrentDoc.clear();
            listOfDocs.add(newDoc);
            indexDoc++;
        }
        this.currentDocNum = docNum;
    }

    /**
     * This method writes us to the posting file the documents we have finished performing an index process
     */
    private  void createPostingFileDocs(){
        String path = this.pathOfDocsPosting + "\\posting.txt";
        FileWriter docsPosting = null;
        try {
            docsPosting = new FileWriter(path, true);
            BufferedWriter writer = new BufferedWriter(docsPosting);
            for (Doc doc: this.listOfDocs) {
                if(doc.getFileId().equals("LA123090-0195")){
                    System.out.println(this.indexDoc + " : " +
                                        this.numberOFDocsTotal + " : " );
                }
                writer.append(doc.toString());
            }
            this.listOfDocs.clear();
            writer.close();
            docsPosting.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method writes us to the posting file the Terms we have finished performing an index process,
     * And puts them in a dictionary
     */
    private void createPostingFileTerms() {
        if(this.Terms.size() >0){

            /**
             * first - sort the array
             */
            List<String> sortTerms = new ArrayList<>(Terms.keySet());
            Collections.sort(sortTerms, String.CASE_INSENSITIVE_ORDER);
            /**
             * open FileWriter , bufferWriter , newFile
             */
            FileWriter currPostingWrite = null;
            BufferedWriter bufCurWrtier = null;
            File newPosting = new File (this.pathOfTermsPosting + "\\" + numberOfPosting + ".txt");

            /**
             * write to the new File
             */
            try {
                currPostingWrite = new FileWriter(newPosting , true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bufCurWrtier = new BufferedWriter(currPostingWrite);


            for(String termName : sortTerms){
                Term termToWrite = Terms.remove(termName);
                try
                {
                    if(checkIfWord(termName) && dictionary.containsKey(termName.toUpperCase())){//FIRST
                        if(checkUpperCase(termName)){
                            termToWrite.changeNameToUpperCase();
                        }
                        else{
                            dictionary.remove(termName.toUpperCase());
                            dictionary.put(termName , new String[2]);
                        }
                    }
                    else if(checkIfWord(termName) && dictionary.containsKey(termName.toLowerCase())){//first
                        if(checkUpperCase(termName)){
                            termToWrite.changeNameToLowerCase();
                        }
                    }
                    else{
                        dictionary.put(termName ,  new String[2]);
                    }
                    bufCurWrtier.append(termToWrite.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

            try {
                bufCurWrtier.flush();
                currPostingWrite.flush();
                bufCurWrtier.close();
                currPostingWrite.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Terms.clear();
        }
    }

    /**
     * This method writes us to the posting file the Entities we have finished performing an index process,
     * And puts them in a dictionary
     * Only the Entities that appear in more than 2 documents
     * The other entities are not written to the dictionary and to the posting files
     * And waiting  the next set of documents
     */
    private void createPostingForEntities(){
        if(this.entities.size()>0){
            /**
             * first - sort the array
             */
            List<String> sortEntites = new ArrayList<>(this.entities.keySet());
            Collections.sort(sortEntites, String.CASE_INSENSITIVE_ORDER);
            /**
             * open FileWriter , bufferWriter , newFile
             */
            FileWriter writeEntities = null;
            File newPostingForEntities = new File(this.pathOfEntityPosting + "\\" + numberOfPosting + ".txt");

            /**
             * write to the new File
             */
            try {
                newPostingForEntities.createNewFile();
                writeEntities = new FileWriter(newPostingForEntities);
                BufferedWriter bufWriter = new BufferedWriter(writeEntities);
                for(String entyName : sortEntites) {
                    Term enty = entities.remove(entyName);
                    if(dictionary.containsKey(entyName)){
                        bufWriter.write(enty.toString());
                        bufWriter.flush();
                    }
                    else if (enty.getNumberOfDocsToTerms() >= 2) {
                        bufWriter.write(enty.toString());
                        bufWriter.flush();
                        dictionary.put(enty.getName() ,  new String[2]);
                    }
                }
                bufWriter.close();
                writeEntities.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * This function is activated by the steamer and notifies the indexer to finish the work.
     * The indexer finishes writing to the temporary files and starts creating the final files,
     * After the temporary merge.
     * The process is done separately for entities and Terms
     * At the end we update the pointers and frequencies in the dictionary
     */
    public void finish(){
        updateDocNum("");
        createPostingFileTerms();
        createPostingForEntities();
        this.entities.clear();
        createPostingFileDocs();

        System.out.printf("num Of docs Total = " + numberOFDocsTotal);
        mergePostingFiles(this.pathOfTermsPosting);
        this.numberOfPosting = mergePostingFiles(this.pathOfEntityPosting);
        this.AvarageLengtOfAllDocs = this.TotalLengthOfDocs/this.numberOFDocsTotal;

        cutFileToSubPostings();
        insertEntityPathToDictionary();
        createDictionary();
    }

    /**
     * This function passes the entity final file and updates the pointers to entities only
     */
    private void insertEntityPathToDictionary() {
        File entityPosting = new File (this.pathOfEntityPosting);
        File[] f = entityPosting.listFiles();
        for(File posting : f){
            try {
                FileReader fr = new FileReader(posting);
                BufferedReader bf = new BufferedReader(fr);
                String line = bf.readLine();
                int numberLine = 1;
                while (line!=null){
                    String[] splitLine = line.split(",");
                    String[] newValue = new String[]{posting.getPath()+","+numberLine,splitLine[2]};
                    this.dictionary.replace(splitLine[0],newValue);
                    numberLine++;
                    line = bf.readLine();
                }
                bf.close();
                fr.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function get a Path of Temporary Posting files and merges them
     * to get a final file without duplicates.
     * It merges iteratively between every 2 temporary files until we get a single file.
     * @param path
     * @return
     */
    private int mergePostingFiles(String path) {

        Queue<File> mergePosting = new LinkedList<>();
        File folder = new File(path);
        File[] allPosting = folder.listFiles();

        Arrays.sort(allPosting, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Integer.compare(Integer.valueOf(f1.getName().replace(".txt","")), Integer.valueOf(f2.getName().replace(".txt","")));
            }
        });

        int counter = this.numberOfPosting;
        File posting1;
        File posting2;
        File newPosting;

        if (allPosting != null) {
            for (int i=0 ; i<allPosting.length ; i++){
                ((LinkedList<File>) mergePosting).addLast(allPosting[i]);
            }
        }

        int index = 0;
        while (mergePosting.size() > 1) {
            newPosting = new File(path + "\\" + counter + ".txt");
            posting1 = ((LinkedList<File>) mergePosting).removeFirst();
            posting2 = ((LinkedList<File>) mergePosting).removeFirst();

            FileReader reader1;
            FileReader reader2;
            FileWriter writerPosting;

            BufferedReader bfRead1;
            BufferedReader bfRead2;

            BufferedWriter bfWritePosting;

            try {
                reader1 = new FileReader(posting1);
                reader2 = new FileReader(posting2);
                writerPosting = new FileWriter(newPosting, true);
                bfRead1 = new BufferedReader(reader1);
                bfRead2 = new BufferedReader(reader2);
                bfWritePosting = new BufferedWriter(writerPosting);

                String line1 = bfRead1.readLine();
                String line2 = bfRead2.readLine();

                while (line1 != null && line2 != null) {

                    Term t1 ;//= new Term("");
                    String[] t1Split =  line1.split(",");
                    String[] t2Split = line2.split(",");

                    String tName1 = t1Split[0];
                    String tName2 = t2Split[0];

                    //compare
                    String finalLine = "";
                    if (tName1.compareToIgnoreCase(tName2) < 0) { // tName1 is smaller!!
                        bfWritePosting.append(line1+"\n");
                        line1 = bfRead1.readLine();
                    } else if (checkIfWord(tName1) && (checkIfWord(tName2)) && (tName1.compareToIgnoreCase(tName2) == 0)) {
                        if (  (checkUpperCase(tName1) && checkLowerCase(tName2)) || (checkUpperCase(tName2) && checkLowerCase(tName1)) ) { // FIRST  == first OR first==FIRST
                            t1Split[0] = t1Split[0].toLowerCase();
                            t2Split[0] = t2Split[0].toLowerCase();
                            t1 = new Term(tName1.toLowerCase());
                        }
                        else{ // first == first or FIRST == FIRST
                            t1 = new Term(tName1);
                        }
                        t1.revToString(t1Split);
                        t1.revToString(t2Split);
                        finalLine = t1.toString();
                        bfWritePosting.append(finalLine);
                        line1 = bfRead1.readLine();
                        line2 = bfRead2.readLine();
                    } else if (!checkIfWord(tName1) && !checkIfWord(tName2) && tName1.compareTo(tName2) == 0) { // 0.01M  == 0.01M
                        t1 = new Term(tName1);
                        t1.revToString(t1Split);
                        t1.revToString(t2Split);
                        finalLine = t1.toString();
                        bfWritePosting.append(finalLine);
                        line1 = bfRead1.readLine();
                        line2 = bfRead2.readLine();
                    } else {
                        bfWritePosting.append(line2+"\n");
                        line2 = bfRead2.readLine();
                    }
                }
                if (line1 == null) { //append only from posting2
                    while (line2 != null) {
                        bfWritePosting.append(line2+"\n");
                        line2 = bfRead2.readLine();
                    }
                } else if (line2 == null) { //append only from posting1
                    while (line1 != null) {
                        bfWritePosting.append(line1+"\n");
                        line1 = bfRead1.readLine();
                    }
                }

                // closing resources
                bfRead1.close();
                bfRead2.close();
                bfWritePosting.close();

                counter++;

                reader1.close();
                reader2.close();

                posting1.delete();
                posting2.delete();

                //remove the first two index in merge posting
                ((LinkedList<File>) mergePosting).addLast(newPosting);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return counter;
    }

    /**
     * This function is activated by the steamer and tells the indexer
     * to write what has been collected into temporary files.
     */
    public void write() {
        createPostingFileTerms();
        createPostingForEntities();
        createPostingFileDocs();
        this.numberOfPosting++;
    }

    /**
     * This function splits the final Posting file into several smaller posting files
     * 40,000 Terms for each file.
     */
    private void cutFileToSubPostings(){
        File posting = new File(this.pathOfTermsPosting + "\\"+(this.numberOfPosting-1) + ".txt");
        FileReader fr;
        BufferedReader br;

        int numOfFinalPosting = 1;
        int numOfNumbers = 0;
        int numOfLine = 1;
        FileWriter fw = null;
        BufferedWriter bw = null;


        String line = null;
        int size = 40000;

        try {
            fr = new FileReader(posting);
            br = new BufferedReader(fr);
            line = br.readLine();
            boolean stop = false;
            while(!stop){

                String currentPath = this.pathOfTermsPosting+"\\"+numOfFinalPosting+".txt";
                File file = new File(currentPath);
                file.createNewFile();
                fw = new FileWriter(file , true);
                bw  = new BufferedWriter(fw);

                while(numOfLine <= size && line != null){
                    String[] split = line.split(",");
                    String Name = split[0];
                    String[] newValue = new String[]{currentPath+","+numOfLine , split[2]};
                    dictionary.replace(Name , this.dictionary.get(Name) , newValue);
                    bw.append(line + "\n");
                    numOfLine++;
                    line = br.readLine();
                    if(!checkIfWord(Name)){
                        numOfNumbers++; //How many numbers we have
                    }
                }

                bw.close();
                fw.close();
                if(line == null){
                    stop = true;
                }
                numOfFinalPosting++;
                numOfLine = 1;

            }
            br.close();
            fr.close();
            posting.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *This function creates the dictionary file that is sorted by lexicographic order
     * For each Term we save the path to its posting file and its frequency in the corpus
     */
    private void createDictionary() {
        File dictionary = new File(this.pathToSavePostings+"\\Dictionary"+this.stemming+".txt");
        ArrayList<String> sort = new ArrayList<>(this.dictionary.keySet());
        Collections.sort(sort , String.CASE_INSENSITIVE_ORDER);
      //  if(dictionary.exists()){
            dictionary.delete();
      //  }
        try {
            dictionary.createNewFile();
            FileWriter toWrite = new FileWriter(dictionary , true);
            BufferedWriter bf = new BufferedWriter(toWrite);
            for(String name: sort){
                StringBuilder line = new StringBuilder() ;

                line.append(name);
                line.append("--");
                line.append(this.dictionary.get(name)[0]+",");
                line.append(this.dictionary.get(name)[1] + "\n");

                bf.append(line);
            }
            bf.close();
            toWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enterNoTextTagDoc(String docNum) {
        Doc emptyDoc = new Doc(docNum,0,0,0,"");
        this.listOfDocs.add(emptyDoc);
        this.numberOFDocsTotal++;
        this.indexDoc++;
    }

    public int getAvargeDocsLength(){
        return this.AvarageLengtOfAllDocs;
    }

}

