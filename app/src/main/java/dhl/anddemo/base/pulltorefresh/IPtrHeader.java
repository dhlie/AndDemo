package dhl.anddemo.base.pulltorefresh;

/**
 * Created by DuanHl on 2015/11/24.
 */
public interface IPtrHeader {
	void onReset();

	void onPullToRefresh();

	void onReleaseToRefresh();

	void onRefreshing();

	void onRefreshComplete();

	void onPositionChanged(int state, int headerHeight, int offset);
}
