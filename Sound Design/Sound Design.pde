import controlP5.*;
import beads.*;
import java.util.Arrays; 

ControlP5 cp5;

AudioContext ac;

// TODO: Alter sine count to change intensity of sound output
// The higher the sine count, the more intense the output signal
int sineCount = 7;

// TODO: Alter frequencies of each emotion to change pitch of sound output
// The lower the frequency, the deeper the sound
float disgustFrequency = 130.0;
float contemptFrequency = 200.0;
float angerFrequency = 360.0;


float sineIntensity = 1.0;

// Array of Glide UGens for series of harmonic frequencies for each wave type (fundamental sine, square, triangle, sawtooth)
Glide[] sineFrequency = new Glide[sineCount];

// Array of Gain UGens for harmonic frequency series amplitudes (i.e. baseFrequency + (1/3)*(baseFrequency*3) + (1/5)*(baseFrequency*5) + ...)
Gain[] sineGain = new Gain[sineCount];
Gain masterGain;
Glide masterGainGlide;

// Array of sine wave generator UGens - will be summed by masterGain to additively synthesize square, triangle, sawtooth waves
WavePlayer[] sineTone = new WavePlayer[sineCount];

void setup() {
  size(400,400);
  ac = new AudioContext();
  
  cp5 = new ControlP5(this);
  
  
  // Add buttons to interface
     
  cp5.addButton("Contempt")
     .setValue(0)
     .setPosition(160,70)
     .setSize(90,40)
     ;
  
  cp5.addButton("Disgust")
     .setValue(0)
     .setPosition(160,150)
     .setSize(90,40)
     ;
     
  cp5.addButton("Anger")
     .setValue(0)
     .setPosition(160,230)
     .setSize(90,40)
     ;
  
  // Instantiate the glide/gain so glide can be set to zero
  masterGainGlide = new Glide(ac, .5, 200);  
  masterGain = new Gain(ac, 1, masterGainGlide);
  ac.out.addInput(masterGain);
  
  ac.start();
}

public void Disgust(int value) {
  
  // Set gain to zero
  masterGainGlide.setValue(0);
  
  // Create new glide/gain combo
  masterGainGlide = new Glide(ac, .5, 200);  
  masterGain = new Gain(ac, 1, masterGainGlide);
  ac.out.addInput(masterGain);
  
  // create a Ugen graph for square wave  
  for( int i = 0, n = 1; i < sineCount; i++, n++) {
    
    sineFrequency[i] = new Glide(ac, disgustFrequency * n, 200);
    
    sineTone[i] = new WavePlayer(ac, sineFrequency[i], Buffer.SINE);
    
    // For a square wave, we only want odd harmonics, so set all even harmonics to 0 gain/intensity
    sineIntensity = (n % 2 == 1) ? (float) (1.0 / n) : 0;
    sineGain[i] = new Gain(ac, 1, sineIntensity);
    sineGain[i].addInput(sineTone[i]);
  
    masterGain.addInput(sineGain[i]);
  }
}

public void Contempt(int value) {
  
  // Set gain/glide to zero
  masterGainGlide.setValue(0);
  
  // Create new glide/gain combo
  masterGainGlide = new Glide(ac, .5, 200);  
  masterGain = new Gain(ac, 1, masterGainGlide);
  ac.out.addInput(masterGain);
  
  // Create ugen for triangle wave
  for( int i = 0, n = 1; i < sineCount; i++, n++) {
    
    sineFrequency[i] = new Glide(ac, contemptFrequency * n, 200);
    
    sineTone[i] = new WavePlayer(ac, sineFrequency[i], Buffer.SINE);
    
    // For a triangle wave, same as square wave but we are decreasing by 1/(n*n)
    sineIntensity = (n % 2 == 1) ? (float) (1.0 / (n*n)) : 0;
    sineGain[i] = new Gain(ac, 1, sineIntensity); 
    sineGain[i].addInput(sineTone[i]);
  
    masterGain.addInput(sineGain[i]);
  }
}

public void Anger(int value) {

  // Set glide/gain to zero
  masterGainGlide.setValue(0);
  
  // Create new glide/gain combo
  masterGainGlide = new Glide(ac, .5, 200);  
  masterGain = new Gain(ac, 1, masterGainGlide);
  ac.out.addInput(masterGain);
  
  // Create ugen for sawtooth wave
  for( int i = 0, n = 1; i < sineCount ; i++, n++) {
    
    sineFrequency[i] = new Glide(ac, angerFrequency * n, 200);
    
    sineTone[i] = new WavePlayer(ac, sineFrequency[i], Buffer.SINE);
    
    // For a sawtooth wave set even harmonics to decreasing 1/n intensity
    sineIntensity = (n % 2 == 0) ? (float) (1.0 / (n)) : 0;
    sineGain[i] = new Gain(ac, 1, sineIntensity); 
    sineGain[i].addInput(sineTone[i]); 
  
    masterGain.addInput(sineGain[i]);
  }
}

void draw() {
  background(0);
}
