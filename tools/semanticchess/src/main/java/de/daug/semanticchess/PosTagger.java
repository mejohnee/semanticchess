package de.daug.semanticchess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

import org.apache.log4j.BasicConfigurator;



/**
 * @desc User queries will tagged by this class with the help of 
 * the Stanford POS tagger (coreNLP)
 * @author Joern-Henning Daug
 *
 */
public class PosTagger{
	
	//attributes
	private String query = "";
	private String taggedQuery = "";
	private StanfordCoreNLP pipeline = null;
	private Annotation document = null;
	private List<Token> tokens = null;
	private Tree tree = null;
	private Map<Integer, CorefChain> graph = null;
	
	/**
	 * constructor
	 * @param query: Input questions by the user
	 */
	public PosTagger(String query){
		this.query = query;
		this.pipeline = setStanfordTagger();
		this.document = setAnnotator(pipeline, query);
		this.graph = document.get(CorefChainAnnotation.class);
		initAnnotations();
	}
	
	/**
	 * initialize the Stanford Core NLP
	 * @return pipeline: object to set up the annotation
	 */
	private StanfordCoreNLP setStanfordTagger(){
		//configure output for the Staford POS tagger
		BasicConfigurator.configure();
		
		//===================================
		//This code is part of the introduction of https://stanfordnlp.github.io/CoreNLP/api.html
		
		//"creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution"
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);  
        //===================================      

        return pipeline;
	}
	
	/**
	 * initialize Annotation for the query
	 * @param pipeline: from setStanfordTagger()
	 * @param query: User input question
	 * @return document: equals user input
	 */
	private Annotation setAnnotator(StanfordCoreNLP pipeline, String query){
		
		//===================================
		//This code is part of the introduction of https://stanfordnlp.github.io/CoreNLP/api.html
		
        //"create an empty Annotation just with the given text"
        Annotation document = new Annotation(query);
        
        //"run all Annotators on this text"
        pipeline.annotate(document);
       //===================================  
        
        return document;
	}
	
	/**
	 * annotate query
	 * builds a list with tokens (word, POS-tag and NER label)
	 * builds a tree with words and POS-tags
	 * sets the tagged query
	 */
    private void initAnnotations(){
    	//===================================
    	//This code is mainly part of the introduction of https://stanfordnlp.github.io/CoreNLP/api.html
    	
    	List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    	tokens = new ArrayList<Token>();
    	
    	for(CoreMap sentence: sentences) {
  		  // traversing the words in the current sentence
  		  // a CoreLabel is a CoreMap with additional token-specific methods
  		  for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
  		    // this is the text of the token
  		    String word = token.get(TextAnnotation.class);
  		    // this is the POS tag of the token
  		    String pos = token.get(PartOfSpeechAnnotation.class);
  		    // this is the NER label of the token
  		    String ne = token.get(NamedEntityTagAnnotation.class);
  		    
  		    //push token to the list of tokens 
  		    Token tok = new Token(word,pos,ne);
  		    tokens.add(tok);
  		    
  		    //set the taggedQuery of the current query
  		    taggedQuery += tok + "\t"; 
  		  }

  		  // this is the parse tree of the current sentence
  		  tree = sentence.get(TreeAnnotation.class);
    	}
    	//=================================== 
    	
    }
    
    /**
     * get the list of tokens of the current query (word,POS,NER)
     * @return list of token
     */
    public List<Token> getTokens(){
		return tokens;  	
    }
    
    /**
     * Text from https://stanfordnlp.github.io/CoreNLP/api.html
     * "This is the coreference link graph
     * Each chain stores a set of mentions that link to each other,
     * along with a method for getting the most representative mention
     * Both sentence and token offsets start at 1!"
     * @return link graph
     */
    public Map<Integer,CorefChain> getGraph(){ 

    	return graph;
    }
    
    /**
     * parse of tree of the current query
     * @return tree: words with POS-tags
     */
    public Tree getTree(){
    	return tree;
    }
	
	/**
	 * get current query
	 * @return query: Input question by the user
	 */
	public String getQuery(){
		return query;
	}
	
	/**
	 * get current tagged query
	 * @return tagged query: tagged question with the help of 
	 * Stanford POS tagger
	 */
	public String getTaggedQuery(){
		return taggedQuery;
	}
	
	/**
	 * set query
	 * @param query: new question by the user
	 */
	public void setQuery(String query){
		this.query = query;
	}
	
	/**
	 * set tagged query
	 * @param taggedQuery: tagged question for further processing
	 */
	public void setTaggedQuery(String taggedQuery){
		this.taggedQuery = taggedQuery;
	}
	
	/**
	 * main method for testing
	 * @param args
	 */
	public static void main (String[] args){
		PosTagger tagger = new PosTagger("What does Anand play against the Sicilian Defense?");
		System.out.println("================================================================");
		System.out.println(tagger.getQuery());
		System.out.println(tagger.getTaggedQuery());
	}
	
	//=========================================================================================
	/**
	 * class for a token (word, POS, NER)
	 */
	private class Token{
		private String word = "";
		private String pos = "";
		private String ne = "";
		
		/**
		 * constructor
		 * @param word: word in the query
		 * @param pos: tag of the current word
		 * @param ne: NER of the cuurent word
		 */
		public Token(String word, String pos, String ne){
			this.word = word;
			this.pos = pos;
			this.ne = ne;
		}
		
		/**
		 * get word from token
		 * @return word
		 */
		public String getWord(){
			return word;
		}
		
		/**
		 * get POS-tag of current word
		 * @return POS
		 */
		public String getPos(){
			return pos;
		}
		
		/**
		 * get NER of the current word
		 * @return NER
		 */
		public String getNe(){
			return ne;
		}
		
		/**
		 * set word
		 * @param word
		 */
		public void setWord(String word){
			this.word = word;
		}
		
		/**
		 * set POS of the current word
		 * @param pos
		 */
		public void setPos(String pos){
			this.pos = pos;
		}
		
		/**
		 * set NER of the current word
		 * @param ne
		 */
		public void setNe(String ne){
			this.ne = ne;
		}
		
		/**
		 * combine word POS and NER
		 */
		public String toString(){
			String taggedWord = word + "\t//" + pos + "\t>>" + ne; 			
			return taggedWord;
		}
		
	}
	
}
