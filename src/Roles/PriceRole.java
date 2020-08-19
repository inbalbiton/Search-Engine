package Roles;

import Roles.AParserRole;

import java.util.ArrayList;

public class PriceRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
        ArrayList<String> convert = new ArrayList<>();
        String finalTerm = "";
        //only number
        if(words.size() == 1) {
            convert.add(words.get(0));
            finalTerm = ConvertFormat(convert);
        }
        //number with size - m OR bn
        if(words.size() == 2){
            convert.add(words.get(0));
            convert.add(words.get(1));
            finalTerm = ConvertFormat(convert);
        }
        return finalTerm;

    }

    @Override
    /*
    In This Form we want only number under 1 M or more than 1 M
     */
    public String ConvertFormat(ArrayList<String> words) {

        double price = GetTheValueOfTheNumber(words.get(0));
        String sPrice = "";
        String finalTerm = "";
        if(words.size() == 1){
            if(price >= 1000000){
                price = price / 1000000.0;
                price = doubleFormat(price);
                sPrice = stringFormat(price);
                finalTerm += sPrice + " M Dollars";
            }
            else{
                price = doubleFormat(price);
                sPrice = stringFormat(price);
                finalTerm += sPrice + " Dollars";
            }
            return finalTerm;
        }
        if(words.get(1).equals("billion") || words.get(1).equals("bn")){
            price = price * 1000.0;
        }
        if(words.get(1).equals("million") || words.get(1).equals("m")){
            price = price / 1000000.0;
        }
        if( words.get(1).equals("trillion")){
            price = price * 1000000.0;
        }
        price = doubleFormat(price);
        sPrice = stringFormat(price);
        finalTerm += sPrice + "M Dollars";
        return finalTerm;
    }
}
