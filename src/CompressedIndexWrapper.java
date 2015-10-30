import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper class for compressing the dictionary index containing term and
 * document frequncy
 * 
 * @author Akshay
 */
public class CompressedIndexWrapper implements Serializable {

	private static final long serialVersionUID = -2907947936815696427L;
	String term;
	Integer documentFrequency;
	List<CompressedPostingListWrapper> compressedPostingList;

	public CompressedIndexWrapper(String uncompressedTerm, int docFreq,
			LinkedList<CompressedPostingListWrapper> cList) {
		this.term=uncompressedTerm;
		this.documentFrequency=docFreq;
		this.compressedPostingList=cList;
	}

}

/**
 * Wrapper class for compressed posting list,having compressed document ID and
 * term frequency
 * 
 * @author Akshay
 *
 */
class CompressedPostingListWrapper implements Serializable {

	private static final long serialVersionUID = -2907947936815696427L;
	byte[] docID;
	byte[] termFrequencyPerDocument;

	public CompressedPostingListWrapper(byte[] gapByte, byte[] termFreqByte) {
		this.docID = gapByte;
		this.termFrequencyPerDocument = termFreqByte;
	}
	
	public String toString() {
		return "*** Compressed Posting List - Gamma|Doc Id :" + Arrays.toString(docID) + "Delta|termperdoc"
				+ Arrays.toString(termFrequencyPerDocument);
	}

}
