package com.testing

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession, functions} // for lit(), first(), etc.

object UserEvents extends Serializable {

  def main(args: Array[String]): Unit = {

    functions.udf[String, String](i => i)

    val spark = SparkSession.builder()
      .appName("UserSessionEvents").master("local[*]").getOrCreate()
    //.appName("UserSessionEvents").master("spark://irach-Inspiron:7077").getOrCreate()
    case class Header(key: String, value: Option[String])
    case class Reg(headers: Map[String,String], body: Array[Byte])

    //def selectBodyFrame(dataFrame: DataFrame): Dataset[Reg] = {
    //  dataFrame.as[Reg]
    //}

    import com.databricks.spark.avro._
    // FIXME: Normally we read the file from hadoop
    val events: DataFrame = spark.sqlContext.read.avro("file:///home/irach/Downloads/data_20170101.log.1492718554874.avro")
    events.printSchema()
    //    events.as[Reg]
    val bodyRDD: RDD[String] = selectBodyFrom(events).map(toStringBody)
    // val dataSet = selectBodyFrame(events)
    // dataSet.show()
    //val bodyDF2: DataFrame = spark.read.json(dataSet)
    val bodyDF: DataFrame = spark.read.json(bodyRDD)

    val resp: Array[Row] = countEventsBySessionId(bodyDF).collect()

    resp.foreach(storeSessionCount)

    println(" reading.....")
    System.in.read()


    spark.stop()
  }

  def selectBodyFrom(dataFrame: DataFrame): RDD[Row] = {
    dataFrame.select("body").rdd
  }

  def countEventsBySessionId(dataFrame: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions.count
    dataFrame.groupBy("uuid").agg(count("uuid"))
  }

  def toStringBody(row: Row): String = row match {
    case Row(body: Array[Byte]) => new String(body)
    case _ => "{}"
  }

  def storeSessionCount(row:Row): Unit = row match {
    // FIXME: normally we save the result somewhere.
    case Row(uuid:String, count:Long)=> println(s" event $uuid has being emitted $count times")
    case _ => println(row)
  }

}
