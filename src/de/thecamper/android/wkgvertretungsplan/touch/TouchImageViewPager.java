package de.thecamper.android.wkgvertretungsplan.touch;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class TouchImageViewPager extends ViewPager {
	public TouchImageViewPager(Context context) {
		super(context);
	}

	public TouchImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof TouchImageView) {
			return ((TouchImageView) v).canScrollHorizontallyFroyo(-dx);
		} else {
			return super.canScroll(v, checkV, dx, x, y);
		}
	}
}
