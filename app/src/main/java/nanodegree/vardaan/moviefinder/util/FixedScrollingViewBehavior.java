package nanodegree.vardaan.moviefinder.util;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Fix for NestedScrollView layout behavior with short content.
 * Source: https://gist.github.com/EmmanuelVinas/c598292f43713c75d18e
 */
public class FixedScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public FixedScrollingViewBehavior() {
    }

    public FixedScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
        int i = 0;

        for (int z = views.size(); i < z; ++i) {
            View view = views.get(i);
            if (view instanceof AppBarLayout) {
                return (AppBarLayout) view;
            }
        }

        return null;
    }

    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec,
            int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (child.getLayoutParams().height == -1) {
            List<View> dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty()) {
                return false;
            }

            AppBarLayout appBar = findFirstAppBarLayout(dependencies);
            if (appBar != null && ViewCompat.isLaidOut(appBar)) {
                if (ViewCompat.getFitsSystemWindows(appBar)) {
                    ViewCompat.setFitsSystemWindows(child, true);
                }

                int scrollRange = appBar.getTotalScrollRange();
                int height = parent.getHeight() - appBar.getMeasuredHeight() + Math
                        .min(scrollRange, parent.getHeight() - heightUsed);
                int heightMeasureSpec = View.MeasureSpec
                        .makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec,
                        heightUsed);
                return true;
            }
        }

        return false;
    }
}
