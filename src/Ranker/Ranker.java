package Ranker;

import Index.Doc;
import Index.Term;

import java.util.*;

public class Ranker {

    private int sizeOfCorpus;
    private double avgSizeOfDocLenght;
    private Map<Doc, Double> docsRank = new HashMap<>();
    private HashMap<String,Doc> docsFromSearcher;
    private ArrayList<Term> termsInSemantic;
    private ArrayList<Term> termsInQuery;
    private ArrayList<Term> termsInDesc;


    public Ranker() {
        this.docsFromSearcher = new HashMap<>();
        this.termsInSemantic = new ArrayList<>();
        this.termsInQuery = new ArrayList<>();
        this.termsInDesc = new ArrayList<>();
    }
    /**
     * This Function Get The Rsult Of THe Searching Process - List Of Terms From Query
     *                                                      - List Of Terms From Description Of THe Query
     *                                                      - List Of Relevant Documents For Those Terms.
     *
     * @param result
     * @param termsInDesc
     * @param termInQuery
     * @param sizeOfCorpus
     * @param avgSizeOfDoc
     */
    public void enterReasult(HashMap<String,Doc> result, ArrayList<Term> termInQuery , ArrayList<Term> termsInDesc , int sizeOfCorpus, double avgSizeOfDoc) {
        this.docsFromSearcher.clear();
        this.termsInQuery.clear();
        this.termsInDesc.clear();
        if( termsInSemantic!=null && termsInSemantic.size() == 0)
            this.termsInSemantic.clear();
        this.docsFromSearcher = new HashMap(result);
        this.termsInQuery = new ArrayList<>(termInQuery);
        this.termsInDesc = new ArrayList<>(termsInDesc);
        this.sizeOfCorpus = sizeOfCorpus;
        this.avgSizeOfDocLenght = avgSizeOfDoc;
    }
    /**
     * This Function Get The Rsult Of THe Searching Process - List Of Terms From Query
     *                                                      - List Of Terms From Description Of THe Query
     *                                                      - List Of Synonyms Terms From Semantic Model
     *                                                      - List Of Relevant Documents For Those Terms.
     *
     * @param result
     * @param termSemantic
     * @param termInDesc
     * @param termInQuery
     * @param sizeOfCorpus
     * @param avgSizeOfDoc
     */
    public void enterResultWithSemantic(HashMap<String,Doc> result , ArrayList<Term> termInQuery, ArrayList<Term> termInDesc , ArrayList<Term> termSemantic, int sizeOfCorpus, double avgSizeOfDoc){
        enterReasult(result , termInQuery , termInDesc, sizeOfCorpus , avgSizeOfDoc);
        this.termsInSemantic.clear();
        this.termsInSemantic = new ArrayList<>(termSemantic);
    }
    /**
     * Start Ranking Documents Using The BM25 Or CosinSimilarity Ranking Forms
     * We Will Return Only THe 50 Highest Ranking Documents
     */
    public List<Map.Entry<Doc, Double>> rankTheDocuments() {
        if (this.docsFromSearcher == null || this.termsInQuery == null || this.termsInDesc == null) {
            System.out.println("there is no result and termsInQuery yet");
        } else {
            List<Map.Entry<Doc, Double>> ToRank = new LinkedList<>();
            ToRank = rankWithBM25();
            return getOnly50(ToRank);
        }
        return null;
    }
    /**
     * this function mapping if its a semantic model or a regular model
     * @return List<Map.Entry<Doc, Double>>
     */
    private List<Map.Entry<Doc, Double>> rankWithBM25() {
        double k1 = 1.2;
        double b = 0.75;
        BM25 rankBM25 = new BM25(k1, b);
        List<Map.Entry<Doc, Double>> tmpResult;

        if(this.termsInSemantic.size()!=0){
            rankSemanticModel(rankBM25);
        }
        else{
            rankRegular(rankBM25);
        }
        tmpResult = sortRank(this.docsRank);
        this.docsRank.clear();
        return tmpResult;
    }
    /**
     * this function rank the document for regular model - without semantic - docsRank save the results
     * Give Rate for Each Documents We Get Using BM25 Ranking and cosineSimilarity ranking
     *      *  - If Term Found In Query title We Give Weight Of 1
     *      *  - If Term Found In Description Of Query We Give Weight Of 1
     *      *  - If Doc Is In Size Zero Or If Cosine Similarity returns
     * @param rankBM25
     */
    private void rankRegular(BM25 rankBM25) {
        CosinSimilarity cosinSimilarity = new CosinSimilarity(this.sizeOfCorpus,this.termsInQuery , this.termsInDesc );
        for (String docId : this.docsFromSearcher.keySet()) {
            Doc doc = this.docsFromSearcher.get(docId);
            Double sumOfRank = new Double(0);
            Double bmRank = new Double(0);
            ArrayList<Term> terms = new ArrayList<>();
            for (Term term : this.termsInQuery) {
                if (!terms.contains(term)) {
                    terms.add(term);
                }
                bmRank += rankBM25.score(term.getNumOfAppearancesInDoc(doc.getFileId()), doc, sizeOfCorpus, term.getNumberOfDocsToTerms(), avgSizeOfDocLenght);
            }
            for (Term term : this.termsInDesc) {
                if (!terms.contains(term)) {
                    terms.add(term);
                }
            }
            for (Term term : terms)
                bmRank += rankBM25.score(term.getNumOfAppearancesInDoc(doc.getFileId()), doc, sizeOfCorpus, term.getNumberOfDocsToTerms(), avgSizeOfDocLenght);
            double cosinScore = 0;
            if(doc.getSizeOfDoc() != 0){
                cosinScore = cosinSimilarity.score(doc);
                if(cosinScore > 0 && cosinScore < 1){
                    sumOfRank = 0.99*bmRank + 0.01*cosinScore;
                }
                else{
                    sumOfRank = bmRank;
                }
            }
            else{
                sumOfRank = bmRank;
            }
            this.docsRank.put(doc, sumOfRank);
        }
    }
    /**
     * this function rank the document for semantic model - docsRank save the results
     * Give Rate for Each Documents We Get Using BM25 Ranking:
     *      *  - If Term Found In Query title We Give Weight Of 0.555
     *      *  - If Term Found In Description Of Query We Give Weight Of 0.445
     *      *  - If Term Found In The Synonyms Of Term In Query/Description We Give Weight Of 0.1
     * @param rankBM25
     */
    private void rankSemanticModel(BM25 rankBM25) {
        for (String docId : this.docsFromSearcher.keySet()) {
            Doc doc = this.docsFromSearcher.get(docId);
            Double sumOfRank = new Double(0);
            for (Term term : this.termsInQuery) {
                sumOfRank += (0.555)*rankBM25.score(term.getNumOfAppearancesInDoc(doc.getFileId()), doc, sizeOfCorpus, term.getNumberOfDocsToTerms(), avgSizeOfDocLenght);
            }
            for(Term term : this.termsInDesc){
                sumOfRank += (0.445)*rankBM25.score(term.getNumOfAppearancesInDoc(doc.getFileId()), doc, sizeOfCorpus, term.getNumberOfDocsToTerms(), avgSizeOfDocLenght);
            }
            for(Term term : this.termsInSemantic){
                sumOfRank += (0.1)*rankBM25.score(term.getNumOfAppearancesInDoc(doc.getFileId()), doc, sizeOfCorpus, term.getNumberOfDocsToTerms(), avgSizeOfDocLenght);
            }
            this.docsRank.put(doc, sumOfRank);
        }
    }
    /**
     * Sort The Documents By The Highest Ranking
     * @param hm
     * @return
     */
    private static List<Map.Entry<Doc, Double>> sortRank(Map<Doc, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<Doc, Double>> list =
                new LinkedList<Map.Entry<Doc, Double>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Doc, Double>>() {
            public int compare(Map.Entry<Doc, Double> o1,
                               Map.Entry<Doc, Double> o2) {
                return (o2.getValue().compareTo(o1.getValue()));
            }
        });
        return list;
    }
    /**
     * Return Only 50 Highest Ranking Documents
     * @param tmp
     * @return
     */
    private List<Map.Entry<Doc, Double>> getOnly50(List<Map.Entry<Doc, Double>> tmp) {
        int size = Math.min(50, tmp.size());
        int index = 0;
        List<Map.Entry<Doc, Double>> only50 = new ArrayList<>();
        for(int i=0 ; i < size ; i++){
            only50.add(tmp.get(i));
        }
        return only50;
    }




}
