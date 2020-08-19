package Roles;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class is responsible for execute a Role on a number of words that represent a particular Term,
 * if it is a word that represents a number There are functions that define a
 * uniform wording for saving a number in a corpus
 */
public abstract class AParserRole implements IParserRole {

    /**
     *Eexcute a Role
     * @param words
     * @return
     */
    public abstract String startRole(ArrayList<String> words);

    /**
     * Get String of number and return the number
     * @param number
     * @return
     */
    public double GetTheValueOfTheNumber(String number){
        double finalNum = 0;
        if(number.contains(",")){
            String[] splitByComma = number.split(",");
            int ten = 1;
            for (int i = splitByComma.length - 1; i >= 0; i--) {
                double num = Double.parseDouble(splitByComma[i]);
                finalNum += (num * ten);
                ten *= 1000;
            }
        }
        else{
                finalNum = Double.parseDouble(number);
            }

        return finalNum;
    }

    /**
     * Get number And return the number in this format  -  d.ddd
     * @param number
     * @return
     */
    public double doubleFormat(double number) {
        DecimalFormat df = new DecimalFormat("#.###");
        String dx = df.format(number);
        number = Double.valueOf(dx);
        return number;
    }

    /**
     * Get number and return string of the number
     * @param finalNum
     * @return
     */
    public String stringFormat(double finalNum) {
        String finalNumString = "";
        if (finalNum == (int) finalNum) {
            finalNumString += String.valueOf((int) finalNum);
        } else {
            finalNumString += String.valueOf(finalNum);
        }
        return finalNumString;
    }

    /**
     * Get List of Words and return number Format
     * numberK
     * numberM
     * numberB
     * @param words
     * @return
     */
    public String ConvertFormat(ArrayList<String> words) {
        double number = 0;
        String finalTerm = "";
        if (words != null) {
            String finalNumString = "";
            if (words.size() >= 1) {
                number = GetTheValueOfTheNumber(words.get(0));
            }
            if (number >= 1000 && number < 1000000) {
                number = number / 1000;
                number = doubleFormat(number);
                finalNumString = stringFormat(number);
                finalTerm += finalNumString + "K";
            }
            else if (number >= 1000000 && number < 1000000000.0) {
                number = number / 1000;
                number = doubleFormat(number);
                finalNumString = stringFormat(number);
                finalTerm += finalNumString + "M";
            }
            else if (number >= 1000000000.0) {
                number = number / 1000;
                number = doubleFormat(number);
                finalNumString = stringFormat(number);
                finalTerm += finalNumString + "B";
            } else {
                finalNumString = stringFormat(number);
                finalTerm += finalNumString;
            }
            if (words.size() >= 2) {
                if (words.get(1).equals("Billion")) {
                    finalTerm +=  "B";
                } else if (words.get(1).equals("Million")) {
                    finalTerm += "M";
                } else if (words.get(1).equals("Thousand")) {
                    finalTerm +=  "K";
                }
            }
        }
        return finalTerm;

    }

    /**
     * Check if word Start with letter
     * @param word
     * @return
     */
    public boolean checkIfNumber(String word){
        if(word.length()>0){
            if(!(word.charAt(0) >= '1' && word.charAt(0) <= '9')){
                return false;
            }
            boolean onlyOneDot = true;
            for (int i=0 ; i<word.length() ; i++){
                if( onlyOneDot && (word.charAt(i) == '.') ){
                    onlyOneDot=false;
                }
                else if(!onlyOneDot && (word.charAt(i)=='.') ){
                    return false;
                }
                if(!((word.charAt(i) >= '0' && word.charAt(i) <= '9') || (word.charAt(i) == ',') || (word.charAt(i) == '/'))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
