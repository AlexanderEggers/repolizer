package repolizer.repository.progress

import repolizer.repository.request.RequestType

abstract class ProgressData {
    lateinit var requestType: RequestType
        internal set
}