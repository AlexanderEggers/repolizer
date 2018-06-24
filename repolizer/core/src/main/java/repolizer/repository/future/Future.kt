package repolizer.repository.future

abstract class Future<E, O> {

    abstract fun get(): E

    abstract fun execute(): O
}