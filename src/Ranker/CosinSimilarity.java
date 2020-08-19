package Ranker;

import Index.Doc;
import Index.Term;

import java.util.ArrayList;

public class CosinSimilarity {
    private Double[] weightTermInDoc;
    private ArrayList<Double> weightTermInQuery = new ArrayList<>();
    private int N ; // number of documents in corpus
    private ArrayList<Term> totalTerms;

    /**
     * this constructor init the list weightTermInQuery :
     *  give to terms in title weight - 0.55
     *  give to terms in description - 0.445
     * @param sizeOfCorpus
     * @param termsInQuery
     * @param termsInDesc
     */
    public CosinSimilarity(int sizeOfCorpus , ArrayList<Term> termsInQuery , ArrayList<Term> termsInDesc ){
        this.N = sizeOfCorpus;
        this.totalTerms = new ArrayList<>();
        for(int i = 0 ; i < termsInQuery.size(); i++){
            this.weightTermInQuery.add(new Double(0.555));
            totalTerms.add(termsInQuery.get(i));
        }
        for(int i = 0 ; i < termsInDesc.size() ; i++){
            this.weightTermInQuery.add(new Double(0.445));
            totalTerms.add(termsInDesc.get(i));
        }
    }

    /**
     * this function give score to Doc_j for all the word in query
     * @param doc_j
     * @return
     */
    public double score(Doc doc_j){
        this.weightTermInDoc = new Double[this.totalTerms.size()];

        /**
         *
         * weight of term i in doc j is
         * wij = tf * idf *( 1 - (position_ij /size of doc))
         * tf = tf_j / size_doc_j
         * idf = log2(N/df)
         * df = num of appearance in doc
         */

        Term term_i = null;
        String docNo  = null;
        double result = 0;
        try{
            double max_tf_in_doc_j = doc_j.getMaxFreqTerm();
            for(int i = 0 ; i < this.totalTerms.size() ; i++){
                term_i = this.totalTerms.get(i);
                docNo = doc_j.getFileId();
            /*
            tf_ij_calculate
             */
                double tf_in_doc_j = term_i.getNumOfAppearancesInDoc(docNo);
                double tf_ij = tf_in_doc_j/doc_j.getSizeOfDoc();

            /*
            idf_calculate
             */
                double df_i = term_i.getNumberOfDocsToTerms();
                double idf_i = Math.log(N/df_i);
            /*
            weight_position_calculate
             */
                double position_of_word_i_in_doc_j = Double.valueOf(term_i.getPossitionInDoc(docNo));
                double pw_i = 1 - ( position_of_word_i_in_doc_j / doc_j.getSizeOfDoc() );
            /*
            wij_calculate
             */
                double wij = idf_i * tf_ij ;//* pw_i ;
                weightTermInDoc[i] = new Double( wij );
            }
        /*
         calculate formula
         */
            double sumMone = 0 ;
            double sumMecane1 = 0;
            double sumMecane2 = 0;
            double mecane = 0;

            for(int i = 0 ; i < this.weightTermInDoc.length ; i++){

                sumMone += weightTermInDoc[i] * weightTermInQuery.get(i);
                sumMecane1 += Math.pow(weightTermInDoc[i],2);
                sumMecane2 += Math.pow(weightTermInQuery.get(i),2);
            }
            mecane = Math.sqrt( sumMecane1 * sumMecane2 );
            result = sumMone/mecane;
        }catch (Exception e){
            System.out.println("The Term is : " + term_i.getName() + " Doc Num : " + docNo);
        }
        return result;
    }

}