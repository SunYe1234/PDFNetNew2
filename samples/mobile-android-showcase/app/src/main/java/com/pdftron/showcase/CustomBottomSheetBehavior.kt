package com.pdftron.showcase

import android.content.Context
import android.os.Build
import android.support.annotation.IntDef
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP
import android.support.annotation.VisibleForTesting
import android.support.design.widget.CoordinatorLayout
import android.support.v4.math.MathUtils
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.*
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.ref.WeakReference
import java.util.*

class CustomBottomSheetBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    /**
     * @return whether the height of the expanded sheet is determined by the height of its contents,
     * or if it is expanded in two stages (half the height of the parent container, full height of
     * parent container).
     */
    /**
     * Sets whether the height of the expanded sheet is determined by the height of its contents, or
     * if it is expanded in two stages (half the height of the parent container, full height of parent
     * container). Default value is true.
     *
     * @param fitToContents whether or not to fit the expanded sheet to its contents.
     */
    // If sheet is already laid out, recalculate the collapsed offset based on new setting.
    // Otherwise, let onLayoutChild handle this later.
    // Fix incorrect expanded settings depending on whether or not we are fitting sheet to contents.
    var isFitToContents = true
        set(fitToContents) {
            if (this.isFitToContents == fitToContents) {
                return
            }
            field = fitToContents
            if (viewRef != null) {
                calculateCollapsedOffset()
            }
            setStateInternal(if (this.isFitToContents && state == STATE_HALF_EXPANDED) STATE_EXPANDED else state)
        }

    private val maximumVelocity: Float

    /** Peek height set by the user.  */
    private var peekHeight: Int = 0

    private var minHeight: Int = 0

    /** Whether or not to use automatic peek height.  */
    private var peekHeightAuto: Boolean = false

    /** Minimum peek height permitted.  */
    @get:VisibleForTesting
    internal var peekHeightMin: Int = 0
        private set

    /** The last peek height calculated in onLayoutChild.  */
    private var lastPeekHeight: Int = 0

    internal var fitToContentsOffset: Int = 0

    internal var halfExpandedOffset: Int = 0

    var collapsedOffset: Int = 0

    var hideOffset: Int = 0




    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return `true` if this bottom sheet can hide.
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable `true` to make this bottom sheet hideable.
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    var isHideable: Boolean = false

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
     * is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
     * is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    var skipCollapsed: Boolean = false

    @State
    internal var state = STATE_COLLAPSED

    internal var viewDragHelper: ViewDragHelper? = null

    private var ignoreEvents: Boolean = false

    private var lastNestedScrollDy: Int = 0

    private var nestedScrolled: Boolean = false

    internal var parentHeight: Int = 0

    internal var viewRef: WeakReference<V>? = null

    internal var nestedScrollingChildRef: WeakReference<View>? = null

    internal var innerNestedScrollingChildRef: WeakReference<View>? = null

    private var touchingInnerScrollView: Boolean = false

    private var callback: BottomSheetCallback? = null

    private var velocityTracker: VelocityTracker? = null

    internal var activePointerId: Int = 0

    private var initialY: Int = 0

    internal var touchingScrollingChild: Boolean = false

    private var importantForAccessibilityMap: MutableMap<View, Int>? = null

    private val yVelocity: Float
        get() {
            if (velocityTracker == null) {
                return 0f
            }
            velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity)
            return velocityTracker!!.getYVelocity(activePointerId)
        }

    private val expandedOffset: Int
        get() = if (isFitToContents) fitToContentsOffset else 0

    private val dragCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (state == STATE_DRAGGING) {
                return false
            }
            if (touchingScrollingChild) {
                return false
            }
            if (state == STATE_EXPANDED && activePointerId == pointerId) {
                val scroll = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false
                }
            }
            return viewRef != null && viewRef!!.get() === child
        }

        override fun onViewPositionChanged(
                changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(top)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top: Int
            @State val targetState: Int
            if (yvel < 0) { // Moving up
                if (isFitToContents) {
                    val currentTop = releasedChild.top
                    if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
                        top = fitToContentsOffset
                        targetState = STATE_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                } else {
                    val currentTop = releasedChild.top
                    if (currentTop > halfExpandedOffset) {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    } else {
                        top = 0
                        targetState = STATE_EXPANDED
                    }
                }
            } else if (isHideable
                    && shouldHide(releasedChild, yvel)
                    && (releasedChild.top > collapsedOffset || Math.abs(xvel) < Math.abs(yvel))) {
                // Hide if we shouldn't collapse and the view was either released low or it was a
                // vertical swipe.
                top = hideOffset
                targetState = STATE_HIDDEN
            } else if (yvel == 0f || Math.abs(xvel) > Math.abs(yvel)) {
                // If the Y velocity is 0 or the swipe was mostly horizontal indicated by the X velocity
                // being greater than the Y velocity, settle to the nearest correct height.
                val currentTop = releasedChild.top
                if (isFitToContents) {
                    if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
                        top = fitToContentsOffset
                        targetState = STATE_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                } else {
                    if (currentTop < halfExpandedOffset) {
                        if (currentTop < Math.abs(currentTop - collapsedOffset)) {
                            top = 0
                            targetState = STATE_EXPANDED
                        } else {
                            top = halfExpandedOffset
                            targetState = STATE_HALF_EXPANDED
                        }
                    } else {
                        if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
                            top = halfExpandedOffset
                            targetState = STATE_HALF_EXPANDED
                        } else {
                            top = collapsedOffset
                            targetState = STATE_COLLAPSED
                        }
                    }
                }
            } else {
                top = collapsedOffset
                targetState = STATE_COLLAPSED
            }
            if (viewDragHelper!!.settleCapturedViewAt(releasedChild.left, top)) {
                setStateInternal(STATE_SETTLING)
                ViewCompat.postOnAnimation(
                        releasedChild, SettleRunnable(releasedChild, targetState))
            } else {
                setStateInternal(targetState)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(
                    top, expandedOffset, if (isHideable) hideOffset else collapsedOffset)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (isHideable) {
                hideOffset
            } else {
                collapsedOffset
            }
        }
    }

    /** Callback for monitoring events about bottom sheets.  */
    abstract class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState The new state. This will be one of [.STATE_DRAGGING], [     ][.STATE_SETTLING], [.STATE_EXPANDED], [.STATE_COLLAPSED], [     ][.STATE_HIDDEN], or [.STATE_HALF_EXPANDED].
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
         * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
         * expanded states and from -1 to 0 it is between hidden and collapsed states.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float)
    }

    /** @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN, STATE_HALF_EXPANDED)
    @Retention(RetentionPolicy.SOURCE)
    annotation class State
    constructor() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout)
        val value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight)
        if (value != null && value.data == PEEK_HEIGHT_AUTO) {
            setPeekHeight(value.data)
        } else {
            setPeekHeight(
                    a.getDimensionPixelSize(
                            R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO))
        }
        isHideable = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false)
        isFitToContents = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true)
        skipCollapsed = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false)
        a.recycle()
        val configuration = ViewConfiguration.get(context)
        maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            child.fitsSystemWindows = true
        }
        val savedTop = child.top
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)
        // Offset the bottom sheet
        parentHeight = parent.height
        if (peekHeightAuto) {
            if (peekHeightMin == 0) {
                peekHeightMin = parent
                        .resources
                        .getDimensionPixelSize(R.dimen.design_bottom_sheet_peek_height_min)
            }
            lastPeekHeight = Math.max(peekHeightMin, parentHeight - parent.width * 9 / 16)
        } else {
            lastPeekHeight = peekHeight
        }
        fitToContentsOffset = Math.max(0, parentHeight - child.height)
        halfExpandedOffset = parentHeight / 2
        hideOffset = parentHeight - minHeight

        calculateCollapsedOffset()

        if (state == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, expandedOffset)
        } else if (state == STATE_HALF_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, halfExpandedOffset)
        } else if (isHideable && state == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, hideOffset)
        } else if (state == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, collapsedOffset)
        } else if (state == STATE_DRAGGING || state == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
        }
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, dragCallback)
        }
        viewRef = WeakReference(child)
        val scrollingChild = findScrollingChild(child)
        nestedScrollingChildRef = WeakReference<View>(scrollingChild)
        val innerScrollChild = findSecondScrollingChild(scrollingChild!!)
        innerNestedScrollingChildRef = WeakReference<View>(innerScrollChild)
        return true
    }

    private fun checkInterceptTouch(parent: CoordinatorLayout, event: MotionEvent) : Boolean {
        val innerScroll = if (innerNestedScrollingChildRef != null) innerNestedScrollingChildRef!!.get() else null
        if (innerScroll != null) {
            if (parent.isPointInChildBounds(innerScroll, event.x.toInt(), event.y.toInt())) {
                touchingInnerScrollView = true
                return false
            }
        }
        touchingInnerScrollView = false
        return true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            ignoreEvents = true
            return false
        }

        val interceptTouch = checkInterceptTouch(parent, event)
        if (!interceptTouch) {
            return false
        }

        val action = event.actionMasked
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchingScrollingChild = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                // Reset the ignore flag
                if (ignoreEvents) {
                    ignoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX = event.x.toInt()
                initialY = event.y.toInt()
                val scroll = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
                    activePointerId = event.getPointerId(event.actionIndex)
                    touchingScrollingChild = true
                }
                ignoreEvents = activePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, initialX, initialY)
            }
        }// fall out
        if (!ignoreEvents
                && viewDragHelper != null
                && viewDragHelper!!.shouldInterceptTouchEvent(event)) {
            return true
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
        return (action == MotionEvent.ACTION_MOVE
                && scroll != null
                && !ignoreEvents
                && state != STATE_DRAGGING
                && !parent.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt())
                && viewDragHelper != null
                && Math.abs(initialY - event.y) > viewDragHelper!!.touchSlop)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val interceptTouch = checkInterceptTouch(parent, event)
        if (!interceptTouch) {
            return false
        }

        val action = event.actionMasked
        if (state == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (viewDragHelper != null) {
            viewDragHelper!!.processTouchEvent(event)
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
            if (Math.abs(initialY - event.y) > viewDragHelper!!.touchSlop) {
                viewDragHelper!!.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !ignoreEvents
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int): Boolean {
        lastNestedScrollDy = 0
        nestedScrolled = false
        if (touchingInnerScrollView) {
            return false
        }
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            // Ignore fling here. The ViewDragHelper handles it.
            return
        }
        val scrollingChild = if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
        if (target !== scrollingChild) {
            return
        }
        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) { // Upward
            if (newTop < expandedOffset) {
                consumed[1] = currentTop - expandedOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            if (!target.canScrollVertically(-1)) {
                if (newTop <= collapsedOffset || isHideable) {
                    consumed[1] = dy
                    ViewCompat.offsetTopAndBottom(child, -dy)
                    setStateInternal(STATE_DRAGGING)
                } else {
                    consumed[1] = currentTop - collapsedOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    setStateInternal(STATE_COLLAPSED)
                }
            }
        }
        dispatchOnSlide(child.top)
        lastNestedScrollDy = dy
        nestedScrolled = true
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            type: Int) {
        if (child.top == expandedOffset) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (nestedScrollingChildRef == null
                || target !== nestedScrollingChildRef!!.get()
                || !nestedScrolled) {
            return
        }
        val top: Int
        val targetState: Int
        if (lastNestedScrollDy > 0) {
            top = expandedOffset
            targetState = STATE_EXPANDED
        } else if (isHideable && shouldHide(child, yVelocity)) {
            top = hideOffset
            targetState = STATE_HIDDEN
        } else if (lastNestedScrollDy == 0) {
            val currentTop = child.top
            if (isFitToContents) {
                if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
                    top = fitToContentsOffset
                    targetState = STATE_EXPANDED
                } else {
                    top = collapsedOffset
                    targetState = STATE_COLLAPSED
                }
            } else {
                if (currentTop < halfExpandedOffset) {
                    if (currentTop < Math.abs(currentTop - collapsedOffset)) {
                        top = 0
                        targetState = STATE_EXPANDED
                    } else {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    }
                } else {
                    if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                }
            }
        } else {
            top = collapsedOffset
            targetState = STATE_COLLAPSED
        }
        if (viewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        nestedScrolled = false
    }

    override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            velocityX: Float,
            velocityY: Float): Boolean {
        if (touchingInnerScrollView) {
            return false
        }
        return if (nestedScrollingChildRef != null) {
            target === nestedScrollingChildRef!!.get() && (state != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY))
        } else {
            false
        }
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed while optionally animating between the
     * old height and the new height.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels, or [     ][.PEEK_HEIGHT_AUTO] to configure the sheet to peek automatically at 16:9 ratio keyline.
     * @param animate Whether to animate between the old height and the new height.
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    @JvmOverloads
    fun setPeekHeight(peekHeight: Int, animate: Boolean = false) {
        var layout = false
        if (peekHeight == PEEK_HEIGHT_AUTO) {
            if (!peekHeightAuto) {
                peekHeightAuto = true
                layout = true
            }
        } else if (peekHeightAuto || this.peekHeight != peekHeight) {
            peekHeightAuto = false
            this.peekHeight = Math.max(0, peekHeight)
            collapsedOffset = parentHeight - peekHeight
            layout = true
        }
        if (layout && state == STATE_COLLAPSED && viewRef != null) {
            val view = viewRef!!.get()
            if (view != null) {
                if (animate) {
                    startSettlingAnimationPendingLayout(state)
                } else {
                    view.requestLayout()
                }
            }
        }
    }

    fun setMinHeight(minHeight: Int) {
        if (this.minHeight != minHeight) {
            this.minHeight = minHeight
            hideOffset = parentHeight - minHeight
        }
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or [.PEEK_HEIGHT_AUTO] if the
     * sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    fun getPeekHeight(): Int {
        return if (peekHeightAuto) PEEK_HEIGHT_AUTO else peekHeight
    }

    /**
     * Sets a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    fun setBottomSheetCallback(callback: BottomSheetCallback) {
        this.callback = callback
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of [.STATE_COLLAPSED], [.STATE_EXPANDED], [.STATE_HIDDEN],
     * or [.STATE_HALF_EXPANDED].
     */
    fun setState(@State state: Int) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if (state == STATE_COLLAPSED
                    || state == STATE_EXPANDED
                    || state == STATE_HALF_EXPANDED
                    || isHideable && state == STATE_HIDDEN) {
                this.state = state
                startSettlingAnimationPendingLayout(state)
            }
            return

    }

    private fun startSettlingAnimationPendingLayout(@State state: Int) {
        val child = viewRef!!.get() ?: return
// Start the animation; wait until a pending layout if there is one.
        val parent = child.parent
        if (parent != null && parent.isLayoutRequested && ViewCompat.isAttachedToWindow(child)) {
            val finalState = state
            child.post { startSettlingAnimation(child, finalState) }
        } else {
            startSettlingAnimation(child, state)
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of [.STATE_EXPANDED], [.STATE_HALF_EXPANDED], [.STATE_COLLAPSED],
     * [.STATE_DRAGGING], [.STATE_SETTLING], or [.STATE_HALF_EXPANDED].
     */
    @State
    fun getState(): Int {
        return state
    }

    internal fun setStateInternal(@State state: Int) {
//        if (this.state == state) {
//            return
//        }
        this.state = state
        if (state == STATE_HALF_EXPANDED || state == STATE_EXPANDED) {
            updateImportantForAccessibility(true)
        } else if (state == STATE_HIDDEN || state == STATE_COLLAPSED) {
            updateImportantForAccessibility(false)
        }

        if (viewRef != null) {
            val bottomSheet = viewRef!!.get()
            if (bottomSheet != null && callback != null) {
                callback!!.onStateChanged(bottomSheet, state)
            }
        }
    }

    private fun calculateCollapsedOffset() {
        if (isFitToContents) {
            collapsedOffset = Math.max(parentHeight - lastPeekHeight, fitToContentsOffset)
        } else {
            collapsedOffset = parentHeight - lastPeekHeight
        }
    }

    private fun reset() {
        activePointerId = ViewDragHelper.INVALID_POINTER
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    internal fun shouldHide(child: View, yvel: Float): Boolean {
        if (skipCollapsed) {
            return true
        }
        if (child.top < collapsedOffset) {
            // It should not hide, but collapse.
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return Math.abs(newTop - collapsedOffset) / peekHeight.toFloat() > HIDE_THRESHOLD
    }

    @VisibleForTesting
    internal fun findScrollingChild(view: View): View? {
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }
        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    @VisibleForTesting
    internal fun findSecondScrollingChild(view: View): View? {
        if (view is ViewGroup) {
            val count = view.childCount
            if (count > 0) {
                return findScrollingChild(view.getChildAt(0))
            }
        }
        return null
    }

    internal fun startSettlingAnimation(child: View?, state: Int) {
        var state = state
        var top: Int
        if (state == STATE_COLLAPSED) {
            top = collapsedOffset
        } else if (state == STATE_HALF_EXPANDED) {
            top = halfExpandedOffset
            if (isFitToContents && top <= fitToContentsOffset) {
                // Skip to the expanded state if we would scroll past the height of the contents.
                state = STATE_EXPANDED
                top = fitToContentsOffset
            }
        } else if (state == STATE_EXPANDED) {
            top = expandedOffset
        } else if (isHideable && state == STATE_HIDDEN) {
            top = hideOffset
        } else {
            throw IllegalArgumentException("Illegal state argument: $state")
        }
        if (viewDragHelper!!.smoothSlideViewTo(child!!, child.left, top)) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
        } else {
            setStateInternal(state)
        }
    }

    internal fun dispatchOnSlide(top: Int) {
        val bottomSheet = viewRef!!.get()
        if (bottomSheet != null && callback != null) {
            if (top > collapsedOffset) {
                callback!!.onSlide(
                        bottomSheet, (collapsedOffset - top).toFloat() / (parentHeight - collapsedOffset))
            } else {
                callback!!.onSlide(
                        bottomSheet, (collapsedOffset - top).toFloat() / (collapsedOffset - expandedOffset))
            }
        }
    }

    private inner class SettleRunnable internal constructor(private val view: View, @param:State @field:State private val targetState: Int) : Runnable {

        override fun run() {
            if (viewDragHelper != null && viewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(view, this)
            } else {
                setStateInternal(targetState)
            }
        }
    }

    private fun updateImportantForAccessibility(expanded: Boolean) {
        if (viewRef == null) {
            return
        }

        val viewParent = viewRef!!.get()?.getParent() as? CoordinatorLayout ?: return

        val childCount = viewParent.childCount
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && expanded) {
            if (importantForAccessibilityMap == null) {
                importantForAccessibilityMap = HashMap(childCount)
            } else {
                // The important for accessibility values of the child views have been saved already.
                return
            }
        }

        for (i in 0 until childCount) {
            val child = viewParent.getChildAt(i)
            if (child === viewRef!!.get()) {
                continue
            }

            if (!expanded) {
                if (importantForAccessibilityMap != null && importantForAccessibilityMap!!.containsKey(child)) {
                    // Restores the original important for accessibility value of the child view.
                    ViewCompat.setImportantForAccessibility(child, importantForAccessibilityMap!![child]!!)
                }
            } else {
                // Saves the important for accessibility value of the child view.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    importantForAccessibilityMap!![child] = child.importantForAccessibility
                }

                ViewCompat.setImportantForAccessibility(
                        child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
            }
        }

        if (!expanded) {
            importantForAccessibilityMap = null
        }
    }

    companion object {

        /** The bottom sheet is dragging.  */
        const val STATE_DRAGGING = 1

        /** The bottom sheet is settling.  */
        const val STATE_SETTLING = 2

        /** The bottom sheet is expanded.  */
        const val STATE_EXPANDED = 3

        /** The bottom sheet is collapsed.  */
        const val STATE_COLLAPSED = 4

        /** The bottom sheet is hidden.  */
        const val STATE_HIDDEN = 5

        /** The bottom sheet is half-expanded (used when mFitToContents is false).  */
        const val STATE_HALF_EXPANDED = 6

        /**
         * Peek at the 16:9 ratio keyline of its parent.
         *
         *
         * This can be used as a parameter for [.setPeekHeight]. [.getPeekHeight]
         * will return this when the value is set.
         */
        val PEEK_HEIGHT_AUTO = -1

        private val HIDE_THRESHOLD = 0.5f

        private val HIDE_FRICTION = 0.1f

        /**
         * A utility function to get the [CustomBottomSheetBehavior] associated with the `view`.
         *
         * @param view The [View] with [CustomBottomSheetBehavior].
         * @return The [CustomBottomSheetBehavior] associated with the `view`.
         */
        fun <V : View> from(view: V): CustomBottomSheetBehavior<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                    ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params.behavior as CustomBottomSheetBehavior<V>
                    ?: throw IllegalArgumentException("The view is not associated with BottomSheetBehavior")
            return behavior
        }
    }
}
/**
 * Sets the height of the bottom sheet when it is collapsed.
 *
 * @param peekHeight The height of the collapsed bottom sheet in pixels, or [     ][.PEEK_HEIGHT_AUTO] to configure the sheet to peek automatically at 16:9 ratio keyline.
 * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
 */
