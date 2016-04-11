package com.cglabs.mulakong;


/* Python code:
for i in range(0f,128):
  a = i * 1.40625
  print(str(math.sin((a*math.pi)/180)) + "f,    // index "  + str(i))
*/

import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;

public class JumpControl{
  final public int JUMP_HORIZONTAL = 0;
  final public int JUMP_UP = 1;
  final public int JUMP_DOWN = 2;

        final float[] sin_table={
          0.0f,                // index 0
          0.0245412285229f,    // index 1
          0.0490676743274f,    // index 2
          0.0735645635997f,    // index 3
          0.0980171403296f,    // index 4
          0.122410675199f,    // index 5
          0.146730474455f,    // index 6
          0.17096188876f,    // index 7
          0.195090322016f,    // index 8
          0.219101240157f,    // index 9
          0.242980179903f,    // index 10
          0.266712757475f,    // index 11
          0.290284677254f,    // index 12
          0.313681740399f,    // index 13
          0.336889853392f,    // index 14
          0.359895036535f,    // index 15
          0.382683432365f,    // index 16
          0.405241314005f,    // index 17
          0.42755509343f,    // index 18
          0.449611329655f,    // index 19
          0.471396736826f,    // index 20
          0.49289819223f,    // index 21
          0.514102744193f,    // index 22
          0.534997619887f,    // index 23
          0.55557023302f,    // index 24
          0.575808191418f,    // index 25
          0.595699304492f,    // index 26
          0.615231590581f,    // index 27
          0.634393284164f,    // index 28
          0.653172842954f,    // index 29
          0.671558954847f,    // index 30
          0.689540544737f,    // index 31
          0.707106781187f,    // index 32
          0.724247082951f,    // index 33
          0.740951125355f,    // index 34
          0.757208846506f,    // index 35
          0.773010453363f,    // index 36
          0.788346427627f,    // index 37
          0.803207531481f,    // index 38
          0.817584813152f,    // index 39
          0.831469612303f,    // index 40
          0.84485356525f,    // index 41
          0.85772861f,    // index 42
          0.870086991109f,    // index 43
          0.881921264348f,    // index 44
          0.893224301196f,    // index 45
          0.903989293123f,    // index 46
          0.914209755704f,    // index 47
          0.923879532511f,    // index 48
          0.932992798835f,    // index 49
          0.941544065183f,    // index 50
          0.949528180593f,    // index 51
          0.956940335732f,    // index 52
          0.963776065795f,    // index 53
          0.970031253195f,    // index 54
          0.975702130039f,    // index 55
          0.980785280403f,    // index 56
          0.985277642389f,    // index 57
          0.989176509965f,    // index 58
          0.992479534599f,    // index 59
          0.995184726672f,    // index 60
          0.997290456679f,    // index 61
          0.998795456205f,    // index 62
          0.999698818696f,    // index 63
          1.0f,               // index 64
          0.999698818696f,    // index 65
          0.998795456205f,    // index 66
          0.997290456679f,    // index 67
          0.995184726672f,    // index 68
          0.992479534599f,    // index 69
          0.989176509965f,    // index 70
          0.985277642389f,    // index 71
          0.980785280403f,    // index 72
          0.975702130039f,    // index 73
          0.970031253195f,    // index 74
          0.963776065795f,    // index 75
          0.956940335732f,    // index 76
          0.949528180593f,    // index 77
          0.941544065183f,    // index 78
          0.932992798835f,    // index 79
          0.923879532511f,    // index 80
          0.914209755704f,    // index 81
          0.903989293123f,    // index 82
          0.893224301196f,    // index 83
          0.881921264348f,    // index 84
          0.870086991109f,    // index 85
          0.85772861f,    // index 86
          0.84485356525f,    // index 87
          0.831469612303f,    // index 88
          0.817584813152f,    // index 89
          0.803207531481f,    // index 90
          0.788346427627f,    // index 91
          0.773010453363f,    // index 92
          0.757208846506f,    // index 93
          0.740951125355f,    // index 94
          0.724247082951f,    // index 95
          0.707106781187f,    // index 96
          0.689540544737f,    // index 97
          0.671558954847f,    // index 98
          0.653172842954f,    // index 99
          0.634393284164f,    // index 100
          0.615231590581f,    // index 101
          0.595699304492f,    // index 102
          0.575808191418f,    // index 103
          0.55557023302f,    // index 104
          0.534997619887f,    // index 105
          0.514102744193f,    // index 106
          0.49289819223f,    // index 107
          0.471396736826f,    // index 108
          0.449611329655f,    // index 109
          0.42755509343f,    // index 110
          0.405241314005f,    // index 111
          0.382683432365f,    // index 112
          0.359895036535f,    // index 113
          0.336889853392f,    // index 114
          0.313681740399f,    // index 115
          0.290284677254f,    // index 116
          0.266712757475f,    // index 117
          0.242980179903f,    // index 118
          0.219101240157f,    // index 119
          0.195090322016f,    // index 120
          0.17096188876f,    // index 121
          0.146730474455f,    // index 122
          0.122410675199f,    // index 123
          0.0980171403296f,    // index 124
          0.0735645635997f,    // index 125
          0.0490676743274f,    // index 126
          0.0245412285229f,    // index 127
  };

  private SimpleVector mSrc;
  private SimpleVector mDst;
  private long mStartTime;
  private int mDurationMilli;
  private int mDelay, mCurve;
  private boolean mFinished = false;
  public JumpControl()
  {

  }

  /*
        duraction_sec: Total jump duraction. delay_to_start is inside this time.
  */

  public void StartJump( SimpleVector src, SimpleVector dst, float duration_sec, int delay_to_start, int curve)
  {
    //Logger.log(String.format("StartJump src = %f, %f, %f, dst =  %f, %f, %f", src.x, src.y, src.z, dst.x, dst.y,dst.z));
    mStartTime = System.currentTimeMillis();
    mSrc = src;
    mDst = dst;
    mDurationMilli = (int) (duration_sec * 1000f);
    mDelay = delay_to_start;
    mFinished = false;
    mCurve = curve;
  }

  public SimpleVector GetPosition__() {

    return (new SimpleVector(0f, 0f, 0f));
  }

  public SimpleVector GetPosition () {
    long now = System.currentTimeMillis();
    if (now > mStartTime + mDurationMilli) {
      if (mFinished == false) {
        mFinished = true;
        //Logger.log("*** Dst ***");
        return mDst;
      }
      //Logger.log("** Jump finished **");
      return null;
    }
    float enlapse = now - (mStartTime +mDelay);
    if ( (int) enlapse < 0) {
        return mSrc;
    }
    float factor = enlapse / (mDurationMilli - mDelay);
    SimpleVector pos = new SimpleVector();
    pos.x = ((mDst.x - mSrc.x) * factor) + mSrc.x;
    pos.y = ((mDst.y - mSrc.y) * factor) + mSrc.y;
    pos.z = ((mDst.z - mSrc.z) * factor) + mSrc.z;
    //Logger.log("pos: " + pos.x + ", " + pos.y + ", " + pos.z);
    if (mCurve != 10) {
      int offset = (int) (factor * 126f);
      pos.y -= sin_table[offset] * .7;
    }
    return pos;
  }

}

