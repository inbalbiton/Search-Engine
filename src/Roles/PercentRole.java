package Roles;

import Roles.AParserRole;

import java.util.ArrayList;

public class PercentRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
            //only number
        return words.get(0)+"%";
    }
}
