package codefod.com.core.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Lớp WebSocketFrame triển khai theo đặc tả WebSocket RFC 6455
 * Dùng để xử lý và tạo các khung WebSocket
 */
public class WebSocketFrame {
    // Trạng thái đóng gói
    private boolean fin;
    // Mã thao tác
    private byte opcode;
    // Cờ đánh dấu mã hóa
    private boolean masked;
    // Độ dài dữ liệu
    private long payloadLength;
    // Khóa mã hóa (nếu có)
    private byte[] maskingKey;
    // Dữ liệu nội dung
    private byte[] payloadData;

    // Các mã thao tác hỗ trợ (theo RFC 6455)
    public static final byte OPCODE_CONTINUATION = 0x0; // Khung tiếp tục
    public static final byte OPCODE_TEXT = 0x1;         // Khung văn bản
    public static final byte OPCODE_BINARY = 0x2;       // Khung nhị phân
    public static final byte OPCODE_CLOSE = 0x8;        // Khung đóng kết nối
    public static final byte OPCODE_PING = 0x9;         // Khung ping
    public static final byte OPCODE_PONG = 0xA;         // Khung pong

    // Các mã trạng thái đóng kết nối
    public static final int CLOSE_NORMAL = 1000;
    public static final int CLOSE_GOING_AWAY = 1001;
    public static final int CLOSE_PROTOCOL_ERROR = 1002;
    public static final int CLOSE_NO_STATUS = 1005;

    // Giới hạn kích thước tối đa của payload (16MB là đủ cho hầu hết trường hợp)
    private static final int MAX_PAYLOAD_SIZE = 16 * 1024 * 1024;

    /**
     * Constructor mặc định - thiết lập các giá trị mặc định
     */
    private WebSocketFrame() {
        this.fin = true;   // Mặc định là khung cuối cùng
        this.masked = false; // Mặc định không mã hóa (cho server->client)
    }

    /**
     * Phân tích ByteBuffer thành WebSocketFrame
     *
     * @param buffer Buffer chứa dữ liệu khung WebSocket
     * @return WebSocketFrame đã được phân tích
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ
     */
    public static WebSocketFrame parse(ByteBuffer buffer) throws IllegalArgumentException {
        if (buffer == null || buffer.remaining() < 2) {
            throw new IllegalArgumentException("Buffer không đủ dữ liệu cho WebSocketFrame");
        }

        WebSocketFrame frame = new WebSocketFrame();

        try {
            // Đọc byte đầu tiên
            byte firstByte = buffer.get();
            frame.fin = (firstByte & 0x80) != 0;
            frame.opcode = (byte) (firstByte & 0x0F);

            // Đọc byte thứ hai
            byte secondByte = buffer.get();
            frame.masked = (secondByte & 0x80) != 0;

            // Xác định độ dài dữ liệu
            int payloadLen = secondByte & 0x7F;
            if (payloadLen == 126) {
                // Độ dài 16-bit
                if (buffer.remaining() < 2) {
                    throw new IllegalArgumentException("Buffer không đủ dữ liệu cho độ dài 16-bit");
                }
                frame.payloadLength = buffer.getShort() & 0xFFFF;
            } else if (payloadLen == 127) {
                // Độ dài 64-bit
                if (buffer.remaining() < 8) {
                    throw new IllegalArgumentException("Buffer không đủ dữ liệu cho độ dài 64-bit");
                }
                frame.payloadLength = buffer.getLong();
            } else {
                // Độ dài 7-bit
                frame.payloadLength = payloadLen;
            }

            // Kiểm tra giới hạn kích thước
            if (frame.payloadLength > MAX_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("Kích thước payload vượt quá giới hạn cho phép: " + frame.payloadLength);
            }

            // Đọc khóa mã hóa nếu có
            if (frame.masked) {
                if (buffer.remaining() < 4) {
                    throw new IllegalArgumentException("Buffer không đủ dữ liệu cho masking key");
                }
                frame.maskingKey = new byte[4];
                buffer.get(frame.maskingKey);
            }

            // Đọc dữ liệu nội dung
            if (buffer.remaining() < frame.payloadLength) {
                throw new IllegalArgumentException("Buffer không đủ dữ liệu cho payload");
            }

            frame.payloadData = new byte[(int) frame.payloadLength];
            buffer.get(frame.payloadData);

            // Giải mã dữ liệu nếu cần
            if (frame.masked) {
                frame.unmaskPayload();
            }

            return frame;
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi phân tích WebSocketFrame: " + e.getMessage(), e);
        }
    }

    /**
     * Giải mã dữ liệu payload với masking key
     */
    private void unmaskPayload() {
        for (int i = 0; i < payloadData.length; i++) {
            payloadData[i] ^= maskingKey[i % 4];
        }
    }

    /**
     * Tạo khung văn bản
     *
     * @param text Nội dung văn bản
     * @return WebSocketFrame chứa văn bản
     */
    public static WebSocketFrame createTextFrame(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text không được là null");
        }

        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_TEXT;
        frame.payloadData = text.getBytes(StandardCharsets.UTF_8);
        frame.payloadLength = frame.payloadData.length;
        return frame;
    }

    /**
     * Tạo khung nhị phân
     *
     * @param data Dữ liệu nhị phân
     * @return WebSocketFrame chứa dữ liệu nhị phân
     */
    public static WebSocketFrame createBinaryFrame(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Dữ liệu không được là null");
        }

        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_BINARY;
        frame.payloadData = data.clone(); // Clone để tránh tham chiếu bên ngoài
        frame.payloadLength = data.length;
        return frame;
    }

    /**
     * Tạo khung đóng kết nối
     *
     * @param code   Mã trạng thái đóng
     * @param reason Lý do đóng kết nối
     * @return WebSocketFrame đóng kết nối
     */
    public static WebSocketFrame createCloseFrame(int code, String reason) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_CLOSE;

        // Xác định kích thước buffer
        int reasonLength = (reason != null) ? reason.getBytes(StandardCharsets.UTF_8).length : 0;
        ByteBuffer buffer = ByteBuffer.allocate(2 + reasonLength);

        // Thêm mã đóng kết nối
        buffer.putShort((short) code);

        // Thêm lý do nếu có
        if (reason != null && !reason.isEmpty()) {
            buffer.put(reason.getBytes(StandardCharsets.UTF_8));
        }

        buffer.flip();
        frame.payloadData = new byte[buffer.remaining()];
        buffer.get(frame.payloadData);
        frame.payloadLength = frame.payloadData.length;

        return frame;
    }

    /**
     * Tạo khung ping
     *
     * @param data Dữ liệu ping (tối đa 125 byte)
     * @return WebSocketFrame ping
     */
    public static WebSocketFrame createPingFrame(byte[] data) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_PING;

        // Giới hạn dữ liệu ping tối đa 125 byte theo RFC
        if (data != null) {
            if (data.length > 125) {
                throw new IllegalArgumentException("Dữ liệu ping không được vượt quá 125 byte");
            }
            frame.payloadData = data.clone();
            frame.payloadLength = data.length;
        } else {
            frame.payloadData = new byte[0];
            frame.payloadLength = 0;
        }

        return frame;
    }

    /**
     * Tạo khung pong
     *
     * @param data Dữ liệu pong (tối đa 125 byte)
     * @return WebSocketFrame pong
     */
    public static WebSocketFrame createPongFrame(byte[] data) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.opcode = OPCODE_PONG;

        // Giới hạn dữ liệu pong tối đa 125 byte theo RFC
        if (data != null) {
            if (data.length > 125) {
                throw new IllegalArgumentException("Dữ liệu pong không được vượt quá 125 byte");
            }
            frame.payloadData = data.clone();
            frame.payloadLength = data.length;
        } else {
            frame.payloadData = new byte[0];
            frame.payloadLength = 0;
        }

        return frame;
    }

    /**
     * Chuyển đổi WebSocketFrame thành ByteBuffer
     *
     * @return ByteBuffer chứa dữ liệu khung đã được đóng gói
     */
    public ByteBuffer toBuffer() {
        // Tính toán kích thước frame
        int frameSize = 2; // 2 byte đầu tiên luôn có

        // Byte bổ sung cho độ dài
        if (payloadLength > 65535) {
            frameSize += 8; // Dùng 8 byte cho độ dài 64-bit
        } else if (payloadLength > 125) {
            frameSize += 2; // Dùng 2 byte cho độ dài 16-bit
        }

        // Thêm kích thước cho masking key nếu cần
        if (masked && maskingKey != null) {
            frameSize += 4;
        }

        // Thêm kích thước payload
        frameSize += payloadData.length;

        // Tạo buffer với kích thước đã tính
        ByteBuffer buffer = ByteBuffer.allocate(frameSize);

        // Byte đầu tiên: FIN + RSV1-3 + Opcode
        byte firstByte = (byte) (opcode & 0x0F);
        if (fin) {
            firstByte |= 0x80; // Thiết lập bit FIN
        }
        buffer.put(firstByte);

        // Byte thứ hai: MASK + độ dài 7-bit hoặc chỉ thị độ dài mở rộng
        byte secondByte = 0;
        if (masked) {
            secondByte |= 0x80; // Thiết lập bit MASK
        }

        // Xác định cách thể hiện độ dài
        if (payloadLength <= 125) {
            // Độ dài 7-bit
            secondByte |= (byte) payloadLength;
            buffer.put(secondByte);
        } else if (payloadLength <= 65535) {
            // Độ dài 16-bit
            secondByte |= 126;
            buffer.put(secondByte);
            buffer.putShort((short) payloadLength);
        } else {
            // Độ dài 64-bit
            secondByte |= 127;
            buffer.put(secondByte);
            buffer.putLong(payloadLength);
        }

        // Thêm masking key nếu khung được mã hóa
        if (masked && maskingKey != null) {
            buffer.put(maskingKey);

            // Nếu khung được mã hóa, cần mã hóa dữ liệu trước khi gửi
            byte[] maskedData = new byte[payloadData.length];
            for (int i = 0; i < payloadData.length; i++) {
                maskedData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
            buffer.put(maskedData);
        } else {
            // Thêm dữ liệu không mã hóa
            buffer.put(payloadData);
        }

        // Chuẩn bị buffer để đọc
        buffer.flip();
        return buffer;
    }

    /**
     * Thiết lập trạng thái mã hóa và khóa mã hóa
     *
     * @param masked Trạng thái mã hóa
     * @param key    Khóa mã hóa (bắt buộc nếu masked=true)
     */
    public void setMasked(boolean masked, byte[] key) {
        this.masked = masked;
        if (masked) {
            if (key == null || key.length != 4) {
                throw new IllegalArgumentException("Masking key phải gồm 4 byte");
            }
            this.maskingKey = key.clone();
        } else {
            this.maskingKey = null;
        }
    }

    /**
     * Kiểm tra xem khung có phải là khung văn bản
     */
    public boolean isText() {
        return opcode == OPCODE_TEXT;
    }

    /**
     * Kiểm tra xem khung có phải là khung nhị phân
     */
    public boolean isBinary() {
        return opcode == OPCODE_BINARY;
    }

    /**
     * Kiểm tra xem khung có phải là khung đóng kết nối
     */
    public boolean isClose() {
        return opcode == OPCODE_CLOSE;
    }

    /**
     * Kiểm tra xem khung có phải là khung ping
     */
    public boolean isPing() {
        return opcode == OPCODE_PING;
    }

    /**
     * Kiểm tra xem khung có phải là khung pong
     */
    public boolean isPong() {
        return opcode == OPCODE_PONG;
    }

    /**
     * Lấy dữ liệu payload dạng byte[]
     */
    public byte[] getPayloadData() {
        return payloadData.clone(); // Clone để tránh sửa đổi trực tiếp
    }

    /**
     * Lấy dữ liệu payload dạng String (chỉ hợp lệ cho khung văn bản)
     */
    public String getTextPayload() {
        if (!isText()) {
            throw new IllegalStateException("Không thể lấy dữ liệu văn bản từ khung không phải văn bản");
        }
        return new String(payloadData, StandardCharsets.UTF_8);
    }

    /**
     * Lấy mã đóng kết nối (cho khung đóng kết nối)
     */
    public int getCloseCode() {
        if (!isClose()) {
            throw new IllegalStateException("Không thể lấy mã đóng từ khung không phải đóng kết nối");
        }

        if (payloadData.length >= 2) {
            return (payloadData[0] & 0xFF) << 8 | (payloadData[1] & 0xFF);
        }
        return CLOSE_NO_STATUS; // Không có mã trạng thái
    }

    /**
     * Lấy lý do đóng kết nối (cho khung đóng kết nối)
     */
    public String getCloseReason() {
        if (!isClose()) {
            throw new IllegalStateException("Không thể lấy lý do đóng từ khung không phải đóng kết nối");
        }

        if (payloadData.length > 2) {
            return new String(payloadData, 2, payloadData.length - 2, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * Lấy trạng thái FIN
     */
    public boolean isFinal() {
        return fin;
    }

    /**
     * Lấy mã thao tác
     */
    public byte getOpcode() {
        return opcode;
    }

    /**
     * Lấy độ dài payload
     */
    public long getPayloadLength() {
        return payloadLength;
    }

    /**
     * Kiểm tra xem khung có được mã hóa không
     */
    public boolean isMasked() {
        return masked;
    }

    /**
     * Thiết lập trạng thái FIN
     */
    public void setFin(boolean fin) {
        this.fin = fin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WebSocketFrame[");
        sb.append("opcode=");
        switch (opcode) {
            case OPCODE_CONTINUATION:
                sb.append("CONTINUATION");
                break;
            case OPCODE_TEXT:
                sb.append("TEXT");
                break;
            case OPCODE_BINARY:
                sb.append("BINARY");
                break;
            case OPCODE_CLOSE:
                sb.append("CLOSE");
                break;
            case OPCODE_PING:
                sb.append("PING");
                break;
            case OPCODE_PONG:
                sb.append("PONG");
                break;
            default:
                sb.append(opcode);
                break;
        }
        sb.append(", fin=").append(fin);
        sb.append(", masked=").append(masked);
        sb.append(", length=").append(payloadLength);

        if (isClose() && payloadData.length >= 2) {
            sb.append(", closeCode=").append(getCloseCode());
            if (payloadData.length > 2) {
                sb.append(", reason='").append(getCloseReason()).append("'");
            }
        }

        sb.append("]");
        return sb.toString();
    }
}