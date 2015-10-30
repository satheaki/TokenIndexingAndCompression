import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Program for tokenizing and stemming words from Cranfield Database
 * 
 * @author Akshay
 *
 */
public class StemParser {

	/* HashMap containing tokens as key with their frequency as value */
	private static TreeMap<String, Integer> stemmedTokenMap;
	private static Stemmer stemmer;

	/**
	 * 
	 */
	public StemParser() {
		stemmedTokenMap = new TreeMap<String, Integer>();
		stemmer = new Stemmer();
	}

	/**
	 * Method for parsing each file from the Cranfield Database
	 * 
	 * @param file
	 *            :File object
	 */
	public TreeMap<String, Integer> parseSingleFile(File file,
			HashSet<String> stopWords) {
		String token = "";
		try {
			stemmedTokenMap = new TreeMap<String, Integer>();
			Scanner wordScanner = new Scanner(file);
			while (wordScanner.hasNext()) {
				token = wordScanner.next();
				token = token.replaceAll("\\<.*?>", "").replaceAll("\\.", "")
						.trim().toLowerCase();

				// TODO:check for regular expression condition
				token = token.replaceAll("[^a-zA-Z]+", "");
				if (stopWords.contains(token)) {
					continue;
				}

				if (!token.isEmpty()) {
					if (token.contains("-")) {
						parseHyphenatedTokens(token);
					} else if (token.contains("\'")) {
						parsePosessiveTokens(token);
					} else if (token.contains("_")) {
						parseUnderscoreTokens(token);
					} else {
						parseSimpleToken(token);
					}
				}
			}
			wordScanner.close();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		}

		return stemmedTokenMap;
	}

	/**
	 * Method for Parsing token and calculating token frequency
	 * 
	 * @param token
	 *            :Single token from each file
	 */
	private static void parseSimpleToken(String token) {
		String stemmedWord = "";
		stemmer.add(token.toCharArray(), token.length());
		stemmer.stem();
		stemmedWord = stemmer.toString();
		if (!stemmedWord.isEmpty()) {
			if (stemmedTokenMap.containsKey(stemmedWord)) {
				stemmedTokenMap.put(stemmedWord,
						stemmedTokenMap.get(stemmedWord) + 1);
			} else {
				stemmedTokenMap.put(stemmedWord, 1);
			}
		}
	}

	/**
	 * Method for parsing tokens having an underscore
	 *
	 * @param token
	 *            :Single token from each file
	 */
	private static void parseUnderscoreTokens(String token) {
		String[] tokenSplitter = null;
		tokenSplitter = token.split("_");
		for (String splittedToken : tokenSplitter) {
			parseSimpleToken(splittedToken);
		}
	}

	/**
	 * Method for parsing tokens containing apostrophe's
	 *
	 * @param token
	 *            :Single token from each file
	 */
	private static void parsePosessiveTokens(String token) {
		if (token.startsWith("\'")) {
			token = token.substring(1, token.length());
			parseSimpleToken(token);
		} else if (token.endsWith("\'")) {
			token = token.substring(0, token.length() - 1);
			parseSimpleToken(token);
		} else if (token.endsWith("\'s")) {
			int n = token.indexOf("\'");
			token = token.substring(0, n - 1);
			parseSimpleToken(token);
		} else if (token.endsWith("\'es")) {
			token = token.substring(0, token.length() - 3);
			parseSimpleToken(token);
		} else {
			token = token.replaceAll("'", "");
			parseSimpleToken(token);
		}
	}

	/**
	 * Method for parsing tokens with hyphen
	 *
	 * @param hyphenToken
	 *            :Single token from each file
	 */
	private static void parseHyphenatedTokens(String hyphenToken) {
		String[] tokenSplitter = null;
		tokenSplitter = hyphenToken.split("-");
		for (String splittedToken : tokenSplitter) {
			parseSimpleToken(splittedToken);
		}
	}

}
