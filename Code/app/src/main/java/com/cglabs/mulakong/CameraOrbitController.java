/************************************************************************

    Copyright 2016: Marcelo Varanda

    This file is part of MulaKong.

    MulaKong is free gaming software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation version 3 of the License.

    MulaKong is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
 along with MulaKong. If not, see http://www.gnu.org/licenses/.

*************************************************************************/

package com.cglabs.mulakong;

import android.graphics.PointF;
import android.text.method.KeyListener;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
//import bones.samples.RenderPanel;

import com.threed.jpct.Camera;
import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;

/**
 * 
 * @author hakan eryargi (r a f t)
 * */
public class CameraOrbitController {
	
	public final SimpleVector cameraTarget = new SimpleVector(0, 0, 0);
	/** the angle with respect to positive Z axis. initial value is PI so looking down to positive Z axis. */
	public float cameraAngle = (float)(Math.PI);
	public float zoom = 0f;

	public float cameraRadius = -20f;
	public float cameraRotationSpeed = 0.1f;
	public float minCameraRadius = 3f;
	public float cameraMoveStepSize = 0.5f;
	
	public float dragTurnAnglePerPixel = (float) (Math.PI / 256);
	public float dragMovePerPixel = 0.01f;
	public float cameraMovePerWheelClick = 1.1f;
	public int width = 0;
	public int height = 0;

	private boolean cameraMovingUp = false;
	private boolean cameraMovingDown = false;
	private boolean cameraMovingIn = false;
	private boolean cameraMovingOut = false;
	private boolean cameraTurningLeft = false;
	private boolean cameraTurningRight = false;
	
 	private float dragStartX, dragStartY;
	private float dragStart2_X, dragStart2_Y;
	private float cameraAngleAtDragStart = 0f;
	private float cameraHeightAtDragStart = 0f;
	private SparseArray<PointF> mActivePointers;

	private int leftPointerID = -1;
	private int rightTopPointerID = -1;
	private int rightBottomPointerID = -1;

	private int startLeftPointerID_X;
	private int startLeftPointerID_Y;
	private int startRightTopPointerID_X;
	private int startRightTopPointerID_Y;
	private int StartRightBottomPointerID_X;
	private int StartRightBottomPointerID_Y;

	final private float MAX_CAM_ANGLE = 3.64f;			// max val of cameraAngle
	final private float MIN_CAM_ANGLE = 2.68f;			// min val of cameraAngle
	final private float MAX_CAM_TARGET_HEIGTH = -21.6f; // cameraTarget.y
	final private float MIN_CAM_TARGET_HEIGTH = 0f;     // cameraTarget.y

	final private float MAX_ZOOM = -1.3f; // cameraRadius
	final private float MIN_ZOOM = -38f; // cameraRadius
	final private float MAX_PAN = 5f;
	final private float MIN_PAN = -5f;
	private float pan = 0f; //
	private float pan_delta = 0f; //


private Camera camera;
	
	public CameraOrbitController(Camera camera) {
		this.camera = camera;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				cameraMovingUp = true;
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				cameraMovingDown = true;
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				cameraTurningRight = true;
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				cameraTurningLeft = true;
				return true;
			case KeyEvent.KEYCODE_A:
				cameraMovingIn = true;
				return true;
			case KeyEvent.KEYCODE_Z:
				cameraMovingOut = true;
				return true;
		}
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				cameraMovingUp = false;
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				cameraMovingDown = false;
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				cameraTurningRight = false;
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				cameraTurningLeft = false;
				return true;
			case KeyEvent.KEYCODE_A:
				cameraMovingIn = false;
				return true;
			case KeyEvent.KEYCODE_Z:
				cameraMovingOut = false;
				return true;
		}
		return false;
	}

	public boolean onTouchEvent(MotionEvent event, int[] shoot) {
		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		int maskedAction = event.getActionMasked();

		//Logger.log("pointerIndex = " + pointerIndex + ", pointerID = " + pointerId);

		switch (maskedAction) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				int x = (int) event.getX(pointerIndex);
				int y = (int) event.getY(pointerIndex);
				if (x < width/2) {
					leftPointerID = pointerId;
					startLeftPointerID_X = x;
					startLeftPointerID_Y = y;
					cameraAngleAtDragStart = cameraAngle;
					cameraHeightAtDragStart = cameraTarget.y;

					Logger.log(String.format("startLeftPointerID id %d: x = %d, y = %d", pointerId, x, y));
				}
				else { // Touch right side of screen
					if (y < height/2) { // ZOOM
						rightTopPointerID = pointerId;
						startRightTopPointerID_X = x;
						startRightTopPointerID_Y = y;
						Logger.log(String.format("startRightTopPointerID id %d: x = %d, y = %d", pointerId, x,y));
					}
					else { // shootting
						rightBottomPointerID = pointerId;
						StartRightBottomPointerID_X = x;
						StartRightBottomPointerID_Y = y;
						Logger.log(String.format("StartRightBottomPointerID id %d: x = %d, y = %d", pointerId, x,y));
						shoot[0] = 1;
					}
				}

				return true;

			case MotionEvent.ACTION_MOVE:

				for (int size = event.getPointerCount(), i = 0; i < size; i++) {
					int id = event.getPointerId(i);
					float candidate;
					if (id == leftPointerID) { // UP/DOWN and Rotate
						cameraAngle = cameraAngleAtDragStart
								+ (event.getX(i) - startLeftPointerID_X) * dragTurnAnglePerPixel;
						cameraTarget.y = cameraHeightAtDragStart
								- (event.getY(i) - startLeftPointerID_Y) * dragMovePerPixel;

					}
					else if (id == rightTopPointerID) { // ZOOM
						candidate = zoom;
						zoom =  (event.getY(i) - startRightTopPointerID_Y) * dragMovePerPixel / 1;
						if (cameraRadius + zoom < MIN_ZOOM || cameraRadius + zoom > MAX_ZOOM)
							zoom = candidate;
						//cameraRadius = cameraRadius + (event.getY(i) - startRightTopPointerID_Y) * dragMovePerPixel / 100;
						float temp = pan_delta;
						pan_delta = (event.getX(i) - startRightTopPointerID_X) * dragMovePerPixel / 1;

						if ((pan + pan_delta >  MAX_PAN) || (pan + pan_delta <  MIN_PAN)) {
							pan_delta = temp;
						}
						if (pan_delta <  MIN_PAN) pan_delta = MIN_PAN;
						Logger.log("pan_delta = " + pan_delta);


					}
					else if (id == rightBottomPointerID) {

					}
					//PointF point = mActivePointers.get(event.getPointerId(i));
					//if (point != null) {
					//	point.x = event.getX(i);
					//	point.y = event.getY(i);
					//}

					SimpleVector pos = camera.getPosition();
					SimpleVector dir = camera.getDirection();
					//Logger.log(String.format("Camera pos: %f, %f, %f, cameraAngle = %f",pos.x, pos.y,pos.z, cameraAngle));
					//Logger.log(String.format("Camera dir: %f, %f, %f, cameraRadius = %f",dir.x, dir.y,dir.z, cameraRadius));


				}
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				//mActivePointers.remove(pointerId);
				if (pointerId == leftPointerID) {
					leftPointerID = -1;
				}
				else if (pointerId == rightTopPointerID) {
					cameraRadius += zoom;
					pan += pan_delta;
					pan_delta = 0;
					zoom = 0f;
					rightTopPointerID = -1;
				}
				else if (pointerId == rightBottomPointerID) {
					rightBottomPointerID = -1;
				}

				return true;

			default:
				return true;
		}
/*
		switch (maskedAction) { //event.getAction()) {
			case MotionEvent.ACTION_DOWN: 
		        dragStartX = event.getX();
		        dragStartY = event.getY();
//				Logger.log(String.format("StartX = %f, StartY = %f", dragStartX, dragStartY));
		        cameraAngleAtDragStart = cameraAngle;
		        cameraHeightAtDragStart = cameraTarget.y;
		        return true;

			case MotionEvent.ACTION_MOVE:

				if (dragStartX < width/2) {
					cameraAngle = cameraAngleAtDragStart
							+ (event.getX() - dragStartX) * dragTurnAnglePerPixel;
					cameraTarget.y = cameraHeightAtDragStart
							- (event.getY() - dragStartY) * dragMovePerPixel;
				}
				else {
					cameraRadius = cameraRadius + (event.getY() - dragStartY) * dragMovePerPixel / 10;
				}
				return true;
			case MotionEvent.ACTION_POINTER_DOWN:
				dragStart2_X = event.getX();
				dragStart2_Y = event.getY();
//				Logger.log(String.format("Start2_X = %f, Start2_Y = %f", dragStart2_X, dragStart2_Y));
				return true;
			case MotionEvent.ACTION_POINTER_UP:
				return true;
			default:
				Logger.log("unhandled touch event");
				//return true;
		}
		*/
	//	return false;
	}

    public void placeCamera() {
    	if (cameraMovingUp)
			cameraTarget.y -= cameraMoveStepSize;
    	if (cameraMovingDown)
			cameraTarget.y += cameraMoveStepSize;
    	if (cameraMovingIn)
			cameraRadius = Math.max(cameraRadius - cameraMoveStepSize, minCameraRadius);
    	if (cameraMovingOut)
			cameraRadius += cameraMoveStepSize;
    	if (cameraTurningRight)
    		cameraAngle += cameraRotationSpeed;
    	if (cameraTurningLeft)
    		cameraAngle -= cameraRotationSpeed;

		if (cameraAngle > MAX_CAM_ANGLE) cameraAngle = MAX_CAM_ANGLE;
		if (cameraAngle < MIN_CAM_ANGLE) cameraAngle = MIN_CAM_ANGLE;

        float camX = (float) Math.sin(cameraAngle) * (cameraRadius + zoom);
        float camZ = (float) Math.cos(cameraAngle) * (cameraRadius + zoom);

		if (cameraTarget.y > MIN_CAM_TARGET_HEIGTH) cameraTarget.y = MIN_CAM_TARGET_HEIGTH;
		if (cameraTarget.y < MAX_CAM_TARGET_HEIGTH) cameraTarget.y = MAX_CAM_TARGET_HEIGTH;

        SimpleVector camPos = new SimpleVector(camX + pan + pan_delta, 0, camZ);
        camPos.add(cameraTarget);
        camera.setPosition(camPos);
			  SimpleVector look = new SimpleVector(pan + pan_delta, 0, 0);
			  look.add(cameraTarget);
        camera.lookAt(look);
	}
	

}
