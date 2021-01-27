package com.darkminstrel.expandable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

private const val DEFAULT_SHADOW_LENGTH_DP = 16
private const val DEFAULT_COLLAPSED_HEIGHT_DP = 128
private const val DEFAULT_DURATION = 200
private const val DEFAULT_EXPANDED = false

class ExpandableContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val density:Float = context.resources.displayMetrics.density

    private var shadowHeight = DEFAULT_SHADOW_LENGTH_DP * density
    private var collapsedHeight = DEFAULT_COLLAPSED_HEIGHT_DP * density
    private var animationDuration = DEFAULT_DURATION
    private var expanded = DEFAULT_EXPANDED
    private var contentFits = true
    private var onContentFitsChangeListener:((contentFits: Boolean)->Unit)? = null

    private var childDesiredHeight = 0
    private var oldChildDesiredHeight = 0
    private var animator:ValueAnimator?=null
    private var dirty = true
    private val colors = intArrayOf(Color.BLACK, Color.TRANSPARENT)
    private val rect = Rect()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandableContainer)
            shadowHeight = ta.getDimension(R.styleable.ExpandableContainer_expandableShadowHeight, shadowHeight)
            collapsedHeight = ta.getDimension(R.styleable.ExpandableContainer_expandableCollapsedHeight, collapsedHeight)
            animationDuration = ta.getInt(R.styleable.ExpandableContainer_expandableDuration, animationDuration)
            expanded = ta.getBoolean(R.styleable.ExpandableContainer_expandableExpandedByDefault, expanded)
            ta.recycle()
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if(childCount>1) throw IllegalStateException("ExpandableContainer can host only one direct child")
    }

    fun isExpanded():Boolean = expanded

    fun setExpanded(expanded: Boolean){
        this.expanded = expanded
        animator?.cancel()
        animator = null
        val src = height
        val dst = if(childDesiredHeight < collapsedHeight || expanded) childDesiredHeight else collapsedHeight.toInt()
        if(src!=dst) {
            animator = ValueAnimator.ofInt(src, dst).apply {
                duration = animationDuration.toLong()
                addUpdateListener { requestLayout() }
                start()
            }
        }
    }

    fun toggle(){
        setExpanded(!expanded)
    }

    fun contentFits() = contentFits

    fun setOnContentFitsChangeListener(listener: ((contentFits: Boolean) -> Unit)?){
        this.onContentFitsChangeListener = listener
    }

    private fun getChild():View? = if(childCount==1) getChildAt(0) else null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChild()
        child?.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED)
        childDesiredHeight = child?.measuredHeight ?: 0

        if(childDesiredHeight!=oldChildDesiredHeight) {
            animator?.cancel()
            animator = null
            oldChildDesiredHeight = childDesiredHeight
            contentFits = childDesiredHeight <= collapsedHeight
            onContentFitsChangeListener?.invoke(contentFits)
        }

        animator?.let{
            val newHeight = it.animatedValue as Int
            if(it.animatedFraction==1f) animator = null
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY))
        } ?: run{
            val newHeight = if (childDesiredHeight < collapsedHeight || expanded) childDesiredHeight else collapsedHeight.toInt()
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY))
        }

    }


    private fun hasShadow():Boolean {
        return childDesiredHeight > height
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dirty = true
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        dirty = true
    }

    private fun updateGradient() {
        val actualHeight = height - paddingTop - paddingBottom
        val size = shadowHeight.toInt().coerceAtMost(actualHeight)
        val l = paddingLeft
        val t = paddingTop + actualHeight - size
        val r = width - paddingRight
        val b = t + size
        rect.set(l, t, r, b)
        val gradient = LinearGradient(l.toFloat(), t.toFloat(), l.toFloat(), b.toFloat(), colors, null, Shader.TileMode.CLAMP)
        paint.shader = gradient
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (visibility != VISIBLE || width == 0 || height == 0 || shadowHeight==0f || !hasShadow()) {
            super.dispatchDraw(canvas)
            return
        }

        if(dirty){
            dirty = false
            updateGradient()
        }
        val count = canvas.saveLayer(0.0f, 0.0f, width.toFloat(), height.toFloat(), null)
        super.dispatchDraw(canvas)
        if (shadowHeight > 0) canvas.drawRect(rect, paint)
        canvas.restoreToCount(count)
    }
}