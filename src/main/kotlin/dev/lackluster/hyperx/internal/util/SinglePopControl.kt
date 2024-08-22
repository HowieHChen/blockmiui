package dev.lackluster.hyperx.internal.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.SparseArray
import android.widget.PopupWindow
import java.lang.ref.WeakReference

class SinglePopControl {
    companion object {
        private val sPops: SparseArray<WeakReference<Any>> = SparseArray()
        fun showPop(context: Context, obj: Any) {
            val hashKey = getHashKey(context)
            if (sPops.get(hashKey) != null) {
                val obj2 = sPops.get(hashKey).get()
                if (obj2 != null && obj2 != obj) {
                    hide(obj2)
                }
            }
            sPops.put(hashKey, WeakReference(obj))
        }
        fun hidePop(context: Context, obj: Any) {
            sPops.remove(getHashKey(context))
        }
        private fun hide(obj: Any) {
            if (obj is PopupWindow) {
                if (obj.isShowing) {
                    obj.dismiss()
                }
            }
        }
        private fun getHashKey(context: Context): Int {
            val associatedActivity = getAssociatedActivity(context)
            return associatedActivity?.hashCode() ?: context.hashCode()
        }

        private fun getAssociatedActivity(context: Context): Activity? {
            var activity: Activity? = null
            var context2: Context? = context
            while (activity == null && context2 != null) {
                if (context2 is Activity) {
                    activity = context2
                } else {
                    context2 = if (context2 is ContextWrapper) context2.baseContext else null
                }
            }
            return activity
        }
    }
}