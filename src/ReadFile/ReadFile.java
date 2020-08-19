package ReadFile;

import Parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class ReadFile {

    private String pathCorpus;
    private int docID;
    private Map<String , String> corpus = new HashMap<>();
    private Parser parse ;
    private int numOfParsingDocs = 0;



    /**
     * The class constructor receives a path from which it reads the files containing the documents in the corpus
     * plus a path where the user wants to save the inverse files.
     * Files and splits are read to documents
     * @param pathToCorpus
     * @param pathToSave
     * @param stemming Whether or not to perform a stemming process
     */
    public ReadFile(Boolean parseQuery,String pathToCorpus , String pathToSave ,Boolean stemming){
        this.parse = new Parser(parseQuery,stemming , pathToCorpus , pathToSave);
        this.docID = 1;
        this.pathCorpus = pathToCorpus+"//corpus";
        File corpus = new File(this.pathCorpus);
        File[] allSubDir = corpus.listFiles();
        int index = 0;

            for (int i = 0; i < allSubDir.length; i++) {
                File[] allSubFiles = allSubDir[i].listFiles();
                CutFileToDocs(allSubFiles[0]);
                index++;
                if( index == 10){
                    this.numOfParsingDocs += this.corpus.size();
                    parse.startParser(this.corpus);
                    this.corpus.clear();
                    index=0;
                }
            }
            if(this.corpus.size() > 0){
          //      System.out.println(this.corpus.size());
                this.numOfParsingDocs += this.corpus.size();
                parse.startParser(this.corpus);
            }
            parse.finish();
    }

    /**
     * This function receives a file and splits it into documents that are in using Jsoup
     * We only split documents that have a <DOCID> tag and a <TEXT> tag
     * @param file
     */
    private void CutFileToDocs(File file){
        try {
            Document xmlFile = Jsoup.parse(file,"UTF-8");
            Elements ListOfDocs =  xmlFile.getElementsByTag("DOC");
          //  System.out.println("size of file "+file.getName()+" is "+ ListOfDocs.size());
            for(int i = 0 ; i < ListOfDocs.size() ; i++){
                Element doc = ListOfDocs.get(i);
                Elements id = doc.getElementsByTag("DOCNO");
                Elements text = doc.getElementsByTag("TEXT");
                corpus.put(id.text() , text.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function receives the final information about the entire process of creating an inverse files
     * @return
     */
    public String getInformation() {
        StringBuilder info = new StringBuilder();
        info.append("number of parsing Docs - " + this.numOfParsingDocs);
        info.append("\n");
        info.append("number of uniq terms - "+ parse.getNumOfUniqTerms());
        info.append("\n");
        return info.toString();
    }
}
