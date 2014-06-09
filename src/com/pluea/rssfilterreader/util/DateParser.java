package com.pluea.rssfilterreader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {

	public static int getYear(String dateString) {
		String regex = "\\d{4}";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		return Integer.valueOf(matcher.group());
    	}
    	return -1;
	}
	
	public static int getMonth(String dateString) {
		String regex = "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		String monthString = matcher.group();
    		if(monthString.compareTo("Jan") == 0) {
    			return 1;
    		}else if(monthString.compareTo("Feb") == 0) {
    			return 2;
    		}else if(monthString.compareTo("Mar") == 0) {
    			return 3;
    		}else if(monthString.compareTo("Apr") == 0) {
    			return 4;
    		}else if(monthString.compareTo("May") == 0) {
    			return 5;
    		}else if(monthString.compareTo("Jun") == 0) {
    			return 6;
    		}else if(monthString.compareTo("Jul") == 0) {
    			return 7;
    		}else if(monthString.compareTo("Aug") == 0) {
    			return 8;
    		}else if(monthString.compareTo("Sep") == 0) {
    			return 9;
    		}else if(monthString.compareTo("Oct") == 0) {
    			return 10;
    		}else if(monthString.compareTo("Nov") == 0) {
    			return 11;
    		}else if(monthString.compareTo("Dec") == 0) {
    			return 12;
    		}
    	}
    	return -1;
	}
	
	public static int getDay(String dateString) {
		String regex = "\\d{2}";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		return Integer.valueOf(matcher.group());
    	}
    	return -1;
	}
	
	public static int getHour(String dateString) {
		String regex = "(\\d{2}):\\d{2}:\\d{2}";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		return Integer.valueOf(matcher.group(1));
    	}
    	return -1;
	}
	
	public static int getMinute(String dateString) {
		String regex = "\\d{2}:(\\d{2}):\\d{2}";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		return Integer.valueOf(matcher.group(1));
    	}
    	return -1;
	}
	
	public static int getSec(String dateString) {
		String regex = "\\d{2}:\\d{2}:(\\d{2})";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(dateString);
    	if(matcher.find()) {
    		return Integer.valueOf(matcher.group(1));
    	}
    	return -1;
	}
}
