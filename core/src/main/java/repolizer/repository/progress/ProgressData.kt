package repolizer.repository.progress

import repolizer.repository.response.RequestType

abstract class ProgressData {
    lateinit var requestType: RequestType
        internal set
}