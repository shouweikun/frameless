package frameless
package ml
package regression

import frameless.ml.feature.TypedVectorAssembler
import org.apache.spark.ml.linalg.Vector
import org.scalatest.MustMatchers

class RegressionIntegrationTests extends FramelessMlSuite with MustMatchers {

  test("predict field3 from field1 and field2 using a RandomForestRegressor") {
    case class Data(field1: Double, field2: Int, field3: Double)

    // Training

    val trainingDataDs = TypedDataset.create(Seq.fill(10)(Data(0D, 10, 0D)))

    case class Features(field1: Double, field2: Int)
    val vectorAssembler = TypedVectorAssembler.create[Features]()

    case class DataWithFeatures(field1: Double, field2: Int, field3: Double, features: Vector)
    val dataWithFeatures = vectorAssembler.transform(trainingDataDs).run().as[DataWithFeatures]

    case class RFInputs(field3: Double, features: Vector)
    val rf = TypedRandomForestRegressor.create[RFInputs]()

    val model = rf.fit(dataWithFeatures).run()

    // Prediction

    val testData = TypedDataset.create(Seq(
      Data(0D, 10, 0D)
    ))
    val testDataWithFeatures = vectorAssembler.transform(testData).run().as[DataWithFeatures]

    case class PredictionResult(field1: Double, field2: Int, field3: Double, features: Vector, predictedField3: Double)
    val predictionDs = model.transform(testDataWithFeatures).run().as[PredictionResult]

    val prediction = predictionDs.select(predictionDs.col('predictedField3)).collect.run().toList

    prediction mustEqual List(0D)
  }

}