import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Driver class for index creation and compression.
 * 
 * @author Akshay
 *
 */
public class StemIndexProcesser {
	private static int phase = 0;
	private static long startTime, endTime, elapsedTime;
	private static HashSet<String> stopWordsSet = new HashSet<String>();
	private static TreeMap<String, IndexWrapper> uncompressedIndexMap = new TreeMap<>();
	private static List<List<CompressedPostingListWrapper>> compressedPostingsList = new ArrayList<List<CompressedPostingListWrapper>>();

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String stopWordFilePath = "";
		String dataSetFilePath = "";
		ArrayList<String> tokenToTestList = new ArrayList<>();
		if (args.length < 2) {
			System.out
					.println("Incorrect input format.Enter <stopwords,dataset> file path");
		}
		stopWordFilePath = args[0];
		dataSetFilePath = args[1];

		// stopWordFilePath =
		// "C:\\My files\\Information Retrieval\\stopwordlist.txt";
		// dataSetFilePath = "C:\\My files\\Information Retrieval\\Test Cran";
		// dataSetFilePath =
		// "C:\\My files\\Information Retrieval\\CranfieldDatabase";

		parseStopWords(stopWordFilePath);

		StemIndexBuilder iBuilder = new StemIndexBuilder(stopWordsSet,
				dataSetFilePath);
		timer();
		uncompressedIndexMap = iBuilder.buildDictionaryIndex();
		timer();

		writeToFile();

		// File uncompressedV2File = new File(
		// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\UncompressedV2.txt");

		File uncompressedV2File = new File("UncompressedV2.txt");
		System.out.println("\nSize of index Version II uncompressed:"
				+ uncompressedV2File.length());

		System.out.println("\nNumber of inverted lists in version II:"
				+ uncompressedIndexMap.size());

		tokenToTestList.add("reynold");
		tokenToTestList.add("nasa");
		tokenToTestList.add("prandtl");
		tokenToTestList.add("flow");
		tokenToTestList.add("pressur");
		tokenToTestList.add("boundari");
		tokenToTestList.add("shock");
		String invertedListDic = new String();
		for (String token : tokenToTestList) {
			if (uncompressedIndexMap.containsKey(token)) {
				System.out.println("\nTerm:" + token);
				IndexWrapper iWrapper = uncompressedIndexMap.get(token);
				DataOutputStream dosTest = new DataOutputStream(
						new FileOutputStream("stemWordsToTestFile.txt"));

				for (PostingListWrapper plw : iWrapper.postingList) {
					dosTest.writeInt(plw.docId);
					dosTest.writeInt(plw.termFrequencyPerDocument);
				}
				// File wordsToTestFile = new File(
				// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\stemWordsToTestFile.txt");

				File wordsToTestFile = new File("stemWordsToTestFile.txt");
				System.out.println("Df of Term:" + iWrapper.documentFrequency
						+ "\tTf of term:" + iWrapper.totalTermFrequency
						+ "\tInverted lists length(bytes):"
						+ wordsToTestFile.length());
				dosTest.flush();
				dosTest.close();
			}

		}

		compressedPostingsList = PostingListCompressor
				.buildCompressedIndex(uncompressedIndexMap);

		String[] tokenArray = uncompressedIndexMap.keySet().toArray(
				new String[0]);

		int checkFlag = 1;
		DictionaryCompressor dc = new DictionaryCompressor();
		dc.frontCoding(compressedPostingsList, tokenArray,
				uncompressedIndexMap, checkFlag);
		// dc.frontCoding(compressedPostingsList, tokenArray);

		// File uncompressedFile = new File(
		// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\compressedVII.txt");

		File uncompressedFile = new File("compressedVII.txt");
		System.out.println("\nSize of Version II compressed:"
				+ uncompressedFile.length());
		System.out.println("\n");
	}

	private static void writeToFile() throws IOException {
		String dictionary = new String();
		DataOutputStream out1 = new DataOutputStream(new FileOutputStream(
				"UncompressedV2.txt"));

		for (Entry<String, IndexWrapper> entry : uncompressedIndexMap
				.entrySet()) {
			IndexWrapper e;
			dictionary = dictionary + "\n " + entry.getKey()
					+"\t"+ entry.getValue().documentFrequency + " ";
			e = uncompressedIndexMap.get(entry.getKey());
			for (PostingListWrapper pl : e.postingList) {
				dictionary = dictionary + "\t" + pl.docId + "-->"
						+ pl.termFrequencyPerDocument;
			}

			e = uncompressedIndexMap.get(entry.getKey());
			out1.writeBytes(entry.getKey() + "[");
			out1.writeInt(e.documentFrequency);
			for (PostingListWrapper pl : e.postingList) {
				out1.writeInt(pl.docId);
				out1.writeBytes(":");
				out1.writeInt(pl.termFrequencyPerDocument);

			}
			out1.writeBytes("]");

		}
		out1.flush();
		out1.close();

		DataOutputStream dict = new DataOutputStream(new FileOutputStream(
				"stemdictionary.txt"));
		dict.writeBytes(dictionary);
		dict.close();

	}

	/**
	 * Method for calculating the total time required for the program
	 */
	public static void timer() {
		if (phase == 0) {
			startTime = System.currentTimeMillis();
			phase = 1;
		} else {
			endTime = System.currentTimeMillis();
			elapsedTime = endTime - startTime;
			System.out.println("\nTime taken to build the index: " + elapsedTime + " msec.");
			phase = 0;
		}
	}

	/**
	 * 
	 * @param stopWordFilePath
	 */
	private static void parseStopWords(String stopWordFilePath) {
		String[] words;
		String stop = "";
		try {
			// TODO:Use Scanner

			BufferedReader buffReader = new BufferedReader(new FileReader(
					stopWordFilePath));
			for (String eachline = buffReader.readLine().toLowerCase(); eachline != null; eachline = buffReader
					.readLine()) {
				// words = eachline.split("");
				// for (String sword : eachline) {
				stopWordsSet.add(eachline);
				// }
			}
			buffReader.close();

		} catch (Exception e) {

		}

	}

}
