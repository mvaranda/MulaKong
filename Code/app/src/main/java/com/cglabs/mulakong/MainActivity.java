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

import com.glfont.GLFont;
//import CameraOrbitController;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.andresjesse.jpctblendae.JPCTBlendScene;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.threed.jpct.Animation;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Logger;
import com.threed.jpct.Mesh;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import raft.jpct.bones.Animated3D;
import raft.jpct.bones.AnimatedGroup;
import raft.jpct.bones.BonesIO;
import raft.jpct.bones.SkeletonPose;
import raft.jpct.bones.SkinClip;


/**
 * JPCTBlend AE sample. Based on EgonOlsen's JPCT-AE Hello World
 * (More information at http://www.jpct.net/jpct-ae/).
 *
 * @author andres
 */
public class MainActivity extends Activity {

	// Used to handle pause and resume...
	private static MainActivity master = null;
	private final String MULAKONG_MESH = "media/meshs/mulakong_bones";
	private final String MULAKONG_TEXTURE = "media/textures/mulakong_texture.png";

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer frameBuffer = null;
	private World world = null;

	private int fps = 0;

	private JPCTBlendScene scn;

	private static final int GRANULARITY = 25;

	private AnimatedGroup mulakong;
	private Object3D mulakongObj;
//	private final List<AnimatedGroup> ninjas = new LinkedList<AnimatedGroup>();
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	private long frameTime = System.currentTimeMillis();
	private long aggregatedTime = 0;
	private float animateSeconds  = 0f;
	private float speed = 1f;

	private int animation = 7;
	private boolean useMeshAnim = false;

	private CameraOrbitController cameraController;
	private JumpControl jump;
	private MulaControl mulaControl;

	private SimpleVector srcPos = new SimpleVector(-0.030186296f, -2.2025304f, -1.7779007f);
	private SimpleVector dstPos = new SimpleVector(-0.030186176f, -3.4372475f, -1.7779007f);
	private boolean toggle = false;

//	private AnimatedGroup masterNinja;
//	private final List<AnimatedGroup> ninjas = new LinkedList<AnimatedGroup>();



	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_NONE};
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

		try {
			Resources res = getResources();
			//InputStream st = res.openRawResource(com.cglabs.mulakong.R.raw.mulakong_bones);
			InputStream st;
			st = getResources().getAssets().open(MULAKONG_MESH);
			mulakong = BonesIO.loadGroup(st);
			createMeshKeyFrames();
			mulakong.setSkeletonPose(new SkeletonPose(mulakong.get(0).getSkeleton()));
			mulakong.getRoot().translate((float) 0, 0, (float) 0);

			//mulakong.addToWorld(world);
			//addNinja();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		jump = new JumpControl();

	}

	private void setMulakongTexture() {
		TextureManager.getInstance().flush();
		Resources res = getResources();
		InputStream st;
		try {
			st = getResources().getAssets().open(MULAKONG_TEXTURE);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Texture texture = new Texture(st);
		texture.keepPixelData(true);
		TextureManager.getInstance().addTexture("mulakong", texture);

		for (Animated3D a : mulakong)
			a.setTexture("mulakong");

	}
	private void createMeshKeyFrames() {
		Config.maxAnimationSubSequences = mulakong.getSkinClipSequence().getSize() + 1; // +1 for whole sequence

		int keyframeCount = 0;
		final float deltaTime = 0.2f; // max time between frames

		for (SkinClip clip : mulakong.getSkinClipSequence()) {
			float clipTime = clip.getTime();
			int frames = (int) Math.ceil(clipTime / deltaTime) + 1;
			keyframeCount += frames;
		}

		Animation[] animations = new Animation[mulakong.getSize()];
		for (int i = 0; i < mulakong.getSize(); i++) {
			animations[i] = new Animation(keyframeCount);
			animations[i].setClampingMode(Animation.USE_CLAMPING);
		}
		//System.out.println("------------ keyframeCount: " + keyframeCount + ", mesh size: " + mulakong.getSize());
		int count = 0;

		int sequence = 0;
		for (SkinClip clip : mulakong.getSkinClipSequence()) {
			float clipTime = clip.getTime();
			int frames = (int) Math.ceil(clipTime / deltaTime) + 1;
			float dIndex = 1f / (frames - 1);

			for (int i = 0; i < mulakong.getSize(); i++) {
				animations[i].createSubSequence(clip.getName());
			}
			//System.out.println(sequence + ": " + clip.getName() + ", frames: " + frames);
			for (int i = 0; i < frames; i++) {
				mulakong.animateSkin(dIndex * i, sequence + 1);

				for (int j = 0; j < mulakong.getSize(); j++) {
					Mesh keyframe = mulakong.get(j).getMesh().cloneMesh(true);
					keyframe.strip();
					animations[j].addKeyFrame(keyframe);
					count++;
					//System.out.println("added " + (i + 1) + " of " + sequence + " to " + j + " total: " + count);
				}
			}
			sequence++;
		}
		for (int i = 0; i < mulakong.getSize(); i++) {
			mulakong.get(i).setAnimationSequence(animations[i]);
		}
		mulakong.get(0).getSkeletonPose().setToBindPose();
		mulakong.get(0).getSkeletonPose().updateTransforms();
		mulakong.applySkeletonPose();
		mulakong.applyAnimation();

		Logger.log("created mesh keyframes, " + keyframeCount + "x" + mulakong.getSize());
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.cglabs.mulakong/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.disconnect();
	}

	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.cglabs.mulakong/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}


    //************************************************
	//*
	//*              T O U C H    E V E N T
	//*
	//************************************************
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int[] shoot = new int[1];
		shoot[0] = 0;

		long now = System.currentTimeMillis();

		if (cameraController.onTouchEvent(event, shoot)) {
			if (shoot[0] > 0) {
				if (toggle) {
					Logger.log("*********** shoot ************");
					animation = 7;
					if (animation > mulakong.getSkinClipSequence().getSize())
						animation = 1;

					float clipTimeInSeconds = mulakong.getSkinClipSequence().getClip(animation - 1).getTime();
					jump.StartJump(srcPos, dstPos, clipTimeInSeconds, 400, jump.JUMP_UP);
				}
				else {
					Object3D mula = mulakong.getRoot();
						mula.clearTranslation();
						mula.translate(srcPos);
				}
				toggle = !toggle;
			}
			return true;
		}

		return super.onTouchEvent(event);
	}

	/******************************
	 *
	 *           INIT SCENE
	 *
	 * ****************************
	 */
	class MyRenderer implements GLSurfaceView.Renderer {
		private int fps = 0;
		private int lfps = 0;

		private long fpsTime = System.currentTimeMillis();

		private GLFont glFont;
		private GLFont buttonFont;

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (frameBuffer != null) {
				frameBuffer.dispose();
			}
			frameBuffer = new FrameBuffer(gl, w, h);

			if (master == null) {

				world = new World();

				//scn = new JPCTBlendScene("media/scenes/sample_scene/sample_scene.xml", getAssets(), world);
				scn = new JPCTBlendScene("media/scene.xml", getAssets(), world);
				mulakong.addToWorld(world);

				int i;
				for (i=0; i<mulakong.getSkinClipSequence().getSize(); i++)
					Logger.log ("Animation: " + (i+1) + " " + mulakong.getSkinClipSequence().getClip(i).getName());

				cameraController = new CameraOrbitController(world.getCamera());
				cameraController.height = h;
				cameraController.width = w;


				// ------------  display all objects  ------------
				Enumeration<Object3D> objs = world.getObjects();
				Object3D obj;
				while (objs.hasMoreElements()){ // (obj = objs.nextElement()) != null) {
					obj = objs.nextElement();
					obj.setTransparency(-1);
					if (obj.getName().contains("location")) {
						obj.setVisibility(obj.OBJ_INVISIBLE);
					}
					SimpleVector loc = obj.getTranslation();
					Logger.log("**Object " + obj.getName() +
							" x = " + loc.x +
							" y = " + loc.y +
							" z = " + loc.z);
				}
				Object3D mula = mulakong.getRoot();
				mula.rotateY(3.1415927f);
				mula.clearTranslation();
				mula.translate(srcPos);
				mulaControl = new MulaControl(world, mulakong);


				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = MainActivity.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			setMulakongTexture();
		}



		public void onDrawFrame_(GL10 gl) {

			//Update JPCTBlend Scene
			scn.update();

			frameBuffer.clear();
			world.renderScene(frameBuffer);
			world.draw(frameBuffer);
			frameBuffer.display();

			//fps log
			if (System.currentTimeMillis() - time >= 1000) {
				Logger.log(fps + "fps");
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
		@Override
		public void onDrawFrame(GL10 gl) {
			if (frameBuffer == null)
				return;


			long now = System.currentTimeMillis();
			aggregatedTime += (now - frameTime);
			frameTime = now;

			if (aggregatedTime > 1000) {
				aggregatedTime = 0;
			}


			while (aggregatedTime > GRANULARITY) {
				aggregatedTime -= GRANULARITY;
				animateSeconds += GRANULARITY * 0.001f * speed;
				cameraController.placeCamera();
			}

			//////////////////////////////////////////////////////////////
			///////////////////////// ANIMATION //////////////////////////
			//////////////////////////////////////////////////////////////
			Object3D mula = mulakong.getRoot();
			MulaControl.Action action = null;
			if (animation == 0) {
				action = mulaControl.GetAction();
				if (action.animation == 0) {
					mula.clearTranslation();
					mula.translate(action.dst);
				}
				else {
					Logger.log(String.format("  Anim = %d, SrcX = %.2f, SrcY = %.2f, DstX = %.2f, DstY = %.2f",
									action.animation, action.src.x, action.src.y, action.dst.x, action.dst.y));

					animation = action.animation;
					float clipTimeInSeconds = mulakong.getSkinClipSequence().getClip(animation - 1).getTime();
					//jump.StartJump(action.src, action.dst, clipTimeInSeconds, action.delay_to_start, jump.JUMP_UP);
					jump.StartJump(action.src, action.dst, action.duration_sec, action.delay_to_start, jump.JUMP_UP);
					Logger.log(String.format("Anim = %d, Duracao = %f, delay = %d", animation, action.duration_sec, action.delay_to_start));
				}
			}

			if (animation > 0 && mulakong.getSkinClipSequence().getSize() >= animation) {
				float clipTime = mulakong.getSkinClipSequence().getClip(animation-1).getTime();
				//Object3D mula = mulakong.getRoot();
				if (animateSeconds > clipTime) {
					animateSeconds = 0;
					animation = 0;

					if (action != null)  {
						mula.clearTranslation();
						mula.translate(action.dst); //dstPos);
					}

				}
				else {
					float index = animateSeconds / clipTime;
					SimpleVector loc = jump.GetPosition();
					if (loc != null) {
						//if (loc.z != 0f) {
							mula.clearTranslation();
							mula.translate(loc);
						//}
						SimpleVector t = mula.getTranslation();
					}
					mulakong.animateSkin(index, animation);
					//mulakong.

				}

			} else {
				animateSeconds = 0f;
			}

			frameBuffer.clear();
			world.renderScene(frameBuffer);
			world.draw(frameBuffer);
/*
			buttonFont.blitString(frameBuffer, "+", 10, 40, 10, RGBColor.WHITE);
			buttonFont.blitString(frameBuffer, "-", frameBuffer.getWidth()-30, 40, 10, RGBColor.WHITE);

			glFont.blitString(frameBuffer, lfps + "/" + ninjas.size() + " " + (useMeshAnim ? "M" : "S"),
					5, frameBuffer.getHeight()-5, 10, RGBColor.WHITE);

*/
			frameBuffer.display();

			if (System.currentTimeMillis() - fpsTime >= 1000) {
				lfps = (fps + lfps) >> 1;
				fps = 0;
				fpsTime = System.currentTimeMillis();
			}
			fps++;

		}

	}
}
