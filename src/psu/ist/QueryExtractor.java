package psu.ist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AbstractFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.Algorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AverageCorpusTFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AverageCorpusTFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TermExAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TermExFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequencyMultiThread;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderDocumentTermFrequencyMultiThread;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderTermNestMultiThread;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureDocumentTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndex;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NGramExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.core.npextractor.WordExtractor;
import uk.ac.shef.dcs.oak.jate.io.ResultWriter2File;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.model.Term;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;

public class QueryExtractor 
{
	private CandidateTermExtractor extractor;
	private TextProcessor textProcessor;
	private CandidateTermExtractor wordextractor;
	private GlobalIndexBuilderMem builder;
    private GlobalIndexMem wordDocIndex;
    private GlobalIndexMem termDocIndex;
    private WordCounter wordcounter; 
	private FeatureCorpusTermFrequency wordFreq;
    private FeatureDocumentTermFrequency termDocFreq;
    private FeatureTermNest termNest;
    private FeatureRefCorpusTermFrequency bncRef;
    private FeatureCorpusTermFrequency termCorpusFreq;
    private String outputFolder;
    private String documentId;
    private String inputFolder;
    private String inputFile;
    private String refrenceCorpusPath;
    
    private Map<Algorithm, AbstractFeatureWrapper> _algregistry = new HashMap<Algorithm, AbstractFeatureWrapper>();
	
    private static Logger _logger = Logger.getLogger(QueryExtractor.class);
    
	// i = 1 =>  NounPhraseExtractorOpenNLP 
	// i = 2 =>  NGramExtractor
	public QueryExtractor(String inputFolder,String inputFile,String refrenceCorpusPath,int i,String outputFolder,String documentId) throws JATEException,IOException 
	{
		textProcessor = new TextProcessor(inputFolder,inputFile,outputFolder);
		
		if (i == 1)
		{
			extractor = new NounPhraseExtractorOpenNLP(textProcessor.getStopList(),textProcessor.getLemmatizer());
		}
		else 
		{
			extractor = new NGramExtractor(textProcessor.getStopList(),textProcessor.getLemmatizer());
		}
		
		wordextractor = new WordExtractor(textProcessor.getStopList(),textProcessor.getLemmatizer(),false,1);
		builder = new GlobalIndexBuilderMem();
		
		wordDocIndex = builder.build(new CorpusImpl(inputFolder), wordextractor);
		termDocIndex = builder.build(new CorpusImpl(inputFolder), extractor);
		
		wordcounter = new WordCounter();
		this.outputFolder = outputFolder;
		this.documentId = documentId;
		this.inputFolder = inputFolder;
		this.inputFile = inputFile;
		this.refrenceCorpusPath = refrenceCorpusPath;
	}
	
	public void buildIndex() throws JATEException
	{
       wordFreq = new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter, textProcessor.getLemmatizer()).build(wordDocIndex);
       
       termDocFreq = new FeatureBuilderDocumentTermFrequencyMultiThread(wordcounter, textProcessor.getLemmatizer()).build(termDocIndex);
             
       termNest = new FeatureBuilderTermNestMultiThread().build(termDocIndex);
             
       System.out.println(refrenceCorpusPath);
       
       bncRef = new FeatureBuilderRefCorpusTermFrequency(refrenceCorpusPath).build(null);
             
       termCorpusFreq = new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter,textProcessor.getLemmatizer()).build(termDocIndex);
	}
	
	private void registerAlgorithm(Algorithm a, AbstractFeatureWrapper f) 
    {
		_algregistry.put(a, f);
    }
    
    public void execute() throws JATEException, IOException 
    {
    	ResultWriter2File writer = new ResultWriter2File(termDocIndex);
        
    	if (_algregistry.size() == 0) 
    		throw new JATEException("No algorithm registered!");
        
    	_logger.info("Running NP recognition...");

        for (Map.Entry<Algorithm, AbstractFeatureWrapper> en : _algregistry.entrySet()) 
	    {
        	_logger.info("Running feature store builder and ATR..." + en.getKey().toString());
        	Term[] result = en.getKey().execute(en.getValue());
        	writer.output(result, outputFolder + File.separator + en.getKey().toString() + ".txt");
	    }
    }
    
    public void executeAllAlgorithms() throws JATEException, IOException
    {
    	buildIndex();
    	
    	registerAlgorithm(new TFIDFAlgorithm(), new TFIDFFeatureWrapper(termCorpusFreq));
    	registerAlgorithm(new GlossExAlgorithm(), new GlossExFeatureWrapper(termCorpusFreq, wordFreq, bncRef));
        registerAlgorithm(new WeirdnessAlgorithm(), new WeirdnessFeatureWrapper(wordFreq, termCorpusFreq, bncRef));
        registerAlgorithm(new CValueAlgorithm(), new CValueFeatureWrapper(termCorpusFreq, termNest));
        registerAlgorithm(new TermExAlgorithm(), new TermExFeatureWrapper(termDocFreq, wordFreq, bncRef));
        registerAlgorithm(new RIDFAlgorithm(), new RIDFFeatureWrapper(termCorpusFreq));
        registerAlgorithm(new AverageCorpusTFAlgorithm(), new AverageCorpusTFFeatureWrapper(termCorpusFreq));
        registerAlgorithm(new FrequencyAlgorithm(), new FrequencyFeatureWrapper(termCorpusFreq));

        execute();	
       textProcessor.postProcessing(documentId);
    }
	
}
