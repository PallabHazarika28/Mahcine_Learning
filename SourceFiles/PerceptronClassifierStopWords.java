
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PerceptronClassifierStopWords
{

	public static HashMap<String,Double> originalVocabularyHashMap = new HashMap<String,Double>(); // This hash map contains all the unique words among all the documents
	
	//data matrix is a variable containing the data of the spam and ham files of m and (n + 2) where m = total number of email documents and n = size of voca
    //-bulary where the vocabulary contains the unique words across all the email documents of spam and ham
	public static double[][] dataMatrix;
	
	public static ArrayList<String> tokenFromVocabulary = new ArrayList<String>(); // This array list contains the tokens from the vocabulary. This helps
    // in getting the index of the token
	
	public static int m,n;
	
	//Weight array of dimension (n - 1) where n= size of originalVocabularyHashMap - 1 initialized to random values
    public static double[] weightForEachToken;
    
    //Number of files which has been successfully counted as spam and ham
    public static int testSpamFileCount; 
    public static int testHamFileCount;
    
    
  	public static String [] stopWords = {"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", 
  		"before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", 
  		"doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", 
  		"haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", 
  		"how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", 
  		"more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", 
  		"our", "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", 
  		"so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", 
  		"these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", 
  		"very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", 
  		"where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", 
  		"you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"};
	
	//Start of main function
	public static void main(String[] args)
	{
		
		   File pathToSpamFiles = new File(args[0]);
		   File pathToHamFiles = new File(args[1]);
		   
		   int numberOfSpamExampleFiles = pathToSpamFiles.listFiles().length;
		   int numberOfHamExampleFiles = pathToHamFiles.listFiles().length;
		   int totalNumberOfExampleFiles = numberOfSpamExampleFiles + numberOfHamExampleFiles;
		   
		   //Parsing over all the email documents of spam and ham folder and extracting unique words and storing them in the originalVocabularyHashMap 
		   extractAndStoreWordsForVocabulary(args,pathToSpamFiles);
		   extractAndStoreWordsForVocabulary(args,pathToHamFiles);
		   
		   //Initializing the values of m and n in the datamatrix
		   m = totalNumberOfExampleFiles;
		   n = originalVocabularyHashMap.size();
		   int p = n + 2;
		   
		   dataMatrix = new double[m][p];
		   
		   
		  //Setting the value of class in the datamatrix, the last column corresponds to the class value
		   //Also setting the first column as dummy threshold value of 
			for(int i = 0; i < m; i++)
			   {
				  
				  if(i < numberOfSpamExampleFiles) 
				  {
					  dataMatrix[i][n+1] = 1.0;
				  }
				  else
					  {
					  dataMatrix[i][n+1] = -1.0;
					  }
				  
				  dataMatrix[i][0] = 1.0;
	
			   }
			
			//Setting the values for each token of vocabulary for each example mail by iterating over columns and iterating over each example mail
			//Finding the occurrences of tokens in spam files
			int i = 0;
			for(File file: pathToSpamFiles.listFiles())
			{
				findOccurrenceStatusOfTokensInEachExample(i,file);
				i++;
			}
			
			//Finding the occurrences of tokens in ham files
			
			
			for(File file: pathToHamFiles.listFiles())
			{
				
				findOccurrenceStatusOfTokensInEachExample(i,file);
				i++;
			}
			
			
			//Initializing the weights array
			weightForEachToken = new double[n+1];
			double start = Double.parseDouble(args[4]);
	        double end = Double.parseDouble(args[5]);
	        double random = new Random().nextDouble();
	        double result = start + (random * (end - start));
	        
			for (int k = 0; k <= n; k++)
			{
				weightForEachToken[k] = result;
			}
			
			//Calling the convergence function
			convergeDataPoints(args);
			
			
			//#########################################################TESTING PHASE###############################################################
			
			System.out.println("Running testing phase for perceptron classifier without stop words");
			String flag = new String();
			//Iterating over the spam files
			File pathToSpamFilesOfTestData = new File(args[2]);
			int numberOfSpamFiles = pathToSpamFilesOfTestData.listFiles().length;
			
			
			flag = "spam";
			for(File file: pathToSpamFilesOfTestData.listFiles())
			{
				testFile(args,flag,file);
			}
			
			
			double accuracySpam = ((double)testSpamFileCount/(double)numberOfSpamFiles)*100.0;
			
			
			System.out.println("Accuracy of spam test files for perceptron classifier is "+accuracySpam);
			
			//Iterating over the ham files
			flag = "ham";
			File pathToHamFilesOfTestData = new File(args[3]);
			int numberOfHamFiles = pathToHamFilesOfTestData.listFiles().length;
			
			for(File file: pathToHamFilesOfTestData.listFiles())
			{
				testFile(args,flag,file);
			}
			
			
			double accuracyHam = ((double)testHamFileCount/(double)numberOfHamFiles)*100.0;
			
			
			System.out.println("Accuracy of ham test files for perceptron classifier is "+accuracyHam);
			
			//Printing overall accuracy
			
			double accuracyOverall = (double)(testSpamFileCount + testHamFileCount)/(double)(numberOfSpamFiles + numberOfHamFiles)*100.0;
			
			System.out.println("Overall accuracy for perceptron classifier is "+accuracyOverall);
			
	}//End of main function
	
	
	//Function to test the test file of each spam and ham
			public static void testFile(String[] args,String flag,File file)
			{
					
				
				HashMap<String, Double> uniqueWordsInTestFile = new HashMap<String,Double>();
					
					//Start of finding the occurrences of each word in the parsed file
			    	  FileReader inputFile;
						 try 
						 {
							inputFile = new FileReader(file);
							BufferedReader br = new BufferedReader(inputFile);
							
							String line; //This variable stores the each line from the file being parsed
							
							try 
							{
								while ((line = br.readLine()) != null) 
								{
									
									
									//Below line keeps only the words and number in the line and replaces the replaced text with empty space
									String tempString = line.replaceAll("[^A-Za-z ]+"," ");
									
									//Below statement replaces duplicate spaces with single space
									String tempString2 = tempString.replaceAll(" +", " ");
									
									
									//The below array contains the words from the line splitted by space
									String[] splittedString = tempString2.split(" ");
									
									//###########################################STOP WORDS PORTION#############################################
									//Start of code for  removing stop words
									final List<String> list =  new ArrayList<String>();
									Collections.addAll(list,splittedString);
									
									String[] stopWord;
									stopWord = stopWords;
									
									for(int i = 0; i < stopWord.length; i++)
									{
									   list.remove(stopWord[i]);
									}
									
									splittedString = list.toArray(new String[list.size()]);//End of code for removing stop words
									
									int numberOfWords = splittedString.length;
									
									for(int i = 0; i < numberOfWords; i++)
									{
										// Checking if the word is already contained in the originalVocabularyHashMap
										if(!(uniqueWordsInTestFile.containsKey((String)splittedString[i])))
										{
											uniqueWordsInTestFile.put(splittedString[i],1.0);
										}
										else
										{
											//Incrementing the value of word as it was found
											uniqueWordsInTestFile.put(splittedString[i],uniqueWordsInTestFile.get(splittedString[i]) + 1.0);
											
										}
										
									}
									
								}
							} 
							catch (IOException e) 
							{
								
								e.printStackTrace();
							}
							
							try {
								inputFile.close();
							} catch (IOException e) {
								
								e.printStackTrace();
							}
						 } 
						 catch (FileNotFoundException e) 
						 {
							
							e.printStackTrace();
						 }
						 //End of finding the occurrences of each word in the parsed file
						 
						 
						 
						 //Finding the weight of the token from the trained weight vector and replace it in the w0 + wi*xi
						 
						 double weight = weightForEachToken[0];
						 
						 for (String key: uniqueWordsInTestFile.keySet())
						 {
							int index;
							double priorWeight = 0.0;
							 if(originalVocabularyHashMap.containsKey(key))
								 {
								    index = tokenFromVocabulary.indexOf(key);
								    priorWeight = weightForEachToken[index + 1];
								 }
							 
							 
							   
	                           weight += priorWeight*uniqueWordsInTestFile.get(key);						 
							 
						 }
						 
						 if(flag.equals("spam"))
							 {
							      						  
							   if( weight > 0.0)
							    testSpamFileCount+=1;
							 }
						 else
						 {   
							 //System.out.println("Weight is "+weight+"Flag is "+flag);
							 if( weight < 0.0)
							 {
								// System.out.println("Hi");
								 testHamFileCount+=1;
							 }
						 }
			}
	
	//Start of function to find the occurrences of each token of vocabulary in each example mail and thereby plot the data points in the data matrix
    public static void findOccurrenceStatusOfTokensInEachExample(int k,File file)
    {
    	   
    	  HashMap<String, Double> wordsFrequencyInCurrentParsedFile = new HashMap<String,Double>();
    	  
    	  //Start of finding the occurrences of each word in the parsed file
    	  FileReader inputFile;
			 try 
			 {
				inputFile = new FileReader(file);
				BufferedReader br = new BufferedReader(inputFile);
				
				String line; //This variable stores the each line from the file being parsed
				
				try 
				{
					while ((line = br.readLine()) != null) 
					{
						
						
						//Below line keeps only the words and number in the line and replaces the replaced text with empty space
						String tempString = line.replaceAll("[^A-Za-z ]+"," ");
						
						//Below statement replaces duplicate spaces with single space
						String tempString2 = tempString.replaceAll(" +", " ");
						
						
						//The below array contains the words from the line splitted by space
						String[] splittedString = tempString2.split(" ");
						
						
						int numberOfWords = splittedString.length;
						
						for(int i = 0; i < numberOfWords; i++)
						{
							// Checking if the word is already contained in the originalVocabularyHashMap
							if(!(wordsFrequencyInCurrentParsedFile.containsKey((String)splittedString[i])))
							{
								wordsFrequencyInCurrentParsedFile.put(splittedString[i],1.0);
							}
							else
							{
								//Incrementing the value of word as it was found
								wordsFrequencyInCurrentParsedFile.put(splittedString[i],wordsFrequencyInCurrentParsedFile.get(splittedString[i]) + 1.0);
								
							}
							
						}
						
					}
				} 
				catch (IOException e) 
				{
					
					e.printStackTrace();
				}
				
				try {
					inputFile.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			 } 
			 catch (FileNotFoundException e) 
			 {
				
				e.printStackTrace();
			 }
			 //End of finding the occurrences of each word in the parsed file
			
			 
			 int j = 1;
			 //Finding the frequency of the token in the word hash map
			 for(String key: originalVocabularyHashMap.keySet())
			 {
				 
				 if(wordsFrequencyInCurrentParsedFile.containsKey(key))
				 {
					 dataMatrix[k][j] = wordsFrequencyInCurrentParsedFile.get(key); 
				 }
				
				 else
				 {
					 dataMatrix[k][j] = 0.0;
				 }
					 
				 j++;
			 }
    	  
    }
				
    //End of function
    
    
  //Function to do convergence of data points
  	//Start of convergeDatapoints function	
    public static void convergeDataPoints(String[] args)
  		{
  		    
    	    //Setting the value of eta
    	    
    	    double eta = Double.parseDouble(args[6]);
	    //double localError,globalError;
	    
	        int iterations = Integer.parseInt(args[7]);
  			//Outer for loop controlling the number of iterations
    	    for(int i =0; i < iterations; i++)
    	    {
    	    	
    	    	//Start of inner for loop iterating over all examples files of spam and ham. Basically we will be iterating over each row of the data matrix which corresponds
    	    	//to each example file
    	    	for(int j = 0; j < m; j++)
    	    	{
    	    		
    	    		//Calculating the value of perceptron output
    	    		double perceptronOutput = weightForEachToken[0];
    	    		for(int k =1 ; k < (n + 1); k++)
    	    		{
    	    			perceptronOutput += dataMatrix[j][k]*weightForEachToken[k];
    	    		}
    	    		
    	    		if(perceptronOutput > 0.0)
    	    			perceptronOutput = 1.0;
    	    		else
    	    			perceptronOutput = -1.0;
    	    		
    	    		//Start of perceptron training
    	    		//Iterating over the features or data points of each example file
    	    		for(int p = 0; p < n ; p++)
    	    		{
    	    			double deltaWeight = eta *(dataMatrix[j][n+1] - (int)perceptronOutput)*dataMatrix[j][p];
    	    			weightForEachToken[p] = weightForEachToken[p] + deltaWeight;
    	    		}
    	    		//End of perceptron training
    	    				
    	    	}
    	    }//End of outer for loop
  			
  		}//End of convergeDataPoints function
	
	//Start of Function to extract unique words from all the example mails of spam and ham mail files
			public static void extractAndStoreWordsForVocabulary(String[] args,File path)
			{
				for (File file : path.listFiles())
				 {
					 FileReader inputFile;
					 try 
					 {
						inputFile = new FileReader(file);
						BufferedReader br = new BufferedReader(inputFile);
						
						String line; //This variable stores the each line from the file being parsed
						
						try 
						{
							while ((line = br.readLine()) != null) 
							{
								
								
								//Below line keeps only the words and number in the line and replaces the replaced text with empty space
								String tempString = line.replaceAll("[^A-Za-z ]+"," ");
								
								//Below statement replaces duplicate spaces with single space
								String tempString2 = tempString.replaceAll(" +", " ");
								
								
								//The below array contains the words from the line splitted by space
								String[] splittedString = tempString2.split(" ");
								
								//###########################################STOP WORDS PORTION#############################################
								//Start of code for  removing stop words
								final List<String> list =  new ArrayList<String>();
								Collections.addAll(list,splittedString);
								
								String[] stopWord;
								stopWord = stopWords;
								
								for(int i = 0; i < stopWord.length; i++)
								{
								   list.remove(stopWord[i]);
								}
								
								splittedString = list.toArray(new String[list.size()]);//End of code for removing stop words
								
								
								int numberOfWords = splittedString.length;
								
								for(int i = 0; i < numberOfWords; i++)
								{
									// Checking if the word is already contained in the originalVocabularyHashMap
									if(!(originalVocabularyHashMap.containsKey((String)splittedString[i])))
									{
										 originalVocabularyHashMap.put(splittedString[i],1.0);
										 tokenFromVocabulary.add(splittedString[i]); // This step add the token into the token array list which will help in getting 
										                                             // the index of the token
									}
									else
									{
										//Incrementing the value of word as it was found
										originalVocabularyHashMap.put(splittedString[i],originalVocabularyHashMap.get(splittedString[i]) + 1.0);
										
									}
									
								}
								
							}
						} 
						catch (IOException e) 
						{
							
							e.printStackTrace();
						}
						
						try {
							inputFile.close();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
					 } 
					 catch (FileNotFoundException e) 
					 {
						
						e.printStackTrace();
					 }
					 
				 }
			}//End of function extractAndStoreWordsForVocabulary
}

