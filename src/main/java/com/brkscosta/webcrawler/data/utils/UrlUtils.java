package com.brkscosta.webcrawler.data.utils;

import java.net.URL;

public class UrlUtils {

    public static boolean isUrlValid(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
