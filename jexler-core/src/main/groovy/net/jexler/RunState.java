/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.jexler;


/**
 * Enum for the run state of a service.
 *
 * @author $(whois jexler.net)
 */
public enum RunState  {

    /** Service is off. */
    OFF ("off"),
    /** Service is busy starting. */
    BUSY_STARTING("busy (starting)"),
    /** Service is running and idle. */
    IDLE("idle"),
    /** Service is running and busy processing an event. */
    BUSY_EVENT("busy (event)"),
    /** Service is busy stopping. */
    BUSY_STOPPING("busy (stopping)");

    private final String info;

    /**
     * Constructor from info.
     */
    RunState(String info) {
        this.info = info;
    }

    /**
     * Get human readable info about the run state.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Convenience method, returns true if OFF.
     */
    public boolean isOff() {
        return this == OFF;
    }

    /**
     * Convenience method, returns true if BUSY_STARTING.
     */
    public boolean isBusyStarting() {
        return this == BUSY_STARTING;
    }

    /**
     * Convenience method, returns true if IDLE.
     */
    public boolean isIdle() {
        return this == IDLE;
    }

    /**
     * Convenience method, returns true if BUSY_EVENT.
     */
    public boolean isBusyEvent() {
        return this == BUSY_EVENT;
    }

    /**
     * Convenience method, returns true if BUSY_STOPPING.
     */
    public boolean isBusyStopping() {
        return this == BUSY_STOPPING;
    }

    /**
     * Convenience method, returns true if not OFF.
     */
    public boolean isOn() {
        return this != OFF;
    }

    /**
     * Convenience method, returns true if IDLE or BUSY_EVENT,
     * in other words, if the service is up and doing what it should do.
     */
    public boolean isOperational() {
        return this == IDLE || this == BUSY_EVENT;
    }

}

