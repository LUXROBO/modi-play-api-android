/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.enums;


public class State {

    public enum Device {
        /**
         * device disconnected
         */
        DISCONNECTED(0),
        /**
         * device connecting
         */
        CONNECTING(1),
        /**
         * device connected
         */
        CONNECTED(2),
        /**
         * device searching
         */
        SEARCHING(3),
        /**
         * stop device searching
         */
        STOPSEARCHING(4);

        private final int code;

        Device(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    public enum Button {
        /**
         * modiplay button unpressed state
         */
        UNPRESSED(false),
        /**
         * modiplay button pressed state
         */
        PRESSED(true);

        private final boolean state;

        Button(boolean state) {
            this.state = state;
        }

        public boolean state() {
            return this.state;
        }
    }

    public enum Joystick {
        /**
         * modiplay joystick unpressed state
         */
        UNPRESSED(0),
        /**
         * modiplay joystick up pressed state
         */
        UP(2),
        /**
         * modiplay joystick down pressed state
         */
        DOWN(3),
        /**
         * modiplay joystick left pressed state
         */
        LEFT(4),
        /**
         * modiplay joystick right pressed state
         */
        RIGHT(5);

        private final int state;

        Joystick(int state) {
            this.state = state;
        }

        public int state() {
            return this.state;
        }
    }

    public enum Buzzer {
        /**
         * modiplay buzzer on state
         */
        OFF(false),
        /**
         * modiplay buzzer off state
         */
        ON(true);

        private final boolean state;

        Buzzer(boolean state) {
            this.state = state;
        }

        public boolean state() {
            return this.state;
        }
    }

}
