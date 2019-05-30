package com.smile.rtlcompat;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jony on 2019/5/16.
 */

public class RtlCompatViewPager extends ViewPager {

    public static final int FLAG_NONE = 0;
    public static final int FLAG_FORCE_LTR = 1;
    public static final int FLAG_FORCE_RTL = 2;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLAG_NONE, FLAG_FORCE_LTR, FLAG_FORCE_RTL})
    public @interface ForceFlag {
        // ForceFlag
    }

    private final boolean isContextRtl;

    @ForceFlag
    private int forceFlag = FLAG_NONE;

    private RtlPagerAdapter rtlPagerAdapter;

    private ArrayMap<OnPageChangeListener, DelegatePageChangeListener> delegateListenerSavedMap = new ArrayMap<>();

    public RtlCompatViewPager(Context context) {
        super(context);
        isContextRtl = isContextRtl(context);
    }

    public RtlCompatViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isContextRtl = isContextRtl(context);
    }

    // isRtlUsed
    private boolean isRtlUsed() {
        if (forceFlag == FLAG_FORCE_LTR) return false;

        return isContextRtl || forceFlag == FLAG_FORCE_RTL;
    }

    // isFlagActive
    private boolean isFlagActive(int newFlag) {
        if (isRtlUsed()) {
            return newFlag == FLAG_FORCE_LTR || newFlag == FLAG_NONE;
        } else {
            return newFlag == FLAG_FORCE_RTL;
        }
    }

    // getUsedPosition
    private int getUsedPosition(int position) {
        if (isRtlUsed()) {
            PagerAdapter adapter = getAdapter();
            int pageCount = adapter == null ? 0 : adapter.getCount();
            if (position >= 0 && position < pageCount) {
                position = pageCount - position - 1;
            }
        }
        return position;
    }

    // getUsedCurrentItem
    private static int getUsedCurrentItem(PagerAdapter adapter, int currentItem) {
        int pageCount = adapter == null ? 0 : adapter.getCount();
        if (currentItem >= 0 && currentItem < pageCount) {
            currentItem = pageCount - currentItem - 1;
        } else {
            currentItem = 0;
        }
        return currentItem;
    }

    // isContextRtl
    private static boolean isContextRtl(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(getUsedPosition(item));
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(getUsedPosition(item), smoothScroll);
    }

    /**
     * 不能修改返回值，因为ViewPager内部在调用！使用{@link #getCurrentItemCompat()}
     */
    @Override
    public int getCurrentItem() {
        return super.getCurrentItem();
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
//        super.setOnPageChangeListener(listener);
    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        DelegatePageChangeListener delegatePageChangeListener = new DelegatePageChangeListener(listener);
        delegateListenerSavedMap.put(listener, delegatePageChangeListener);
        super.addOnPageChangeListener(delegatePageChangeListener);
    }

    @Override
    public void removeOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        listener = delegateListenerSavedMap.remove(listener);
        if (listener != null) {
            super.removeOnPageChangeListener(listener);
        }
    }

    @Override
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        delegateListenerSavedMap.clear();
    }

    @Override
    public PagerAdapter getAdapter() {
        PagerAdapter adapter = super.getAdapter();
        if (adapter instanceof RtlPagerAdapter) {
            return ((RtlPagerAdapter) adapter).pagerAdapter;
        }
        return adapter;
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        internalSetAdapter(adapter);
        if (isRtlUsed()) {
            setCurrentItem(0, false);
        }
    }

    // internalSetAdapter
    private void internalSetAdapter(@Nullable PagerAdapter adapter) {
        if (rtlPagerAdapter != null) {
            rtlPagerAdapter.detach();
            rtlPagerAdapter = null;
        }
        if (isRtlUsed() && adapter != null) {
            rtlPagerAdapter = new RtlPagerAdapter(adapter);
            adapter = rtlPagerAdapter;
        }
        super.setAdapter(adapter);
    }

    /**
     * setAdapter
     */
    public void setAdapter(@Nullable PagerAdapter adapter, int initialPosition) {
        internalSetAdapter(adapter);
        setCurrentItem(initialPosition, false);
    }

    /**
     * getCurrentItemCompat
     */
    public int getCurrentItemCompat() {
        return getUsedPosition(getCurrentItem());
    }

    /**
     * updateForceFlag
     *
     * @param flag {@link #FLAG_NONE,#FLAG_FORCE_LTR,#FLAG_FORCE_RTL}
     */
    public void updateForceFlag(@ForceFlag int flag) {
        if (forceFlag == flag) return;

        PagerAdapter adapter = getAdapter();
        if (adapter == null) {
            forceFlag = flag;
            return;
        }

        boolean isFlagActive = isFlagActive(flag);
        forceFlag = flag;
        if (isFlagActive) {
            int currentItem = getUsedCurrentItem(adapter, getCurrentItem());
            setAdapter(adapter);
            super.setCurrentItem(currentItem, false);
        }
    }

    /**
     * DelegatePageChangeListener
     */
    private class DelegatePageChangeListener implements OnPageChangeListener {

        OnPageChangeListener onPageChangeListener;

        DelegatePageChangeListener(@NonNull OnPageChangeListener listener) {
            onPageChangeListener = listener;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (onPageChangeListener == null) return;

            position = getUsedPosition(position);
            if (isRtlUsed()) {
                float result = position - positionOffset;
                int usedPosition = (int) result;
                float usedPositionOffset = result - usedPosition;
                position = usedPosition;
                positionOffset = usedPositionOffset;
            }
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            if (onPageChangeListener == null) return;

            onPageChangeListener.onPageSelected(getUsedPosition(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (onPageChangeListener == null) return;

            onPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    /**
     * RtlPagerAdapter
     */
    private static class RtlPagerAdapter extends PagerAdapter {

        PagerAdapter pagerAdapter;

        private DataSetObserver dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                RtlPagerAdapter.super.notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                onChanged();
            }
        };

        RtlPagerAdapter(@NonNull PagerAdapter adapter) {
            pagerAdapter = adapter;
            adapter.registerDataSetObserver(dataSetObserver);
        }

        // detach
        void detach() {
            try {
                pagerAdapter.unregisterDataSetObserver(dataSetObserver);
            } catch (Throwable e) {
                // ignore
            }
        }

        // getUsedPosition
        private int getUsedPosition(int position) {
            return getCount() - position - 1;
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            pagerAdapter.startUpdate(container);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            return pagerAdapter.instantiateItem(container, getUsedPosition(position));
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            pagerAdapter.destroyItem(container, getUsedPosition(position), object);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            pagerAdapter.setPrimaryItem(container, getUsedPosition(position), object);
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            pagerAdapter.finishUpdate(container);
        }

        @Override
        public Parcelable saveState() {
            return pagerAdapter.saveState();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            pagerAdapter.restoreState(state, loader);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            int position = pagerAdapter.getItemPosition(object);
            if (position != POSITION_UNCHANGED && position != POSITION_NONE) {
                position = getUsedPosition(position);
            }
            return position;
        }

        @Override
        public void registerDataSetObserver(@NonNull DataSetObserver observer) {
            try {
                pagerAdapter.registerDataSetObserver(observer);
            } catch (Throwable e) {
                // ignore
            }
        }

        @Override
        public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
            try {
                pagerAdapter.unregisterDataSetObserver(observer);
            } catch (Throwable e) {
                // ignore
            }
        }

        @Override
        public void notifyDataSetChanged() {
            pagerAdapter.notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagerAdapter.getPageTitle(getUsedPosition(position));
        }

        @Override
        public float getPageWidth(int position) {
            return pagerAdapter.getPageWidth(getUsedPosition(position));
        }

        @Override
        public int getCount() {
            return pagerAdapter.getCount();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return pagerAdapter.isViewFromObject(view, object);
        }
    }
}
