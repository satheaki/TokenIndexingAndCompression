import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Class for compressing the dictionary index and posting list
 * 
 * @author Akshay
 */
public class PostingListCompressor {

	public static List<List<CompressedPostingListWrapper>> compressedList = null;

	public static List<List<CompressedPostingListWrapper>> buildCompressedIndex(
			TreeMap<String, IndexWrapper> uncompressedIndexMap) {

		compressedList = new ArrayList<List<CompressedPostingListWrapper>>();

		for (String term : uncompressedIndexMap.keySet()) {
			int prevDocID = 0;
			IndexWrapper iWrapper = uncompressedIndexMap.get(term);
			LinkedList<CompressedPostingListWrapper> compressedPostingList = new LinkedList<>();

			for (PostingListWrapper plist : iWrapper.postingList) {
				int gap = plist.docId - prevDocID;
				int doctf = plist.termFrequencyPerDocument;
				prevDocID = plist.docId;
				byte[] gammaDocID = getByteArray(getGammaCode(gap).trim());
				byte[] deltaDocTF = getByteArray(getDeltaCode(doctf).trim());

				CompressedPostingListWrapper cpWrapper = new CompressedPostingListWrapper(
						gammaDocID, deltaDocTF);
				compressedPostingList.add(cpWrapper);

			}
			compressedList.add(compressedPostingList);
		}
		return compressedList;

	}

	/*
	 * private static TreeMap<String, CompressedIndexWrapper>
	 * compressedIndexMap;
	 *//**
	 * Method to compress the dictionary index and posting list
	 * 
	 * @param uncompressedIndexMap
	 *            :Uncompressed index map having term against document frequency
	 *            and posting list
	 * @return :Returns a compressed index map
	 */
	
	/**
	 * Method to calculate the delta code
	 * 
	 * @param doctf
	 *            :Term frequency in each document
	 * @return :Returns delta code as string
	 */
	private static String getDeltaCode(int doctf) {
		String binaryNumber = Integer.toBinaryString(doctf);
		int lengthinBinary = binaryNumber.length();
		String offset = binaryNumber.substring(1);
		String gammaCode = getGammaCode(lengthinBinary);
		String deltaCode = gammaCode + offset;
		return deltaCode;
	}

	/**
	 * Method for retrieving the byteArray of the given code
	 * 
	 * @param code
	 *            :gamma code or delta code
	 * @return :Returns a byte array for the given code
	 */
	private static byte[] getByteArray(String code) {
		BitSet b = new BitSet(code.length());
		for (int i = 0; i < code.length(); i++) {
			if (code.charAt(i) == '1') {
				b.set(i, true);
			} else {
				b.set(i, false);
			}

		}
		return b.toByteArray();
	}

	/**
	 * Method to calculate the gamma code.
	 * 
	 * @param gap
	 *            :Gap between current document and previous document
	 * @return :Returns gamma code in the form of string
	 */
	private static String getGammaCode(int gap) {
		String binaryNumber = Integer.toBinaryString(gap);
		String offset = binaryNumber.substring(1);
		int length = offset.length();
		String unary = getUnary(length);
		String gammaCode = unary + offset;
		return gammaCode;
	}

	/**
	 * Method to represent length of offset in terms of binary one's
	 * 
	 * @param length
	 *            :length of offset
	 * @return : Returns a string of one's appended with 0
	 */
	private static String getUnary(int length) {
		String unary = "";
		while (length > 0) {
			unary = unary + "1";
			length--;
		}
		unary = unary + "0";
		return unary;
	}

	}
