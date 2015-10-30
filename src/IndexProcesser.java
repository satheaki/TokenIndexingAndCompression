import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class for index creation and compression.
 * 
 * @author Akshay
 *
 */
public class IndexProcesser {
	private static int phase = 0;
	private static long startTime, endTime, elapsedTime;
	/* A set containing all the stop words */
	private static HashSet<String> stopWordsSet = new HashSet<String>();
	/* Uncompressed map having term against df and posting list mapping */
	private static TreeMap<String, IndexWrapper> uncompressedIndexMap = new TreeMap<>();
	private static TreeMap<String, CompressedIndexWrapper> compressedIndexMap = new TreeMap<>();
	private static List<List<CompressedPostingListWrapper>> compressedPostingsList = new ArrayList<List<CompressedPostingListWrapper>>();

	/**
	 * 
	 * @param args
	 * @throws IOException
	 *             :Input/Output Exception
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

		IndexBuilder iBuilder = new IndexBuilder(stopWordsSet, dataSetFilePath);
		timer();
		uncompressedIndexMap = iBuilder.buildDictionaryIndex();
		timer();

		writeToFile();

		// File uncompressedVIFile = new File(
		// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\UncompressedV1.txt");

		File uncompressedVIFile = new File("UncompressedV1.txt");
		System.out.println("\nSize of index Version I uncompressed:"
				+ uncompressedVIFile.length());

		System.out.println("\nNumber of inverted lists in index Version I:"
				+ uncompressedIndexMap.size());

		tokenToTestList.add("reynold");
		tokenToTestList.add("nasa");
		tokenToTestList.add("prandtl");
		tokenToTestList.add("flow");
		tokenToTestList.add("pressure");
		tokenToTestList.add("boundary");
		tokenToTestList.add("shock");
		String invertedListDic = new String();
		for (String token : tokenToTestList) {
			if (uncompressedIndexMap.containsKey(token)) {
				System.out.println("\nTerm:" + token);
				IndexWrapper iWrapper = uncompressedIndexMap.get(token);
				DataOutputStream dosTest = new DataOutputStream(
						new FileOutputStream("wordsToTestFile.txt"));

				for (PostingListWrapper plw : iWrapper.postingList) {
					dosTest.write(plw.termFrequencyPerDocument);
					dosTest.writeInt(plw.docId);
					// dosTest.writeInt(plw.termFrequencyPerDocument);
				}
				// File wordsToTestFile = new File(
				// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\wordsToTestFile.txt");

				File wordsToTestFile = new File("wordsToTestFile.txt");
				System.out.println("Df of Term:" + iWrapper.documentFrequency
						+ "\tTf of term:" + iWrapper.totalTermFrequency
						+ "\tInverted lists length(bytes):"
						+ wordsToTestFile.length());
			}
		}

		compressedPostingsList = PostingListCompressor
				.buildCompressedIndex(uncompressedIndexMap);
		/*
		 * DataOutputStream dosc = new DataOutputStream(new FileOutputStream(
		 * "compressedPostingsV1.txt")); for (List<CompressedPostingListWrapper>
		 * clist : compressedPostingsList) { for (CompressedPostingListWrapper
		 * cpl : clist) { dosc.write(cpl.docID);
		 * dosc.write(cpl.termFrequencyPerDocument); } }
		 */

		String[] tokenArray = uncompressedIndexMap.keySet().toArray(
				new String[0]);

		int checkFlag = 0;
		DictionaryCompressor dc = new DictionaryCompressor();
		dc.frontCoding(compressedPostingsList, tokenArray,
				uncompressedIndexMap, checkFlag);
		// dc.frontCoding(compressedPostingsList, tokenArray);

		// File uncompressedFile = new File(
		// "C:\\My files\\Eclipse Workspace\\TokenIndexingAndCompression\\compressedVI.txt");

		File uncompressedFile = new File("compressedVI.txt");
		System.out.println("\nSize of Version I compressed:"
				+ uncompressedFile.length());
		System.out.println("\n");
		/*
		 * int i=0; for (List<CompressedPostingListWrapper> clist :
		 * compressedPostingsList) { dos.writeBytes("("); for
		 * (CompressedPostingListWrapper cpl : clist) { dos.write(cpl.docID);
		 * dos.write(cpl.termFrequencyPerDocument); }
		 * dos.writeInt(uncompressedIndexMap
		 * .get(tokenArray[i++]).documentFrequency); dos.writeBytes(")"); }
		 * dos.flush(); dos.close();
		 */
		// writecompressedDataToFile();
	}

	/**
	 * Method for writing data of uncompressed dictionary index to a file
	 * 
	 * @throws IOException
	 *             :throws I/O exception
	 */
	private static void writeToFile() throws IOException {
		String dictionary = new String();
		DataOutputStream out1 = new DataOutputStream(new FileOutputStream(
				"UncompressedV1.txt"));

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

		}
		DataOutputStream dict = new DataOutputStream(new FileOutputStream(
				"dictionary.txt"));
		dict.writeBytes(dictionary);
		dict.close();

		out1.flush();
		out1.close();

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
	 * Method for parsing file containing stop words and storing the stop words
	 * in a Set
	 * 
	 * @param stopWordFilePath
	 *            :Path of file containing stopwords
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
