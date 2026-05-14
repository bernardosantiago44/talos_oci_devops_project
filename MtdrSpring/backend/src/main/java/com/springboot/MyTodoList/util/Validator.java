package com.springboot.MyTodoList.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$");

    public static boolean isValidHexColor(String color) {
        if (color == null) return false;
        return HEX_COLOR_PATTERN.matcher(color).matches();
    }
}
