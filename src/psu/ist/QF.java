package psu.ist;

import java.io.IOException;
import java.util.Date;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;

public class QF 
{
	public static void main(String args[]) throws JATEException, IOException
	{
		
		if (args.length < 6) 
		{
			System.out.println("Args 1 : Input Folder for indexing");
			System.out.println("Args 2 : Input FileName for Stanford NLP indexing");
			System.out.println("Args 3 : Jate Refrence Corpus Path");
			System.out.println("Args 4 : Extraction Algorithm Choice : ");
			System.out.println("1   for  NounPhraseExtractorOpenNLP ");
			System.out.println(">=2 for  NGramExtractor");
			System.out.println("Args 5 : OutputFolder");
			System.out.println("Args 6 : Document Id");
			return;
		}
			
		System.out.println(new Date());
		QueryExtractor tester = new QueryExtractor(args[0],args[1],args[2],Integer.parseInt(args[3]),args[4],args[5]);
		tester.executeAllAlgorithms();
        System.out.println(new Date());
	}
	
}

// /Users/myth/Learn/GradSchool/IST441/Project/QueryFormulation/evaluation
// suspicious-document001-batch1.txt
// /Users/myth/Learn/GradSchool/IST441/Project/jate_1.11/nlp_resources/bnc_unifrqs.normal
// 1
// /Users/myth/Learn/GradSchool/IST441/Project/QueryFormulation/output
// 001