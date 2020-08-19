package Ranker;

import Index.Doc;

public class BM25 {
    private double k1;
    private double b;

    public BM25(double k1, double b) {
        if (k1 < 0) {
            throw new IllegalArgumentException("Negative k1 = " + k1);
        }
        if (b < 0 || b > 1) {
            throw new IllegalArgumentException("Invalid b = " + b);
        }
        this.k1 = k1;
        this.b = b;
    }
    /**
     * Returns a relevance score between a term and a document based on a corpus.
     * @param freq term frequency of in the document.
     * @param N the number of documents in the corpus.
     * @param n the number of documents containing the given term in the corpus;
     */
    public double score( double freq , Doc doc , long N, long n, double avgSizeOfDoc) {

        int sizeOfDoc = doc.getSizeOfDoc();
        if (freq <= 0) return 0.0;
        int doc_max_freq_term = doc.getMaxFreqTerm();
        if(doc_max_freq_term <= 0){
            return 0.0;
        }
        double tf = freq ; /// doc_max_freq_term ;
        double mone = (k1 + 1) * tf ;

        double idf = Math.log10((N - n + 0.5) / (n + 0.5));

        double nirmul = tf + ( k1*(1 - b + b * sizeOfDoc / avgSizeOfDoc) );


        return  mone * idf / nirmul;
    }

}