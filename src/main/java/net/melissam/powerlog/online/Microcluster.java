package net.melissam.powerlog.online;

import java.util.Arrays;
import java.util.Date;

/**
 * Representation of a micro-cluster maintained in the online phase.
 * 
 * @author melissam
 *
 */
public class MicroCluster {

	/** Number of elements in the cluster. */
	private int size;
	
	/** Sum of squares of the values of each feature vector added to the micro-cluster. */
	//vector(CF2x)
	private double[] sumOfSquaresOfValues;
	
	/** Linear sum of values of each feature vector added to the micro-cluster. */
	// vector(CF1x)
	private double[] sumOfValues;
	
	/** Sum of the squares of the timestamps at which each feature vector arrives. */
	// CF2t
	private double sumOfSquaresOfTimestamps;
	
	
	/** Sum of timestamps at which each feature vector arrives. */
	// CF1t
	private double sumOfTimestamps;
	
	/** 
	 * Factor used for predicting whether a new feature vector belongs to the cluster.
	 * Used for calculating the maximum boundary of the micro-cluster -> a factor of t of the the root-square-means deviation of 
	 * all the data points in the cluster.
	 */
	private double t;
	
	/** Cluster number. */
	// The algorithms defines 'm' as the maximum number of microclusters the algorithm should create
	// Here 'm' represents the sequence number of the particular micro-cluster amoung all micro-clusters.
	private int m;
	
	
	/**
	 * Construct a Microcluster from a single feature vector. This initial addition is also the center of the cluster.
	 * @param center
	 */
	public MicroCluster(double[] center, long timestamp, double t, int m){
		
		// nothing to add with
		sumOfValues	= center;
		
		// sum the values of center and populate sumOfSquares
		sumOfSquaresOfValues = new double[center.length];
		for (int i = 0; i<center.length; i++){
			sumOfSquaresOfValues[i] = Math.pow(center[i], 2);
		}
		
		// first timestamp
		this.sumOfTimestamps = timestamp;
		
		// sum of square of timestamps
		this.sumOfSquaresOfTimestamps = Math.pow(timestamp, 2);
		
		this.t = t;
		this.m = m;
	}
	
	/**
	 * Add a feature vector to this micro-cluster.
	 * 
	 * @param featureVector 
	 * @param timestamp
	 */
	public void addFeatureVector(double[] featureVector, long timestamp){
		
		assert(featureVector.length == sumOfValues.length);
		
		// adjust sumOfValues
		for (int i = 0; i < sumOfValues.length; i++){
			sumOfValues[i] += featureVector[i];
		}
		
		// adjust sum of squares
		for (int i = 0; i < sumOfSquaresOfValues.length; i++){
			sumOfSquaresOfValues[i] += Math.pow(featureVector[i], 2);
		}
		
		// adjust timestamp values
		sumOfTimestamps += timestamp;
		sumOfSquaresOfTimestamps += Math.pow(timestamp, 2);
		
		// increment size
		size++;
	}
	
	
	/**
	 * Get the center of the micro-cluster from the sum of all feature vectors the cluster contains.
	 * 
	 * @return A vector of center values.
	 */
	public double[] getCenter(){
		
		assert (size > 0);
		double center[] = new double[sumOfValues.length];
		for (int i = 0; i < sumOfValues.length; i++) {
			center[i] = sumOfValues[i] / size;
		}
		return center;
		
	}
	
	
	
	/**
	 * Calculates the (Euclidean) distance of a point from the centroid of this micro-cluster.	 * 
	 * This is required to decide which cluster a point belongs to.
	 * 
	 * @param point The point to find the distance of.
	 * @return	The distance of the point from the center of this micro-cluster
	 */
	public double getDistance(double[] point){
	
		double[] center = getCenter();
		double distance = 0.0;
		
		for (int i = 0; i < center.length; i++){
			
			//square o
			distance += Math.pow(center[i] - point[i], 2);
			
		}
		
		return Math.sqrt(distance);
		
	}
	
	
	
	/**
	 * Returns the maximum boundary of the cluster. This is defined as a factor of t of the root-means-square deviation of the data points from the center.
	 * 
	 * For clusters with 1 point, then the maximum boundary is the distance to the closest cluster, which needs to be calculated externally to the cluster.
	 * In the case of a 1 point cluster, this method will return 0 so that the online algorithm knows it needs to find the closest cluster instead.
	 * 
	 * @return The maximum boundary of the cluster. This is also referred to as the radius.
	 */
	public double getMaximumBoundary(){
	
		if(size == 1) return 0; 			// paper says "If the cluster only has one point the maximum boundary is the distance to the closest cluster
											// this needs to be calculated external to the micro-cluster, so return 0 will be indicative of this
		else return getDeviation() * t;
		
	}
	
	
	/**
	 * 
	 * @param other
	 */
	public void merge(MicroCluster other){
		
	}
	
	
	// ------------------ Private methods. ---------------------- /
	
	// Calculation of root-square-means deviation
	
	// Calculate the variation of the points from the center
	private double[] getVariance(){
		
		double[] variance = new double[this.sumOfValues.length];
	
		for (int i = 0; i < this.sumOfValues.length; i++) {
				 
			// this is the value of the center CF for point i
			double avgSum = this.sumOfValues[i] / this.size;
		  	
			// this is the sum of squares center CF for point i
		    double avgSumOfSquares = this.sumOfSquaresOfValues[i] / size;
            
		    // calculate the variance 
		    // TODO: confirm that taking the absolute value is the correct way to deal with negatives here?
		    // shall we round to 0 instead?
		    variance[i] = Math.abs(avgSumOfSquares - Math.pow(avgSum, 2));
		    
		}
		 
		return variance;
		
	}
	
	
	// The root-means-square (RMS) deviation
	private double getDeviation(){
		
		// get the variance of all points from the centroid
		double[] variance = getVariance();
		double sumOfDeviation = 0.0;
		
		// calculate the sum of the square roots of the variance of each data point
		for (int i = 0; i < variance.length; i++) {
		    sumOfDeviation += Math.sqrt(variance[i]);;
		}
		
		// take the mean value
		return sumOfDeviation / variance.length;
	}
	
}
