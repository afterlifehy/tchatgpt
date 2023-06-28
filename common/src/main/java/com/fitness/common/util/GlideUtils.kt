package com.fitness.common.util

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.Glide
import com.fitness.base.BaseApplication

class GlideUtils private constructor() {
    /**
     * 初始化一个默认的配置
     */
    private fun initOption() {
        defaultOption = RequestOptions()
//        defaultOption!!.error(R.mipmap.ic_default_placeholder).placeholder(R.mipmap.ic_default_placeholder)
        /*
            DiskCacheStrategy.NONE： 表示不缓存任何内容。
            DiskCacheStrategy.DATA： 表示只缓存原始图片。
            DiskCacheStrategy.RESOURCE： 表示只缓存转换过后的图片。
            DiskCacheStrategy.ALL ： 表示既缓存原始图片，也缓存转换过后的图片。
            DiskCacheStrategy.AUTOMATIC： 表示让Glide根据图片资源智能地选择使用哪一种缓存策略（默认选项）
         */defaultOption!!.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    }

    fun loadImage(iv: ImageView?, url: String?) {
        Glide.with(BaseApplication.instance()).load(url).apply(defaultOption!!).into(iv!!)
    }

    fun loadImage(iv: ImageView?, url: Int) {
        Glide.with(BaseApplication.instance()).load(url).apply(defaultOption!!).into(iv!!)
    }

    fun loadImageNoOption(iv: ImageView?, url: Int) {
        Glide.with(BaseApplication.instance()).load(url).into(iv!!)
    }

    fun loadImage(iv: ImageView?, url: Bitmap?) {
        Glide.with(BaseApplication.instance()).load(url).apply(defaultOption!!).into(iv!!)
    }

    fun loadImage(iv: ImageView?, url: String?, placeholder: Int) {
        val options =
            RequestOptions().error(placeholder).placeholder(placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(BaseApplication.instance()).load(url).apply(options).into(iv!!)
    }

    fun loadImage(iv: ImageView?, url: Int, placeholder: Int) {
        val options =
            RequestOptions().error(placeholder).placeholder(placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(BaseApplication.instance()).load(url).apply(options).into(iv!!)
    }

    fun loadImage(iv: ImageView, url: String?, placeholder: Int, mRadius: Int) {
        val options = RequestOptions.noAnimation().transform(CropRoundRadiusTransformation(iv.context, mRadius))
            .error(placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(BaseApplication.instance()).load(url).apply(options).into(iv)
    }

    companion object {
        private var mGlideUtils: GlideUtils? = null

        /**
         * 返回默认的配置
         *
         * @return
         */
        var defaultOption: RequestOptions? = null
        val instance: GlideUtils?
            get() {
                if (mGlideUtils == null) {
                    synchronized(GlideUtils::class.java) {
                        if (mGlideUtils == null) {
                            mGlideUtils = GlideUtils()
                        }
                    }
                }
                return mGlideUtils
            }
    }

    init {
        initOption()
    }
}