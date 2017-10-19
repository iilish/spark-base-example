package com.testing

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.scalatest._

class UserEventsTest extends FeatureSpec with GivenWhenThen with SparkSpec {

  feature("User events") {
    import spark.implicits._

    scenario("a simple table with one column") {

      Given("file in DF ")
      val rdd = Seq("one", "two", "three").toDF("body")

      When("select body")
      val result = UserEvents.selectBodyFrom(rdd)

      Then("I got the expected DF with One column")
      val expectedSchema = List(StructField("body", StringType, nullable = true))
      val expectedData = Seq(Row("one"), Row("two"), Row("three"))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData), StructType(expectedSchema))
      assert(result.collect().sameElements(expectedDF.collect()))

    }

    scenario("a simple table with 2 columns") {

      Given("file in DF with 2 columns")
      val rdd = Seq(("one",1), ("two",2), ("three",3)).toDF("body", "type")

      When("select body")
      val result = UserEvents.selectBodyFrom(rdd)

      Then("I got the expected DF with2 columns")
      val expectedSchema = List(StructField("body", StringType, nullable = true))
      val expectedData = Seq(Row("one"),Row("two"),Row("three"))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData),StructType(expectedSchema))

      assert(result.collect().sameElements(expectedDF.collect()))
    }

    scenario("test toStringBody") {

      Given("a byte array")
      val row = Row("eso es todo amigos".getBytes)

      When("converted to string")
      val result = UserEvents.toStringBody(row)

      Then("the body is retrieved")
      assert(result.equals("eso es todo amigos"))
    }

    scenario("convert events") {

      Given("DF in columns")
      val dataFrame = Seq(("one","toto"), ("two","titi"), ("two","ttz"),("one","toto"), ("two","titi"), ("three","ttz")).toDF("uuid", "saying")

      When("parse body")
      val result = UserEvents.countEventsBySessionId(dataFrame)

      Then("I got the expected DF with 2 columns")
      val expectedSchema = List(StructField("uuid", StringType, nullable = true), StructField("grouped", IntegerType, nullable = true))
      val expectedData = Seq(Row("two",3),Row("one",2),Row("three",1))
      val expectedDF = spark.createDataFrame(spark.sparkContext.parallelize(expectedData),StructType(expectedSchema))

      assert(result.collect().sameElements(expectedDF.collect()))
    }
  }

}

