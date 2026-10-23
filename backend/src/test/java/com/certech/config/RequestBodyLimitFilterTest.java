package com.certech.config;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class RequestBodyLimitFilterTest {
    @Test
    void acceptsEmptyAndExactLimitBodiesWithoutChangingBytes() throws Exception {
        for (byte[] body : new byte[][] {new byte[0], "áá".getBytes(StandardCharsets.UTF_8)}) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/contacto");
            request.setContent(body);
            AtomicBoolean reached = new AtomicBoolean();
            new RequestBodyLimitFilter(4).doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                assertArrayEquals(body, req.getInputStream().readAllBytes());
                reached.set(true);
            });
            assertTrue(reached.get());
        }
    }

    @Test
    void rejectsOversizedDeclaredAndUnknownLengthBeforeTheHandler() throws Exception {
        for (boolean unknownLength : new boolean[] {false, true}) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/contacto") {
                @Override public long getContentLengthLong() {
                    return unknownLength ? -1 : super.getContentLengthLong();
                }
            };
            request.setContent("private-payload".getBytes(StandardCharsets.UTF_8));
            MockHttpServletResponse response = new MockHttpServletResponse();
            new RequestBodyLimitFilter(4).doFilter(request, response, (req, res) -> fail("Handler reached"));
            assertEquals(413, response.getStatus());
            assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
            assertFalse(response.getContentAsString().contains("private-payload"));
        }
    }

    @Test
    void failsClosedForUnsafeLimitConfiguration() {
        for (int limit : new int[] {-1, 0, 1_048_577, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new RequestBodyLimitFilter(limit));
        }
    }
}
