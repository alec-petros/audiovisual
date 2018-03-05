import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import ddf.minim.signals.*; 
import ddf.minim.spi.*; 
import ddf.minim.ugens.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class line_test extends PApplet {









Minim minim;
AudioInput input;
FFT fft;

ControlP5 cp5;

int bgColor = color(0,0,0);

float STROKE_MAX = 10;
float STROKE_MIN = 2;
float audioThresh = 1.5f;
float[] circles = new float[10];
float[] other = new float[10];
float DECAY_RATE = 4;

PShader blur;

public void setup() {
  minim = new Minim(this);
  cp5 = new ControlP5(this);

  
  frameRate(60);

  blur = loadShader("blur.glsl");

  noFill();
  ellipseMode(RADIUS);

 //  cp5.addSlider("STROKE_MIN")
 //  .setRange(0,40);
 //
 // cp5.addSlider("STROKE_MAX")
 //  .setRange(0,40);
 //
 // cp5.addSlider("audioThresh")
 //  .setRange(0,1);

  input = minim.getLineIn(minim.MONO, 2048);
  fft = new FFT(input.bufferSize(), input.sampleRate());
  fft.logAverages( 40, 3);
}

public void draw() {

  background(fft.getAvg(1)/2, fft.getAvg(1)/2, fft.getAvg(1)/2);
  pushMatrix();
  translate(width/2, height/2);
  fft.forward(input.mix);
  stroke(255, 0, 0, 128);

  rotate(radians(frameCount/2));

  // draw the spectrum as a series of vertical lines
  // I multiple the value of getBand by 4
  // so that we can see the lines better
  for(int i = 0; i < 10; i++)
  {
    float amplitude = map(fft.getAvg(i), 0, 50, 0, 10);
    if (amplitude<audioThresh) {
      circles[i] = min(height/2,amplitude*(height/2));
    } else {
      circles[i] = max(0, min(height,circles[i]-DECAY_RATE));
    }

    float centerFrequency = fft.getAverageCenterFrequency(i);

    float averageWidth = fft.getAverageBandWidth(i);

    float lowFreq = centerFrequency - averageWidth/2;
    float highFreq = centerFrequency - averageWidth/2;

    int xl = (int)fft.freqToIndex(lowFreq);
    int xr = (int)fft.freqToIndex(highFreq);

    pushStyle();

    // ELLIPSES
    scale(1, 1);
    stroke(255,amplitude*255,255,amplitude*255);
    strokeWeight(map(amplitude, 0, 1, STROKE_MIN, STROKE_MAX));
    ellipse(0, 0, circles[i], circles[i]);

    // RECTANGLES
    stroke(255-amplitude*40, 255,255-amplitude*10,255-amplitude*200);
    strokeWeight(map(amplitude, 0, 1, 0, 4));
    scale(1, min(1, amplitude/3));
    if (i%3 == 0) {
      rect(0, 0, circles[i], circles[i]);
    } else {
      rect(0, 0, -circles[i], -circles[i]);
    }

    // LINES
    scale(1, 1);
    stroke(255-amplitude*40, 255,255-amplitude*10,amplitude*10);
    if (i%4 == 0) {
      strokeWeight(map(amplitude, 0, 10, 0, .5f));
      rotate(radians(0-frameCount/4));
      line(0, 0, height-frameCount, width-frameCount);
    }
    filter(blur);

    popStyle();

  }

  println(fft.getAvg(1));
  popMatrix();

  // for(int i = 0; i < input.left.size() - 1; i++)
  // {
  //   line(i, 50 + input.left.get(i)*50, i+1, 50 + input.left.get(i+1)*50);
  //   line(i, 150 + input.right.get(i)*50, i+1, 150 + input.right.get(i+1)*50);
  // }
  // for (int i = 0; i < width; i += 1) {
  //   stroke(1 * i);
  //   line(0, 0, mouseX, mouseY);
  //   line(width, 0, mouseX, mouseY);
  // }
}
  public void settings() {  size(1920, 1080, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "line_test" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
