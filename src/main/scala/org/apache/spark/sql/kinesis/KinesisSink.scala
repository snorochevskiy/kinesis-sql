/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.kinesis

import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.streaming.OutputMode

private[kinesis] class KinesisSink(sqlContext: SQLContext,
                                   sinkOptions: Map[String, String],
                                   outputMode: OutputMode)
  extends Sink with Logging {

  @volatile private var latestBatchId = -1L

  override def toString: String = "KinesisSink"

  override def addBatch(batchId: Long, data: DataFrame): Unit = {
    if (batchId <= latestBatchId) {
      logInfo(s"Skipping already committed batch $batchId")
    } else {
      KinesisWriter.write(sqlContext.sparkSession, data.queryExecution, sinkOptions)
      latestBatchId = batchId
    }
  }
}
