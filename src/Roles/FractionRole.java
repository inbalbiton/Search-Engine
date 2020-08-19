package Roles;

import java.util.ArrayList;

/**
 * our role for fraction - if its a number we write numberRole1/numberRole2
 *                       - if its a word we save it word1/word2
 */
public class FractionRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
        String finalRole = "";
        String tmpWord = "";
        ArrayList<String> tmp = new ArrayList<>();
        if(words != null){
            for(int i=0 ; i< words.size() ; i++){
                tmpWord = words.get(i);
                if(checkIfNumber(tmpWord)){
                    tmp.add(tmpWord);
                    finalRole += ConvertFormat(tmp);
                }
                else {
                    finalRole += tmpWord;
                }
            }
        }
        return finalRole;
    }
}
