package com.testing

import org.apache.spark.sql.SparkSession

trait SparkSpec {
  /*
    private var _sc: SparkContext = _

    override def beforeAll(): Unit = {
      super.beforeAll()

      val conf = new SparkConf()
        .setMaster("local[*]")
        .setAppName(this.getClass.getSimpleName)

      sparkConfig.foreach { case (k, v) => conf.setIfMissing(k, v) }

      _sc = new SparkContext(conf)
    }

    def sparkConfig: Map[String, String] = Map.empty

    override def afterAll(): Unit = {
      if (_sc != null) {
        _sc.stop()
        _sc = null
      }
      super.afterAll()
    }

    def sc: SparkContext = _sc
    */
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("spark test example")
      .getOrCreate()
  }

}
