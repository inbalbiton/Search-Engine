package Roles;

import Roles.AParserRole;

import java.util.ArrayList;

public class RangeRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
        if(words.size() == 0){
            return "";
        }
        String finalTerm = "";
        for(int i=0 ; i<words.size()-1 ; i++){

            finalTerm+=words.get(i)+"-";
        }
        return finalTerm+words.get(words.size()-1) ;
    }
}
