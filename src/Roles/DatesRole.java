package Roles;

import Roles.AParserRole;

import java.util.ArrayList;

/**
 * this role class gets in the input words[ day , month ]
 * or words [ month , year ]
 * or words [ day , month , year ]
 * and return it to month-day or year-month or day-month-year pattern
 */
public class DatesRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
        String sDate = "";
        if(words.get(0).length() == 1){
            sDate = words.get(1) + "-0" + words.get(0);
        }
        else{
            sDate = words.get(1) + "-" + words.get(0);
        }
        if(words.size() == 3 ){
            return words.get(2) + "-" + sDate;
        }
        return sDate;
    }
}
