package io.mdcatapult.klein.mongo

import java.time.LocalDateTime
import java.util.UUID

case class TestDoc(
                    _id: UUID,
                    created: LocalDateTime
                  )
