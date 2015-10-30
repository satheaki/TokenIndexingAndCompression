import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * 
 * @author Akshay
 *
 */
public class StemIndexBuilder {

	private HashSet<String> stopWordsSet = new HashSet<>();
	String cranfieldDataSetPath = "";
	TreeMap<String, IndexWrapper> indexerMap;
	TreeMap<Integer, DocumentInfoWrapper> documentInfoMap;

	/**
	 * 
	 * @param stopWordsSet
	 * @param dataSetFilePath
	 */
	public StemIndexBuilder(HashSet<String> stopWordsSet, String dataSetFilePath) {
		this.stopWordsSet = stopWordsSet;
		this.cranfieldDataSetPath = dataSetFilePath;
		indexerMap = new TreeMap<String, IndexWrapper>();
		documentInfoMap=new TreeMap<>();
	}

	/**
	 * 
	 * @return
	 */
	
	public TreeMap<String, IndexWrapper> buildDictionaryIndex() {
		TreeMap<String, Integer> stemmedTokenMap;
		int docId = 0;
		StemParser stemParser = new StemParser();

		File folder = new File(cranfieldDataSetPath);
		if (folder.exists() && folder.isDirectory()) {
			File[] cranfieldDatabaseFiles = folder.listFiles();
			for (File file : cranfieldDatabaseFiles) {
				++docId;
				if (file.isFile()) {
					stemmedTokenMap = stemParser.parseSingleFile(file,
							stopWordsSet);

					addToDictionaryIndex(stemmedTokenMap, file, docId);
				}
			}
		} else {
			System.out.println("Folder Path cannot be found");
		}

		return indexerMap;

	}

	/**
	 * 
	 * @param stemmedTokenMap
	 * @param file
	 * @param docId
	 */
	private void addToDictionaryIndex(
			TreeMap<String, Integer> stemmedTokenMap, File file, int docId) {

		int termFrequency = 0, maximumTermFrequency = 0;
		String maxTerm = "";
		for (String lemma : stemmedTokenMap.keySet()) {
			termFrequency = stemmedTokenMap.get(lemma);
			if(termFrequency>maximumTermFrequency){
				maximumTermFrequency=termFrequency;
				maxTerm=lemma;
			}
			
			constructPostingList(lemma, stemmedTokenMap.get(lemma), file,
					docId);
		}
		
		//TODO:check for stemmedtokenMap size-->included/excluded stop words
		documentInfoMap.put(docId, new DocumentInfoWrapper(
				maximumTermFrequency, stemmedTokenMap.size(), maxTerm));

	}

	/**
	 * 
	 * @param term
	 * @param termFrequency
	 * @param file
	 * @param docId
	 */
	private void constructPostingList(String term, Integer termFrequency,
			File file, int docId) {

		IndexWrapper indexEntryWrapper = indexerMap.get(term);
		if (indexEntryWrapper == null) {

			// indexEntryWrapper=new IndexWrapper(term,0,0,new
			// LinkedList<PostingListWrapper>());
			PostingListWrapper listWrapper = new PostingListWrapper(docId,
					termFrequency);
			indexEntryWrapper = new IndexWrapper(term, 1, termFrequency,
					new LinkedList<PostingListWrapper>());
			
//			indexEntryWrapper = new IndexWrapper(term, 1,
//					new LinkedList<PostingListWrapper>());
			indexEntryWrapper.postingList.add(listWrapper);
			indexerMap.put(term, indexEntryWrapper);
		}

		else {
			indexEntryWrapper.documentFrequency += 1;
			indexEntryWrapper.totalTermFrequency += termFrequency;
			PostingListWrapper listWrapper = new PostingListWrapper(docId,
					termFrequency);
			indexEntryWrapper.postingList.add(listWrapper);
			indexerMap.put(term, indexEntryWrapper);
		}

	}

}
