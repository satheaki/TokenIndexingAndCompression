import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public class DictionaryCompressor {

	public static int currentIndexPointer = 0;

	/**
	 * Method for performing front coding compression on tokens and stems
	 * 
	 * @param compressedPostingsList
	 *            :ArrayList of lists containing the postings list
	 * @param tokenArray
	 *            :Array containing all the tokens and stems in the collection
	 * @param uncompressedIndexMap 
	 * @param uncompressedIndexMap
	 * @param checkFlag 
	 * @return
	 * @return
	 */
	public void frontCoding(
			List<List<CompressedPostingListWrapper>> compressedPostingsList,
			String[] tokenArray, TreeMap<String, IndexWrapper> uncompressedIndexMap, int checkFlag) throws IOException {
		DataOutputStream cdosl = new DataOutputStream(new FileOutputStream(
				"compressedVI.txt"));
		DataOutputStream cdosst = new DataOutputStream(new FileOutputStream(
				"compressedVII.txt"));
		while (currentIndexPointer < tokenArray.length) {
			int blockCodingChecker = 0;
			StringBuffer sb = new StringBuffer();
			while (blockCodingChecker < 8) {
				// if (currentIndexPointer < tokenArray.length) {
				int commonPrefixLength = 0;
				int prevIndexPointer = currentIndexPointer;

				String prefix = sameCharacterChecker(tokenArray,
						prevIndexPointer);
				commonPrefixLength = prefix.length();
				if (commonPrefixLength == 0) {
					sb.append(tokenArray[currentIndexPointer - 1].length())
							.append(tokenArray[currentIndexPointer - 1]);
					blockCodingChecker++;
				} else {
					int size = currentIndexPointer - prevIndexPointer;
					blockCodingChecker = size + blockCodingChecker;
					sb.append(commonPrefixLength)
							.append(tokenArray[prevIndexPointer].substring(0,
									commonPrefixLength)).append("*");
					for (int j = 0; j < size; j++) {
						int prefixSize = (tokenArray[prevIndexPointer].length())
								- commonPrefixLength;
						sb.append(prefixSize)
								.append(tokenArray[prevIndexPointer].substring(
										commonPrefixLength,
										tokenArray[prevIndexPointer].length()))
								.append("^");
						prevIndexPointer++;

					}

				}
				if (currentIndexPointer + 1 == tokenArray.length) {
					currentIndexPointer = currentIndexPointer + 1;
					break;
				}

			}
			if(checkFlag==0)
			cdosl.writeBytes(sb.toString());
			else if(checkFlag==1)
				cdosst.writeBytes(sb.toString());

		}
		if(checkFlag==0){
		int i = 0;
		for (List<CompressedPostingListWrapper> clist : compressedPostingsList) {
			cdosl.writeBytes("(");
			for (CompressedPostingListWrapper cpl : clist) {
				cdosl.write(cpl.docID);
				cdosl.write(cpl.termFrequencyPerDocument);
			}
			 cdosl.writeInt(uncompressedIndexMap.get(tokenArray[i++]).documentFrequency);
			 cdosl.writeBytes(")");
		}
		
		cdosl.flush();
		cdosl.close();
		}
		else if(checkFlag==1){
			int i = 0;
			for (List<CompressedPostingListWrapper> clist : compressedPostingsList) {
				cdosst.writeBytes("(");
				for (CompressedPostingListWrapper cpl : clist) {
					cdosst.write(cpl.docID);
					cdosst.write(cpl.termFrequencyPerDocument);
				}
				 cdosst.writeInt(uncompressedIndexMap.get(tokenArray[i++]).documentFrequency);
				 cdosst.writeBytes(")");
			}
			
			cdosst.flush();
			cdosst.close();
		}
	}

	/**
	 * Method for comparing each character of two tokens and calculate the
	 * longest matching string
	 * 
	 * @param tokenArray
	 *            :Array containing all the tokens and stems in the collection
	 * @param position
	 *            :Position of token in the array of tokens
	 * @return:Returns a string of longest matching string
	 */
	private String sameCharacterChecker(String[] tokenArray, int position) {
		String longestPrefix = "";
		int breakFlag = 0;
		if (currentIndexPointer < tokenArray.length) {
			if (tokenArray[position].length() > 0) {
				longestPrefix = tokenArray[position];
				currentIndexPointer++;
			}
		}
		for (int i = position + 1; i < position + 8 && breakFlag != 1; i++) {
			if (i < tokenArray.length) {
				String nextToken = tokenArray[i];
				int j = 0;
				for (; j < Math.min(longestPrefix.length(),
						tokenArray[i].length()); j++) {
					if (longestPrefix.charAt(j) != nextToken.charAt(j)) {
						breakFlag = 1;
						break;
					}
				}
				currentIndexPointer++;
				longestPrefix = tokenArray[i].substring(0, j);
			}

		}

		return longestPrefix;
	}

}
