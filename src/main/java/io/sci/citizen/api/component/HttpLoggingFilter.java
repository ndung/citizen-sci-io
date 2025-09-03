package io.sci.citizen.api.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);
    private static final int MAX_PAYLOAD = 10_000;
    private static final Set<String> SKIP_PATHS = Set.of("/actuator/health"); // add your own

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return SKIP_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip large/binary uploads
        String ct = request.getContentType();
        if (ct != null && (ct.startsWith("multipart/") || ct.startsWith("image/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        var req = new ContentCachingRequestWrapper(request);
        var res = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(req, res);
        } finally {
            long took = System.currentTimeMillis() - start;

            String requestBody  = getBody(req.getContentAsByteArray(), req.getCharacterEncoding());
            String responseBody = getBody(res.getContentAsByteArray(), res.getCharacterEncoding());

            // mask simple secrets in JSON-ish content (very naive)
            requestBody  = maskSecrets(requestBody);
            responseBody = maskSecrets(responseBody);

            String uri = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
            String client = clientIp(request);

            log.info("HTTP {} {} from={} status={} took={}ms\n> headers={} \n> body={}\n< body={}",
                    req.getMethod(), uri, client, res.getStatus(), took,
                    req.getHeaderNames().asIterator().hasNext() ? "present" : "none",
                    truncate(requestBody), truncate(responseBody));

            // IMPORTANT: write response body back to the client
            res.copyBodyToResponse();
        }
    }

    private static String getBody(byte[] buf, String encoding) {
        if (buf == null || buf.length == 0) return "";
        var cs = encoding != null ? Charset.forName(encoding) : Charset.defaultCharset();
        var s = new String(buf, cs);
        return truncate(s);
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > MAX_PAYLOAD ? s.substring(0, MAX_PAYLOAD) + "...(truncated)" : s;
    }

    private static String maskSecrets(String s) {
        if (s == null) return null;
        // Very simple redactions; tailor to your payload structure
        return s
                .replaceAll("(?i)\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\".*?\"", "\"token\":\"***\"");
    }

    private static String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        String xri = req.getHeader("X-Real-IP");
        return (xri != null && !xri.isBlank()) ? xri : req.getRemoteAddr();
    }
}
