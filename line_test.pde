import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;
import controlP5.*;

Minim minim;
AudioInput input;
FFT fft;

ControlP5 cp5;

color bgColor = color(0,0,0);

float STROKE_MAX = 10;
float STROKE_MIN = 2;
float audioThresh = 1.5;
float[] circles = new float[10];
float[] other = new float[10];
float DECAY_RATE = 4;

PShader blur;

void setup() {
  minim = new Minim(this);
  cp5 = new ControlP5(this);

  size(1920, 1080, P3D);
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

void draw() {

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
    float amplitude = fft.getAvg(i);
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
    stroke(255,amplitude*255,255,amplitude*255);
    strokeWeight(map(amplitude, 0, 1, STROKE_MIN, STROKE_MAX));
    ellipse(0, 0, circles[i], circles[i]);

    // RECTANGLES
    stroke(255-amplitude*40, 255,255-amplitude*10,255-amplitude*255);
    strokeWeight(map(amplitude, 0, 1, 0, 3));
    if (i%2 == 0) {
      rect(0, 0, circles[i], circles[i]);
    } else {
      rect(0, 0, -circles[i], -circles[i]);
    }

    // LINES
    if (i%4 == 0) {
      strokeWeight(map(amplitude, 0, 10, 0, .5));
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
