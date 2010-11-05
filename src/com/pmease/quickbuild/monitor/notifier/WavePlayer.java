/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 22, 2010 9:12:00 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.IOUtils;

public class WavePlayer {
	enum Position {
        LEFT, RIGHT, NORMAL
    };

    private PlayThread thread;
    
    public WavePlayer() {
    	
    }
    
    public void play(InputStream in) {
    	thread = new PlayThread(in);
    	thread.start();
    }
    
    public void close() {
    	if (thread != null) {
    		thread.pause();
    	}
    }
    
	class PlayThread extends Thread {
		private Position curPosition = Position.NORMAL;
		 
	    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	 
	    private InputStream in;
	    
	    private boolean paused = false;
	    
	    public PlayThread(InputStream in) {
	    	this.in = in;
	    }
	    
	    public PlayThread(String fileName) {
	    	try {
				this.in = new FileInputStream(new File(fileName));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
	    }
	    
	    public void pause() {
	    	paused = true;
	    }
	    
	    public void unpause() {
	    	paused = false;
	    }
	    
		@Override
		public void run() {
			AudioInputStream audioIn = null;
			try {
				audioIn = AudioSystem.getAudioInputStream(in);
				AudioFormat format = audioIn.getFormat();
				
				SourceDataLine auline = null;
		        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		 
	            auline = (SourceDataLine) AudioSystem.getLine(info);
	            auline.open(format);
		 
		        if (auline.isControlSupported(FloatControl.Type.PAN)) {
		            FloatControl pan = (FloatControl) auline.getControl(FloatControl.Type.PAN);
		            if (curPosition == Position.RIGHT)
		                pan.setValue(1.0f);
		            else if (curPosition == Position.LEFT)
		                pan.setValue(-1.0f);
		        } 
		 
		        auline.start();
		        int nBytesRead = 0;
		        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		 
		        try {
		            while (nBytesRead != -1 && !paused) {
		                nBytesRead = audioIn.read(abData, 0, abData.length);
		                if (nBytesRead >= 0)
		                    auline.write(abData, 0, nBytesRead);
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		            return;
		        } finally {
		            auline.drain();
		            auline.close();
		        }
		 
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(audioIn);
			}
		}
	}
}
