package dev.lackluster.hyperx.animation.property

import android.util.FloatProperty
import android.view.View
import cn.fkj233.ui.R

abstract class ViewProperty(name: String): FloatProperty<View>(name) {
    companion object {
        val TRANSLATION_Y: ViewProperty = object : ViewProperty("translationY") {
            override fun get(p0: View?): Float {
                return p0?.translationY ?: 0.0f
            }
            override fun setValue(p0: View?, p1: Float) {
                p0?.translationY = p1
            }
        }
        val SCALE_X: ViewProperty = object : ViewProperty("scaleX") {
            override fun get(p0: View?): Float {
                return p0?.scaleX ?: 0.0f
            }
            override fun setValue(p0: View?, p1: Float) {
                p0?.scaleX = p1
            }
        }
        val SCALE_Y: ViewProperty = object : ViewProperty("scaleY") {
            override fun get(p0: View?): Float {
                return p0?.scaleY ?: 0.0f
            }
            override fun setValue(p0: View?, p1: Float) {
                p0?.scaleY = p1
            }
        }
        val ALPHA: ViewProperty = object : ViewProperty("alpha") {
            override fun get(p0: View?): Float {
                return p0?.alpha ?: 0.0f
            }
            override fun setValue(p0: View?, p1: Float) {
                p0?.alpha = p1
            }
        }
        fun isInInitLayout(view: View?): Boolean {
            return view?.getTag(R.id.hyperx_animation_tag_init_layout) != null
        }
    }

    override fun toString(): String {
        return "ViewProperty{mPropertyName='$name'}"
    }
}