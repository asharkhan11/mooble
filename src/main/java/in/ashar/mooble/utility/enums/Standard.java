package in.ashar.mooble.utility.enums;

import lombok.Getter;

@Getter
public enum Standard {

    FIRST("1st"),
    SECOND("2nd"),
    THIRD("3rd"),
    FOURTH("4th"),
    FIFTH("5th"),
    SIXTH("6th"),
    SEVENTH("7th"),
    EIGHTH("8th"),
    NINTH("9th"),
    TENTH("10th"),
    ELEVENTH("11th"),
    TWELFTH("12th");

    private final String standard;

    Standard(String s) {
        standard = s;
    }

}
