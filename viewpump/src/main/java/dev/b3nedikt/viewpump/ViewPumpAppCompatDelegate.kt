@file:Suppress("PackageDirectoryMismatch")

package androidx.appcompat.app

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.LayoutInflaterCompat
import dev.b3nedikt.viewpump.InflateRequest
import dev.b3nedikt.viewpump.InflateResult
import dev.b3nedikt.viewpump.ViewPump
import dev.b3nedikt.viewpump.internal.InterceptorChain
import dev.b3nedikt.viewpump.internal.LegacyLayoutInflater

/**
 * A [AppCompatDelegate] to be used with [ViewPump]
 *
 * @param baseDelegate the [AppCompatDelegate] which will handle all calls which [ViewPump] does
 * not need to overwrite
 * @param baseContext the [Context] which will be used to retrieve the [LayoutInflater] to install
 * [ViewPump]s [LayoutInflater.Factory2]
 * @param wrapContext optional function to wrap the [Context] after it has been attached
 */
class ViewPumpAppCompatDelegate @JvmOverloads constructor(
        baseDelegate: AppCompatDelegate,
        private val baseContext: Context,
        wrapContext: ((baseContext: Context) -> Context)? = null
) : AppCompatDelegateWrapper(baseDelegate, wrapContext), LayoutInflater.Factory2 {

    override fun installViewFactory() {
        val layoutInflater = LayoutInflater.from(baseContext)
        if (layoutInflater.factory == null) {
            LayoutInflaterCompat.setFactory2(layoutInflater, this)
        } else {
            if (layoutInflater.factory2 !is AppCompatDelegateImpl) {
                Log.i(TAG, "The Activity's LayoutInflater already has a Factory installed"
                        + " so we can not install ViewPump's")
            }
        }
    }

    override fun createView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        return inflate(
                InflateRequest(
                        name = name,
                        context = context,
                        attrs = attrs,
                        parent = parent,
                        fallbackViewCreator = {
                            var view = super.createView(parent, name, context, attrs)

                            if (view == null) {
                                view = runCatching {
                                    createViewCompat(context, name, attrs)
                                }.getOrNull()
                            }

                            view
                        }
                )
        ).view
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        return createView(parent, name, context, attrs)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return createView(null, name, context, attrs)
    }

    private fun inflate(originalRequest: InflateRequest): InflateResult {
        val chain = InterceptorChain(
                interceptors = ViewPump.interceptors ?: emptyList(),
                index = 0,
                request = originalRequest
        )

        return chain.proceed(originalRequest)
    }

    private fun createViewCompat(context: Context, name: String, attrs: AttributeSet): View? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LayoutInflater.from(baseContext).createView(context, name, null, attrs)
        } else {
            LegacyLayoutInflater(context).createViewLegacy(context, name, attrs)
        }
    }
}