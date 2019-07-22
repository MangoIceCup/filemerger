class TrimSpecialEndOutputStream extends OutputStream {
    private byte[] buffer;
    byte[] specialEndBuffer;
    private int begin_index = 0;
    private int next_index = 0;
    private boolean isFirstCircle = true;

    OutputStream outputStream;

    TrimSpecialEndOutputStream(OutputStream outputStream, byte[] specialEndBuffer) {
        if (outputStream == null || specialEndBuffer == null || specialEndBuffer.size() == 0) throw new RuntimeException("Wrong Parameter")
        this.outputStream = outputStream
        this.specialEndBuffer = specialEndBuffer
        this.buffer = new byte[specialEndBuffer.size()]
    }

    @Override
    synchronized void write(int b) throws IOException {
        if (isFirstCircle) {
            buffer[next_index] = (byte) b;
            next_index += 1
            if (next_index == buffer.size()) {
                isFirstCircle = false;
                next_index = next_index % buffer.size()
            }
        } else {
            outputStream.write(buffer[begin_index])
            begin_index = (begin_index + 1) % buffer.size()
            buffer[next_index] = (byte) b
            next_index = (next_index + 1) % buffer.size()
        }
    }
/**
 *  Trim Special End Now Or Push Data Not special to Left Stream Now
 *  And this method will call parent's trimSpecialEndOrPushEndDataNow() if parent is TrimSpecialEndOutputStream
 */
    synchronized void trimSpecialEndOrPushEndDataNow() {
        boolean needTrim = false;
        if (isFirstCircle) {
            needTrim = false
        } else {
            def index = 0
            boolean isDontNeedTrimHappened = false
            for (int i = begin_index; i < next_index + buffer.size(); i++) {
                if (specialEndBuffer[index] != buffer[i % buffer.size()]) {
                    isDontNeedTrimHappened = true
                    break
                } else {
                    index += 1
                }
            }
            if (isDontNeedTrimHappened) {
                needTrim = false
            } else {
                needTrim = true
            }
        }
        if (!needTrim) {
            if (!isFirstCircle) {
                for (int i = begin_index; i < next_index + buffer.size(); i++) {
                    outputStream.write(buffer[i])
                }
            } else {
                if (isFirstCircle) {
                    for (int i = begin_index; i < next_index; i++) {
                        outputStream.write(buffer[i])
                    }

                }
            }
        }
        begin_index = 0
        next_index = 0
        isFirstCircle = true

        if (outputStream instanceof TrimSpecialEndOutputStream) {
            outputStream.trimSpecialEndOrPushEndDataNow()
        }
    }

    @Override
    void flush() throws IOException {
        trimSpecialEndOrPushEndDataNow()
        outputStream.flush()
    }

    @Override
    void close() throws IOException {
        flush()
        outputStream.close()
    }
}