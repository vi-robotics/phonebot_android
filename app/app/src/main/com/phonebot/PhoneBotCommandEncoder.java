package main.com.phonebot;


/**
 * Encodes PhoneBot commands to byte arrays in order to send over BLE.
 */
public class PhoneBotCommandEncoder {
    private final static String TAG = PhoneBotCommandEncoder.class.getSimpleName();


    public static class InvalidPayloadException extends RuntimeException {
        public InvalidPayloadException() {
            super();
        }

        public InvalidPayloadException(String s) {
            super(s);
        }

        public InvalidPayloadException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public InvalidPayloadException(Throwable throwable) {
            super(throwable);
        }

    }

    // TODO(Max): Always ensure this is up to date with the interpreter on the firmware end

    /**
     * Contains the commands which are supported by PhoneBot. Each command has a byteValue
     * associated with it, which is matched by the firmware.
     */
    public enum PhoneBotCommand {
        SET_LEG_POSITIONS((byte) 0),
        REQUEST_BATTERY_VOLTAGE((byte) 1),
        SET_DEVICE_NAME((byte) 2);

        public final byte byteValue;

        PhoneBotCommand(byte byteValue) {
            this.byteValue = byteValue;
        }
    }


    // Payload bytes can be anything in the range of 0 to 200. Byte values of 201 to 255 are reserved
    // for command names, headers, etc.

    /**
     * Encodes a payload of bytes and a PhoneBotCommand into a parsable byte array which can be
     * interpreted by the PhoneBotBoard when sent over BLE.
     *
     * @param command The command to send, which contains a byte value
     * @param values  The payload bytes, currently a maximum of 15 payload bytes are allowed.
     * @return A byte array which is in the proper format to be interpreted correctly by the
     * PhoneBot BLE interpreter. This byte array can be directly sent to the Transparent UART
     * BLE service.
     */
    public static byte[] encodeCommand(PhoneBotCommand command, byte[] values) {

        byte[] CommandPreamble = {(byte) 255, (byte) 255};
        byte[] CommandHeader = {command.byteValue};
        byte[] CommandByteLength = {(byte) values.length};
        byte[] CommandFooter = {(byte) values.length}; // The footer is always the same as the CommandByteLength

        byte[][] res_construct = {CommandPreamble, CommandHeader, CommandByteLength, values, CommandFooter};

        byte[] res = new byte[CommandPreamble.length +
                CommandHeader.length +
                CommandByteLength.length +
                CommandFooter.length +
                values.length]; // Add preamble, header and footer


        // Check the payload values
        if (command == PhoneBotCommand.SET_LEG_POSITIONS) {

            if (values.length != 8) {
                throw new InvalidPayloadException("Length of values needs to be 8. Length is: " + values.length);
            }

            for (int i = 0; i < values.length; i++) {
                int checkVal = values[i] & 0xFF;
                if ((checkVal > 180) | (checkVal < 0)) {
                    throw new InvalidPayloadException(String.format("Payload value is invalid. Value = %d, Index = %d", checkVal, i));
                } else {
                    res[i + 1] = values[i];
                }
            }
        }

        // Concatenate all of the arrays
        int pos = 0;
        for (byte[] element : res_construct) {
            for (byte byte_val : element) {
                res[pos] = byte_val;
                pos++;
            }
        }

        return res;

    }

}
