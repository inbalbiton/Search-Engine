package Index;

import java.util.HashMap;
import java.util.Map;

public class Doc {
    private String fileId;
    private int sizeOfDoc;
    private int maxFreqTerm;
    private String mostFreqTerm;
    private int numberOfUniqeTerms;
    private HashMap<String,Integer> entitiesFreq;

    public Doc(){
        this.entitiesFreq = new HashMap<>();
        this.fileId = "";
        this.mostFreqTerm = "";
    }

    /**
     * The class constructor get the following parameters and creates a document object
     * that save the relevant information to us
     * @param id of the doc
     * @param maxFreqTerm The number of most frequent word appearance in a document
     * @param numberOfUniqeTerms
     * @param size the length of the document without Stop Words
     * @param mostFreqTerm The most frequent word in the document
     */
    public Doc(String id , int maxFreqTerm , int numberOfUniqeTerms, int size , String mostFreqTerm){
        this.fileId = id;
        this.maxFreqTerm = maxFreqTerm;
        this.numberOfUniqeTerms = numberOfUniqeTerms;
        this.sizeOfDoc = size;
        this.mostFreqTerm = mostFreqTerm;
    }

    /**
     *These functions are gateways to the class fields that retain the information on the document
     * @return
     */
    public String getFileId(){
        return fileId;
    }
    public int getMaxFreqTerm(){
        return this.maxFreqTerm;
    }
    public String getMostFreqTerm(){
        return this.mostFreqTerm;
    }
    public int getSizeOfDoc(){ return this.sizeOfDoc; }

    public void addEntitiesToDoc(Map<String,Integer> enties){
        this.entitiesFreq = new HashMap<>(enties);
    }


    @Override
    public String toString(){
        StringBuilder docToString = new StringBuilder();

        docToString.append(this.fileId + "," );//+
        docToString.append(this.sizeOfDoc + ",");// +
        docToString.append(this.maxFreqTerm + ",") ;//+
        docToString.append(this.numberOfUniqeTerms + "#");
        if(this.entitiesFreq != null){
            for(String enty : this.entitiesFreq.keySet()){
                docToString.append(enty+",");
                docToString.append(this.entitiesFreq.get(enty)+"#");
            }
        }
        return docToString + "\n";
    }

    public void revToString(String[] doc){
        String[] metaData = doc[0].split(",");
        this.fileId = metaData[0];
        this.sizeOfDoc = Integer.valueOf(metaData[1]);
        this.maxFreqTerm = Integer.valueOf(metaData[2]);
        this.numberOfUniqeTerms = Integer.valueOf(metaData[3]);
        for(int i = 1 ; i < doc.length ; i++){
            String[] entyInfo = doc[i].split(",");
            this.entitiesFreq.put(entyInfo[0] , Integer.valueOf(entyInfo[1]) );
        }

    }
    public HashMap<String , Integer> getEnteties(){
        return new HashMap<>(this.entitiesFreq);
    }
}