package dev.lackluster.hyperx.app

import android.animation.LayoutTransition
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.DialogInterface.OnShowListener
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewStub
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.CursorAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.core.view.ViewCompat
import cn.fkj233.ui.R
import dev.lackluster.hyperx.app.AlertDialog.OnDialogShowAnimListener
import dev.lackluster.hyperx.core.util.EnvStateManager
import dev.lackluster.hyperx.core.util.EnvStateManager.isFreeFormMode
import dev.lackluster.hyperx.core.util.HyperXUIUtils
import dev.lackluster.hyperx.core.util.WindowUtils.getScreenSize
import dev.lackluster.hyperx.internal.util.LiteUtils
import dev.lackluster.hyperx.internal.widget.DialogButtonPanel
import dev.lackluster.hyperx.internal.widget.DialogParentPanel2
import dev.lackluster.hyperx.internal.widget.DialogRootView
import dev.lackluster.hyperx.internal.widget.GroupButton
import dev.lackluster.hyperx.internal.widget.NestedScrollViewExpander
import dev.lackluster.hyperx.os.Build
import dev.lackluster.hyperx.os.DeviceHelper
import dev.lackluster.hyperx.view.DensityChangedHelper.updateTextSizeDefaultUnit
import dev.lackluster.hyperx.view.DensityChangedHelper.updateTextSizeSpUnit
import dev.lackluster.hyperx.view.DensityChangedHelper.updateViewMargin
import dev.lackluster.hyperx.view.DensityChangedHelper.updateViewPadding
import dev.lackluster.hyperx.view.DensityChangedHelper.updateViewSize
import dev.lackluster.hyperx.view.animation.CubicEaseInOutInterpolator
import dev.lackluster.hyperx.widget.DialogAnimHelper
import dev.lackluster.hyperx.widget.DialogAnimHelper.OnDismiss
import dev.lackluster.hyperx.widget.DialogAnimHelper.executeDismissAnim
import dev.lackluster.hyperx.widget.DialogAnimHelper.executeShowAnim
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min


class AlertController(
    context: Context,
    dialog: AlertDialog,
    window: Window
) {
    companion object {
        fun canTextInput(view: View): Boolean {
            if (view.onCheckIsTextEditor()) {
                return true
            }
            if (view !is ViewGroup) {
                return false
            }
            var childCount = view.childCount
            while (childCount > 0) {
                childCount --
                if (canTextInput(view.getChildAt(childCount))) {
                    return true
                }
            }
            return false
        }
    }
    private var buildJustNow = false
    private var configurationAfterInstalled: Configuration? = null
    var mAdapter: ListAdapter? = null
    private val mAlertDialogLayout: Int
    var mButtonNegative: Button? = null
    var mButtonNegativeMessage: Message? = null
    private var mButtonNegativeText: CharSequence? = null
    var mButtonNeutral: Button? = null
    var mButtonNeutralMessage: Message? = null
    private var mButtonNeutralText: CharSequence? = null
    var mButtonPositive: Button? = null
    var mButtonPositiveMessage: Message? = null
    private var mButtonPositiveText: CharSequence? = null
    private var mCheckBoxMessage: CharSequence? = null
    private var mComment: CharSequence? = null
    private var mCommentView: TextView? = null
    private val mContext: Context = context
    private val mCreateThread: Thread
    private var mCurrentDensityDpi: Int
    private var mCustomTitleView: View? = null
    val mDialog: AlertDialog = dialog
    private var mDialogContentLayout = 0
    private var mDialogRootView: DialogRootView? = null
    private var mDimBg: View? = null
    private var mExtraButtonList: List<ButtonInfo>? = null
    private var mFakeLandScreenMinorSize: Int = 0
    val mHandler: Handler = ButtonHandler(dialog)
    var mHapticFeedbackEnabled = false
    private var mIcon: Drawable? = null
    private var mIconHeight = 0
    private var mIconWidth = 0
    private var mInflatedView: View? = null
    private var mIsChecked = false
    private var mIsDialogAnimating = false
    private var mIsFromRebuild: Boolean = false
    private var mLandscapePanel = false
    var mListItemLayout = 0
    var mListLayout = 0
    var mListView: ListView? = null
    private var mLiteVersion = 0
    private var mMessage: CharSequence? = null
    private var mMessageView: TextView? = null
    var mMultiChoiceItemLayout = 0
    private var mPanelAndImeMargin = 0
    private var mPanelMaxWidth: Int
    private var mPanelMaxWidthLand: Int
    private var mPanelOriginLeftMargin = 0
    private var mPanelOriginRightMargin = 0
    private var mParentPanel: DialogParentPanel2? = null
    private var mPreferLandscape = false
    private var mScreenMinorSize: Int = 0
    private var mSetupWindowInsetsAnimation = false
    private var mShowAnimListener: OnDialogShowAnimListener? = null
    private val mShowTitle: Boolean
    var mSingleChoiceItemLayout = 0
    private var mSmallIcon = false
    private var mTitle: CharSequence? = null
    private var mTitleView: TextView? = null
    private var mTreatAsLandConfig: Boolean
    private var mView: View? = null
    private var mViewLayoutResId = 0
    private val mWindow: Window = window
    private var mWindowManager: WindowManager? = null
    private val mIsDebugEnabled = false
    private var mExtraImeMargin = -1
    private var mIsInFreeForm: Boolean = false
    private val mDuringTransition = false
    private var mNonImmersiveDialogHeight = -2
    private val mDefaultButtonsTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
        override fun afterTextChanged(p0: Editable?) {
            val parent = this@AlertController.mParentPanel ?: return
            parent.findViewById<ViewGroup>(R.id.buttonPanel)?.requestLayout()
        }
    }
    private var mIconId = 0
    private var mCustomTitleTextView: TextView? = null
    var mCheckedItem = -1
    private var mCancelable: Boolean = true
    private var mCanceledOnTouchOutside = true
    private var mScreenOrientation = 0
    private val mTitleTextSize = 18.0f
    private val mMessageTextSize = 17.0f
    private val mCommentTextSize = 14.0f
    private var mCustomTitleTextSize = 18.0f
    private val mRootViewSize = Point()
    private val mRootViewSizeDp = Point()
    private val mScreenRealSize = Point()
    private val mDisplayCutoutSafeInsets = Rect()
    private val mShowAnimListenerWrapper: OnDialogShowAnimListener =
        object : OnDialogShowAnimListener {
            override fun onShowAnimComplete() {
                this@AlertController.mIsDialogAnimating = false
                this@AlertController.mShowAnimListener?.onShowAnimComplete()
            }
            override fun onShowAnimStart() {
                this@AlertController.mIsDialogAnimating = true
                this@AlertController.mShowAnimListener?.onShowAnimStart()
            }
        }
    private val mButtonHandler: View.OnClickListener = View.OnClickListener { view ->
        var msg: Message? = null
        when (view) {
            this@AlertController.mButtonPositive -> {
                val message = this@AlertController.mButtonPositiveMessage
                message?.let {
                    msg = Message.obtain(it)
                }
            }
            this@AlertController.mButtonNegative -> {
                val message = this@AlertController.mButtonNegativeMessage
                message?.let {
                    msg = Message.obtain(it)
                }
            }
            this@AlertController.mButtonNeutral -> {
                val message = this@AlertController.mButtonNeutralMessage
                message?.let {
                    msg = Message.obtain(it)
                }
            }
            else -> {
                val extraButtonList = this@AlertController.mExtraButtonList
                if (!extraButtonList.isNullOrEmpty()) {
                    for (button in extraButtonList) {
                        if (view == button.mButton) {
                            val message = button.mMsg
                            message?.let {
                                msg = Message.obtain(it)
                            }
                        }
                    }
                }
            }
        }
        msg?.sendToTarget()
        this@AlertController.mHandler.sendEmptyMessage(-1651327837)
    }
    private var mInsetsAnimationPlayed = false
    var mEnableEnterAnim = true
    private val mLayoutChangeListener: LayoutChangeListener = LayoutChangeListener(this)
    private var mIsEnableImmersive: Boolean = !LiteUtils.isCommonLiteStrategy()
    init {
        mCurrentDensityDpi = context.resources.configuration.densityDpi
        initScreenMinorSize(context)
        val obtainStyledAttributes = context.obtainStyledAttributes(null, R.styleable.AlertDialog, android.R.attr.alertDialogStyle, R.style.AlertDialog)
        mAlertDialogLayout = obtainStyledAttributes.getResourceId(R.styleable.AlertDialog_layout, R.layout.hyperx_alert_dialog)
        mListLayout = obtainStyledAttributes.getResourceId(R.styleable.AlertDialog_listLayout, R.layout.hyperx_select_dialog)
        mMultiChoiceItemLayout = obtainStyledAttributes.getResourceId(R.styleable.AlertDialog_multiChoiceItemLayout, R.layout.hyperx_select_dialog_multichoice)
        mSingleChoiceItemLayout = obtainStyledAttributes.getResourceId(R.styleable.AlertDialog_singleChoiceItemLayout, R.layout.hyperx_select_dialog_singlechoice)
        mListItemLayout = obtainStyledAttributes.getResourceId(R.styleable.AlertDialog_listItemLayout, R.layout.hyperx_select_dialog_item)
        mShowTitle = obtainStyledAttributes.getBoolean(R.styleable.AlertDialog_showTitle, true)
        obtainStyledAttributes.recycle()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mTreatAsLandConfig = context.resources.getBoolean(R.bool.treat_as_land)
        mPanelMaxWidth = context.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_max_width)
        mPanelMaxWidthLand = context.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_max_width_land)
        mCreateThread = Thread.currentThread()
    }

    private fun getCutoutMode(orientation: Int, i2: Int): Int {
        return if (i2 == 0) {
            if (orientation == 2) {
                2
            } else {
                1
            }
        } else {
            i2
        }
    }
    fun getDialogPanelExtraBottomPadding(): Int {
        return 0
    }
    fun setPanelSizeChangedListener(onPanelSizeChangedListener: OnDialogShowAnimListener?) {
    }
    fun installContent(bundle: Bundle?) {
        mIsFromRebuild = bundle != null
        mIsInFreeForm = HyperXUIUtils.isFreeformMode(mContext)
        mDialog.setContentView(mAlertDialogLayout)
        mDialogRootView = mWindow.findViewById(R.id.dialog_root_view)
        mDimBg = mWindow.findViewById(R.id.dialog_dim_bg)
        mDialogRootView?.setConfigurationChangedCallback(object : DialogRootView.ConfigurationChangedCallback {
            override fun onConfigurationChanged(
                configuration: Configuration,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int
            ) {
                this@AlertController.onConfigurationChanged(configuration, z = false, z2 = false)
            }
        })
        val configuration = mContext.resources.configuration
        updateRootViewSize(configuration)
        setupWindow()
        setupView()
        configurationAfterInstalled = configuration
        buildJustNow = true
        mDialogRootView?.post {
            this@AlertController.updateRootViewSize(this@AlertController.mDialogRootView!!)
        }
    }
    fun setTitle(charSequence: CharSequence?) {
        mTitle = charSequence
        mTitleView?.text = charSequence
    }
    fun setPreferLandscape(z: Boolean) {
        mPreferLandscape = z
    }
    fun setEnableEnterAnim(enabled: Boolean) {
        mEnableEnterAnim = enabled
    }
    fun setCustomTitle(view: View) {
        mCustomTitleView = view
    }
    fun setMessage(charSequence: CharSequence?) {
        mMessage = charSequence
        mMessageView?.text = charSequence
    }
    fun setComment(charSequence: CharSequence?) {
        mComment = charSequence
        mCommentView?.text = charSequence
    }
    fun getMessageView(): TextView? {
        return mMessageView
    }
    fun setView(resId: Int) {
        mView = null
        mViewLayoutResId = resId
    }
    fun setView(view: View?) {
        mView = view
        mViewLayoutResId = 0
    }
    fun setButton(i: Int, charSequence: CharSequence?, onClickListener: OnClickListener?, msg: Message?) {
        val message = if (msg == null && onClickListener != null) {
            mHandler.obtainMessage(i, onClickListener)
        } else {
            msg
        }
        when (i) {
            -3 -> {
                mButtonNeutralText = charSequence
                mButtonNeutralMessage = message
            }
            -2 -> {
                mButtonNegativeText = charSequence
                mButtonNegativeMessage = message
            }
            -1 -> {
                mButtonPositiveText = charSequence
                mButtonPositiveMessage = message
            }
            else -> {
                throw IllegalArgumentException("Button does not exist")
            }
        }
    }
    fun setIcon(resId: Int) {
        mIcon = null
        mIconId = resId
    }
    fun setIcon(drawable: Drawable?) {
        mIcon = drawable
        mIconId = 0
    }
    fun setUseSmallIcon(enabled: Boolean) {
        mSmallIcon = enabled
    }
    fun setIconSize(width: Int, height: Int) {
        mIconWidth = width
        mIconHeight = height
    }
    fun getIconAttributeResId(resId: Int): Int {
        val typedValue = TypedValue()
        mContext.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.resourceId
    }
    fun getListView(): ListView? {
        return mListView
    }
    fun getButton(i: Int): Button? {
        when(i) {
            -3 -> return mButtonNeutral
            -2 -> return mButtonNegative
            -1 -> mButtonPositive
        }
        val list = mExtraButtonList
        if (list.isNullOrEmpty()) {
            return null
        }
        for (buttonInfo in list) {
            if (buttonInfo.mWhich == i) {
                return buttonInfo.mButton
            }
        }
        return null
    }
    fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        return keyEvent.keyCode == 82
    }
    fun setCancelable(enabled: Boolean) {
        mCancelable = enabled
    }
    fun setCanceledOnTouchOutside(enabled: Boolean) {
        mCanceledOnTouchOutside = enabled
    }
    private fun isCancelable(): Boolean {
        return mCancelable
    }
    private fun isCanceledOnTouchOutside(): Boolean {
        return mCanceledOnTouchOutside
    }
    private fun hideSoftIME() {
        val inputMethodManager  = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(mParentPanel?.windowToken, 0)
    }
    private fun setupView() {
        setupView(z = true, z2 = false, z3 = false, f = 1.0f)
        storeCustomViewInitialTextSize()
    }
    private fun storeCustomViewInitialTextSize() {
        val displayMetrics = mContext.resources.displayMetrics
        val f = displayMetrics.scaledDensity
        val f2 = displayMetrics.density
        val view = mCustomTitleView
        if (view != null) {
            mCustomTitleTextView = view.findViewById(android.R.id.title)
        }
        val textView = mCustomTitleTextView
        if (textView != null) {
            mCustomTitleTextSize = textView.textSize
            val textSizeUnit = textView.textSizeUnit
            if (textSizeUnit == 1) {
                mCustomTitleTextSize /= f2
            } else if (textSizeUnit == 2) {
                mCustomTitleTextSize /= f
            }
        }
    }
    private fun setupView(z: Boolean, z2: Boolean, z3: Boolean, f: Float) {
        var listAdapter: ListAdapter? = null
        if (isDialogImmersive() || isSpecifiedDialogHeight()) {
            mDimBg?.setOnClickListener {
                if (isCancelable() && isCanceledOnTouchOutside()) {
                    hideSoftIME()
                    mDialog.cancel()
                }
            }
            updateDialogPanel()
        } else {
            mDimBg?.visibility = View.GONE
        }
        val parentPanel = mParentPanel ?: return
        if (z || z2 || mPreferLandscape) {
            val viewGroup = parentPanel.findViewById<ViewGroup>(R.id.topPanel)
            val viewGroup2 = parentPanel.findViewById<ViewGroup>(R.id.contentPanel)
            val viewGroup3 = parentPanel.findViewById<ViewGroup>(R.id.buttonPanel)
            if (viewGroup != null) {
                setupTitle(viewGroup)
            }
            if (viewGroup2 != null) {
                setupContent(viewGroup2, z3)
            }
            if (viewGroup3 != null) {
                setupButtons(viewGroup3)
            }
            if (!(viewGroup == null || viewGroup.visibility == View.GONE)) {
                val findViewById: View? =
                    if (mMessage == null && mListView == null) null else viewGroup.findViewById(R.id.titleDividerNoCustom)
                if (findViewById != null) {
                    findViewById.visibility = View.VISIBLE
                }
            }
            val listView = mListView
            if (listView != null && mAdapter.also { listAdapter = it } != null) {
                listView.adapter = listAdapter
                val i = mCheckedItem
                if (i > -1) {
                    listView.setItemChecked(i, true)
                    listView.setSelection(i)
                }
            }
            val viewStub = parentPanel.findViewById<View>(R.id.checkbox_stub) as? ViewStub
            if (viewStub != null) {
                setupCheckbox(parentPanel, viewStub)
            }
            if (!z) {
                onLayoutReload()
            }
        } else {
            parentPanel.post {
                val pPanel = mParentPanel ?: return@post
                val viewGroup = pPanel.findViewById<ViewGroup>(R.id.contentPanel)
                val viewGroup2 = pPanel.findViewById<ViewGroup>(R.id.buttonPanel)
                if (viewGroup != null) {
                    updateContent(viewGroup)
                    if (viewGroup2 != null && !mPreferLandscape) {
                        updateButtons(viewGroup2, viewGroup)
                    }
                }
                if (f != 1.0f) {
                    updateViewOnDensityChanged(f)
                }
            }
        }
        parentPanel.post {
        }
    }
    fun updateViewOnDensityChanged(f: Float) {
        val dialogParentPanel2 = mParentPanel
        if (dialogParentPanel2 != null) {
            updateViewPadding(dialogParentPanel2, f)
        }
        val textView = mCustomTitleTextView
        if (mCustomTitleView != null && textView != null) {
            updateTextSizeDefaultUnit(textView, mCustomTitleTextSize)
        }
        val textView2 = mTitleView
        if (textView2 != null) {
            updateTextSizeSpUnit(textView2, mTitleTextSize)
        }
        val textView3 = mMessageView
        if (textView3 != null) {
            updateTextSizeSpUnit(textView3, mMessageTextSize)
        }
        val textView4 = mCommentView
        if (textView4 != null) {
            updateTextSizeSpUnit(textView4, mCommentTextSize)
            updateViewPadding(textView4, f)
        }
        val findViewById = mWindow.findViewById<View>(R.id.buttonPanel)
        if (findViewById != null) {
            updateViewMargin(findViewById, f)
        }
        val viewGroup = mWindow.findViewById<ViewGroup>(R.id.topPanel)
        if (viewGroup != null) {
            updateViewPadding(viewGroup, f)
        }
        val findViewById2 = mWindow.findViewById<ViewGroup>(R.id.contentView)
        if (findViewById2 != null) {
            updateViewMargin(findViewById2, f)
        }
        val checkBox = mWindow.findViewById<CheckBox>(android.R.id.checkbox)
        if (checkBox != null) {
            updateViewMargin(checkBox, f)
        }
        val imageView = mWindow.findViewById<ImageView>(android.R.id.icon)
        if (imageView != null) {
            updateViewSize(imageView, f)
            updateViewMargin(imageView, f)
        }
    }
    private fun setupCustomContent(viewGroup: ViewGroup): Boolean {
        val view = mInflatedView
        var view2: View? = null
        if (view != null && view.parent != null) {
            safeRemoveFromParent(mInflatedView!!)
            mInflatedView = null
        }
        val view3 = mView
        if (view3 != null) {
            view2 = view3
        } else if (mViewLayoutResId != 0) {
            view2 = LayoutInflater.from(mContext).inflate(mViewLayoutResId, viewGroup, false)
            mInflatedView = view2
        }
        val z = view2 != null
        if (!z || !canTextInput(view2!!)) {
            mWindow.setFlags(131072, 131072)
        }
        if (z) {
            safeMoveView(view2!!, viewGroup)
        } else {
            safeRemoveFromParent(viewGroup)
        }
        return z
    }
    private fun setupTitle(viewGroup: ViewGroup) {
        val imageView = mWindow.findViewById<ImageView>(android.R.id.icon)
        val view = mCustomTitleView
        if (view != null) {
            safeRemoveFromParent(view)
            viewGroup.addView(mCustomTitleView, 0, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            mWindow.findViewById<TextView>(R.id.alertTitle).visibility = View.GONE
            imageView.visibility = View.GONE
            return
        }
        if (!TextUtils.isEmpty(mTitle) && mShowTitle) {
            val textView = mWindow.findViewById<TextView>(R.id.alertTitle)
            mTitleView = textView
            textView.text = mTitle
            val i = mIconId
            if (i != 0) {
                imageView.setImageResource(i)
            } else {
                val drawable = mIcon
                if (drawable != null) {
                    imageView.setImageDrawable(drawable)
                } else {
                    mTitleView?.setPadding(
                        imageView.paddingLeft,
                        imageView.paddingTop,
                        imageView.paddingRight,
                        imageView.paddingBottom
                    )
                    imageView.visibility = View.GONE
                }
            }
            if (mSmallIcon) {
                val layoutParams = imageView.layoutParams
                layoutParams.width =
                    mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_icon_drawable_width_small)
                layoutParams.height =
                    mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_icon_drawable_height_small)
            }
            if (mIconWidth != 0 && mIconHeight != 0) {
                val layoutParams2 = imageView.layoutParams
                layoutParams2.width = mIconWidth
                layoutParams2.height = mIconHeight
            }
            if (mMessage == null || viewGroup.visibility == 8) {
                return
            }
            changeTitlePadding(mTitleView!!)
            return
        }
        mWindow.findViewById<View>(R.id.alertTitle).visibility = View.GONE
        imageView.visibility = View.GONE
        viewGroup.visibility = View.GONE
    }
    fun getSingleItemMinHeight(): Int {
        return mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_list_preferred_item_height)
    }
    private fun listViewIsNeedFullScroll(): Boolean {
        return getSingleItemMinHeight() * (mAdapter?.count ?: 0) > (mRootViewSize.y.toFloat() * 0.35f).toInt()
    }
    private fun resetListMaxHeight() {
        val singleItemMinHeight = getSingleItemMinHeight()
        val i = singleItemMinHeight * ((mRootViewSize.y * 0.35f).toInt() / singleItemMinHeight)
        mListView!!.minimumHeight = i
        val layoutParams = mListView!!.layoutParams
        layoutParams.height = i
        mListView!!.layoutParams = layoutParams
    }
    private fun adjustHeight2WrapContent() {
        val layoutParams = mListView!!.layoutParams
        layoutParams.height = -2
        mListView!!.layoutParams = layoutParams
    }
    private fun setupContent(viewGroup: ViewGroup, z: Boolean) {
        var frameLayout = viewGroup.findViewById<FrameLayout>(android.R.id.custom)
        if (frameLayout != null) {
            if (z) {
                val layoutTransition = LayoutTransition()
                layoutTransition.setDuration(0, 200L)
                layoutTransition.setInterpolator(0, CubicEaseInOutInterpolator())
                frameLayout.layoutTransition = layoutTransition
            } else {
                frameLayout.layoutTransition = null
            }
        }
        if (mListView != null) {
            if (frameLayout?.let { setupCustomContent(it) } == true) {
                viewGroup.removeView(viewGroup.findViewById(R.id.contentView))
                safeRemoveFromParent(frameLayout)
                val linearLayout = LinearLayout(viewGroup.context)
                linearLayout.orientation = LinearLayout.VERTICAL
                safeRemoveFromParent(mListView!!)
                ViewCompat.setNestedScrollingEnabled(mListView!!, true)
                linearLayout.addView(mListView, 0, MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
                val listViewIsNeedFullScroll = listViewIsNeedFullScroll()
                if (!listViewIsNeedFullScroll) {
                    adjustHeight2WrapContent()
                    linearLayout.addView(frameLayout, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f))
                } else {
                    resetListMaxHeight()
                    linearLayout.addView(frameLayout, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.0f))
                }
                viewGroup.addView(linearLayout, 0, MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
                val viewGroup2 = viewGroup.findViewById<ViewGroup>(R.id.contentView)
                viewGroup2?.let { setupContentView(it) }
                (viewGroup as NestedScrollViewExpander).setExpandView(if (listViewIsNeedFullScroll) null else linearLayout)
                return
            }
            viewGroup.removeView(viewGroup.findViewById(R.id.contentView))
            safeRemoveFromParent(frameLayout)
            safeRemoveFromParent(mListView!!)
            mListView!!.minimumHeight = getSingleItemMinHeight()
            ViewCompat.setNestedScrollingEnabled(mListView!!, true)
            viewGroup.addView(mListView, 0, MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            (viewGroup as NestedScrollViewExpander).setExpandView(mListView)
            return
        }
        val viewGroup3 = viewGroup.findViewById<ViewGroup>(R.id.contentView)
        viewGroup3?.let { setupContentView(it) }
        var hasCustomView = false
        if (frameLayout != null) {
            hasCustomView = setupCustomContent(frameLayout)
            val childAt: View? = frameLayout.getChildAt(0)
            if (hasCustomView && childAt != null) {
                ViewCompat.setNestedScrollingEnabled(childAt, true)
            }
        }
        val nestedScrollViewExpander = viewGroup as NestedScrollViewExpander
        if (!hasCustomView) {
            frameLayout = null
        }
        nestedScrollViewExpander.setExpandView(frameLayout)
    }
    fun updateContent(viewGroup: ViewGroup) {
        var frameLayout = viewGroup.findViewById<FrameLayout>(android.R.id.custom)
        val z = frameLayout != null && frameLayout.childCount > 0
        val listView = mListView
        if (listView == null) {
            if (z) {
                ViewCompat.setNestedScrollingEnabled(frameLayout!!.getChildAt(0), true)
            }
            val nestedScrollViewExpander = viewGroup as NestedScrollViewExpander
            if (!z) {
                frameLayout = null
            }
            nestedScrollViewExpander.setExpandView(frameLayout)
            return
        }
        if (z) {
            if (!listViewIsNeedFullScroll()) {
                adjustHeight2WrapContent()
                val layoutParams = frameLayout!!.layoutParams as LinearLayout.LayoutParams
                layoutParams.height = 0
                layoutParams.weight = 1.0f
                frameLayout.layoutParams = layoutParams
                (viewGroup as NestedScrollViewExpander).setExpandView(frameLayout.parent as View)
                viewGroup.requestLayout()
                return
            }
            resetListMaxHeight()
            val layoutParams2 = frameLayout!!.layoutParams as LinearLayout.LayoutParams
            layoutParams2.height = -2
            layoutParams2.weight = 0.0f
            frameLayout.layoutParams = layoutParams2
            (viewGroup as NestedScrollViewExpander).setExpandView(null)
            viewGroup.requestLayout()
            return
        }
        (viewGroup as NestedScrollViewExpander).setExpandView(listView)
    }
    private fun setupContentView(viewGroup: ViewGroup) {
        val charSequence: CharSequence? = mMessage
        mMessageView = viewGroup.findViewById<TextView>(R.id.message)
        mCommentView = viewGroup.findViewById<TextView>(R.id.comment)
        val textView = mMessageView
        if (textView != null && charSequence != null) {
            textView.text = charSequence
            val textView2 = mCommentView
            if (textView2 != null) {
                val charSequence2 = mComment
                if (charSequence2 != null) {
                    textView2.text = charSequence2
                    return
                } else {
                    textView2.visibility = View.GONE
                    return
                }
            }
            return
        }
        safeRemoveFromParent(viewGroup)
    }
    private fun disableForceDark(view: View) {
        view.isForceDarkAllowed = false
    }
    private fun setupButtons(viewGroup: ViewGroup) {
        var i: Int
        val button = viewGroup.findViewById<Button>(android.R.id.button1)
        mButtonPositive = button
        button.setOnClickListener(mButtonHandler)
        mButtonPositive!!.removeTextChangedListener(mDefaultButtonsTextWatcher)
        mButtonPositive!!.addTextChangedListener(mDefaultButtonsTextWatcher)
        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive!!.visibility = View.GONE
            i = 0
        } else {
            mButtonPositive!!.text = mButtonPositiveText
            mButtonPositive!!.visibility = View.VISIBLE
            disableForceDark(mButtonPositive!!)
            i = 1
        }
        val button2 = viewGroup.findViewById<Button>(android.R.id.button2)
        mButtonNegative = button2
        button2.setOnClickListener(mButtonHandler)
        mButtonNegative!!.removeTextChangedListener(mDefaultButtonsTextWatcher)
        mButtonNegative!!.addTextChangedListener(mDefaultButtonsTextWatcher)
        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative!!.visibility = View.GONE
        } else {
            mButtonNegative!!.text = mButtonNegativeText
            mButtonNegative!!.visibility = View.VISIBLE
            i++
            disableForceDark(mButtonNegative!!)
        }
        val button3 = viewGroup.findViewById<Button>(android.R.id.button3)
        mButtonNeutral = button3
        button3.setOnClickListener(mButtonHandler)
        mButtonNeutral!!.removeTextChangedListener(mDefaultButtonsTextWatcher)
        mButtonNeutral!!.addTextChangedListener(mDefaultButtonsTextWatcher)
        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral!!.visibility = View.GONE
        } else {
            mButtonNeutral!!.text = mButtonNeutralText
            mButtonNeutral!!.visibility = View.VISIBLE
            i++
            disableForceDark(mButtonNeutral!!)
        }
        val list = mExtraButtonList
        if (!list.isNullOrEmpty()) {
            for (buttonInfo in list) {
                if (buttonInfo.mButton != null) {
                    safeRemoveFromParent(buttonInfo.mButton!!)
                }
            }
            for (buttonInfo2 in list) {
                if (buttonInfo2.mButton == null) {
                    val layoutParams = LinearLayout.LayoutParams(0, -2, 1.0f)
                    buttonInfo2.mButton = GroupButton(
                        mContext,
                        null,
                        buttonInfo2.mStyle
                    )
                    buttonInfo2.mButton!!.let {
                        it.text = buttonInfo2.mText
                        it.setOnClickListener(mButtonHandler)
                        it.layoutParams = layoutParams
                        it.maxLines = 2
                        it.gravity = Gravity.CENTER
                    }
                }
                if (buttonInfo2.mMsg == null) {
                    buttonInfo2.mMsg = mHandler.obtainMessage(
                        buttonInfo2.mWhich,
                        buttonInfo2.mOnClickListener
                    )
                }
                if (buttonInfo2.mButton!!.visibility != View.GONE) {
                    i++
                    disableForceDark(buttonInfo2.mButton!!)
                }
                viewGroup.addView(buttonInfo2.mButton)
            }
        }
        if (i == 0) {
            viewGroup.visibility = View.GONE
        } else {
            (viewGroup as DialogButtonPanel).setForceVertical(mLandscapePanel)
            viewGroup.invalidate()
        }
        val point = Point()
        getScreenSize(mContext, point)
        val max = max(point.x, point.y)
        val nestedScrollViewExpander = mParentPanel!!.findViewById<ViewGroup>(R.id.contentPanel) as? NestedScrollViewExpander
        val z = mRootViewSize.y.toFloat() <= max.toFloat() * 0.3f
        if (mLandscapePanel) {
            return
        }
        if (!z) {
            safeMoveView(viewGroup, mParentPanel)
        } else {
            safeMoveView(viewGroup, nestedScrollViewExpander)
            nestedScrollViewExpander?.setExpandView(null as View?)
        }
    }
    fun updateButtons(viewGroup: ViewGroup, viewGroup2: ViewGroup) {
        val point = Point()
        getScreenSize(mContext, point)
        if (!(mRootViewSize.y.toFloat() <= max(point.x, point.y).toFloat() * 0.3f)
        ) {
            safeMoveView(viewGroup, mParentPanel)
        } else {
            safeMoveView(viewGroup, viewGroup2)
            (viewGroup2 as NestedScrollViewExpander).setExpandView(null as View?)
        }
    }
    private fun safeRemoveFromParent(view: View) {
        val viewGroup = view.parent as? ViewGroup
        viewGroup?.removeView(view)
    }
    private fun safeMoveView(view: View, viewGroup: ViewGroup?) {
        val viewGroup2 = view.parent as? ViewGroup
        if (viewGroup2 == viewGroup) {
            return
        }
        viewGroup2?.removeView(view)
        viewGroup?.addView(view)
    }
    private fun setupWindowInsetsAnimation() {
        if (isDialogImmersive()) {
            mWindow.setSoftInputMode(mWindow.attributes.softInputMode and 15 or 48)
            mWindow.decorView.setWindowInsetsAnimationCallback(object : WindowInsetsAnimation.Callback(
                DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {
                var isTablet: Boolean = false
                override fun onPrepare(animation: WindowInsetsAnimation) {
                    super.onPrepare(animation)
                    DialogAnimHelper.cancelAnimator()
                    this@AlertController.mInsetsAnimationPlayed = false
                    isTablet = this@AlertController.isTablet()
                }
                override fun onStart(
                    animation: WindowInsetsAnimation,
                    bounds: WindowInsetsAnimation.Bounds
                ): WindowInsetsAnimation.Bounds {
                    if (this@AlertController.mParentPanel == null) {
                        return super.onStart(animation, bounds)
                    }
                    this@AlertController.mPanelAndImeMargin = (this@AlertController.getDialogPanelMargin() + this@AlertController.mParentPanel!!.translationY).toInt()
                    if (this@AlertController.mPanelAndImeMargin <= 0) {
                        this@AlertController.mPanelAndImeMargin = 0
                    }
                    return super.onStart(animation, bounds)
                }
                override fun onProgress(
                    p0: WindowInsets,
                    p1: MutableList<WindowInsetsAnimation>
                ): WindowInsets {
                    val insets = p0.getInsets(WindowInsets.Type.ime())
                    val insets2 = p0.getInsets(WindowInsets.Type.navigationBars())
                    val max = insets.bottom - max(this@AlertController.mPanelAndImeMargin, insets2.bottom)
                    if (p0.isVisible(WindowInsets.Type.ime())) {
                        this@AlertController.translateDialogPanel(-(if (max < 0) 0 else max))
                    }
                    if (!isTablet) {
                        this@AlertController.updateDimBgBottomMargin(max)
                    }
                    return p0
                }
                override fun onEnd(animation: WindowInsetsAnimation) {
                    super.onEnd(animation)
                    this@AlertController.mInsetsAnimationPlayed = true
                    val rootWindowInsets = this@AlertController.mWindow.decorView.rootWindowInsets
                    if (this@AlertController.mParentPanel != null && rootWindowInsets != null) {
                        val insets = rootWindowInsets.getInsets(WindowInsets.Type.ime())
                        if (insets.bottom <= 0 && this@AlertController.mParentPanel!!.translationY < 0.0f) {
                            this@AlertController.translateDialogPanel(0)
                        }
                        this@AlertController.updateParentPanelMarginByWindowInsets(rootWindowInsets)
                        if (isTablet) {
                            return
                        }
                        this@AlertController.updateDimBgBottomMargin(insets.bottom)
                    }
                }
            })
            mWindow.decorView.setOnApplyWindowInsetsListener { view: View?, windowInsets: WindowInsets? ->
                if (this@AlertController.mParentPanel == null) {
                    return@setOnApplyWindowInsetsListener windowInsets ?: WindowInsets.CONSUMED
                }
                this@AlertController.mPanelAndImeMargin = (this@AlertController.getDialogPanelMargin() + this@AlertController.mParentPanel!!.translationY).toInt()
                if (view != null && windowInsets != null) {
                    view.post {
                        this@AlertController.updateDialogPanelByWindowInsets(windowInsets)
                    }
                }
                return@setOnApplyWindowInsetsListener WindowInsets.CONSUMED
            }
            mSetupWindowInsetsAnimation = true
        }
    }
    private fun cleanWindowInsetsAnimation() {
        if (mSetupWindowInsetsAnimation) {
            mWindow.decorView.setWindowInsetsAnimationCallback(null)
            mWindow.decorView.setOnApplyWindowInsetsListener(null)
            mSetupWindowInsetsAnimation = false
        }
    }
    fun getDialogPanelMargin(): Int {
        val iArr = IntArray(2)
        mParentPanel!!.getLocationInWindow(iArr)
        if (mExtraImeMargin == -1) {
            mExtraImeMargin =
                mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_ime_margin)
        }
        return mWindow.decorView.height - (iArr[1] + mParentPanel!!.height) - mExtraImeMargin
    }
    fun isChecked(): Boolean {
        val checkBox = mWindow.findViewById<CheckBox>(android.R.id.checkbox) ?: return false
        val isChecked = checkBox.isChecked
        mIsChecked = isChecked
        return isChecked
    }
    fun setCheckBox(z: Boolean, charSequence: CharSequence?) {
        mIsChecked = z
        mCheckBoxMessage = charSequence
    }
    private fun initScreenMinorSize(context: Context) {
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        updateMinorScreenSize()
        mFakeLandScreenMinorSize = context.resources.getDimensionPixelSize(R.dimen.fake_landscape_screen_minor_size)
    }
    private fun updateMinorScreenSize() {
        val configuration = mContext.resources.configuration
        val min = (min(configuration.screenWidthDp, configuration.screenHeightDp) * (configuration.densityDpi / 160.0f)).toInt()
        if (min > 0) {
            mScreenMinorSize = min
            return
        }
        val windowManager = mWindowManager ?: return
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        mScreenMinorSize = min(point.x, point.y)
    }
    private fun setupCheckbox(viewGroup: ViewGroup, viewStub: ViewStub) {
        if (mCheckBoxMessage != null) {
            viewStub.inflate()
            val checkBox = viewGroup.findViewById<CheckBox>(android.R.id.checkbox)
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = mIsChecked
            checkBox.text = mCheckBoxMessage
            val textView = mMessageView
            if (textView != null) {
                textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            }
            val textView2 = mCommentView
            if (textView2 != null) {
                textView2.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            }
        }
    }
    fun setEnableImmersive(z: Boolean) {
        mIsEnableImmersive = z
    }
    fun setLiteVersion(i: Int) {
        mLiteVersion = i
    }
    fun isDialogImmersive(): Boolean {
        return mIsEnableImmersive
    }
    private fun isLandscape(): Boolean {
        return isLandscape(getScreenOrientation())
    }
    private fun isLandscape(i: Int): Boolean {
        if (mTreatAsLandConfig) {
            return true
        }
        if (i != 2) {
            return false
        }
        if (!isInPcMode()) {
            return true
        }
        getScreenSize(mContext, mScreenRealSize)
        val point = mScreenRealSize
        return point.x > point.y
    }
    private fun isInPcMode(): Boolean {
        return Settings.Secure.getInt(mContext.contentResolver, "synergy_mode", 0) == 1
    }
    private fun shouldLimitWidth(): Boolean {
        return mRootViewSizeDp.x >= 394
    }
    private fun getDialogWidthMargin(): Int {
        return if (mRootViewSizeDp.x < 360) {
            mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_width_small_margin)
        } else {
            mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_width_margin)
        }
    }
    private fun shouldUseLandscapePanel(): Boolean {
        var i = if (!TextUtils.isEmpty(mButtonNegativeText)) 1 else 0
        if (!TextUtils.isEmpty(mButtonNeutralText)) {
            i++
        }
        if (!TextUtils.isEmpty(mButtonPositiveText)) {
            i++
        }
        val list = mExtraButtonList
        if (list != null) {
            i += list.size
        }
        if (i == 0) {
            return false
        }
        val point = mRootViewSize
        val i2 = point.x
        return i2 >= mPanelMaxWidthLand && i2 * 2 > point.y && mPreferLandscape
    }
    private fun getPanelWidth(z: Boolean, z2: Boolean): Int {
        val i: Int
        var i2: Int = R.layout.hyperx_alert_dialog_content
        mLandscapePanel = false
        if (mPreferLandscape && shouldUseLandscapePanel()) {
            i2 = R.layout.hyperx_alert_dialog_content_land
            mLandscapePanel = true
            i = mPanelMaxWidthLand
        } else if (z2) {
            i = mPanelMaxWidth
        } else if (z) {
            i = if (mTreatAsLandConfig) mFakeLandScreenMinorSize else mScreenMinorSize
        } else {
            i = -1
        }
        if (mDialogContentLayout != i2) {
            mDialogContentLayout = i2
            val view: View? = mParentPanel
            if (view != null) {
                mDialogRootView!!.removeView(view)
            }
            val view2 = LayoutInflater.from(mContext).inflate(
                mDialogContentLayout,
                mDialogRootView,
                false
            ) as DialogParentPanel2
            mParentPanel = view2
            mDialogRootView!!.addView(view2)
        }
        return i
    }
    private fun getGravity(): Int {
        return if (isTablet()) Gravity.CENTER else (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
    }
    fun isTablet(): Boolean {
        return Build.IS_TABLET
    }
    private fun updateDialogPanel() {
        val isLandscape = isLandscape()
        val shouldLimitWidth = shouldLimitWidth()
        val layoutParams = FrameLayout.LayoutParams(getPanelWidth(isLandscape, shouldLimitWidth), -2)
        layoutParams.gravity = getGravity()
        val dialogWidthMargin = if (shouldLimitWidth) 0 else getDialogWidthMargin()
        layoutParams.rightMargin = dialogWidthMargin
        layoutParams.leftMargin = dialogWidthMargin
        mPanelOriginLeftMargin = dialogWidthMargin
        mPanelOriginRightMargin = dialogWidthMargin
        mParentPanel!!.layoutParams = layoutParams
    }
    fun updateParentPanelMarginByWindowInsets(windowInsets: WindowInsets?) {
        val i: Int
        val i2: Int
        if (isTablet() || windowInsets == null) {
            return
        }
        val insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
        val insets2 = windowInsets.getInsets(WindowInsets.Type.statusBars())
        mDisplayCutoutSafeInsets.setEmpty()
        val displayCutout = windowInsets.displayCutout
        if (displayCutout != null && !mIsInFreeForm) {
            mDisplayCutoutSafeInsets.set(displayCutout.safeInsetLeft, displayCutout.safeInsetTop, displayCutout.safeInsetRight, displayCutout.safeInsetBottom)
        }
        val paddingRight = mDialogRootView!!.paddingRight
        val paddingLeft = mDialogRootView!!.paddingLeft
        val marginLayoutParams = mParentPanel!!.layoutParams as MarginLayoutParams
        val i3: Int = insets2.top
        val dimensionPixelSize = mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_ime_margin)
        val max = max(max(i3, dimensionPixelSize), mDisplayCutoutSafeInsets.top)
        val x = mParentPanel!!.x.toInt().coerceAtLeast(0)
        var z = false
        val x2 = (mRootViewSize.x - mParentPanel!!.x - mParentPanel!!.width).toInt().coerceAtLeast(0)
        val max2 = max(mDisplayCutoutSafeInsets.left, insets.left - paddingLeft)
        i = if (max2 == 0) {
            mPanelOriginLeftMargin
        } else if (x >= max2) {
            max(0, (max2 - (x - marginLayoutParams.leftMargin)))
        } else {
            max(0, (max2 - x - mPanelOriginLeftMargin))
        }
        val max3 = max(mDisplayCutoutSafeInsets.right, insets.right - paddingRight)
        i2 = if (max3 == 0) {
            mPanelOriginRightMargin
        } else if (x2 >= max3) {
            max(0, (max3 - (x2 - marginLayoutParams.rightMargin)))
        } else {
            max(0, (max3 - x2 - mPanelOriginRightMargin))
        }
        val i4: Int = dimensionPixelSize + insets.bottom
        var z2 = true
        if (marginLayoutParams.topMargin != max) {
            marginLayoutParams.topMargin = max
            z = true
        }
        if (marginLayoutParams.bottomMargin != i4) {
            marginLayoutParams.bottomMargin = i4
            z = true
        }
        if (marginLayoutParams.leftMargin != i) {
            marginLayoutParams.leftMargin = i
            z = true
        }
        if (marginLayoutParams.rightMargin != i2) {
            marginLayoutParams.rightMargin = i2
        } else {
            z2 = z
        }
        if (z2) {
            mParentPanel!!.requestLayout()
        }
    }
    fun updateDimBgBottomMargin(i: Int) {
        val marginLayoutParams = mDimBg!!.layoutParams as MarginLayoutParams
        if (marginLayoutParams.bottomMargin != i) {
            marginLayoutParams.bottomMargin = i
            mDimBg!!.requestLayout()
        }
    }
    private fun setupWindow() {
        if (isDialogImmersive()) {
            setupImmersiveWindow()
        } else {
            setupNonImmersiveWindow()
        }
    }
    private fun setupImmersiveWindow() {
        mWindow.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mWindow.setBackgroundDrawableResource(R.color.hyperx_transparent)
        mWindow.setDimAmount(0.0f)
        mWindow.setWindowAnimations(R.style.Animation_Dialog_NoAnimation)
        mWindow.addFlags(-2147481344)
        val associatedActivity = mDialog.getAssociatedActivity()
        if (associatedActivity != null) {
            mWindow.attributes.layoutInDisplayCutoutMode = getCutoutMode(
                getScreenOrientation(),
                associatedActivity.window.attributes.layoutInDisplayCutoutMode
            )
        } else {
            mWindow.attributes.layoutInDisplayCutoutMode =
                if (getScreenOrientation() != 2)
                    LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                else
                    LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }
        clearFitSystemWindow(mWindow.decorView)
        mWindow.attributes.fitInsetsSides = 0
        val associatedActivity2 = mDialog.getAssociatedActivity()
        if (associatedActivity2 == null || associatedActivity2.window.attributes.flags and 1024 != 0) {
            return
        }
        mWindow.clearFlags(1024)
    }
    private fun setupNonImmersiveWindow() {
        val isLandscape = isLandscape()
        val shouldLimitWidth = shouldLimitWidth()
        var panelWidth = getPanelWidth(isLandscape, shouldLimitWidth)
        if (!shouldLimitWidth && panelWidth == -1) {
            panelWidth = mRootViewSize.x - getDialogWidthMargin() * 2
        }
        val i = mNonImmersiveDialogHeight
        val i2 = if (i <= 0 || i < mRootViewSize.y) i else -1
        val gravity = getGravity()
        mWindow.setGravity(gravity)
        if (gravity and Gravity.BOTTOM > 0) {
            val dimensionPixelSize =
                mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_ime_margin)
            if (mWindow.attributes.flags and 0x08000000 != 0) {
                mWindow.clearFlags(0x08000000)
            }
            mWindow.attributes.verticalMargin = dimensionPixelSize * 1.0f / mRootViewSize.y
        }
        mWindow.addFlags(2)
        mWindow.addFlags(262144)
        mWindow.setDimAmount(0.3f)
        mWindow.setLayout(panelWidth, i2)
        mWindow.setBackgroundDrawableResource(R.color.hyperx_transparent)
        val dialogParentPanel2 = mParentPanel
        if (dialogParentPanel2 != null) {
            val layoutParams = dialogParentPanel2.layoutParams as FrameLayout.LayoutParams
            layoutParams.width = panelWidth
            layoutParams.height = -2
            mParentPanel!!.layoutParams = layoutParams
            mParentPanel!!.tag = null
        }
        if (mEnableEnterAnim) {
            if (isTablet()) {
                mWindow.setWindowAnimations(R.style.Animation_Dialog_Center)
                return
            }
            return
        }
        mWindow.setWindowAnimations(0)
    }
    private fun clearFitSystemWindow(view: View?) {
        if (view is DialogParentPanel2 || view == null) {
            return
        }
        var i = 0
        view.fitsSystemWindows = false
        if (view !is ViewGroup) {
            return
        }
        while (true) {
            if (i >= view.childCount) {
                return
            }
            clearFitSystemWindow(view.getChildAt(i))
            i++
        }
    }
    private fun reInitLandConfig() {
        mTreatAsLandConfig = mContext.resources.getBoolean(R.bool.treat_as_land)
        mFakeLandScreenMinorSize =
            mContext.resources.getDimensionPixelSize(R.dimen.fake_landscape_screen_minor_size)
        updateMinorScreenSize()
    }
    private fun updateRootViewSize(configuration: Configuration) {
        val windowInfo = EnvStateManager.getWindowInfo(mContext, configuration)
        val point = mRootViewSizeDp
        val point2: Point = windowInfo.windowSizeDp
        point.x = point2.x
        point.y = point2.y
        val point3 = mRootViewSize
        val point4: Point = windowInfo.windowSize
        point3.x = point4.x
        point3.y = point4.y
    }
    fun updateRootViewSize(view: View) {
        mRootViewSize.x = view.width
        mRootViewSize.y = view.height
        val f = mContext.resources.displayMetrics.density
        val point = mRootViewSizeDp
        val point2 = mRootViewSize
        point.x = (point2.x / f).toInt()
        point.y = (point2.y / f).toInt()
    }
    private fun isConfigurationChanged(configuration: Configuration): Boolean {
        val configuration2 = configurationAfterInstalled
        return configuration2!!.uiMode != configuration.uiMode ||
                configuration2.screenLayout != configuration.screenLayout ||
                configuration2.orientation != configuration.orientation ||
                configuration2.screenWidthDp != configuration.screenWidthDp ||
                configuration2.screenHeightDp != configuration.screenHeightDp ||
                (if (configuration2.fontScale > configuration.fontScale) 1 else if (configuration2.fontScale == configuration.fontScale) 0 else -1) != 0
                || configuration2.smallestScreenWidthDp != configuration.smallestScreenWidthDp
                || configuration2.keyboard != configuration.keyboard
    }
    fun onConfigurationChanged(configuration: Configuration, z: Boolean, z2: Boolean) {
        mIsInFreeForm = HyperXUIUtils.isFreeformMode(mContext)
        val i = configuration.densityDpi
        val f = i * 1.0f / mCurrentDensityDpi
        if (f != 1.0f) {
            mCurrentDensityDpi = i
        }
        if (!buildJustNow || isConfigurationChanged(configuration) || z) {
            buildJustNow = false
            mExtraImeMargin = -1
            updateRootViewSize(configuration)
            if (!checkThread()) {
                Log.w(
                    "AlertController",
                    "dialog is created in thread:" + mCreateThread + ", but onConfigurationChanged is called from different thread:" + Thread.currentThread() + ", so this onConfigurationChanged call should be ignore"
                )
                return
            }
            if (isDialogImmersive()) {
                mWindow.decorView.removeOnLayoutChangeListener(mLayoutChangeListener)
            }
            if (mWindow.decorView.isAttachedToWindow) {
                if (f != 1.0f) {
                    mPanelMaxWidth = mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_max_width)
                    mPanelMaxWidthLand = mContext.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_max_width_land)
                }
                reInitLandConfig()
                if (isDialogImmersive()) {
                    updateWindowCutoutMode()
                } else {
                    setupNonImmersiveWindow()
                }
                setupView(false, z, z2, f)
            }
            if (isDialogImmersive()) {
                mWindow.decorView.addOnLayoutChangeListener(mLayoutChangeListener)
                val rootWindowInsets = mWindow.decorView.rootWindowInsets
                rootWindowInsets?.let { updateDialogPanelByWindowInsets(it) }
            }
            mDialogRootView!!.post {
                this@AlertController.updateRootViewSize(this@AlertController.mDialogRootView!!)
            }
        }
    }
    private fun onLayoutReload() {
        mDialog.onLayoutReload()
    }
    private fun updateWindowCutoutMode() {
        val screenOrientation: Int = getScreenOrientation()
        if (mScreenOrientation != screenOrientation) {
            mScreenOrientation = screenOrientation
            val associatedActivity = mDialog.getAssociatedActivity()
            if (associatedActivity != null) {
                val cutoutMode = getCutoutMode(
                    screenOrientation,
                    associatedActivity.window.attributes.layoutInDisplayCutoutMode
                )
                if (mWindow.attributes.layoutInDisplayCutoutMode != cutoutMode) {
                    mWindow.attributes.layoutInDisplayCutoutMode = cutoutMode
                    if (mDialog.isShowing) {
                        mWindowManager!!.updateViewLayout(mWindow.decorView, mWindow.attributes)
                        return
                    }
                    return
                }
                return
            }
            val i = if (getScreenOrientation() != 2) 1 else 2
            if (mWindow.attributes.layoutInDisplayCutoutMode != i) {
                mWindow.attributes.layoutInDisplayCutoutMode = if (i != 2) LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES else LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                if (mDialog.isShowing) {
                    mWindowManager!!.updateViewLayout(mWindow.decorView, mWindow.attributes)
                }
            }
        }
    }
    private fun getScreenOrientation(): Int {
        val windowManager = mWindowManager ?: return 0
        val rotation = windowManager.defaultDisplay.rotation
        return if (rotation == 1 || rotation == 3) 2 else 1
    }
    fun setShowAnimListener(onDialogShowAnimListener: OnDialogShowAnimListener?) {
        mShowAnimListener = onDialogShowAnimListener
    }
    fun onStart() {
        if (isDialogImmersive()) {
            if (mDimBg != null) {
                updateDimBgBottomMargin(0)
            }
            reInitLandConfig()
            updateWindowCutoutMode()
            if (!mIsFromRebuild && mEnableEnterAnim) {
                executeShowAnim(
                    mParentPanel, mDimBg, isLandscape(),
                    mShowAnimListenerWrapper
                )
            } else {
                mParentPanel!!.tag = null
                mDimBg!!.alpha = 0.3f
            }
            mWindow.decorView.addOnLayoutChangeListener(mLayoutChangeListener)
        }
    }
    fun onStop() {
        if (isDialogImmersive()) {
            mWindow.decorView.removeOnLayoutChangeListener(mLayoutChangeListener)
        }
    }
    private fun checkAndClearFocus() {
        val currentFocus = mWindow.currentFocus
        if (currentFocus != null) {
            currentFocus.clearFocus()
            hideSoftIME()
        }
    }
    private fun checkThread(): Boolean {
        return mCreateThread === Thread.currentThread()
    }
    fun onAttachedToWindow() {
        reInitLandConfig()
        setupWindowInsetsAnimation()
    }

    fun onDetachedFromWindow() {
        translateDialogPanel(0)
    }
    fun dismiss(onDismiss: OnDismiss?) {
        cleanWindowInsetsAnimation()
        val dialogParentPanel2 = mParentPanel
        if (dialogParentPanel2 == null) {
            onDismiss?.end()
        } else {
            if (dialogParentPanel2.isAttachedToWindow) {
                checkAndClearFocus()
                executeDismissAnim(mParentPanel, mDimBg, onDismiss)
                return
            }
            Log.d("AlertController", "dialog is not attached to window when dismiss is invoked")
            try {
                mDialog.realDismiss()
            } catch (e: java.lang.IllegalArgumentException) {
                Log.wtf(
                    "AlertController",
                    "Not catch the dialog will throw the illegalArgumentException (In Case cause the crash , we expect it should be caught)",
                    e
                )
            }
        }
    }
    private fun changeTitlePadding(textView: TextView) {
        textView.setPadding(textView.paddingLeft, textView.paddingTop, textView.paddingRight, 0)
    }
    fun translateDialogPanel(i: Int) {
        mParentPanel!!.animate().cancel()
        mParentPanel!!.translationY = i.toFloat()
    }
    private fun isFreeFormMode(): Boolean {
        return isFreeFormMode(mContext)
    }
    fun updateDialogPanelByWindowInsets(windowInsets: WindowInsets) {
        updateParentPanelMarginByWindowInsets(windowInsets)
        if (isNeedUpdateDialogPanelTranslationY()) {
            val isInMultiWindowMode: Boolean = HyperXUIUtils.isInMultiWindowMode(mContext)
            val insets: Insets = windowInsets.getInsets(WindowInsets.Type.ime())
            val insets2: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            val isTablet = isTablet()
            if (!isTablet) {
                updateDimBgBottomMargin(insets.bottom)
            }
            var i: Int = insets.bottom
            if (isInMultiWindowMode && !isTablet) {
                i -= insets2.bottom
            }
            updateDialogPanelTranslationYByIme(i, isInMultiWindowMode, isTablet)
        }
    }
    private fun isNeedUpdateDialogPanelTranslationY(): Boolean {
        val c: Int
        val isInMultiWindowMode: Boolean = HyperXUIUtils.isInMultiWindowMode(mContext)
        val i = mContext.resources.configuration.keyboard
        val z = i == 2 || i == 3
        val z2 = Build.IS_TABLET
        c = if (!isInMultiWindowMode || isFreeFormMode()) {
            65535
        } else {
            if (DeviceHelper.isWideScreen(mContext)) 0 else 1
        }
        if (mIsDialogAnimating) {
            if (z2 && z || c != 0) {
                return false
            }
        } else {
            if (z2 && z || !mSetupWindowInsetsAnimation) {
                return false
            }
            if (!mInsetsAnimationPlayed && !isInMultiWindowMode) {
                return false
            }
        }
        return true
    }
    fun setNonImmersiveDialogHeight(i: Int) {
        mNonImmersiveDialogHeight = i
    }
    private fun isSpecifiedDialogHeight(): Boolean {
        return !(isDialogImmersive() || mNonImmersiveDialogHeight == -2)
    }
    private fun updateDialogPanelTranslationYByIme(i: Int, z: Boolean, z2: Boolean) {
        if (i > 0) {
            val dialogPanelMargin = (getDialogPanelMargin() + mParentPanel!!.translationY)
            mPanelAndImeMargin = dialogPanelMargin.toInt()
            if (dialogPanelMargin <= 0) {
                mPanelAndImeMargin = 0
            }
            val i2 =
                if (!z2 || i >= mPanelAndImeMargin) if (!z || z2) -i + mPanelAndImeMargin else -i else 0
            if (mIsDialogAnimating) {
                mParentPanel!!.animate().cancel()
                mParentPanel!!.animate().setDuration(200L).translationY(i2.toFloat()).start()
                return
            }
            translateDialogPanel(i2)
            return
        }
        if (mParentPanel!!.translationY < 0.0f) {
            translateDialogPanel(0)
        }
    }

    class ButtonInfo {
        var mButton: GroupButton? = null
        var mMsg: Message?
        val mOnClickListener: OnClickListener?
        val mStyle: Int
        val mText: CharSequence?
        val mWhich: Int
        constructor(text: CharSequence, style: Int, message: Message) {
            mText = text
            mStyle = style
            mMsg = message
            mOnClickListener = null
            mWhich = 0
        }
        constructor(text: CharSequence, style: Int, onClickListener: OnClickListener, which: Int) {
            mText = text
            mStyle = style
            mMsg = null
            mOnClickListener = onClickListener
            mWhich = which
        }
    }

    class LayoutChangeListener(alertController: AlertController): View.OnLayoutChangeListener {
        private val mHost: WeakReference<AlertController>
        private val mWindowVisibleFrame: Rect = Rect()
        init {
            mHost = WeakReference<AlertController>(alertController)
        }
        override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
            mHost.get()?.let {
                p0?.getWindowVisibleDisplayFrame(mWindowVisibleFrame)
                handleMultiWindowLandscapeChange(it, p3)
            }
        }
        private fun handleImeChange(view: View, rect: Rect, alertController: AlertController) {
            val height = (view.height - alertController.getDialogPanelExtraBottomPadding()) - rect.bottom
            val translateY: Int
            if (height > 0) {
                val i = -height
                val rootWindow = view.rootWindowInsets
                translateY = (rootWindow?.getInsets(WindowInsets.Type.systemBars())?.bottom ?: 0).and(i)
                DialogAnimHelper.cancelAnimator()
            } else {
                translateY = 0
            }
            alertController.translateDialogPanel(translateY)
        }
        private fun changeViewPadding(view: View?, left: Int, right: Int) {
            view?.setPadding(left, 0, right, 0)
        }
        private fun handleMultiWindowLandscapeChange(alertController: AlertController, i: Int) {
            if (HyperXUIUtils.isInMultiWindowMode(alertController.mContext)) {
                val rect = mWindowVisibleFrame
                if (rect.left > 0) {
                    val width = i - rect.width()
                    if (mWindowVisibleFrame.right == i) {
                        changeViewPadding(alertController.mDialogRootView, width, 0)
                        return
                    } else {
                        changeViewPadding(alertController.mDialogRootView, 0, width)
                        return
                    }
                }
                changeViewPadding(alertController.mDialogRootView, 0, 0)
                return
            }
            val dialogRootView = alertController.mDialogRootView ?: return
            if (dialogRootView.paddingLeft > 0 || dialogRootView.paddingRight > 0) {
                changeViewPadding(dialogRootView, 0, 0)
            }
        }
        fun hasNavigationBarHeightInMultiWindowMode(): Boolean {
            val host = mHost.get() ?: return false
            getScreenSize(host.mContext, host.mScreenRealSize)
            val rect = mWindowVisibleFrame
            return !(rect.left == 0 && rect.right == host.mScreenRealSize.x && rect.top <= HyperXUIUtils.getStatusBarHeight(host.mContext))
        }

        fun isInMultiScreenTop(): Boolean {
            val host = mHost.get() ?: return false
            getScreenSize(host.mContext, host.mScreenRealSize)
            val rect = mWindowVisibleFrame
            if (rect.left != 0 || rect.right != host.mScreenRealSize.x) {
                return false
            }
            val i = (host.mScreenRealSize.y.toFloat() * 0.75f).toInt()
            return rect.top >= 0 && rect.bottom <= i
        }
    }
    class ButtonHandler(dialogInterface: DialogInterface): Handler() {
        companion object val MSG_DISMISS_DIALOG: Int = -1651327837
        private val mDialog: WeakReference<DialogInterface>
        init {
            mDialog = WeakReference(dialogInterface)
        }
        override fun handleMessage(msg: Message) {
            val dialogInterface = mDialog.get()
            val i = msg.what
            if (i != MSG_DISMISS_DIALOG) {
                (msg.obj as OnClickListener).onClick(dialogInterface, i)
            } else {
                dialogInterface?.dismiss()
            }
        }
    }
    
    class AlertParams(context: Context) {
        var iconHeight = 0
        var iconWidth = 0
        var mAdapter: ListAdapter? = null
        var mCheckBoxMessage: CharSequence? = null
        var mCheckedItems: BooleanArray? = null
        var mComment: CharSequence? = null
        val mContext: Context
        var mCursor: Cursor? = null
        var mCustomTitleView: View? = null
        var mEnableEnterAnim = false
        var mExtraButtonList: List<ButtonInfo>? = null
        var mHapticFeedbackEnabled = false
        var mIcon: Drawable? = null
        val mInflater: LayoutInflater
        var mIsChecked = false
        var mIsCheckedColumn: String? = null
        var mIsMultiChoice = false
        var mIsSingleChoice = false
        var mItems: Array<CharSequence?>? = null
        var mLabelColumn: String? = null
        var mLiteVersion = 0
        var mMessage: CharSequence? = null
        var mNegativeButtonListener: OnClickListener? = null
        var mNegativeButtonText: CharSequence? = null
        var mNeutralButtonListener: OnClickListener? = null
        var mNeutralButtonText: CharSequence? = null
        var mOnCancelListener: DialogInterface.OnCancelListener? = null
        var mOnCheckboxClickListener: OnMultiChoiceClickListener? = null
        var mOnClickListener: OnClickListener? = null
        var mOnDialogShowAnimListener: OnDialogShowAnimListener? = null
        var mOnDismissListener: DialogInterface.OnDismissListener? = null
        var mOnItemSelectedListener: AdapterView.OnItemSelectedListener? = null
        var mOnKeyListener: DialogInterface.OnKeyListener? = null
        var mOnPrepareListViewListener: OnPrepareListViewListener? = null
        var mOnShowListener: OnShowListener? = null
        var mPanelSizeChangedListener: AlertDialog.OnPanelSizeChangedListener? = null
        var mPositiveButtonListener: OnClickListener? = null
        var mPositiveButtonText: CharSequence? = null
        var mPreferLandscape = false
        var mSmallIcon = false
        var mTitle: CharSequence? = null
        var mView: View? = null
        var mViewLayoutResId = 0
        var mIconId = 0
        var mIconAttrId = 0
        var mCheckedItem = -1
        var mCancelable = true
        var mEnableDialogImmersive: Boolean = !LiteUtils.isCommonLiteStrategy()
        var mNonImmersiveDialogHeight = -2
        
        init {
            mContext = context
            mLiteVersion = 0
            mEnableEnterAnim = true
            mExtraButtonList = ArrayList<ButtonInfo>()
            mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        fun apply(alertController: AlertController) {
            val view = mCustomTitleView
            if (view != null) {
                alertController.setCustomTitle(view)
            } else {
                val charSequence = mTitle
                if (charSequence != null) {
                    alertController.setTitle(charSequence)
                }
                val drawable = mIcon
                if (drawable != null) {
                    alertController.setIcon(drawable)
                }
                val i2 = mIconId
                if (i2 != 0) {
                    alertController.setIcon(i2)
                }
                val i3 = mIconAttrId
                if (i3 != 0) {
                    alertController.setIcon(alertController.getIconAttributeResId(i3))
                }
                if (mSmallIcon) {
                    alertController.setUseSmallIcon(true)
                }
                val i4 = iconWidth
                val i5 = iconHeight
                if (i4 != 0 && i5 != 0) {
                    alertController.setIconSize(i4, i5)
                }
            }
            val charSequence2 = mMessage
            if (charSequence2 != null) {
                alertController.setMessage(charSequence2)
            }
            val charSequence3 = mComment
            if (charSequence3 != null) {
                alertController.setComment(charSequence3)
            }
            val charSequence4 = mPositiveButtonText
            if (charSequence4 != null) {
                alertController.setButton(
                    -1,
                    charSequence4,
                    mPositiveButtonListener,
                    null
                )
            }
            val charSequence5 = mNegativeButtonText
            if (charSequence5 != null) {
                alertController.setButton(
                    -2,
                    charSequence5,
                    mNegativeButtonListener,
                    null
                )
            }
            val charSequence6 = mNeutralButtonText
            if (charSequence6 != null) {
                alertController.setButton(
                    -3,
                    charSequence6,
                    mNeutralButtonListener,
                    null
                )
            }
            if (mExtraButtonList != null) {
                alertController.mExtraButtonList = ArrayList(mExtraButtonList!!)
            }
            if (mItems != null || mCursor != null || mAdapter != null) {
                createListView(alertController)
            }
            val view2 = mView
            if (view2 != null) {
                alertController.setView(view2)
            } else {
                val i5 = mViewLayoutResId
                if (i5 != 0) {
                    alertController.setView(i5)
                }
            }
            val charSequence7 = mCheckBoxMessage
            if (charSequence7 != null) {
                alertController.setCheckBox(mIsChecked, charSequence7)
            }
            alertController.mHapticFeedbackEnabled = mHapticFeedbackEnabled
            alertController.setEnableImmersive(mEnableDialogImmersive)
            alertController.setNonImmersiveDialogHeight(mNonImmersiveDialogHeight)
            alertController.setLiteVersion(mLiteVersion)
            alertController.setPreferLandscape(mPreferLandscape)
            alertController.setPanelSizeChangedListener(null)
            alertController.setEnableEnterAnim(mEnableEnterAnim)
        }
        private fun createListView(alertController: AlertController) {
            var adapter: ListAdapter?
            val listView = mInflater.inflate(alertController.mListLayout, null) as ListView
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = object : ArrayAdapter<CharSequence?>(
                        mContext, alertController.mMultiChoiceItemLayout, android.R.id.text1, mItems!!
                    ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val superView = super.getView(position, convertView, parent)
                            val checkedItems = mCheckedItems
                            if (checkedItems != null && checkedItems[position]) {
                                listView.setItemChecked(position, true)
                            }
                            superView.isForceDarkAllowed = false
                            return superView
                        }
                    }
                } else {
                    adapter = object : CursorAdapter(
                        mContext, mCursor, false
                    ) {
                        private val mIsCheckedIndex: Int
                        private val mLabelIndex: Int
                        init {
                            val cursor2 = cursor
                            mLabelIndex = cursor2.getColumnIndexOrThrow(mLabelColumn)
                            mIsCheckedIndex = cursor2.getColumnIndexOrThrow(mIsCheckedColumn)
                        }
                        override fun newView(p0: Context?, p1: Cursor?, p2: ViewGroup?): View {
                            val inflate = mInflater.inflate(alertController.mMultiChoiceItemLayout, p2, false)
                            inflate.isForceDarkAllowed = false
                            return inflate
                        }
                        override fun bindView(p0: View?, p1: Context?, p2: Cursor?) {
                            if (p2 == null) return
                            p0?.findViewById<CheckedTextView>(android.R.id.text1)?.text = p2.getString(mLabelIndex)
                            listView.setItemChecked(p2.position, p2.getInt(mIsCheckedIndex) == 1)
                        }
                    }
                }
            } else {
                val layoutResId = if (mIsSingleChoice) {
                    alertController.mSingleChoiceItemLayout
                } else {
                    alertController.mListItemLayout
                }
                if (mCursor != null) {
                    adapter = object : SimpleCursorAdapter(
                        mContext, layoutResId, mCursor, arrayOf<String?>(mLabelColumn), intArrayOf(16908308)
                    ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup?
                        ): View {
                            val superView = super.getView(position, convertView, parent)
                            return superView
                        }
                    }
                } else {
                    adapter = mAdapter
                    if (adapter == null) {
                        adapter = CheckedItemAdapter(mContext, layoutResId, 0x01020014, mItems!!)
                    }
                }
            }
            val onPrepareListViewListener = mOnPrepareListViewListener
            onPrepareListViewListener?.onPrepareListView(listView)
            alertController.mAdapter = adapter
            alertController.mCheckedItem = mCheckedItem
            if (mOnClickListener != null) {
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, p2, _ ->
                        mOnClickListener?.onClick(alertController.mDialog, p2)
                        if (mIsSingleChoice) {
                            return@OnItemClickListener
                        }
                        alertController.mDialog.dismiss()
                    }
            } else if (mOnCheckboxClickListener != null) {
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, p2, _ ->
                        val checkedItems = mCheckedItems
                        if (checkedItems != null) {
                            checkedItems[p2] = listView.isItemChecked(p2)
                        }
                        mOnCheckboxClickListener?.onClick(alertController.mDialog, p2, listView.isItemChecked(p2))
                    }
            }
            val onItemSelectedListener = mOnItemSelectedListener
            if (onItemSelectedListener != null) {
                listView.onItemSelectedListener = onItemSelectedListener
            }
            if (mIsSingleChoice) {
                listView.choiceMode = 1
            } else if (mIsMultiChoice) {
                listView.choiceMode = 2
            }
            alertController.mListView = listView
        }

        class CheckedItemAdapter(
            context: Context, layoutResId: Int, textviewId: Int, charArr: Array<CharSequence?>
        ) : ArrayAdapter<CharSequence?>(
            context, layoutResId, textviewId, charArr
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val superView = super.getView(position, convertView, parent)
                return superView
            }
            override fun getItemId(position: Int): Long {
                return position.toLong()
            }
            override fun hasStableIds(): Boolean {
                return true
            }
        }

        interface OnPrepareListViewListener {
            fun onPrepareListView(listView: ListView)
        }
    }
}