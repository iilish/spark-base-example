package com.testing

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.scalatest.FunSpec

class UserEventsSpec extends FunSpec with SparkSpec {

  import spark.implicits._

  describe(".selectBodyFrom") {
    it("should collect body column") {
      // Given
      val rdd = Seq("one", "two", "three").toDF("body")

      // When
      val result = UserEvents.selectBodyFrom(rdd)

      // Then
      val expectedSchema = List(StructField("body", StringType, nullable = true))
      val expectedData = Seq(Row("one"), Row("two"), Row("three"))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData), StructType(expectedSchema))
      assert(result.collect().sameElements(expectedDF.collect()))
    }

    it("Should use the right body column") {
      // Given
      val rdd = Seq(("one",1), ("two",2), ("three",3)).toDF("body", "type")

      // When
      val result = UserEvents.selectBodyFrom(rdd)

      // Then
      val expectedSchema = List(StructField("body", StringType, nullable = true))
      val expectedData = Seq(Row("one"),Row("two"),Row("three"))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData),StructType(expectedSchema))

      assert(result.collect().sameElements(expectedDF.collect()))
    }

  }

  describe(".toStringBody"){
    it("should turn a byte array into a string"){
      // Given
      val row = Row("eso es todo amigos".getBytes)

      // When
      val result = UserEvents.toStringBody(row)

      // Then
      assert(result.equals("eso es todo amigos"))
    }
  }

  describe("countEventsBySessionId") {
    it("should count the uuid columns") {
      // Given
      val dataFrame = Seq(("one","toto"), ("two","titi"), ("two","ttz"),("one","toto"), ("two","titi"), ("three","ttz")).toDF("uuid", "saying")

      // When parse body
      val result = UserEvents.countEventsBySessionId(dataFrame)

      // Then
      val expectedSchema = List(StructField("uuid", StringType, nullable = true), StructField("grouped", IntegerType, nullable = true))
      val expectedData = Seq(Row("two",3),Row("one",2),Row("three",1))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData),StructType(expectedSchema))

      assert(result.collect().sameElements(expectedDF.collect()))
    }
  }
}
