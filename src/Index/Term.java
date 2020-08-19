package Index;
import java.io.Serializable;
import java.util.*;

public class Term  {
    private String name;
    private Map<String , Integer> listOfDocs = new HashMap<>();
    private Map<String , String> listOfPossitionInDocs = new HashMap<>();
    private List<String> inTitle = new ArrayList<>();
    private Map<String , String> listOfDocsPointers = new HashMap<>();
    private int Freq; // tf

    public Term(String name){
        this.name = name;
        this.Freq = 0;
    }

    private void addDocToTheList(String docID , String possition , boolean inTitle , String pointerToDoc){
        this.listOfDocs.put(docID , 1);
        this.listOfPossitionInDocs.put(docID , possition);
        this.Freq++;
        if(inTitle){
            this.inTitle.add(docID);
        }
        this.listOfDocsPointers.put(docID , pointerToDoc);
    }

    /**
     *This function updates the list of documents in which the Term appears by the parameters it receives
     * @param docID
     * @param possition
     * @param inTitle
     * @param pointerToDoc
     */
    public void updateDocCounter(String docID , String possition , boolean inTitle , String pointerToDoc){
        if(!appearInDoc(docID)){
            addDocToTheList(docID , possition , false , pointerToDoc); // we Save Only The first Possition of Term in the document
        }
        else{
            int counter = this.listOfDocs.get(docID);
            this.listOfDocs.replace(docID , counter , ++counter);
            this.Freq++;
        }
    }

    /**
     * These functions are getters for fields and information stored in this object
     * @return
     */
    public String getName(){
        return this.name;
    }
    public String getPossitionInDoc(String docId){
        if(!this.listOfDocs.containsKey(docId)){
            return "0";
        }
        return listOfPossitionInDocs.get(docId);
    }
    public int getNumOfAppearancesInDoc(String docId){
        if(!this.listOfDocs.containsKey(docId)){
            return 0;
        }
        return this.listOfDocs.get(docId);
    }
    public String getDocPointer(String docId){
        if(!this.listOfDocs.containsKey(docId)){
            return "0";
        }
        return this.listOfDocsPointers.get(docId);
    }
    public boolean inTitle(String docId){
        return this.inTitle.contains(docId);
    }
    public boolean appearInDoc(String docId){
        return this.listOfDocs.containsKey(docId);
    }
    public int getNumberOfDocsToTerms(){
        return this.listOfDocs.size();
    }
    public ArrayList<String> docsId(){
        ArrayList<String> toReturn = new ArrayList<>();
        ArrayList<Map.Entry<String,Integer>> sortedDocs = new ArrayList<>(this.listOfDocs.entrySet());
        Collections.sort(sortedDocs, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if(o1.getValue() < o2.getValue()){
                    return 1;
                }
                if(o1.getValue() < o2.getValue()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        });

        for(int i = 0 ; i < sortedDocs.size() ; i++){
            toReturn.add(sortedDocs.get(i).getKey());
        }

        return toReturn;
    }
    public int getFreq(){
        return this.Freq;
    }

    public void changeNameToLowerCase(){
        this.name = this.name.toLowerCase();
    }
    public void changeNameToUpperCase() {
        this.name = this.name.toUpperCase();
    }

    @Override
    public String toString(){
        StringBuilder termToString = new StringBuilder();
        termToString.append(this.name+",");
        termToString.append(getNumberOfDocsToTerms()+",");
        termToString.append(this.Freq+",");
        for (String doc: this.listOfDocs.keySet() ) {
            termToString.append(doc+",");
            termToString.append(this.listOfDocs.get(doc)+",");
            termToString.append(inTitle(doc) + ",");
            termToString.append(this.listOfDocsPointers.get(doc)+",");
            termToString.append(this.listOfPossitionInDocs.get(doc) + ",#,");
        }
        return termToString.substring(0,termToString.length()-1) + "\n";
    }

    /**
     * This function accepts an Array of string representations Term
     * and updates the Term according to the information in the strings
     * @param termToString
     */
    public void revToString(String[] termToString){
        this.name = termToString[0];
        this.Freq += Integer.valueOf(termToString[2]);
        //termToString[3] == #
        for( int i = 3 ; i <termToString.length-4 ; i++){
            this.listOfDocs.put(termToString[i] , Integer.valueOf(termToString[i+1]));
            if(termToString[i+2].equals("true")){
                this.inTitle.add(termToString[i]);
            }
            //termToString[6] == false
            this.listOfDocsPointers.put(termToString[i] , termToString[i+3]);
            this.listOfPossitionInDocs.put(termToString[i] , termToString[i+4]);
            i+=5; // #
        }
    }


}