@file:JvmName("-InterceptorChain")
package dev.b3nedikt.viewpump.internal

import dev.b3nedikt.viewpump.InflateRequest
import dev.b3nedikt.viewpump.InflateResult
import dev.b3nedikt.viewpump.Interceptor
import dev.b3nedikt.viewpump.Interceptor.Chain

/**
 * A concrete interceptor chain that carries the entire interceptor chain.
 */
@Suppress("ClassName")
internal class `-InterceptorChain`(private val interceptors: List<Interceptor>, private val index: Int,
    private val request: InflateRequest) : Chain {

  override fun request(): InflateRequest {
    return request
  }

  override fun proceed(request: InflateRequest): InflateResult {
    if (index >= interceptors.size) {
      throw AssertionError("no interceptors added to the chain")
    }

    // Call the next interceptor in the chain.
    val next = `-InterceptorChain`(interceptors, index + 1,
        request)
    val interceptor = interceptors[index]

    return interceptor.intercept(next)
  }
}