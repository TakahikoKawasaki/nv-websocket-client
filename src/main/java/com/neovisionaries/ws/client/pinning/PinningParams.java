package com.neovisionaries.ws.client.pinning;


import java.util.ArrayList;
import java.util.List;

public class PinningParams {

	private int mPinningEnforceTimeout;
	private List<String> mPins = new ArrayList<String>();

	public PinningParams(int pinningEnforceTimeout, List<String> pins) {
		mPinningEnforceTimeout = pinningEnforceTimeout;
		mPins = pins;
	}

	public int getPinningEnforceTimeout() {
		return mPinningEnforceTimeout;
	}

	public List<String> getPins() {
		return mPins;
	}
}
