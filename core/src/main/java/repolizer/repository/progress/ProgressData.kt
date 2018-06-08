package repolizer.repository.progress

import repolizer.repository.util.RequestType

abstract class ProgressData {
    lateinit var requestType: RequestType
        internal set
}