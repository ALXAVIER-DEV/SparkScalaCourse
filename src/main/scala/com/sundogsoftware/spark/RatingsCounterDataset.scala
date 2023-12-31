package com.sundogsoftware.spark

import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import org.apache.log4j._
import org.apache.spark.sql.types.{IntegerType, LongType, StructType}
import org.apache.spark.sql.functions.{col, to_date, to_timestamp}
import org.apache.spark.sql.types.DateType


/** Count up how many of each star rating exists in the MovieLens 100K data set. */
object RatingsCounterDataset {

  // Create case class with schema of u.data
  case class UserRatings(userID: Int, movieID: Int, rating: Int, timestamp: Long)

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkSession using every core of the local machine, named RatingsCounter
    val spark = SparkSession
      .builder
      .appName("RatingsCounter")
      .master("local[*]")
      .getOrCreate()

    // Create schema when reading u.data
    val userRatingsSchema = new StructType()
      .add("userID", IntegerType, nullable = true)
      .add("movieID", IntegerType, nullable = true)
      .add("rating", IntegerType, nullable = true)
      .add("timestamp", LongType, nullable = true)
    //val df = userRatingsSchema.show()

    // Load up the data into spark dataset
    // Use tab as separator, load schema from userRatingsSchema and force case class to read it as dataset
    import spark.implicits._
    val ratingsDS_ = spark.read
      .option("sep", "\t")
      .schema(userRatingsSchema)
      .csv("/Users/alxavier-dev/Downloads/ml-100k/u.data")
      .as[UserRatings]
    ratingsDS_.show()

    //spark.sql("select to_timestamp(1563853753) as ts").show(false)
    //ratingsDS_.withColumn("timestamp",to_timestamp("timestamp"),"yyyy-MM-dd").show(false)
    // Select only ratings column
    // (The file format is userID, movieID, rating, timestamp)
    val ratings = ratingsDS_.select("rating")

    // Count up how many times each value (rating) occurs
    val results = ratings.groupBy("rating").count()

    // Sort the resulting dataset by count column
    val sortedResults = results.sort("count")

    // Print results from the dataset
    sortedResults.show()
  }
}
