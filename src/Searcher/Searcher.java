package Searcher;

import Index.Doc;
import Index.Term;
import Parser.Parser;
import Ranker.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Searcher {

    private boolean semantic ;
    private boolean stemming;
    private String pathToAllPostingFiles;
    private String pathToCurpos;
    private int numberOfTotalDocs = 0;
    private int avarageLengthOfDoc = 0;

    private Parser QueryParser;
    private Word2Vec SemanticModel = new Word2Vec();
    ;
    private Ranker ranker = new Ranker();
    private NGramCorrectness NGram = new NGramCorrectness(2);


    private Map<String , String[]> Dictionary;
    private ArrayList<Doc> DocsInCorpus = new ArrayList<>();

    private Object[] TmpEntitiesForDoc = new Object[5];


    private HashMap<String , ArrayList<String>> PostingFiles = new HashMap<>();
    private HashMap<String , Doc> DocsToRanker = new HashMap<>();
    private HashMap<String , String[]> FiveImportantEnties = new HashMap<>();
    private ArrayList<Term> TermsToRankerQuery = new ArrayList<>();
    private ArrayList<Term> TermsToRankerDesc = new ArrayList<>();
    private ArrayList<Term> SynsTermsToRanker = new ArrayList<>();
    private List<Map.Entry<Doc , Double>> ResultForQuery = new ArrayList<>();



    //Methods
    /**
     * Constructor Of The Class
     * Initialize All The Fields In The Class,
     * Initialize The Doc Objects In The Corpus
     * Performs division of NGram Distribution of k = 2;
     * @param semantic - true , To Use Semantic Model
     * @param clickStreamData
     * @param steming - true , To Perform Stemming To The Query
     * @param dictionary - The Dictionary Of All The Terms In Corpus
     * @param pathToSaveCurpos - Path Of The Corpus Files
     * @param pathToPostingFiles - Path Of All The Posting Files That Necessary
     */
    public Searcher(boolean semantic , boolean clickStreamData, boolean steming , HashMap<String , String[]> dictionary ,String pathToSaveCurpos , String pathToPostingFiles){
        this.semantic = semantic;
        this.stemming = steming;
        this.pathToAllPostingFiles = pathToPostingFiles;
        this.pathToCurpos = pathToSaveCurpos;
        QueryParser = new Parser(true , steming , pathToSaveCurpos , pathToPostingFiles);
        this.Dictionary = this.NGram.nGramsDistribution(dictionary);
        initialDocs();
    }


    private void initialDocs(){
        FileReader postingToReadDocs;
        BufferedReader readDocs;

        String pathToDocsPosting = this.pathToAllPostingFiles + "\\DocsPosting" + this.stemming + "\\posting.txt";
        String line;
        int sum = 0;

        try {
            postingToReadDocs = new FileReader(new File(pathToDocsPosting));
            readDocs = new BufferedReader(postingToReadDocs);
            line = readDocs.readLine();
            while(line != null){
                Doc newDoc = new Doc();
                newDoc.revToString(line.split("#"));
                this.DocsInCorpus.add(newDoc);
                sum += newDoc.getSizeOfDoc();
                this.numberOfTotalDocs++;
                line = readDocs.readLine();
            }
            readDocs.close();
            postingToReadDocs.close();
            this.avarageLengthOfDoc = sum / this.numberOfTotalDocs;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void addToImportantFive(String enty, Integer freq) {
        Object[] newArray = new Object[]{enty , freq};
        for(int i = 0 ; i < this.TmpEntitiesForDoc.length ; i++){
            if(this.TmpEntitiesForDoc[i] == null){
                this.TmpEntitiesForDoc[i] = newArray;
                break;
            }
            else{
                Object[] tmpArr = (Object[])this.TmpEntitiesForDoc[i];
                int entyFreq = (Integer)tmpArr[1];
                if(freq > entyFreq) {
                    Object[] tmp = tmpArr;
                    this.TmpEntitiesForDoc[i] = newArray;
                    newArray = tmp;
                }
            }
        }
    }


    /**
     * This Function Get A Query And The Description Of The Query If We Have It And To The Following Action:
     * 1) Parse The Query And The Description To Terms And Insert Them Acording To The Following List - termsFromQuery , TermsFromDesc
     * 2) If The User Want To Use The Semantic Model We Will Get The Synonyms Words For Each Term We Get and Add Them To The Terms We Already Have
     *    Now We Will Work With AllTheTermsToRanker List - TermsFromQuery + TermsFromDesc + TermsFromSemanticModel
     * 3) Collect All The Terms That Appear In The Same Posting File In PostingFiles HashMap To Streamline The Posting Files Reading Process
     *    If A Particular Word Does Not Exist In The Dictionary, We Will Correct Using The Jacquard Similarity Using NGram
     * 4) For Each Term That Appear In AllTheTermsToRanker We Will Find All The Documents That The Terms Appear In Them:
     *    For Each Record That Describe A Term We Will Create Term Object That Contain All The Information We Need For This Process
     *    We Will To This Process Only for Term That We See In The First Time for This Query
     * 5) Add All The Documents We Get For Any Term In The List To The Following List:
     *      DocsToRanker - Docs For Term That We Get And Appear In Query Or In The Description Of The Query
     *                     And For Term We Get From The Semantic Model If The User Choose On It.
     *      No Document Duplicates
     * 6) Add All The Terms To The Following List:
     *      TermsToRankerQuery - If The Term Appear In The Query
     *      TermsToRankerDesc - If The Term Appear In The Description
     *      SynsTermsToRanker - Synonyms Terms For Terms That Apear In Query Or Description And We Found Them Using The Semantic Model
     *      There May Be Duplicates For The Rankin Process
     * 7) Send The Result Of Doduments To The Ranker To Find To Most Relevant Documents For The Query
     *    We Will Save The Result In The List ResultForQuery
     *    This List Is THe Answer For The Query That The User Insert.
     * 8) For Each Document In The ResultForQuery List We Will Find The 5 Most Frequent Entities
     * @param queryANDdisc - If There Is Description To The Query The String Will Be : "query#Description"
     *                       Else : "query"
     */
    public void insertQuery(String queryANDdisc){
      /*
      clean the databases
       */
        this.PostingFiles.clear();
        this.DocsToRanker.clear();
        this.FiveImportantEnties.clear();
        this.TermsToRankerQuery.clear();
        this.TermsToRankerDesc.clear();
        this.SynsTermsToRanker.clear();
        this.ResultForQuery.clear();

        long start = System.nanoTime();
        this.QueryParser = new Parser(true , this.stemming , this.pathToCurpos , this.pathToAllPostingFiles);

        FileReader postingToReadTerms;
        BufferedReader readTerms;
        String line = "";
        String desc = "";
        String query = "";
        /*
        Start Step Number 1 : Parse Query And Description
         */
        ArrayList<String> termsFromDesc = new ArrayList<>();
        if(queryANDdisc.contains("#")){
            String[] split = queryANDdisc.split("#");
            query = split[0];
            desc = split[1];
            /*desctiption*/
            termsFromDesc = this.QueryParser.statParser(desc);
        }
        else{
            query = queryANDdisc;
        }
        this.QueryParser = new Parser(true , this.stemming , this.pathToCurpos , this.pathToAllPostingFiles);
        /*query*/
        ArrayList<String> termsFromQuery = this.QueryParser.statParser(query);

        ArrayList<String> allTermsForRanking = new ArrayList<>(termsFromQuery);
        for(String name : termsFromDesc){
            allTermsForRanking.add(name);
        }

        HashSet<String> alreadyCheck = new HashSet<>();
        HashMap<String , List<String>> Synonyms;
        /*
         if its an Entity word - split the word
         */
        allTermsForRanking = addTermsIfEntity(allTermsForRanking);


        /*
        if its semantic - in allTermsForRanking will be the new words
         */
        if(this.semantic){
            this.SemanticModel.startModel(allTermsForRanking);
            Synonyms = this.SemanticModel.getSemanticForQuery();
            for(String term : Synonyms.keySet()){
                List<String> synonym = Synonyms.get(term);
                for(String syn : synonym){
                    allTermsForRanking.add(syn);
                }
            }
        }

        /*
        here the allTermsForRanking includes all the term we need to search
         */
        try{
            /*
            here we iter each term and check if the word is in dictionary
             */
            for(String termName : allTermsForRanking){
                /*
                here save the posting files for each word
                 */
                if(this.QueryParser.checkIfWord(termName) && this.Dictionary.containsKey(termName.toUpperCase())){
                    String toSplit = this.Dictionary.get(termName.toUpperCase())[0];
                    String[] path = toSplit.split(",");
                    if(!this.PostingFiles.containsKey(path[0])){
                        this.PostingFiles.put(path[0] , new ArrayList<>());
                    }
                    ArrayList<String> val = this.PostingFiles.get(path[0]);
                    val.add(termName.toUpperCase());
                    this.PostingFiles.replace( path[0] ,this.PostingFiles.get(path[0])  , val);
                }
                else if(this.QueryParser.checkIfWord(termName) && this.Dictionary.containsKey(termName.toLowerCase())){
                    String toSplit = this.Dictionary.get(termName.toLowerCase())[0];
                    String[] path = toSplit.split(",");
                    if(!this.PostingFiles.containsKey(path[0])){
                        this.PostingFiles.put(path[0] , new ArrayList<>());
                    }
                    ArrayList<String> val = this.PostingFiles.get(path[0]);
                    val.add(termName.toLowerCase());
                    this.PostingFiles.replace( path[0] ,this.PostingFiles.get(path[0])  , val);
                }
                else if(this.Dictionary.containsKey(termName)){
                    String toSplit = this.Dictionary.get(termName.toLowerCase())[0];
                    String[] path = toSplit.split(",");
                    if(!this.PostingFiles.containsKey(path[0])){
                        this.PostingFiles.put(path[0] , new ArrayList<>());
                    }
                    ArrayList<String> val = this.PostingFiles.get(path[0]);
                    val.add(termName.toLowerCase());
                    this.PostingFiles.replace( path[0] ,this.PostingFiles.get(path[0])  , val);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        /*
        here we read the posting files from the disc and save the terms!
         */
        try{
            for(String path : this.PostingFiles.keySet()) {
                File file = new File (path);
                ArrayList<String> terms = this.PostingFiles.get(path);
                for (String term : terms) {
                    postingToReadTerms = new FileReader(file);
                    readTerms = new BufferedReader(postingToReadTerms);
                    String[] val = this.Dictionary.get(term)[0].split(",");
                    int numberOfLine = Integer.valueOf(val[1]);
                    int counter = 1;
                    line = readTerms.readLine();
                    while (line != null && counter < numberOfLine) {
                        line = readTerms.readLine();
                        counter++;
                    }
                    postingToReadTerms.close();
                    Term newTerm = new Term(term);
                    newTerm.revToString(line.split(","));
                    if(termsFromQuery.contains(term.toUpperCase()) || termsFromQuery.contains((term.toLowerCase()))){
                        this.TermsToRankerQuery.add(newTerm);
                    }
                    else if(termsFromDesc.contains(term.toUpperCase()) || termsFromDesc.contains(term.toLowerCase())){
                        this.TermsToRankerDesc.add(newTerm);
                    }
                    else if(this.semantic){
                        this.SynsTermsToRanker.add(newTerm);
                    }
                    if(alreadyCheck.contains(newTerm.getName().toLowerCase()) || alreadyCheck.contains((newTerm.getName().toUpperCase()))){
                        continue;
                    }
                    alreadyCheck.add(newTerm.getName());
                    ArrayList<String> DocIdForTerm = newTerm.docsId();
                    for (int j = 0 ; j < DocIdForTerm.size() ; j++) {
                        String pointerToDoc = newTerm.getDocPointer(DocIdForTerm.get(j));
                        int index = Integer.valueOf(pointerToDoc);
                        Doc docToReturn = this.DocsInCorpus.get(index-1);
                        if(!this.DocsToRanker.containsKey(docToReturn.getFileId())){
                            this.DocsToRanker.put(docToReturn.getFileId() , docToReturn);
                        }
                    }
                    readTerms.close();
                    postingToReadTerms.close();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(this.semantic){
            this.ranker.enterResultWithSemantic(this.DocsToRanker , this.TermsToRankerQuery , this.TermsToRankerDesc , this.SynsTermsToRanker , this.numberOfTotalDocs , this.avarageLengthOfDoc);
        }
        else{
            this.ranker.enterReasult(this.DocsToRanker , this.TermsToRankerDesc , this.TermsToRankerQuery , this.numberOfTotalDocs , this.avarageLengthOfDoc);
        }
        this.ResultForQuery = this.ranker.rankTheDocuments();

        for (Map.Entry DocEntry : this.ResultForQuery) {
            Doc docToReturn =(Doc) DocEntry.getKey();
            if (!this.FiveImportantEnties.containsKey(docToReturn.getFileId())) {
                HashMap<String, Integer> entetiesInDoc = docToReturn.getEnteties();
                for (String enty : entetiesInDoc.keySet()) {
                    if (this.Dictionary.containsKey(enty)) {
                        addToImportantFive(enty, entetiesInDoc.get(enty));
                    }
                }
                entetiesInDoc.clear();
                String[] insert = new String[5];
                for (int i = 0; i < this.TmpEntitiesForDoc.length; i++) {
                    if(this.TmpEntitiesForDoc[i] != null){
                        Object[] tmp = (Object[])this.TmpEntitiesForDoc[i];
                        insert[i] = (String)tmp[0];
                    }
                }
                this.FiveImportantEnties.put(docToReturn.getFileId(), insert);
                this.TmpEntitiesForDoc = new Object[5];
            }
        }

        System.out.println("The Query Is : " + query);
        long end = System.nanoTime();
        long total = (end-start)/1000000;
        System.out.println("TIME : " + total + " ms");
    }

    /**
     * This function An Entity That We Get From Parser To Sub Terms,
     * And Add Them To The List Of All Terms To Rank
     * @param termsFromQuery - The List Of All The Terms From THe Query And Description
     */
    private ArrayList<String> addTermsIfEntity(ArrayList<String> termsFromQuery) {
        int size = termsFromQuery.size();
        for(int i = 0 ; i< size ; i++) {
            String termName = termsFromQuery.get(i);
            if (this.QueryParser.checkIfWord(termName)) {
                String[] entitySplit = null;
                if (termName.contains(" ")) {
                    entitySplit = termName.split(" ");
                } else if (termName.contains("-")) {
                    entitySplit = termName.split("-");
                } else if (termName.contains("'/'")) {
                    entitySplit = termName.split("/");
                }
                if (entitySplit != null) {
                    for (int j = 0; j < entitySplit.length; j++) {
                        termsFromQuery.add(entitySplit[j]);
                    }
                }
            }
        }
        return termsFromQuery;
    }
    /**
     * Returns The 50 Highest Ranked Documents For A Particular Query
     * @return
     */
    public ArrayList<String> getDocsResultsFromQuery() {
        ArrayList<String>  docsNumbers = new ArrayList<>();
        for(Map.Entry<Doc,Double> entry : this.ResultForQuery){
            Doc doc = entry.getKey();
            docsNumbers.add(doc.getFileId());
        }
        return docsNumbers;
    }

    /**
     * Retrun The Five Most Frequent Entities In The 50 Documents That We retrieve
     * @return HashMap <Doc ID , List Of 5 Entities>
     */
    public HashMap<String , String[]> getEnties(){
        return this.FiveImportantEnties;
    }


    /**
     * Return The Term That Is In The List Of The Terms We Get From The Query,
     * By Tha Name Of The Term
     * @param termName - Term Name
     * @return Term
     */
    private Term getTermByNameQ(String termName) {
        Term tr = null;
        for(Term t : this.TermsToRankerQuery){
            if(t.getName().equals(termName)){
                tr =  t;
                break;
            }
        }
        return tr;
    }
    /**
     * Return The Term That Is In The List Of The Terms We Get From The Description Of The Query,
     * By Tha Name Of The Term
     * @param termName - Term Name
     * @return Term
     */
    private Term getTermByNameD(String termName) {
        Term tr = null;
        for(Term t : this.TermsToRankerDesc){
            if(t.getName().equals(termName)){
                tr =  t;
                break;
            }
        }
        return tr;
    }

    /**
     * Return The Term That Is In The List Of The Terms We Get From The Semantic Model For The Query,
     * By Tha Name Of The Term
     * @param termName - Term Name
     * @return Term
     */
    private Term getTermByNameS(String termName) {
        Term tr = null;
        for(Term t : this.SynsTermsToRanker){
            if(t.getName().equals(termName)){
                tr =  t;
                break;
            }
        }
        return tr;
    }

}