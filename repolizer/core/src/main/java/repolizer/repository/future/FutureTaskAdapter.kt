package repolizer.repository.future

abstract class FutureTaskAdapter: FutureTaskCallback {

    override fun onExecute() {
        //do nothing by default
    }

    override fun onFinished() {
        //do nothing by default
    }
}