/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mdcatapult.klein.mongo

import io.doclib.util.models.result._
import org.mongodb.scala.result.{DeleteResult, InsertManyResult, InsertOneResult, UpdateResult}

/**
 * Convert Mongo return objects into simpler mdcatapult util result objects.
 * Util result objects should always be preferred whenever possible.
 */
object ResultConverters {

  def toDeletionResult(r: DeleteResult): DeletionResult =
    DeletionResult(r.wasAcknowledged, r.getDeletedCount)

  def toInsertionResult(r: InsertManyResult): InsertionResult =
    InsertionResult(r.wasAcknowledged, r.getInsertedIds.size)

  def toInsertionResult(r: InsertOneResult): InsertionResult =
    InsertionResult(r.wasAcknowledged, 1)

  def toUpdatedResult[T](r: UpdateResult): UpdatedResult =
    UpdatedResult(r.wasAcknowledged, r.getModifiedCount, r.getMatchedCount)
}