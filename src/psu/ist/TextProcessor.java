package psu.ist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;

public class TextProcessor 
{
	private Properties props;
	private StanfordCoreNLP pipeline;
	private String content;
	private List<CoreMap> sentences;
	private StopList stop;
    private Lemmatizer lemmatizer;
	private Set<String> keywords;
	private String outputFolder;
	
    TextProcessor(String folderName,String fileName,String outputFolder) throws IOException
	{
		props = new Properties();
		props.put("annotators","tokenize ssplit");
	
		pipeline = new StanfordCoreNLP(props);
		
		/* Todo: Integrate Stanford OpenNLP
		File file = new File(folderName+"/"+fileName);
		byte[] data = readFileAsString(file);
		content = new String(data, "UTF-8");
		*/
		
		lemmatizer = new Lemmatizer(); 
		stop = new StopList(true);
		
		this.outputFolder = outputFolder;
	}
	
    /* Todo: Integrate Stanford OpenNLP
     * 
	public List<CoreMap> getSentences()
	{
		 Annotation document = new Annotation(content);
		 pipeline.annotate(document);
		 sentences = document.get(SentencesAnnotation.class);
		 return sentences;
	}
	*/
	
	public StopList getStopList()
	{
			return stop;
	}
	
	public Lemmatizer getLemmatizer()
	{
			return lemmatizer;
	}
	
	// Writing final query file for submission script
	public void postProcessing(String documentId) throws IOException
	{
		File folder = new File(outputFolder);
		
		for (File fileEntry : folder.listFiles()) 
		{
			keywords = new LinkedHashSet<String>();		//Maintain order
			
	        if (fileEntry.isDirectory() || fileEntry.isHidden() == true ) 
	        {
	        	System.out.println("Directories and Hidden Files skipped");
	        } 
	        else 
	        { 
	        	String line;
	        	FileReader fReader = new FileReader(fileEntry);
	        	BufferedReader bReader = new BufferedReader(fReader);
	        	
	        	System.out.println("File "+fileEntry.getName());
	        	
	            while( (line = bReader.readLine()) != null )
	            {            	
	                StringTokenizer st = new StringTokenizer(line,"|");	            
	            	keywords.add(st.nextToken().trim());
	            }
	        
	        
	            //Write the file for each method
	            PrintWriter writer = new PrintWriter(outputFolder+"/clean/"+fileEntry.getName(),"UTF-8");
	            Iterator<String>  keywordIterator = keywords.iterator(); 
	        
	            while(keywordIterator.hasNext())
	            {	
	            	writer.print(documentId+" ");
	        	
	            	for(int c=1;c<=10;c++)
	            	{
	            		if (keywordIterator.hasNext() == false)
	        			break;
	        		
	            		writer.print(keywordIterator.next()+" "); 
	            	}
	            	writer.println();
	            }
	            writer.close();
	        }
		}
	}
	
	private byte[] readFileAsString(File fileEntry) throws IOException
	{
		FileInputStream fis = new FileInputStream(fileEntry);
		byte[] data = new byte[(int)fileEntry.length()];
		
		fis.read(data);
		fis.close();		    

		return data;
	}
	
	private String returnQueryWords()
	{
		
		return content;		
	}
	

}