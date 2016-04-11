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

/*

1- JumpHandL
2- JumpHandLHandL
3- JumpHandLHandL_stand
4- JumpHandR
5- JumpHandRHandR
6- JumpHandRHandR_stand
7- JumpUp
8- WalkAction
9- L2R
10- R2L
11- Head_1
12- FallR
13- FallL
14- FallR_floor
15- FallL_floor

Jumps:
Idle_pos
         —> Right jump via JumpHandL (End Pos)-> Hanging_facing_Right
         -> Left jump via JumpHandR (End Pos)-> Hanging_facing_Left
         -> JumpUp (End Pos) -> Idle_pos

Hanging_facing_Right
         -> Right jump via JumpHandLHandL (End Pos)-> Hanging_facing_Right
         -> Right jump via JumpHandLHandL_stand (End Pos)-> Idle_pos
         -> Flip via L2R (End Pos)-> Hanging_facing_Left

Hanging_facing_Left
         -> Left jump via JumpHandRHandR (End Pos)-> Hanging_facing_Left
         -> Left jump via JumpHandRHandR_stand (End Pos)-> Idle_pos
         -> Flip via R2L (End Pos)-> Hanging_facing_Right

JumpHandL
JumpHandLHandL
JumpHandLHandL_stand
JumpHandR
JumpHandRHandR
JumpHandRHandR_stand

JumpUp -> JumpUp, JumpHandX( left or right)
JumpHandX -> JumpHandXHandX, JumpHandXHandX_stand *Need to turn
JumpHandXHandX -> JumpHandXHandX, JumpHandXHandX_stand *Need to turn
JumpHandXHandX_stand -> JumpUp, JumpHandX (left or right)


JumpHandL duration             1.708333 41 frames (@24fps): delay 166ms, dur: 0.875
JumpHandLHandL duration        2.000000 delay = 833, dur = 1.416
JumpHandLHandL_stand duration  1.583333 delay = 791, dur = 1.5
JumpHandR duration             1.708333 delay = 208, dur = 0.833
JumpHandRHandR duration        2.000000 delay = 833, dur = 1.416
JumpHandRHandR_stand duration  1.583333 delay = 291, dur = 1.458
JumpUp duration                1.166667 delay = 208, dur = 1.166
WalkAction duration            2.375000 delay = 0, dur = 2.375
L2R duration                   0.375000 delay = 0, dur = 0.375
R2L duration                   0.416667 delay = 0, dur = 0.416
Head_1 duration                9.625000
FallR duration                 0.791667 delay = 9, dur = 0.708
FallL duration                 0.833333 delay = 0, dur = 0.750
FallR_floor duration           0.791667 delay = 0, due = 0.791
FallL_floor duration           0.791667 delay = 0, due = 0.791


 */

import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import java.util.Enumeration;
import java.util.Random;

import raft.jpct.bones.AnimatedGroup;


public class MulaControl{

  // anim_id
  final public int JumpHandL                      = 0;
  final public int JumpHandLHandL                 = 1;
  final public int JumpHandLHandL_stand           = 2;
  final public int JumpHandR                      = 3;
  final public int JumpHandRHandR                 = 4;
  final public int JumpHandRHandR_stand           = 5;
  final public int JumpUp                         = 6;
  final public int WalkAction                     = 7;
  final public int L2R                            = 8;
  final public int R2L                            = 9;
  final public int Head_1                         = 10;
  final public int FallR                          = 11;
  final public int FallL                          = 12;
  final public int FallR_floor                    = 13;
  final public int FallL_floor                    = 14;
  final public int num_animations                 = 15;

  private AnimatedGroup mulakong;

  // final_orientation:
  final private int idle                 = 0;
  final private int hanging_facing_right = 1;
  final private int hanging_facing_left  = 2;
  final private int lay_down             = 3;

  final private int LAST_JUMP                     = 7;

  final private int ST_INIT                       = 0;
  final private int ST_WAITING_DELAY              = 1;
  final private int ST_END_ANIMATION              = 2;
  final private int ST_TURNING                    = 3;
  final private int ST_DONE_TURNING               = 4;

  private int max_delay_ms  = 2000;
  private int min_delay_ms  = 500;
  private long delay_expire_at;

  private int floor = 100;
  private int state = ST_WAITING_DELAY;

  private SimpleVector mSrc;
  private SimpleVector mDst;
  private long mStartTime;
  private int mDurationMilli;
  private int mDelay, mCurve;
  private boolean mFinished = false;
  private int mLastOrientation = -1;
  private SimpleVector mLastDst = new SimpleVector(0f,0f,0f);
  private int mChosenAnim = 0;
  private int mLastChosenAnim = JumpUp;
  private Action mActionAfterRotation;


  private World world;
  private final int NUM_FLOORS = 11;
  private final int NUM_MAX_RANGES_PER_FLOOR = 4;
  private Range[][] ranges = new Range[NUM_FLOORS][NUM_MAX_RANGES_PER_FLOOR];
  private JumpDescriptor[] JumpDescriptorArray = new JumpDescriptor[num_animations];

  public class Action{
    public int animation;
    public SimpleVector src;
    public SimpleVector dst;
    public float duration_sec; // duraction_sec: Total jump duraction. delay_to_start is inside this time.
    public int delay_to_start; // in milliseconds


    public Action(int animation_, SimpleVector src_, SimpleVector dst_, float duration_sec_, int delay_to_start_) {
      animation = animation_;
      src = new SimpleVector(src_);
      dst = new SimpleVector(dst_);
      duration_sec = duration_sec_;
      delay_to_start = delay_to_start_;
    }

    public Action( Action obj) { // copy constructor
      animation = obj.animation;
      src = new SimpleVector(obj.src);
      dst = new SimpleVector(obj.dst);
      duration_sec = obj.duration_sec;
      delay_to_start = obj.delay_to_start;
    }
  }

  private class JumpDescriptor {
    public int anim_id;
    public float final_y_offset;
    public int final_orientation;
    public float duration_sec; // duraction_sec: Total jump duraction. delay_to_start is inside this time.
    public int delay_to_start; // in milliseconds
    public int num_possible_anim;
    public int[] possible_anim;
    private final int MAX_NUM_ANIM = 4;

    public JumpDescriptor(  int anim_id_,
                            float final_y_offset_,
                            int final_orientation_,
                            float duration_sec_,
                            int delay_to_start_,
                            int num_possible_anim_,
                            int a0, int a1, int a2, int a3) {
      anim_id = anim_id_;
      final_y_offset = final_y_offset_;
      final_orientation = final_orientation_;
      duration_sec = duration_sec_;
      delay_to_start = delay_to_start_;
      num_possible_anim = num_possible_anim_;
      possible_anim = new int[MAX_NUM_ANIM];
      possible_anim[0] = a0;
      possible_anim[1] = a1;
      possible_anim[2] = a2;
      possible_anim[3] = a3;

    }
  }


  private class Range {
    public SimpleVector HighLimit;
    public SimpleVector LowLimit;
    public Range(SimpleVector h, SimpleVector l) {HighLimit = h; LowLimit = l;}
  }

  public Action GetAction() {
    Action a;
    long now;
    switch (state) {
      case ST_INIT:
        state = ST_WAITING_DELAY;
        now = System.currentTimeMillis();
        delay_expire_at = System.currentTimeMillis() + randInt(min_delay_ms, max_delay_ms);
        floor = 0;
        SimpleVector dst = new SimpleVector(0, 0, 0);
        a = new Action(0, dst, dst, 0f, 0);
        mLastDst = new SimpleVector(dst);
        return a;

      case ST_WAITING_DELAY:
        now = System.currentTimeMillis();
        if (now > delay_expire_at) {
          // time to start a new animation
          if (++floor > NUM_FLOORS) {
            // for now we restart
            floor = 0;
              dst = new SimpleVector(0, 0, 0);
            a = new Action(0, dst, dst, 0f, 0);
            delay_expire_at = System.currentTimeMillis() + randInt(min_delay_ms, max_delay_ms);
            return a;
          }
          // select animation
          int idx = randInt(0, JumpDescriptorArray[mLastChosenAnim - 1].num_possible_anim - 1);
          Logger.log("mLastChosenAnim = " + mLastChosenAnim + ", idx = " + idx);
          mChosenAnim = JumpDescriptorArray[mLastChosenAnim - 1].possible_anim[idx] + 1;
          int chosen_range = randInt(0, NUM_MAX_RANGES_PER_FLOOR - 1);
          float chosen_dst_x = randFloat(ranges[floor - 1][chosen_range].LowLimit.x, ranges[floor - 1][chosen_range].HighLimit.x);
          if (chosen_dst_x > mLastDst.x) {
            switch(mChosenAnim) {
              case JumpHandL: mChosenAnim = JumpHandR; break;
              case JumpHandLHandL: mChosenAnim = JumpHandRHandR; break;
              case JumpHandLHandL_stand: mChosenAnim = JumpHandRHandR_stand; break;
              default: break;

            }
          }

          mLastChosenAnim = mChosenAnim;
          // TODO: check if turn is required
          SimpleVector dst_loc = new SimpleVector(ranges[floor - 1][chosen_range].LowLimit);
          dst_loc.x = chosen_dst_x;
          Logger.log(">>>> " + mChosenAnim);
          a = new Action(   mChosenAnim,
                            mLastDst,
                            dst_loc,
                            JumpDescriptorArray[mChosenAnim - 1].duration_sec,
                            JumpDescriptorArray[mChosenAnim - 1].delay_to_start);

          if (mLastOrientation == hanging_facing_right) { // facing right
            if (dst_loc.x > mLastDst.x) { // need to rorate to left
              mActionAfterRotation = new Action(a); // save next action (for after rotation is done)
              a = new Action(   L2R + 1,
                      mLastDst,
                      mLastDst,
                      JumpDescriptorArray[L2R].duration_sec,
                      JumpDescriptorArray[L2R].delay_to_start);
              state = ST_TURNING;
              mLastOrientation = JumpDescriptorArray[mChosenAnim - 1].final_orientation;
              Logger.log("** Turn Left ANIMATION = " + mChosenAnim);
              return a;
            }
          }

          if (mLastOrientation == hanging_facing_left) { // facing right
            if (dst_loc.x < mLastDst.x) { // need to rorate to left
              mActionAfterRotation = new Action(a); // save next action (for after rotation is done)
              a = new Action(   R2L + 1,
                      mLastDst,
                      mLastDst,
                      JumpDescriptorArray[R2L].duration_sec,
                      JumpDescriptorArray[R2L].delay_to_start);
              state = ST_TURNING;
              mLastOrientation = JumpDescriptorArray[mChosenAnim - 1].final_orientation;
              Logger.log("** Turn Right ANIMATION = " + mChosenAnim);
              return a;
            }
          }

          mLastDst = new SimpleVector(dst_loc);
          mLastOrientation = JumpDescriptorArray[mChosenAnim - 1].final_orientation;
          state = ST_END_ANIMATION;
          Logger.log("** Normal ANIMATION = " + mChosenAnim);
          return a;
        }
        // Time has not expired yet
        a = new Action(0, mLastDst, mLastDst, 0f, 0);
        return a;

      case ST_END_ANIMATION:
        state = ST_WAITING_DELAY;
        now = System.currentTimeMillis();
        delay_expire_at = System.currentTimeMillis() + randInt(min_delay_ms, max_delay_ms);
        a = new Action(0, mLastDst, mLastDst, 0f, 0);
        return a;

      case ST_TURNING:
        //mLastDst = new SimpleVector(mActionAfterRotation.dst);
        a = new Action(0, mLastDst, mLastDst, 0f, 0);
        state = ST_DONE_TURNING;
        Logger.log("** finishing Turn ANIMATION = " + mChosenAnim);
        return a;

      case ST_DONE_TURNING:
        mLastDst = new SimpleVector(mActionAfterRotation.dst);
        state = ST_END_ANIMATION;
        Logger.log("** End Turn ANIMATION = " + mChosenAnim);
        return mActionAfterRotation;

      default:
        Logger.log("unexpected state");
        return null;
    }

  }

  public MulaControl(World world_, AnimatedGroup mulakong_) {
    world = world_;
    mulakong = mulakong_;
    SimpleVector location_0_0 = null; // x = -0.030186296 y = -2.2025304 z = -1.7779007
    SimpleVector location_0_1 = null; // x = -0.030186176 y = -3.4372475 z = -1.7779007
    SimpleVector location_1_0 = null; // x = -2.3591924   y = -2.2025304 z = -1.7779007
    SimpleVector location_2_0 = null; // x = -4.0570183   y = -2.1874661 z = -2.058723
    SimpleVector location_3_0 = null; // x = -4.598459    y = -2.1874661 z = -2.058723

    Enumeration<Object3D> objs = world.getObjects();
    Object3D obj;
    while (objs.hasMoreElements()) {
      obj = objs.nextElement();
      if (obj.getName().contains("location")) {
        if (obj.getName().equals("location_0_0_jPCT3"))
          location_0_0 = new SimpleVector(obj.getTranslation());
        if (obj.getName().equals("location_0_1_jPCT4"))
          location_0_1 = new SimpleVector(obj.getTranslation());
        if (obj.getName().equals("location_1_0_jPCT5"))
          location_1_0 = new SimpleVector(obj.getTranslation());
        if (obj.getName().equals("location_2_0_jPCT6"))
          location_2_0 = new SimpleVector(obj.getTranslation());
        if (obj.getName().equals("location_3_0_jPCT7"))
          location_3_0 = new SimpleVector(obj.getTranslation());
      }
 /*     SimpleVector loc = obj.getTranslation();
      Logger.log("**Object " + obj.getName() +
              " x = " + loc.x +
              " y = " + loc.y +
              " z = " + loc.z);
    }*/
    }
    //ranges = new Range[NUM_FLOORS][NUM_MAX_RANGES_PER_FLOOR];
    ranges[0][0] = new Range(new SimpleVector(location_1_0), new SimpleVector(location_0_0));
    ranges[0][1] = new Range(new SimpleVector(location_3_0), new SimpleVector(location_2_0));
    ranges[0][2] = new Range(new SimpleVector(location_1_0), new SimpleVector(location_0_0));
    ranges[0][2].HighLimit.x *= -1f;
    ranges[0][2].LowLimit.x *= -1f;
    ranges[0][3] = new Range(new SimpleVector(location_3_0), new SimpleVector(location_2_0));
    ranges[0][3].HighLimit.x *= -1f;
    ranges[0][3].LowLimit.x *= -1f;

    float floorBase = location_0_0.y;
    float floorOffset = location_0_1.y - location_0_0.y;
    int i,z;
    for (i=0; i<NUM_FLOORS; i++) {
      for (z=0; z<NUM_MAX_RANGES_PER_FLOOR; z++) {
        if (i != 0) {
          ranges[i][z] = new Range(new SimpleVector(ranges[0][z].HighLimit), new SimpleVector(ranges[0][z].LowLimit));
          ranges[i][z].HighLimit.y = ranges[i][z].LowLimit.y = floorBase + floorOffset * i;
        }
        Logger.log(String.format("ranges[%d][%d]: HighLimit.x = %f, HighLimit.y = %f, HighLimit.y = %f", i,z,
                ranges[i][z].HighLimit.x, ranges[i][z].HighLimit.y, ranges[i][z].HighLimit.y));
        Logger.log(String.format("ranges[%d][%d]: LowLimit.x = %f, LowLimit.y = %f, LowLimit.y = %f", i,z,
                ranges[i][z].LowLimit.x, ranges[i][z].LowLimit.y, ranges[i][z].LowLimit.y));
      }
    }

    //-------------- init animation descriptors ----------------
    float clipTimeInSeconds;
    for (i=0; i<num_animations; i++) {
      clipTimeInSeconds = mulakong.getSkinClipSequence().getClip(i).getTime();
      Logger.log(String.format( "Animation: %s duration %f",
                                mulakong.getSkinClipSequence().getClip(i).getName(),
                                clipTimeInSeconds));
    }
    /*JumpUp -> JumpUp, JumpHandX( left or right)
    JumpHandX -> JumpHandXHandX, JumpHandXHandX_stand *Need to turn
    JumpHandXHandX -> JumpHandXHandX, JumpHandXHandX_stand *Need to turn
    JumpHandXHandX_stand -> JumpUp, JumpHandX (left or right)*/

    JumpDescriptorArray[0] = new JumpDescriptor(JumpHandL, 0f, hanging_facing_right, 0.833f, 208,
            2, JumpHandLHandL, JumpHandLHandL_stand, 0, 0);

    JumpDescriptorArray[1] = new JumpDescriptor(JumpHandLHandL, 0f, hanging_facing_right, 1.416f, 883,
            2, JumpHandLHandL, JumpHandLHandL_stand, 0, 0);

    JumpDescriptorArray[2] = new JumpDescriptor(JumpHandLHandL_stand, 0f, idle, 1.5f, 791,
            2, JumpUp, JumpHandL, 0, 0);

    JumpDescriptorArray[3] = new JumpDescriptor(JumpHandR, 0f, hanging_facing_left, 0.833f, 208,
            2, JumpHandLHandL, JumpHandLHandL_stand, 0, 0);

    JumpDescriptorArray[4] = new JumpDescriptor(JumpHandRHandR, 0f, hanging_facing_left, 1.416f, 833,
            2, JumpHandLHandL, JumpHandLHandL_stand, 0, 0);

    JumpDescriptorArray[5] = new JumpDescriptor(JumpHandRHandR_stand, 0f, idle, 1.458f, 291,
            2, JumpUp, JumpHandL, 0, 0);

    JumpDescriptorArray[6] = new JumpDescriptor(JumpUp, 0f, idle, 1.166f, 208,
            2, JumpUp, JumpHandL, 0, 0);

    JumpDescriptorArray[7] = new JumpDescriptor(WalkAction, 0f, idle, 2.375f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[8] = new JumpDescriptor(L2R, 0f, hanging_facing_right, 0.375f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[9] = new JumpDescriptor(R2L, 0f, hanging_facing_left, 0.416f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[10] = new JumpDescriptor(Head_1, 0f, idle, 9.625f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[11] = new JumpDescriptor(FallR, 0f, idle, 0.708f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[12] = new JumpDescriptor(FallL, 0f, idle, 0.750f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[13] = new JumpDescriptor(FallR_floor, 0f, lay_down, 0.791f, 0,
            0,0,0,0,0);

    JumpDescriptorArray[14] = new JumpDescriptor(FallL_floor, 0f, lay_down, 0.791f, 0,
            0,0,0,0,0);

  }

  public static int randInt(int min, int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;
    return randomNum;
  }

  public static float randFloat(float min, float max) {
    Random rand = new Random();
    float finalX = rand.nextFloat() * (max - min) + min;
    return finalX;
  }

}



