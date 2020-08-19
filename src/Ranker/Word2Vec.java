package Ranker;

import com.medallia.word2vec.Word2VecModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Word2Vec {

    private HashMap<String,List<String>> semanticTerms = new HashMap<>();
    private Word2VecModel word2VecModel;
    private com.medallia.word2vec.Searcher semanticSearcher;

    public Word2Vec(){
        try {
            word2VecModel = Word2VecModel.fromTextFile(new File(System.getProperty("user.dir")+"\\word2vec.c.output.model.txt"));
            semanticSearcher = word2VecModel.forSearch();
        } catch (Exception e) {
            //System.out.println("Something Is Wrong With The Semantic Model");
        }
    }

    public void startModel(List<String> TermInquery){
        int numOfResultInList = 2;
        for(String queryTerm : TermInquery){
            try{
                List<com.medallia.word2vec.Searcher.Match> matches = this.semanticSearcher.getMatches((queryTerm),numOfResultInList);
                List<String> matchForQuery = new ArrayList<>();
                for(com.medallia.word2vec.Searcher.Match match : matches){
                    matchForQuery.add(match.match());
                }
                semanticTerms.put(queryTerm,matchForQuery);
            }catch(Exception e){
                continue;
            }
        }
    }

    public HashMap<String,List<String>> getSemanticForQuery(){
        return this.semanticTerms;
    }

}