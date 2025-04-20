package codefod.com.core.websocket;

import java.nio.ByteBuffer;

public class WebSocketFrame {
    private boolean fin;
    private byte opcode;
    private boolean masked;
    private long payloadLength;
    private byte[] maskingKey;
    private byte[] payloadData;

    // Frame opcodes
    public static final byte OPCODE_CONTINUATION = 0x0;
    public static final byte OPCODE_TEXT = 0x1;
    public static final byte OPCODE_BINARY = 0x2;
    public static final byte OPCODE_CLOSE = 0x8;
    public static final byte OPCODE_PING = 0x9;
    public static final byte OPCODE_PONG = 0xA;

    private WebSocketFrame() {
        this.fin = true;
        this.masked = false;
    }

    public static WebSocketFrame parse(ByteBuffer buffer) {
        WebSocketFrame frame = new WebSocketFrame();

        // Read first byte
        byte firstByte = buffer.get();
        frame.fin = (firstByte & 0x80) != 0;
        frame.opcode = (byte) (firstByte & 0x0F);

        // Read second byte
        byte secondByte = buffer.get();
        frame.masked = (secondByte & 0x80) != 0;

        // Get payload length
        int payloadLen = secondByte & 0x7F;
        if (payloadLen == 126) {
            frame.payloadLength = buffer.getShort() & 0xFFFF;
        } else if (payloadLen == 127) {
            frame.payloadLength = buffer.getLong();
        } else {
            frame.payloadLength = payloadLen;
        }

        // Read masking key if present
        if (frame.masked) {
            frame.maskingKey = new byte[4];
            buffer.get(frame.maskingKey);
        }

        // Read payload data
        frame.payloadData = new byte[(int) frame.payloadLength];
        buffer.get(frame.payloadData);

        // Apply mask if necessary
        if (frame.masked) {
            for (int i = 0; i < frame.payloadData.length; i++) {
                frame.payloadData[i] ^= frame.maskingKey[i % 4];
            }
        }

        return frame;
    }

    public static WebSocketFrame createTextFrame(String text) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_TEXT;
        frame.payloadData = text.getBytes();
        frame.payloadLength = frame.payloadData.length;
        return frame;
    }

    public static WebSocketFrame createBinaryFrame(byte[] data) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_BINARY;
        frame.payloadData = data;
        frame.payloadLength = data.length;
        return frame;
    }

    public static WebSocketFrame createCloseFrame(int code, String reason) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_CLOSE;

        ByteBuffer buffer = ByteBuffer.allocate(2 + (reason != null ? reason.getBytes().length : 0));
        buffer.putShort((short) code);
        if (reason != null) {
            buffer.put(reason.getBytes());
        }
        buffer.flip();
        frame.payloadData = new byte[buffer.remaining()];
        buffer.get(frame.payloadData);
        frame.payloadLength = frame.payloadData.length;

        return frame;
    }

    public static WebSocketFrame createPingFrame(byte[] data) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_PING;
        frame.payloadData = data;
        frame.payloadLength = data.length;
        return frame;
    }

    public static WebSocketFrame createPongFrame(byte[] data) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_PONG;
        frame.payloadData = data;
        frame.payloadLength = data.length;
        return frame;
    }

    public ByteBuffer toBuffer() {
        int frameSize = 2; // First two bytes

        // Length bytes
        if (payloadLength > 65535) {
            frameSize += 8;
        } else if (payloadLength > 125) {
            frameSize += 2;
        }

        // Masking key (not used for outgoing frames)
        if (masked) {
            frameSize += 4;
        }

        // Payload
        frameSize += payloadData.length;

        ByteBuffer buffer = ByteBuffer.allocate(frameSize);

        // First byte
        byte firstByte = (byte) (opcode & 0x0F);
        if (fin) {
            firstByte |= 0x80;
        }
        buffer.put(firstByte);

        // Second byte
        byte secondByte = 0;
        if (masked) {
            secondByte |= 0x80;
        }

        if (payloadLength <= 125) {
            secondByte |= (byte) payloadLength;
            buffer.put(secondByte);
        } else if (payloadLength <= 65535) {
            secondByte |= 126;
            buffer.put(secondByte);
            buffer.putShort((short) payloadLength);
        } else {
            secondByte |= 127;
            buffer.put(secondByte);
            buffer.putLong(payloadLength);
        }

        // Masking key (not used for outgoing frames)
        if (masked && maskingKey != null) {
            buffer.put(maskingKey);
        }

        // Payload data
        buffer.put(payloadData);

        buffer.flip();
        return buffer;
    }

    public boolean isText() {
        return opcode == OPCODE_TEXT;
    }

    public boolean isBinary() {
        return opcode == OPCODE_BINARY;
    }

    public boolean isClose() {
        return opcode == OPCODE_CLOSE;
    }

    public boolean isPing() {
        return opcode == OPCODE_PING;
    }

    public boolean isPong() {
        return opcode == OPCODE_PONG;
    }

    public byte[] getPayloadData() {
        return payloadData;
    }

    public String getTextPayload() {
        return new String(payloadData);
    }

    public int getCloseCode() {
        if (payloadData.length >= 2) {
            return (payloadData[0] & 0xFF) << 8 | (payloadData[1] & 0xFF);
        }
        return 1005; // No status code present
    }

    public String getCloseReason() {
        if (payloadData.length > 2) {
            return new String(payloadData, 2, payloadData.length - 2);
        }
        return "";
    }
}
