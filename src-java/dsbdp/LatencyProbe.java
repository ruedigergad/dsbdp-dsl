/*
 *
 * Copyright (C) 2016 Ruediger Gad
 *
 * This software is released under the terms of the Eclipse Public License 
 * (EPL) 1.0. You can find a copy of the EPL at: 
 * http://opensource.org/licenses/eclipse-1.0.php
 *
 */

package dsbdp;

/**
 * This is a simple probe for measuring latency.
 */
public class LatencyProbe {

    private long startTime;
    private long endTime;

    public LatencyProbe() {
        this.startTime = System.nanoTime();
    }

    public void done() {
        this.endTime = System.nanoTime();
    }

    public long getDelta() {
        return this.endTime - this.startTime;
    }

}

