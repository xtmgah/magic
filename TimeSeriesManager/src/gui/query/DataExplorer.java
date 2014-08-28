package gui.query;

import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import swing2swt.layout.FlowLayout;
import tsdb.aggregated.AggregationInterval;
import tsdb.raw.TimestampSeries;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseMoveListener;

public class DataExplorer extends Composite {

	private Canvas canvas;

	private TimeSeriesView dataView;

	private boolean leftMouseDown;
	private boolean leftMouseDownMoved;
	private int leftMouseDownPrevX;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DataExplorer(Composite parent, int style) {
		super(parent, style);

		leftMouseDown = false;
		leftMouseDownMoved = false;

		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if(leftMouseDown) {
					leftMouseDownMoved = true;
					int diffX = e.x-leftMouseDownPrevX;
					
					double offset = dataView.getViewOffset();
					offset += diffX*60*24;
					dataView.setViewOffset(offset);
					
					
					leftMouseDownPrevX = e.x;
					canvas.redraw();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				System.out.println("zoom*");
				if(e.button==1) {
					if(!leftMouseDownMoved) {
						System.out.println("zoom+");					
						double zoomFactor = dataView.getZoomFactor();
						if(zoomFactor<100) {
							zoomFactor++;
						}
						dataView.setZoomFactor(zoomFactor);
					}
					leftMouseDown = false;
					leftMouseDownMoved = false;
				} else if(e.button==3) {
					System.out.println("zoom-");
					double zoomFactor = dataView.getZoomFactor();
					if(zoomFactor>1) {
						zoomFactor--;
					}
					dataView.setZoomFactor(zoomFactor);
				}
				canvas.redraw();
			}
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button==1) {
					leftMouseDown = true;
					leftMouseDownMoved = false;
					leftMouseDownPrevX = e.x;
				}
			}
		});
		setLayout(new BorderLayout(0, 0));

		canvas = new Canvas(this, SWT.NONE);		
		canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				dataView.paintCanvas(e.gc, true);
			}
		});
		canvas.setEnabled(false);
		canvas.setLayoutData(BorderLayout.CENTER);

		dataView = new TimeSeriesView();
		dataView.setCanvas(canvas);

	}

	public TimestampSeries getTimeSeries() {
		return dataView.getTimeSeries();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setData(TimestampSeries resultTimeSeries, AggregationInterval aggregationInterval) {
		dataView.updateViewData(resultTimeSeries, aggregationInterval," ",null);
		canvas.redraw();

	}
}
