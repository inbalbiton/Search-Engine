package Searcher;

import java.util.HashMap;
import java.util.Map;

public class NGramCorrectness {

    private int k;

    public NGramCorrectness(int k){
        this.k = k;
    }

    public Map<String , String[]> nGramsDistribution(Map<String , String[]> dictionary) {
        String distribution;
        System.out.println("Size Of Dictionary : " + dictionary.size());
        for (String term : dictionary.keySet()) {
            distribution = kGram(term, k);
            String[] tmp = dictionary.get(term);
            String[] newVal = new String[]{tmp[0], tmp[1], distribution};
            dictionary.replace(term, tmp, newVal);
        }
        return new HashMap<>(dictionary);
    }
    public String GetMostSimilarTerm(String term , Map<String , String[]> dictionary) {
        String nGramForTerm = kGram(term , this.k );
        double maxVal = 0;
        String mostSimilar = "";
        for(String termInDic : dictionary.keySet()){
            String val = dictionary.get(termInDic)[2];
            double[] result = checkGrams(nGramForTerm , val);
            double similarity = result[0] / result[1];
            if(similarity > maxVal){
                maxVal = similarity;
                mostSimilar = termInDic;
            }
        }
        return mostSimilar;

    }

    private String kGram(String term , int k){
        if(checkIfWord(term)){
            return "";
        }
        String result = "";
        for(int i = 0 ; i < term.length() - (k-1) ; i++){
            String kGram = term.substring(i , i+k);
            result += kGram + ",";
        }
        try{
            return result.substring(0 , result.length()-1);
        }catch(Exception e){
        }
        return "";
    }
    private double[] checkGrams(String term1 , String term2 ){
        String[] split1 = term1.split(",");
        String[] split2 = term2.split(",");

        int minimumLength = Math.min(split1.length , split2.length);
        double[] result = new double[2];

        for(int i = 0 ; i < minimumLength ; i++){
            if(split1[i].equalsIgnoreCase(split2[i])){
                result[0]++;
            }
        }

//        result[1] = split1.length + split2.length;
        result[1] = minimumLength - result[0];

        return result;
    }
    private boolean checkIfWord(String term) {
        for(int i = 0 ; i < term.length() ; i++){
            char c = term.charAt(i);
            if( !('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z')){
                return false;
            }
        }
        return true;
    }


}
