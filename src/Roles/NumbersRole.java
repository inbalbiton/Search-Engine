package Roles;

import Roles.AParserRole;

import java.util.ArrayList;

public class NumbersRole extends AParserRole {
    @Override
    public String startRole(ArrayList<String> words) {
        return ConvertFormat(words);
    }
}
