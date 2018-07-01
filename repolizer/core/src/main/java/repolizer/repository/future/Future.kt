package repolizer.repository.future

abstract class Future<B> {

    abstract fun execute(): B?
}