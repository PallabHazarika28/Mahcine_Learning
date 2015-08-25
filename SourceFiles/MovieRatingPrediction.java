

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MovieRatingPrediction 
{
    
	//Below hashmap(HashMap1) contains the movie ids as keys and value a Hashmap which stores the key as user id and rating as value.
	public static HashMap<String, HashMap<String,Double>> movieIdsAndUserIdsAndRatings = new HashMap<String, HashMap<String,Double>>();
	
	//Below HashMap(hashmap2) contain userid as key and value hashmap which contain movie id as key and rating as value. This Hashmap would be used for finding mean votes
	public static HashMap<String, HashMap<String,Double>> userdsAndMovieIdsRatings = new HashMap<String, HashMap<String,Double>>();
	
	//Hashmap to store over mean vote for user ids
	public static HashMap<String, Double> meanVoteForUserIds = new HashMap<String, Double>();
	
	//Start of main function
	public static void main(String[] args)
	{
		
		File trainFile = new File(args[0]);
		
		//Start of Iterating over the lines of the training file and generating the hashmaps HashMap1 and HashMap2
		FileReader inputTrainFile;
		
		try
		{
			inputTrainFile = new FileReader(trainFile);
			BufferedReader br = new BufferedReader(inputTrainFile);
			
			String line; //This variable stores each line from the file being parsed
			
			try 
			{
				
				
				while ((line = br.readLine()) != null) 
				{
					
					
					HashMap<String,Double> tempUserIdsAndRatings = new HashMap<String,Double>(); //This temp variable contain the values of userid and rating
					HashMap<String,Double> tempMovieIdsAndRatings = new HashMap<String,Double>();
					
					String[] splittedString = line.split(",");
					
					
					//Start of creating the hashmap1
					if(!(movieIdsAndUserIdsAndRatings.containsKey(splittedString[0])))
					{
						tempUserIdsAndRatings.put(splittedString[1], Double.parseDouble(splittedString[2]));
						
						movieIdsAndUserIdsAndRatings.put(splittedString[0],tempUserIdsAndRatings);
					}
					else
					{
						HashMap<String,Double> temp1 = movieIdsAndUserIdsAndRatings.get(splittedString[0]);
						temp1.put(splittedString[1], Double.parseDouble(splittedString[2]));
						
						
						movieIdsAndUserIdsAndRatings.put(splittedString[0],temp1);
						
					}
					//End of creating the hashmap1
					
					//Start of creating hashmap2
					if(!userdsAndMovieIdsRatings.containsKey(splittedString[1]))
					{
						tempMovieIdsAndRatings.put(splittedString[0], Double.parseDouble(splittedString[2]));
						
						userdsAndMovieIdsRatings.put(splittedString[1], tempMovieIdsAndRatings);
						
					}
					else
					{
						HashMap<String,Double> temp2 = userdsAndMovieIdsRatings.get(splittedString[1]);
						
						temp2.put(splittedString[0],Double.parseDouble(splittedString[2]));
						
						userdsAndMovieIdsRatings.put(splittedString[1],temp2);
					}
					//End of creating hashmap2
					
					
				}
				
				
				
			} 
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
			
			try {
				inputTrainFile.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		 } 
		 catch (FileNotFoundException e) 
		 {
			
			e.printStackTrace();
		 }
		//End of  Iterating over the lines of the training file and generating the hashmaps HashMap1 and HashMap2
				
		
		//##############################################TESTING PHASE################################################################################
		
		
		//Start of Iterating over the lines of the training file and generating the hashmaps HashMap1 and HashMap2
				
		      
		        double meanError = 0.0;
		        double meanSquareError = 0.0;
		        
		        int numberOfLinesInTestFile = 0;
		        
		        File testFile = new File(args[1]);	
		        FileReader inputTestFile;
		        
		        
		        //Computing and storing the mean votes of all users of hashmap2
		        for(String key: userdsAndMovieIdsRatings.keySet())
		        {
		        	calculateAndStoreMeanVoteForUser(key);
		        }
				
				try
				{
					inputTestFile = new FileReader(testFile);
					BufferedReader br = new BufferedReader(inputTestFile);
					
					String line; //This variable stores the each line from the file being parsed
					
					try 
					{
						
						while ((line = br.readLine()) != null) 
						{
							
						    String[] splittedTestString = line.split(",");
						    
						    //Calling function to predict rating for movie by an active user
						    double rating = findPredictRating(splittedTestString[0],splittedTestString[1]);
						    
					 		meanError += Math.abs((rating - Double.parseDouble(splittedTestString[2])));
					 		meanSquareError += (rating - Double.parseDouble(splittedTestString[2]))*(rating - Double.parseDouble(splittedTestString[2])); 
					 		
					 		numberOfLinesInTestFile++;
							
						}
					} 
					catch (IOException e) 
					{
						
						e.printStackTrace();
					}
					
					try {
						inputTestFile.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				 } 
				 catch (FileNotFoundException e) 
				 {
					
					e.printStackTrace();
				 }
				
				
				 System.out.println("Mean absolute error is "+meanError/numberOfLinesInTestFile);
				 System.out.println("Root mean square error is "+Math.sqrt(meanSquareError/numberOfLinesInTestFile));
		
		
	}
	//End of main function
	
	
	//Function to calculate the prediction rating for a user for a movie
	public static double findPredictRating(String movieId,String activeUserId)
	{
		
		
		HashMap<String,Double> weights = new HashMap<String,Double>();
		double normalizationFactor = 0.0;
		double value = 0.0;
		
		
		for(String key: movieIdsAndUserIdsAndRatings.get(movieId).keySet())
		{
			double weight = findWeight(activeUserId,key);
			weights.put(key,weight);
			value += Math.abs(weight);
		}
		
		
		normalizationFactor = 1.0/value; 
		
		
		double predicatedRating = meanVoteForUserIds.get(activeUserId);
		double temp = 0.0;
		for(String key: movieIdsAndUserIdsAndRatings.get(movieId).keySet())
		{
			temp += weights.get(key)*(userdsAndMovieIdsRatings.get(key).get(movieId) - meanVoteForUserIds.get(key));
		}
		
		predicatedRating += normalizationFactor*temp;
		
	    return predicatedRating;
	}
	
	
	//Start of findWeight function
	public static double findWeight(String activeUserId, String userId)
	{
		
		//Getting the mean vote of the active user and userid
		double meanVoteForActiveUser = meanVoteForUserIds.get(activeUserId);
		double meanVoteForUserId = meanVoteForUserIds.get(userId);
		
		double numerator = 0.0;
		double denominator = 0.0;
		double value1 = 0.0;
		double value2 = 0.0;
		//Iterating over the movies of hashMap1 to find which movies contain both the active user and userid
		for(String key: movieIdsAndUserIdsAndRatings.keySet())
		{
			HashMap<String,Double> userIdsAndRatings = movieIdsAndUserIdsAndRatings.get(key);
			
			if(userIdsAndRatings.containsKey(activeUserId)&&userIdsAndRatings.containsKey(userId))
			{
				
				double ratingOfActiveUser = userIdsAndRatings.get(activeUserId);
				double ratingOfUserId = userIdsAndRatings.get(userId);
				double value = (ratingOfActiveUser - meanVoteForActiveUser)*(ratingOfUserId - meanVoteForUserId);
				numerator += value;
				
				value1 += (ratingOfActiveUser - meanVoteForActiveUser)*(ratingOfActiveUser - meanVoteForActiveUser);
				value2 += (ratingOfUserId - meanVoteForUserId)*(ratingOfUserId - meanVoteForUserId);
				
				
			}
			
			
		}
		
		denominator = Math.sqrt(value1 * value2);
		
		//Finding the weight and returning it
		double weight = numerator / denominator;
		if(Double.isNaN(weight))
			return 1.0;
		else
		    return weight;
	}
	//End of findWeight function
	
	//Start of calculateAndStoreMeanVoteForUser function
	public static void calculateAndStoreMeanVoteForUser(String userId)
	{
	
		    double meanVote = 0.0;
			
			//Finding the movies which the user have rated
			for(Double value: userdsAndMovieIdsRatings.get(userId).values())
			{
				
				meanVote += value;
							
			}
			
			meanVote = meanVote/userdsAndMovieIdsRatings.get(userId).size();
			
			meanVoteForUserIds.put(userId,meanVote);
	}
	//End of calculateAndStoreMeanVoteForUser function
}
