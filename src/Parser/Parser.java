package Parser;
import Index.*;
import Roles.*;

import java.io.*;
import java.util.*;
public class Parser {
    private HashSet<String> stopWord;
    private Map<String , String> monthBase = new HashMap<String , String>(){{
        put("January", "01");
        put("Jan", "01");
        put("February", "02");
        put("Feb", "02");
        put("March", "03");
        put("Mar", "03");
        put("April", "04");
        put("Apr", "04");
        put("May", "05");
        put("June", "06");
        put("Jun", "06");
        put("July", "07");
        put("Jul", "07");
        put("August", "08");
        put("Aug", "08");
        put("September", "09");
        put("October", "10");
        put("Sep", "09");
        put("Oct", "10");
        put("November", "11");
        put("Nov", "11");
        put("December", "12");
        put("Dec", "12");
    }};
    private String[] splitBySpace;  //for one doc
    private int currentIndex;
    private String currenDocNum;
    private int termIndex;

    private FractionRole fractionRole = new FractionRole();
    private RangeRole rangeRole = new RangeRole();
    private PriceRole priceRole = new PriceRole();
    private PercentRole percentRole = new PercentRole();
    private NumbersRole numbersRole = new NumbersRole();
    private DatesRole datesRole = new DatesRole();
    private Stemmer stemmer;
    //New Part B
    private Boolean parsQuery;
    private String[] parseTermsInquery ;
    //New Part B
    public Parser (Boolean parsQuery , Boolean stemming , String pathToCorpus , String pathToSave){
        this.parsQuery = parsQuery;
        this.stemmer=new Stemmer(this.parsQuery ,pathToSave , stemming);
        readStopWords(pathToCorpus);
    }
    /**
     * This function reads the stop words From the file in the path
     * and saves them in a data structure.
     * @param pathOfStopWords
     */
    private void readStopWords(String pathOfStopWords) {
        this.stopWord = new HashSet<>();
        File stopWordsFile = new File(pathOfStopWords + "//stopWords.txt");
        try{
            BufferedReader br = new BufferedReader(new FileReader(stopWordsFile));
            String stopWord;
            while ((stopWord = br.readLine()) != null)
                this.stopWord.add(wordCorrectness(stopWord));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //New Part B
    public ArrayList<String> statParser(String query){
        String q = query.replaceAll(",|\\*|\\(|\\)|'|\"|:|;|`|\\{|}|\\?|\\[|]|\\\\|#|--|\\+|---|&|\\.\\.\\.|\\.\\.|\\||=|>|<|//|", "");
        this.splitBySpace = q.split("[ |:]");
        parserRoleChecking();
        return this.stemmer.getQueryTerms();
    }
    /**
     * This function receives a corpus of documents and performs the process of Pars
     * to each document in the corpus.
     * After 10 folders, we will let Steamer start writing for temporary files so we can continue The process efficiently
     * @param corpusFromReadFile
     */
    public void startParser(Map<String , String> corpusFromReadFile){
        int numOfDoc = 0;
        for (String docNum: corpusFromReadFile.keySet()) {
            if (corpusFromReadFile.get(docNum).equals("")) {
                parseNotTextTag(docNum, corpusFromReadFile.get(docNum));
            } else {
                String text = corpusFromReadFile.get(docNum).replaceAll(",|\\*|\\(|\\)|'|\"|:|;|`|\\{|}|\\?|\\[|]|\\\\|#|--|\\+|---|&|\\.\\.\\.|\\.\\.|\\||=|>|<|//|", "");
                this.splitBySpace = text.split("[ |:]");
                this.currenDocNum = docNum;
                currentIndex = 0;
                this.termIndex = 0;
                parserRoleChecking();
                numOfDoc++;
            }
        }
        this.stemmer.startIndex();
    }
    private void parseNotTextTag(String docNum, String s) {
        stemmer.enterDocToDictionary(docNum,s);
    }
    /**
     *This function is activated by the ReedFile and notifies the Parser that there are no more documents
     */
    public void finish(){
        this.stemmer.finish();
    }
    /**
     *This function is the central function of the process
     *and all the laws and Roles are tested according to the appropriate requirements.
     * This function performs a process of tokenization and passes word by word until a Term is created.
     * After creating each Term we will pass it on to the steamer and move to the next word
     */
    private void parserRoleChecking(){
        for(int i = 0 ; i < splitBySpace.length ; i++){
            String currentWord  = getCurrentWord();
            ArrayList<String> suspect = new ArrayList<>();
            if( currentWord.contains("--") || currentWord.length() == 0  || this.stopWord.contains(currentWord) || ( currentWord.length()==1 && !checkLowerCase(currentWord) && !checkUpperCase(currentWord) && !checkIfNumber(currentWord) ) ){
                currentIndex++;
                continue;
            }
            /**
             * ///////////////////////Ranges///////////////////////
             * FIRST CASE - In this case, these are ranges.
             * It may be a hyphen separated entity, "King-Of-Denemark"
             * or numbers or prices, "100-1500" / "$30-$40"
             * or a hyphenated phrase "step-by-step"
             */
            else if(checkIfRange(currentWord)) {
                String[] ranges = currentWord.split("-");
                boolean capital = false;
                for (int j = 0; j < ranges.length; j++) {
                    ArrayList<String> numbersInRange = new ArrayList<>();
                    String tmp = wordCorrectness(ranges[j]);
                     /*
                        If This is number it can be "27" or "3/4"
                     */
                    if (checkIfNumber(tmp)) {
                        numbersInRange.add(tmp);
                        if(!checkIfFraction(tmp)){
                            tmp = numbersRole.startRole(numbersInRange);

                        }
                    }
                    /**
                     * If the first word in the range is a word,
                     * it can be an entity or phrase or a word-word range
                     */
                    else if (checkIfWord(tmp)) {
                        if(tmp.equalsIgnoreCase("kilogramme") || tmp.equalsIgnoreCase("kilogrammes") || tmp.equalsIgnoreCase("kilogram")){
                            tmp = "kg";
                            suspect.add(tmp);
                        }
                        else if(tmp.equalsIgnoreCase("kilometer") || tmp.equalsIgnoreCase("kilometers")){
                            tmp = "km";
                            suspect.add(tmp);
                        }

                        //entitiey - if we find one word with capital letter we save this term as a Entity
                        else if (checkUpperCase(tmp) || capital) {//entitiey
                            tmp = tmp.toUpperCase();
                            int size = suspect.size();
                            for(int k = 0 ; k < size && !capital ; k++){
                                String wordInLowerCase = suspect.get(k);
                                if(checkIfWord(wordInLowerCase)){
                                    suspect.add(k , wordInLowerCase.toUpperCase());
                                }
                            }
                            capital = true;
                        }
                        /**
                         * If it is not one of the cases we will enter the word after remove unnecessary signs
                         */
                    }
                    /**
                     * If there is a dollar sign at first it is a range of prices
                     */
                    else if (tmp!=null && tmp.length()>0 && tmp.charAt(0) == '$') {
                        numbersInRange.add(tmp.substring(1));
                        if(checkIfNumber(tmp)){
                            tmp = priceRole.startRole(numbersInRange);
                        }
                    }
                    if(tmp.length() > 0)
                        suspect.add(tmp);
                }
                if (!suspect.isEmpty()) {
                    enterWordToDictionary( rangeRole.startRole(suspect));
                }
                else{
                    continue;
                }
            }
            /**
             * ///////////////////////Number///////////////////////
             * SECOND CASE - if it is a number we have several options,
             * whether it is a price, or a percentage, or a clean number
             * and after describing the number size
             */
            else if(checkIfNumber(currentWord)){
                if(checkIfFraction(currentWord)){
                    enterWordToDictionary(currentWord);
                    currentIndex++;
                    continue;
                }
                suspect.add(currentWord); // number
                // CHECK THE NEXT WORD - million/trillion/billion or 2/3 or Dollars or mounth or U.S. or Million or m or bn
                currentIndex++;
                currentWord = getCurrentWord();
                if(currentWord.equals("Dollars")){
                    //ONLY NUMBER OF PRICE
                    enterWordToDictionary(priceRole.startRole(suspect));
                }
                else if ( currentWord.equals("Million") || currentWord.equals("Thousand") || currentWord.equals("Billion") ){
                    suspect.add(currentWord);
                    //ONE NUMBER WITH DESCRIPTION OF SIZE
                    enterWordToDictionary(this.numbersRole.startRole(suspect));
                }
                else if ( currentWord.equals("percent") || currentWord.equals("percentage") ){
                    suspect.add(currentWord);
                    //ONE NUMBER OF PERCENT
                    enterWordToDictionary(percentRole.startRole(suspect));
                }
                else if ( currentWord.equals("m") || currentWord.equals("bn") || currentWord.equals("million") || currentWord.equals("billion") ){
                    //ONE NUMBER OF PRICE WITH DESCRIPTION OF SIZE
                    suspect.add(currentWord);
                    //CHECK THE NEXT WORD - Dollars / U.S. AND SKIP ON
                    currentIndex++;
                    currentWord = getCurrentWord();
                    if ( currentWord.equals("U.S.") ){
                        currentIndex++;
                    }
                    enterWordToDictionary(priceRole.startRole(suspect));
                }
                /**
                 * This is a number that is part of a date
                 * Month Day or Day Month
                 * Month Year or Year month
                 * Day Month Year or Year Month Day
                 */
                else if(checkIfDate(currentWord)){
                    suspect.add(monthBase.get(currentWord));
                    //check if the next word is a year
                    String lastWord = currentWord;
                    currentIndex++;
                    currentWord = getCurrentWord();
                    if(checkIfYear(currentWord)){
                        suspect.add(currentWord);
                        //call role - date with day - mounth - year
                        enterWordToDictionary(datesRole.startRole(suspect));
                    }
                    else {
                        currentIndex--;
                        currentWord = lastWord;
                    }
                    //call role - date with day - mounth
                    enterWordToDictionary(datesRole.startRole(suspect));
                }
                //check if 3/2 Dollars
                else if(checkIfNumber(currentWord)){
                    if(currentWord.contains("/") && checkIfFraction(currentWord) ){
                        suspect.add(currentWord);
                        enterWordToDictionary(priceRole.startRole(suspect));
                        currentIndex++ ; /// skip Dollars
                    }
                }
                else {
                    /**
                     * Its a regular number or percent
                     */
                    currentWord=suspect.get(0);
                    if (currentWord.charAt(currentWord.length()-1) == '%'){
                        currentWord = currentWord.substring(0,currentWord.length()-1);
                        //call role - percent with suspect[1]
                        suspect.add(currentWord);
                        enterWordToDictionary(percentRole.startRole(suspect));
                    }
                    else{
                        //call role - number with suspect[1]
                        enterWordToDictionary(numbersRole.startRole(suspect));
                    }
                }
            }
            /**
             * ///////////////////////between///////////////////////
             * THIRD CASE - This is ranges with the word Between OR Between
             * Between Number and Number
             */
            else if(currentWord.equals("between") || currentWord.equals("Between")){
                String[] ranges = new String[2];
                currentIndex++;
                currentWord = getCurrentWord();
                if(checkIfNumber(currentWord)){
                    ranges[0] = currentWord;
                    currentIndex++;
                    currentWord = getCurrentWord();
                    if(currentWord.equals("and")){
                        currentIndex++;
                        currentWord = getCurrentWord();
                        if(checkIfNumber(currentWord)){
                            ranges[1] = currentWord;
                            //NUMBER-NUMBER
                            //call role -Range with string[]
                            suspect.add(ranges[0]);
                            suspect.add(ranges[1]);
                            enterWordToDictionary(rangeRole.startRole(suspect));
                        }
                    }
                }
            }
            /**
             * ///////////////////////Dollar///////////////////////
             * FOURTH CASE - we received word that its first sign is a dollar sign of price
             */
            else if(currentWord.length()>0 && currentWord.charAt(0)=='$'){
                currentWord.substring(1);
                currentWord = wordCorrectness(currentWord);
                if(checkIfNumber(currentWord)){
                    suspect.add(currentWord);
                    currentIndex++;
                    currentWord = getCurrentWord();
                    if( currentWord.equals("billion") || currentWord.equals("million") ){
                        suspect.add(currentWord);
                        //call role -price with suspect [2]
                        enterWordToDictionary(priceRole.startRole(suspect));
                    }
                    else{
                        //call role -price with suspect [1]
                        currentIndex --;
                        enterWordToDictionary(priceRole.startRole(suspect));
                    }
                }
            }
            /**
             * ///////////////////////words///////////////////////
             * Fifth case - this is a word:
             * ENTITY
             * Word start with capital letter - "First"
             * Word/Word
             * Date - May 01 OR may 1990
             * STOP WORD
             */
            else if(checkIfWord(currentWord)){
                String entity = "";
                while(checkUpperCase(currentWord)){
                    entity += currentWord.toUpperCase() + " ";
                    currentIndex++;
                    currentWord = getCurrentWord();
                }
                if(entity.length() > 0){
                    entity = entity.substring(0,entity.length()-1);
                    enterWordToDictionary(entity);
                    continue;
                }

                /**
                 *  ///////////////////////fraction///////////////////////
                 *                  ourRole to fraction
                 */
                if(currentWord.contains("/")){
                    ArrayList<String> suspectFreq = new ArrayList<>();
                    String[] splitBySlash = currentWord.split("/");
                    for (int j = 0 ; j<splitBySlash.length ; j++){
                        String wordSplited = wordCorrectness(splitBySlash[j]);
                        suspectFreq.add(wordSplited);
                    }
                    if(suspectFreq.size()>0){
                        enterWordToDictionary(this.fractionRole.startRole(suspectFreq));
                    }
                }
                else if(this.stopWord.contains(currentWord.toLowerCase())){
                    currentIndex++;
                    continue;
                }
                else if(checkIfDate(currentWord)){
                    String keepTheCurrentWord = currentWord;
                    suspect.add(monthBase.get(currentWord));
                    currentIndex++;
                    currentWord = getCurrentWord();
                    if (checkIfYear(currentWord)){
                        suspect.add(currentWord);
                    }
                    else if (checkIfOnlyNumber(currentWord)){
                        suspect.add(currentWord);
                    }
                    else {
                        //its only a name of month
                        if (this.stopWord.contains(keepTheCurrentWord) || this.stopWord.contains(keepTheCurrentWord)){
                            continue;
                        }
                        enterWordToDictionary(keepTheCurrentWord);
                        continue;
                    }
                    //call date - role with suspect [2]
                    enterWordToDictionary(datesRole.startRole(suspect));
                }
                if (this.stopWord.contains(currentWord) || this.stopWord.contains(currentWord)){
                    continue;
                }
                enterWordToDictionary(currentWord);
            }
            currentIndex++;
        }
    }
    /**
     * Return the current word to check
     * @return
     */
    private String getCurrentWord(){
        if(currentIndex <splitBySpace.length){
            String currentWord = wordCorrectness(splitBySpace[currentIndex]);
            return currentWord;
        }
        else return "";
    }
    /**
     * These functions test us whether the word is a certain Role
     * @param word
     * @return False / True
     */
    public boolean checkIfWord(String word){
        return checkLowerCase(word) ||
                checkUpperCase(word);
    }
    private boolean checkUpperCase(String word) {
        if(word.length()>0) {
            return (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z');
        }
        return false;
    }
    private boolean checkLowerCase(String word){
        if(word.length()>0){
            return (word.charAt(0) >= 'a' && word.charAt(0) <= 'z');
        }
        return false;
    }
    private boolean checkIfNumber(String word){
        return this.numbersRole.checkIfNumber(word);
    }
    private boolean checkIfDate(String word){
        return monthBase.containsKey(word);
    }
    private boolean checkIfYear(String word) {
        if (word.length() == 4) {
            return checkIfOnlyNumber(word);
        }
        else
            return false;
    }
    private boolean checkIfOnlyNumber(String word){
        String regex = "[0-9]+";
        return word.matches(regex);
    }
    private boolean checkIfRange(String word){
        return word.contains("-");
    }
    private boolean checkIfFraction(String word){
        word = wordCorrectness(word);
        if(word.contains("/")){
            String[] shever = word.split("/");
            for(int i=0;i<shever.length;i++){
                for(int j=0 ; j<shever[i].length() ; j++){
                    if ( !checkIfDigit(shever[i].charAt(j))){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    private boolean checkIfDigit(char num){
        return ( num <= '9' && num >= '0' ) ;
    }
    /**
     *     this function clean the comma ',' at the end of a word
     *     this function clean '/' at the start and the end of a word
     */
    private String wordCorrectness (String word){
        if(word.length() != 0){
            boolean stopCorrectness = false;
            while(!stopCorrectness && word.length() != 0){
                if( word.equals("-"))
                    return "";
                if((word.charAt(0) == '\'' || word.charAt(0) == '"' || word.charAt(0) == '-' ) ||
                        word.charAt(0) == '.' || word.charAt(0) == '/' || word.charAt(0) == '(' ||
                        word.charAt(0) == ',' || word.charAt(0) == '_' || word.charAt(0) == '¥' ){
                    word = word.substring(1);
                }
                if(word.length()<=2 && !checkLowerCase(word) && !checkUpperCase(word) && !checkIfNumber(word)){
                    return "";
                }
                if( ( word.length() != 0 ) &&  (  word.charAt(word.length() -1 ) == '"' || word.charAt(word.length() -1 ) == '\'' ||
                        word.charAt(word.length() -1) == '-' || word.charAt(word.length()-1) == ',' || word.charAt(word.length()-1) == '!' ||
                        word.charAt(word.length()-1) == '/' || word.charAt(word.length()-1) == '.' ||
                        word.charAt(word.length()-1) == ':' || word.charAt(word.length()-1) == ')' ) ){
                    word = word.substring(0, word.length() - 1);
                }
                else{
                    stopCorrectness = true;
                }
            }
            return word;
        }
        return "";
    }
    /**
     * In this function we check if the word is not empty and not a stop word and then pass the word to the steamer
     * @param word
     */
    private void enterWordToDictionary(String word){
        if(word != null && word.length() > 0){
            if(word.charAt(0) == '.' || word.charAt(0) == '_' || word.charAt(0) == '$' || word.charAt(0) == '¥'){
                word = wordCorrectness(word);
            }
            if(!stopWord.contains(word) && !stopWord.contains(word.toLowerCase())){
                if(!this.parsQuery){
                    stemmer.startStemming(word, currenDocNum, String.valueOf(termIndex), false);
                    this.termIndex++;
                }
                else{
                    stemmer.startStemming(word,"", "", false);
                }
            }
        }
    }
    /**
     * Rteurn From the Stemmer THe number of unique Terms in the Corpus
     * @return
     */
    public int getNumOfUniqTerms() {
        return stemmer.getNumOfUniqTerms();
    }
}