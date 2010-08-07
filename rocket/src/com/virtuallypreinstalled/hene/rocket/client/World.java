package com.virtuallypreinstalled.hene.rocket.client;

import org.vaadin.gwtgraphics.client.DrawingArea;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;

public class World extends FocusPanel implements KeyDownHandler, KeyUpHandler {

	public static int SCREEN_SIZE = 500;

	private final DrawingArea screen;

	private final AbstractRocket rocket;

	private final Timer timer;

	private long drawCnt = 0;

	public World() {
		int performanceTestDur = getPerformanceTestDuration();

		setStyleName("focuspanel");
		//setSize(SCREEN_SIZE + "px", SCREEN_SIZE + "px");
		addKeyDownHandler(this);
		addKeyUpHandler(this);

		screen = new DrawingArea(SCREEN_SIZE, SCREEN_SIZE);
		screen.setStyleName("screen");
		clearScreen();
		add(screen);

		rocket = new Rocket();

		if (performanceTestDur > 0) {
			Timer perfoTimer = new PerformanceTestTimer();
			perfoTimer.schedule(performanceTestDur * 1000);
		}

		timer = new RocketTimer();
		timer.scheduleRepeating((performanceTestDur > 0) ? 1 : 25);

	}

	private void clearScreen() {
		screen.clear();
	}
	
	public void onKeyDown(KeyDownEvent event) {
		if (event.isLeftArrow()) {
			rocket.setThrusterOn(0, true);
		} else if (event.isRightArrow()) {
			rocket.setThrusterOn(1, true);
		}
	}

	public void onKeyUp(KeyUpEvent event) {
		if (event.isLeftArrow()) {
			rocket.setThrusterOn(0, false);
		} else if (event.isRightArrow()) {
			rocket.setThrusterOn(1, false);
		} else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			rocket.reset();
		}
		
	}

	private native int getPerformanceTestDuration() /*-{
		var regexp = /performanceTest=(\d+)/;
		var res = $wnd.location.search.match(regexp);
		if (res) {
			return parseInt(res[1]);
		}
		return 0;
	}-*/;

	public class RocketTimer extends Timer {

		@Override
		public void run() {
			clearScreen();
			rocket.update();
			rocket.draw(screen);
			drawCnt++;
		}

	}

	public class PerformanceTestTimer extends Timer {

		@Override
		public void run() {
			timer.cancel();
			Window.alert(drawCnt + " draws!");
		}

	}
}
