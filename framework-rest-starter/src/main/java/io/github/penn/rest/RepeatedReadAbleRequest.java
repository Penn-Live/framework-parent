package io.github.penn.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author tangzhongping
 */
@Slf4j
public class RepeatedReadAbleRequest extends HttpServletRequestWrapper {

    private byte[] body = new byte[0];

    public RepeatedReadAbleRequest(HttpServletRequest request) {
        super(request);
        try {
            this.body = IOUtils.toByteArray(request.getReader());
        } catch (IOException e) {
            log.error("error trans body reader to byte array.");
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bodyByteInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bodyByteInputStream.read();
            }
        };
    }
}